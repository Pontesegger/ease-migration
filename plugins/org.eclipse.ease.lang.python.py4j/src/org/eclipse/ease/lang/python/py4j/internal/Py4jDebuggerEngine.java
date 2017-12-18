/*******************************************************************************
 * Copyright (c) 2014 Martin Kloesch and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Martin Kloesch - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.lang.python.py4j.internal;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.ease.Script;
import org.eclipse.ease.ScriptEngineException;
import org.eclipse.ease.debugging.ScriptStackTrace;
import org.eclipse.ease.debugging.dispatcher.EventDispatchJob;
import org.eclipse.ease.debugging.model.EaseDebugVariable;
import org.eclipse.ease.lang.python.debugger.IPythonDebugEngine;
import org.eclipse.ease.lang.python.debugger.PythonDebugger;
import org.eclipse.ease.lang.python.debugger.ResourceHelper;
import org.eclipse.ease.lang.python.debugger.model.PythonDebugTarget;

/***
 * A script engine to debug Python code on a PY4J engine.
 *
 * Uses most of {@link Py4jDebuggerEngine}'s functionality and only extends it when file is to be debugged.
 */

public class Py4jDebuggerEngine extends Py4jScriptEngine implements IPythonDebugEngine {
	private PythonDebugger fDebugger = null;

	@Override
	public void setDebugger(final PythonDebugger debugger) {
		fDebugger = debugger;
	}

	@Override
	protected void setupEngine() throws ScriptEngineException {
		super.setupEngine();

		// in case we were called using "Run as"
		if (fDebugger != null) {
			try {
				// load python part of debugger
				final InputStream stream = ResourceHelper.getResourceStream("org.eclipse.ease.lang.python", "pysrc/edb.py");

				internalSetVariable(PythonDebugger.PYTHON_DEBUGGER_VARIABLE, fDebugger);
				super.internalExecute(new Script("Load Python debugger", stream), null);

			} catch (final Throwable e) {
				throw new ScriptEngineException("Failed to load Python Debugger", e);
			}
		}
	}

	@Override
	protected Object internalExecute(final Script script, final String fileName) throws Throwable {
		if (fDebugger != null) {
			return fDebugger.execute(script);
		}
		return super.internalExecute(script, fileName);
	}

	@Override
	public void setupDebugger(final ILaunch launch, final boolean suspendOnStartup, final boolean suspendOnScriptLoad, final boolean showDynamicCode) {
		final PythonDebugTarget target = new PythonDebugTarget(launch, suspendOnStartup, suspendOnScriptLoad, showDynamicCode);
		launch.addDebugTarget(target);

		final PythonDebugger debugger = new PythonDebugger(this, showDynamicCode);

		setDebugger(debugger);

		new EventDispatchJob(target, debugger);
	}

	@Override
	public ScriptStackTrace getExceptionStackTrace() {
		// FIXME to be implemented
		return null;
	}

	@Override
	public Object removeVariable(final String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<EaseDebugVariable> getVariables(Object scope) {
		// FIXME to be implemented correctly on the scope

		final Collection<EaseDebugVariable> variables = new ArrayList<>();

		for (final Entry<String, Object> entry : getVariables().entrySet())
			variables.add(new EaseDebugVariable(entry.getKey(), entry.getValue(), "Python"));

		return variables;
	}
}
