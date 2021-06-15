/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License_Identifier: EPL-2.0
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import org.eclipse.ease.AbstractReplScriptEngine;
import org.eclipse.ease.IExecutionListener;
import org.eclipse.ease.Script;
import org.eclipse.ease.ScriptEngineCancellationException;
import org.eclipse.ease.ScriptExecutionException;
import org.eclipse.ease.ScriptObjectType;
import org.eclipse.ease.ScriptResult;
import org.eclipse.ease.classloader.EaseClassLoader;
import org.eclipse.ease.debugging.EaseDebugFrame;
import org.eclipse.ease.debugging.IScriptDebugFrame;
import org.eclipse.ease.debugging.ScriptStackTrace;
import org.eclipse.ease.debugging.model.EaseDebugVariable;
import org.eclipse.ease.debugging.model.EaseDebugVariable.Type;
import org.eclipse.ease.lang.javascript.JavaScriptHelper;
import org.eclipse.ease.tools.RunnableWithResult;
import org.eclipse.swt.widgets.Display;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.NativeJavaArray;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.WrappedException;
import org.mozilla.javascript.debug.Debugger;

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

	private static boolean hasNoChildElements(Object scope) {
		if (scope instanceof Double)
			return true;

		if (scope == null)
			return true;

		return false;
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
			Context.class.getDeclaredField("VERSION_ES6");
			fContext.setLanguageVersion(Context.VERSION_ES6);
		} catch (final Exception e) {
			try {
				Context.class.getDeclaredField("VERSION_1_8");
				fContext.setLanguageVersion(Context.VERSION_1_8);
			} catch (final Exception e1) {
				fContext.setLanguageVersion(Context.VERSION_1_7);
			}
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

		super.teardownEngine();
	}

	@Override
	protected Object execute(final Script script, final Object reference, final String fileName, final boolean uiThread) throws Throwable {
		if (uiThread) {
			// run in UI thread
			final RunnableWithResult<Object> runnable = new RunnableWithResult<Object>() {

				@Override
				public Object runWithTry() throws Throwable {
					// initialize scope
					getContext().initStandardObjects(fScope);

					// call execute again, now from correct thread
					return internalExecute(script, reference, fileName);
				}
			};

			Display.getDefault().syncExec(runnable);

			return runnable.getResultOrThrow();

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
			if (result instanceof Undefined)
				return ScriptResult.VOID;

			if (result == null)
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
				fExceptionStackTrace.add(0, new EaseDebugFrame(script, e.lineNumber(), IScriptDebugFrame.TYPE_FILE));
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
		if (Thread.currentThread().equals(getThread()))
			throw new ScriptEngineCancellationException();
		else
			// requested by a different thread, so do not use getContext() here
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

	public Map<String, Object> getVariables(final Scriptable scope) {
		return runInJobContext(new RunnableWithResult<Map<String, Object>>() {

			@Override
			public Map<String, Object> runWithTry() throws Throwable {
				final Map<String, Object> result = new TreeMap<>();

				// first handle parent scope
				final Scriptable parent = scope.getParentScope();
				if (parent != null)
					result.putAll(getVariables(parent));

				// make sure current thread has a context attached, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=548644
				getContext();

				// local scope variables may hide parent scope variables
				for (final Object key : scope.getIds()) {
					final Object value = getVariable(scope, key.toString());
					if ((value == null) || (!value.getClass().getName().startsWith("org.mozilla.javascript.gen")))
						result.put(key.toString(), value);
				}

				return result;
			}
		});
	}

	private Object getVariable(final Scriptable scope, final String name) {
		return runInJobContext(new RunnableWithResult<Object>() {
			@Override
			public Object runWithTry() throws Throwable {
				final Object value = scope.get(name, scope);
				if (value instanceof NativeJavaObject)
					return ((NativeJavaObject) value).unwrap();
				else
					return value;
			}
		});
	}

	@Override
	protected boolean internalHasVariable(final String name) {

		return runInJobContext(new RunnableWithResult<Boolean>() {
			@Override
			public Boolean runWithTry() throws Throwable {
				final Object value = fScope.get(name, fScope);
				return !Scriptable.NOT_FOUND.equals(value);
			}
		});
	}

	private <T> T runInJobContext(RunnableWithResult<T> runnable) {
		if (Context.getCurrentContext() != null) {
			runnable.run();
			return runnable.getResult();
		} else {
			getContext();
			runnable.run();
			Context.exit();
			return runnable.getResult();
		}
	}

	@Override
	protected void internalSetVariable(final String name, final Object content) {
		runInJobContext(new RunnableWithResult<Boolean>() {
			@Override
			public Boolean runWithTry() throws Throwable {
				if (!JavaScriptHelper.isSaveName(name))
					throw new RuntimeException("\"" + name + "\" is not a valid JavaScript variable name");

				final Scriptable scope = fScope;

				final Object jsOut = internaljavaToJS(content, scope);
				scope.put(name, scope, jsOut);

				return true;
			}
		});
	}

	protected Object internaljavaToJS(final Object value, final Scriptable scope) {
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

	@Override
	protected Collection<EaseDebugVariable> getDefinedVariables(Object scope) {
		final Collection<EaseDebugVariable> result = new HashSet<>();

		if (scope instanceof ImporterTopLevel) {
			final Object[] objectIDs = ((ImporterTopLevel) scope).getIds();

			for (final Object id : objectIDs) {
				final Object object = ((ImporterTopLevel) scope).get(id);
				if (acceptVariable(object)) {
					final EaseDebugVariable variable = createVariable(id.toString(), object);
					result.add(variable);
				}
			}

		} else if (scope instanceof NativeArray) {
			for (final int indexId : getArrayIndexIds(((NativeArray) scope))) {
				final EaseDebugVariable variable = createVariable("[" + indexId + "]", ((NativeArray) scope).get(indexId));
				result.add(variable);
			}

			for (final Object id : ((NativeArray) scope).getIds()) {
				// integers are already handled by the previous loop
				if (!(id instanceof Integer)) {
					final EaseDebugVariable variable = createVariable(id.toString(), ((NativeArray) scope).get(id));
					result.add(variable);
				}
			}

		} else if (scope instanceof NativeObject) {
			for (final Entry<String, Object> entry : getNativeChildObjects((NativeObject) scope).entrySet()) {
				final EaseDebugVariable variable = createVariable(entry.getKey(), entry.getValue());
				result.add(variable);
			}

		} else if (scope instanceof NativeJavaArray) {
			for (final Object id : ((NativeJavaArray) scope).getIds()) {
				if (id instanceof Integer) {
					final EaseDebugVariable variable = createVariable("[" + id + "]", ((NativeJavaArray) scope).get((Integer) id, (Scriptable) scope));
					result.add(variable);
				}
			}

		} else if (scope instanceof Scriptable) {
			for (final Object id : ((Scriptable) scope).getIds()) {
				final EaseDebugVariable variable = createVariable(id.toString(), ((Scriptable) scope).get(id.toString(), (Scriptable) scope));
				result.add(variable);
			}

		} else if (hasNoChildElements(scope))
			return result;

		else
			// marker that child variables should be resolved dynamically
			return null;

		return result;
	}

	/**
	 * Get array indices for a given native array. Rhino changed the interface of this method, therefore we need to take care of proper wrapping.
	 *
	 * @param nativeArray
	 *            native array to get indices for
	 * @return list of indices or empty lsit
	 */
	private static List<Integer> getArrayIndexIds(NativeArray nativeArray) {
		final Object indexIds = nativeArray.getIndexIds();

		List<Integer> transformedIndexIds;
		if (indexIds instanceof List<?>)
			transformedIndexIds = (List<Integer>) indexIds;
		else if (indexIds instanceof Integer[])
			transformedIndexIds = Arrays.asList((Integer[]) indexIds);
		else
			transformedIndexIds = Collections.emptyList();

		return transformedIndexIds;
	}

	@Override
	protected EaseDebugVariable createVariable(String name, Object value) {
		final EaseDebugVariable variable = super.createVariable(name, value);

		if (value instanceof NativeArray) {
			variable.getValue().setValueString("array[" + ((NativeArray) value).getIds().length + "]");
			variable.setType(Type.NATIVE_ARRAY);

		} else if (value instanceof NativeObject) {
			variable.getValue().setValueString("object{" + getNativeChildObjects((NativeObject) value).size() + "}");
			variable.setType(Type.NATIVE_OBJECT);

		} else if (value instanceof String) {
			try {
				if (hasVariable(name)) {
					// test for java string objects

					// we need to disable a debugger in case it is set as it cannot operate on the wrong context
					final Debugger debugger = getContext().getDebugger();
					final Object debuggerContextData = getContext().getDebuggerContextData();
					getContext().setDebugger(null, null);

					final Object inject = inject(name + ".length;", false);
					if (inject instanceof Integer)
						variable.setType(Type.NATIVE_OBJECT);

					// restore debugger settings after execution
					getContext().setDebugger(debugger, debuggerContextData);
				}
			} catch (final Exception e) {
				// could not execute type check, ignore
			}
		}

		return variable;
	}

	private Map<String, Object> getNativeChildObjects(NativeObject parent) {
		final HashMap<String, Object> childObjects = new HashMap<>();

		for (final Object id : parent.getIds()) {
			final Object object = parent.get(id);
			if (acceptVariable(object))
				childObjects.put(id.toString(), object);
		}

		return childObjects;
	}

	@Override
	protected boolean acceptVariable(Object value) {
		if ((value != null) && ((value.getClass().getName().startsWith("org.mozilla.javascript.gen"))
				|| (value.getClass().getName().startsWith("org.mozilla.javascript.Arguments"))))
			return false;

		return true;
	}

	@Override
	public ScriptObjectType getType(Object object) {
		if (object != null) {
			if (object instanceof NativeArray)
				return ScriptObjectType.NATIVE_ARRAY;

			if (object instanceof NativeObject)
				return ScriptObjectType.NATIVE_OBJECT;

			if (object.getClass().getName().startsWith("org.mozilla.javascript"))
				return ScriptObjectType.NATIVE;
		}

		return super.getType(object);
	}

	@Override
	public String toString(Object object) {
		if (object instanceof NativeArray) {
			final ArrayList<Object> elements = new ArrayList<>();
			for (final int indexId : ((NativeArray) object).getIndexIds())
				elements.add(((NativeArray) object).get(indexId));

			return buildArrayString(elements);
		}

		if (object instanceof NativeObject)
			return buildObjectString(getNativeChildObjects((NativeObject) object));

		if (getType(object) == ScriptObjectType.NATIVE)
			return "{}";

		return super.toString(object);
	}

	@Override
	protected void notifyExecutionListeners(Script script, int status) {
		if (!getTerminateOnIdle()) {
			// high probability to run in interactive shell mode

			if (IExecutionListener.SCRIPT_END == status) {
				try {
					setVariable("_", script.getResult().get());
				} catch (final ExecutionException e) {
					setVariable("_", e.getCause());
				}
			}
		}

		super.notifyExecutionListeners(script, status);
	}
}
