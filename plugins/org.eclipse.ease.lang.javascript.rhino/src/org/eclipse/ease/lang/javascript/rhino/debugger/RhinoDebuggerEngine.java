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
package org.eclipse.ease.lang.javascript.rhino.debugger;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.ease.IDebugEngine;
import org.eclipse.ease.Script;
import org.eclipse.ease.debugging.ScriptStackTrace;
import org.eclipse.ease.debugging.dispatcher.EventDispatchJob;
import org.eclipse.ease.debugging.events.debugger.ThreadCreatedEvent;
import org.eclipse.ease.debugging.events.debugger.ThreadTerminatedEvent;
import org.eclipse.ease.debugging.model.EaseDebugTarget;
import org.eclipse.ease.debugging.model.EaseDebugVariable;
import org.eclipse.ease.lang.javascript.JavaScriptHelper;
import org.eclipse.ease.lang.javascript.rhino.RhinoScriptEngine;
import org.eclipse.ease.lang.javascript.rhino.debugger.RhinoDebugger.RhinoDebugFrame;
import org.eclipse.ease.lang.javascript.rhino.debugger.model.RhinoDebugTarget;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory.Listener;
import org.mozilla.javascript.Scriptable;

/**
 * A script engine to execute/debug JavaScript code on a Rhino interpreter.
 */
public class RhinoDebuggerEngine extends RhinoScriptEngine implements IDebugEngine, Listener {

	public static final String ENGINE_ID = "org.eclipse.ease.javascript.rhinoDebugger";

	public static boolean isSimpleType(final Object value) {
		return (value instanceof Integer) || (value instanceof Byte) || (value instanceof Short) || (value instanceof Boolean) || (value instanceof Character)
				|| (value instanceof Long) || (value instanceof Double) || (value instanceof Float);
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

		context.getFactory().addListener(this);
	}

	@Override
	protected synchronized void teardownEngine() {
		getContext().getFactory().removeListener(this);

		super.teardownEngine();
	}

	@Override
	protected Collection<EaseDebugVariable> getDefinedVariables(Object scope) {

		if (scope instanceof RhinoDebugFrame) {
			// rhino stackframe
			final Collection<EaseDebugVariable> result = new HashSet<>();
			final Map<String, Object> variables = ((RhinoDebugFrame) scope).getVariables();

			for (final Entry<String, Object> entry : variables.entrySet()) {
				if (acceptVariable(entry.getValue())) {
					final EaseDebugVariable variable = createVariable(entry.getKey(), entry.getValue());
					result.add(variable);
				}
			}

			final EaseDebugTarget debugTarget = new EaseDebugTarget(null, false, false, false) {
				@Override
				protected IBreakpoint[] getBreakpoints(Script script) {
					return null;
				}

				@Override
				public boolean supportsBreakpoint(IBreakpoint breakpoint) {
					return false;
				}
			};

			for (final EaseDebugVariable entry : result)
				entry.setParent(debugTarget);

			return result;
		}

		return super.getDefinedVariables(scope);
	}

	@Override
	public Collection<EaseDebugVariable> getVariables(Object scope) {
		return getDefinedVariables(scope);
	}

	@Override
	protected boolean acceptVariable(Object value) {
		if ((value != null) && ("org.mozilla.javascript.InterpretedFunction".equals(value.getClass().getName())))
			return false;

		return super.acceptVariable(value);
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
		removeExecutionListener(fDebugger);

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

	@Override
	public ScriptStackTrace getExceptionStackTrace(Object thread) {
		if ((fDebugger.getExceptionStacktrace(thread) == null) || (fDebugger.getExceptionStacktrace(thread).isEmpty()))
			return super.getExceptionStackTrace();

		return fDebugger.getExceptionStacktrace(thread);
	}

	@Override
	public void contextCreated(Context cx) {
		cx.setDebugger(fDebugger, null);
		fDebugger.fireDispatchEvent(new ThreadCreatedEvent());
	}

	@Override
	public void contextReleased(Context cx) {
		cx.setDebugger(null, null);
		fDebugger.fireDispatchEvent(new ThreadTerminatedEvent());
	}
}
