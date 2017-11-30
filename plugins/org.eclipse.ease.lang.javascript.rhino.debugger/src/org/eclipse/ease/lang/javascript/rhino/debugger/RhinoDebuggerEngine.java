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
package org.eclipse.ease.lang.javascript.rhino.debugger;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.ease.IDebugEngine;
import org.eclipse.ease.debugging.ScriptStackTrace;
import org.eclipse.ease.debugging.dispatcher.EventDispatchJob;
import org.eclipse.ease.debugging.model.EaseDebugVariable;
import org.eclipse.ease.debugging.model.EaseDebugVariable.Type;
import org.eclipse.ease.lang.javascript.JavaScriptHelper;
import org.eclipse.ease.lang.javascript.rhino.RhinoScriptEngine;
import org.eclipse.ease.lang.javascript.rhino.debugger.RhinoDebugger.RhinoDebugFrame;
import org.eclipse.ease.lang.javascript.rhino.debugger.model.RhinoDebugTarget;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;

/**
 * A script engine to execute/debug JavaScript code on a Rhino interpreter.
 */
public class RhinoDebuggerEngine extends RhinoScriptEngine implements IDebugEngine {

	public static final String ENGINE_ID = "org.eclipse.ease.javascript.rhinoDebugger";

	public static boolean isSimpleType(final Object value) {
		return (value instanceof Integer) || (value instanceof Byte) || (value instanceof Short) || (value instanceof Boolean) || (value instanceof Character)
				|| (value instanceof Long) || (value instanceof Double) || (value instanceof Float);
	}

	private static String getReferenceType(Object value) {
		if (value != null) {
			if (value instanceof NativeArray)
				return "JavaScript Array";

			if (value instanceof NativeObject)
				return "JavaScript Object";

			if (value.getClass().getName().startsWith("org.mozilla.javascript"))
				return "Generic JavaScript";

			if (value instanceof Integer)
				return "int";

			if (value instanceof Byte)
				return "byte";

			if (value instanceof Short)
				return "short";

			if (value instanceof Boolean)
				return "boolean";

			if (value instanceof Character)
				return "char";

			if (value instanceof Long)
				return "long";

			if (value instanceof Double)
				return "double";

			if (value instanceof Float)
				return "float";

			return "Java Object";

		} else
			return "";
	}

	private RhinoDebugger fDebugger;

	/**
	 * Creates a new Rhino Debugger interpreter.
	 */
	public RhinoDebuggerEngine() {
		super("Rhino Debugger");

		fDebugger = new LineNumberDebugger(this);
	}

	@Override
	protected synchronized void setupEngine() {
		super.setupEngine();

		final Context context = getContext();

		context.setOptimizationLevel(-1);
		context.setGeneratingDebug(true);
		context.setGeneratingSource(true);
		context.setDebugger(fDebugger, null);
	}

	private EaseDebugVariable createVariable(String name, Object value) {
		final String referenceType = getReferenceType(value);
		final EaseDebugVariable variable = new EaseDebugVariable(name, value, referenceType);
		if (value instanceof NativeArray) {
			variable.getValue().setValueString("array[" + ((NativeArray) value).getIds().length + "]");
			variable.setType(Type.NATIVE_ARRAY);
		}

		if (value instanceof NativeObject) {
			variable.getValue().setValueString("object{" + ((NativeObject) value).getIds().length + "}");
			variable.setType(Type.NATIVE_OBJECT);
		}

		// TODO find nicer approach to set type
		if ("Java Object".equals(referenceType))
			variable.setType(Type.JAVA_OBJECT);

		variable.getValue().setVariables(getVariables(value));

		return variable;
	}

	@Override
	public Collection<EaseDebugVariable> getVariables(Object scope) {
		final Collection<EaseDebugVariable> result = new HashSet<>();

		if (scope instanceof RhinoDebugFrame) {
			// rhino stackframe
			final Map<String, Object> variables = ((RhinoDebugFrame) scope).getVariables();

			for (final Entry<String, Object> entry : variables.entrySet()) {
				if (acceptVariable(entry.getValue())) {
					final EaseDebugVariable variable = createVariable(entry.getKey(), entry.getValue());
					result.add(variable);
				}
			}

		} else if (scope instanceof NativeArray) {
			for (final int indexId : ((NativeArray) scope).getIndexIds()) {
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
			for (final Object id : ((NativeObject) scope).getIds()) {
				final EaseDebugVariable variable = createVariable(id.toString(), ((NativeObject) scope).get(id));
				result.add(variable);
			}

		} else if (scope instanceof Scriptable) {
			if ("org.mozilla.javascript.Arguments".equals(scope.getClass().getName())) {
				for (int id = 0; id < ((Scriptable) scope).getIds().length; id++) {
					final EaseDebugVariable variable = createVariable("[" + id + "]", ((Scriptable) scope).get(id, (Scriptable) scope));
					result.add(variable);
				}

			} else {
				for (final Object id : ((Scriptable) scope).getIds()) {
					final EaseDebugVariable variable = createVariable(id.toString(), ((Scriptable) scope).get(id.toString(), (Scriptable) scope));
					result.add(variable);
				}
			}
		} else if (hasNoChildElements(scope))
			return result;

		else
			return null;

		return result;
	}

	private boolean hasNoChildElements(Object scope) {
		if (scope instanceof Double)
			return true;

		if (scope == null)
			return true;

		return false;
	}

	private boolean acceptVariable(Object value) {
		if ((value != null) && ("org.mozilla.javascript.InterpretedFunction".equals(value.getClass().getName())))
			return false;

		return true;
	}

	@Override
	public Object removeVariable(String name) {
		final Object result = getVariable(name);
		getScope().delete(name);

		return result;
	}

	public void setVariable(String name, Object content, Scriptable scope) {
		if (!JavaScriptHelper.isSaveName(name))
			throw new RuntimeException("\"" + name + "\" is not a valid JavaScript variable name");

		final Object jsOut = internaljavaToJS(content, scope);
		scope.put(name, scope, jsOut);
	}

	@Override
	public void setOptimizationLevel(final int level) {
		// ignore as debugging requires not to use optimizations
	}

	@Override
	public void setupDebugger(final ILaunch launch, final boolean suspendOnStartup, final boolean suspendOnScriptLoad, final boolean showDynamicCode) {
		final RhinoDebugTarget debugTarget = new RhinoDebugTarget(launch, suspendOnStartup, suspendOnScriptLoad, showDynamicCode);
		launch.addDebugTarget(debugTarget);

		fDebugger = new RhinoDebugger(this, showDynamicCode);

		new EventDispatchJob(debugTarget, fDebugger);
	}

	@Override
	public ScriptStackTrace getStackTrace() {
		return fDebugger.getStacktrace();
	}

	@Override
	public ScriptStackTrace getExceptionStackTrace() {
		if ((fDebugger.getExceptionStacktrace() == null) || (fDebugger.getExceptionStacktrace().isEmpty()))
			return super.getExceptionStackTrace();

		return fDebugger.getExceptionStacktrace();
	}
}
