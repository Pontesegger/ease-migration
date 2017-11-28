/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *     Mathieu Velten - Bug correction
 *******************************************************************************/
package org.eclipse.ease.lang.javascript.rhino;

import java.io.InputStreamReader;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.ease.AbstractReplScriptEngine;
import org.eclipse.ease.Script;
import org.eclipse.ease.ScriptExecutionException;
import org.eclipse.ease.classloader.EaseClassLoader;
import org.eclipse.ease.debugging.IScriptDebugFrame;
import org.eclipse.ease.debugging.ScriptDebugFrame;
import org.eclipse.ease.debugging.ScriptStackTrace;
import org.eclipse.ease.lang.javascript.JavaScriptHelper;
import org.eclipse.ease.tools.RunnableWithResult;
import org.eclipse.swt.widgets.Display;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.WrappedException;

/**
 * A script engine to execute JavaScript code on a Rhino interpreter.
 */
public class RhinoScriptEngine extends AbstractReplScriptEngine {

	private static final EaseClassLoader CLASSLOADER;

	static {
		CLASSLOADER = new EaseClassLoader();
		// set context factory that is able to terminate script execution
		ContextFactory.initGlobal(new ObservingContextFactory());

		// set a custom class loader to find everything in the eclipse universe
		AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
			ContextFactory.getGlobal().initApplicationClassLoader(CLASSLOADER);
			return null;
		});
	}

	public static final String ENGINE_ID = "org.eclipse.ease.javascript.rhino";

	public static Context getContext() {
		Context context = Context.getCurrentContext();
		if (context == null) {
			synchronized (ContextFactory.getGlobal()) {
				context = Context.enter();
			}
		}

		return context;
	}

	/** Rhino Scope. Created when interpreter is initialized */
	private ScriptableObject fScope;

	private Context fContext;

	private int fOptimizationLevel = 9;

	private ScriptStackTrace fExceptionStackTrace = null;

	/**
	 * Creates a new Rhino interpreter.
	 */
	public RhinoScriptEngine() {
		super("Rhino");
	}

	/**
	 * Creates a new Rhino interpreter.
	 *
	 * @param name
	 *            name of interpreter (used for the jobs name)
	 */
	protected RhinoScriptEngine(final String name) {
		super(name);
	}

	public void setOptimizationLevel(final int level) {
		fOptimizationLevel = level;
	}

	@Override
	protected synchronized void setupEngine() {
		fContext = getContext();

		fContext.setGeneratingDebug(false);
		fContext.setOptimizationLevel(fOptimizationLevel);
		fContext.setDebugger(null, null);

		fScope = new ImporterTopLevel(fContext);

		// enable script termination support
		fContext.setGenerateObserverCount(true);
		fContext.setInstructionObserverThreshold(10);

		// enable JS v1.8 language constructs
		try {
			Context.class.getDeclaredField("VERSION_1_8");
			fContext.setLanguageVersion(Context.VERSION_1_8);
		} catch (final Exception e) {
			fContext.setLanguageVersion(Context.VERSION_1_7);
		}
	}

	@Override
	protected synchronized void teardownEngine() {
		// remove debugger to allow for garbage collection
		fContext.setDebugger(null, null);

		// cleanup context
		Context.exit();
		fContext = null;
		fScope = null;

		// unregister from classloader
		CLASSLOADER.unregisterEngine(this);
	}

	@Override
	protected Object execute(final Script script, final Object reference, final String fileName, final boolean uiThread) throws Throwable {
		if (uiThread) {
			// run in UI thread
			final RunnableWithResult<Object> runnable = new RunnableWithResult<Object>() {

				@Override
				public void runWithTry() throws Throwable {
					// initialize scope
					getContext().initStandardObjects(fScope);

					// call execute again, now from correct thread
					setResult(internalExecute(script, reference, fileName));
				}
			};

			Display.getDefault().syncExec(runnable);

			return runnable.getResultFromTry();

		} else
			// run in engine thread
			return internalExecute(script, reference, fileName);
	}

	private Object internalExecute(final Script script, final Object reference, final String fileName) throws Throwable {
		// remove an eventually cached terminate request
		((ObservingContextFactory) ContextFactory.getGlobal()).cancelTerminate(getContext());

		try {
			final Object result;

			// execution
			if (script.getCommand() instanceof NativeFunction)
				result = ((NativeFunction) script.getCommand()).call(getContext(), fScope, fScope, ScriptRuntime.emptyArgs);

			else if (script.getCommand() instanceof org.mozilla.javascript.Script)
				// execute anonymous functions
				result = ((org.mozilla.javascript.Script) script.getCommand()).exec(getContext(), fScope);

			else {
				final InputStreamReader codeReader = new InputStreamReader(script.getCodeStream());
				result = getContext().evaluateReader(fScope, codeReader, fileName, 1, null);
				codeReader.close();
			}

			// evaluate result
			if ((result == null) || (result instanceof Undefined))
				return null;

			else if (result instanceof NativeJavaObject)
				return ((NativeJavaObject) result).unwrap();

			else if (result.getClass().getName().equals("org.mozilla.javascript.InterpretedFunction"))
				return null;

			return result;

		} catch (final RhinoException e) {
			// build exception stacktrace
			fExceptionStackTrace = getStackTrace().clone();
			if ((fExceptionStackTrace.isEmpty()) || (((script != null) && (!script.equals(fExceptionStackTrace.get(0).getScript()))))) {
				// topmost script is not what we expected, seems it was not put on the stack
				fExceptionStackTrace.add(0, new ScriptDebugFrame(script, e.lineNumber(), IScriptDebugFrame.TYPE_FILE));
			}

			// now handle error
			String message = e.getMessage();
			String errorName = "Error";
			Throwable cause = null;

			if (e instanceof WrappedException) {
				final Throwable wrapped = ((WrappedException) e).getWrappedException();
				if (wrapped instanceof ScriptExecutionException)
					throw wrapped;

				else if (wrapped != null) {
					// java exception thrown
					message = wrapped.getMessage();
					errorName = "JavaError";
					cause = wrapped;
				}

			} else if (e instanceof EcmaError) {
				message = ((EcmaError) e).getErrorMessage();
				errorName = ((EcmaError) e).getName();

			} else if (e instanceof JavaScriptException) {
				// throw statement from javascript
				final Object value = ((JavaScriptException) e).getValue();
				if (value instanceof NativeJavaObject) {
					final Object unwrapped = ((NativeJavaObject) value).unwrap();
					if (unwrapped instanceof Throwable) {
						message = ((Throwable) unwrapped).getMessage();
						errorName = "JavaError";
						cause = (Throwable) unwrapped;
					} else {
						message = (unwrapped != null) ? unwrapped.toString() : null;
						errorName = "ScriptException";
					}

				} else {
					message = (((JavaScriptException) e).getValue() != null) ? ((JavaScriptException) e).getValue().toString() : null;
					errorName = "ScriptException";
				}

			} else if (e instanceof EvaluatorException) {
				// invalid syntax or similar rhino exception
				errorName = "SyntaxError";

			} else {
				message = "Error running script";
			}

			throw new ScriptExecutionException(message, e.columnNumber(), e.lineSource(), errorName, getExceptionStackTrace(), cause);
		}
	}

	public ScriptStackTrace getExceptionStackTrace() {
		return fExceptionStackTrace;
	}

	@Override
	public void terminateCurrent() {
		// typically requested by a different thread, so do not use getContext() here
		((ObservingContextFactory) ContextFactory.getGlobal()).terminate(fContext);
	}

	@Override
	public synchronized void registerJar(final URL url) {
		CLASSLOADER.registerURL(this, url);
	}

	@Override
	protected Object internalGetVariable(final String name) {
		return getVariable(fScope, name);
	}

	@Override
	protected Map<String, Object> internalGetVariables() {
		return getVariables(fScope);
	}

	public static Map<String, Object> getVariables(final Scriptable scope) {
		final Map<String, Object> result = new TreeMap<>();

		for (final Object key : scope.getIds()) {
			final Object value = getVariable(scope, key.toString());
			if ((value == null) || (!value.getClass().getName().startsWith("org.mozilla.javascript.gen")))
				result.put(key.toString(), value);
		}

		// add parent scope
		final Scriptable parent = scope.getParentScope();
		if (parent != null)
			result.putAll(getVariables(parent));

		return result;
	}

	public static Object getVariable(final Scriptable scope, final String name) {
		final Object value = scope.get(name, scope);
		if (value instanceof NativeJavaObject)
			return ((NativeJavaObject) value).unwrap();

		return value;
	}

	@Override
	protected boolean internalHasVariable(final String name) {
		final Object value = fScope.get(name, fScope);
		return !Scriptable.NOT_FOUND.equals(value);
	}

	@Override
	protected void internalSetVariable(final String name, final Object content) {
		if (!JavaScriptHelper.isSaveName(name))
			throw new RuntimeException("\"" + name + "\" is not a valid JavaScript variable name");

		final Scriptable scope = fScope;

		final Object jsOut = internaljavaToJS(content, scope);
		scope.put(name, scope, jsOut);
	}

	private Object internaljavaToJS(final Object value, final Scriptable scope) {
		Object result = null;
		if (isPrimitiveType(value) || (value instanceof Scriptable)) {
			result = value;
		} else if (value instanceof Character) {
			result = String.valueOf(((Character) value).charValue());
		} else {
			result = getContext().getWrapFactory().wrap(getContext(), scope, value, null);
		}
		return result;

	}

	private boolean isPrimitiveType(final Object value) {
		return (value instanceof String) || (value instanceof Number) || (value instanceof Boolean);
	}

	/**
	 * Method to get the global scope of this engine.
	 *
	 * @return fScope
	 */
	public ScriptableObject getScope() {
		return fScope;
	}

	protected Context getCurrentContext() {
		return fContext;
	}
}
