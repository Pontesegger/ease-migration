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

import org.eclipse.debug.core.ILaunch;
import org.eclipse.ease.IDebugEngine;
import org.eclipse.ease.debugging.EventDispatchJob;
import org.eclipse.ease.debugging.ScriptStackTrace;
import org.eclipse.ease.lang.javascript.rhino.RhinoScriptEngine;
import org.eclipse.ease.lang.javascript.rhino.debugger.model.RhinoDebugTarget;
import org.mozilla.javascript.Context;

/**
 * A script engine to execute/debug JavaScript code on a Rhino interpreter.
 */
public class RhinoDebuggerEngine extends RhinoScriptEngine implements IDebugEngine {

	public static final String ENGINE_ID = "org.eclipse.ease.javascript.rhinoDebugger";

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

	@Override
	public void setOptimizationLevel(final int level) {
		// ignore as debugging requires not to use optimizations
	}

	@Override
	public void setupDebugger(final ILaunch launch, final boolean suspendOnStartup, final boolean suspendOnScriptLoad, final boolean showDynamicCode) {
		final RhinoDebugTarget debugTarget = new RhinoDebugTarget(launch, suspendOnStartup, suspendOnScriptLoad, showDynamicCode);
		launch.addDebugTarget(debugTarget);

		fDebugger = new RhinoDebugger(this, showDynamicCode);

		final EventDispatchJob dispatcher = new EventDispatchJob(debugTarget, fDebugger);
		debugTarget.setDispatcher(dispatcher);
		fDebugger.setDispatcher(dispatcher);
		dispatcher.schedule();
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
