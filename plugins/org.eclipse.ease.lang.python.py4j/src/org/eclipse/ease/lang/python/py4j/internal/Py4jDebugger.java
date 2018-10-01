/*******************************************************************************
 * Copyright (c) 2018 Martin Kloesch and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Martin Kloesch - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.python.py4j.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.ease.ExitException;
import org.eclipse.ease.IDebugEngine;
import org.eclipse.ease.Script;
import org.eclipse.ease.debugging.events.debugger.IDebuggerEvent;
import org.eclipse.ease.lang.python.debugger.IPythonScriptRegistry;
import org.eclipse.ease.lang.python.debugger.PythonBreakpoint;
import org.eclipse.ease.lang.python.debugger.PythonDebugger;

/**
 * Extension of {@link PythonDebugger} with additional {@link ICodeTraceFilter} to lower amount of trace dispatches.
 */
public class Py4jDebugger extends PythonDebugger {
	/**
	 * Extended code tracer doing pre-filtering.
	 */
	private ICodeTraceFilter fTraceFilter;

	/**
	 * @see PythonDebugger#PythonDebugger(IDebugEngine, boolean)
	 */
	public Py4jDebugger(IDebugEngine engine, boolean showDynamicCode) {
		super(engine, showDynamicCode);
	}

	/**
	 * Sets extended code tracer doing pre-filtering of dispatch calls..
	 *
	 * @param traceFilter
	 *            Extended code tracer.
	 */
	public void setTraceFilter(ICodeTraceFilter traceFilter) {
		fTraceFilter = traceFilter;
	}

	/**
	 * Returns list of all breakpoints in given file.
	 *
	 * @param filename
	 *            Filename to get all breakpoints for.
	 * @return List of breakpoints in given file.
	 */
	public List<PythonBreakpoint> getBreakpoints(String filename) {
		final IPythonScriptRegistry registry = getScriptRegistry();
		if (registry != null) {

			final Script script = registry.getScript(filename);
			if (script != null) {
				final List<IBreakpoint> breakpoints = fBreakpoints.get(script);
				if (breakpoints != null) {
					final List<PythonBreakpoint> pythonBreakpoints = new ArrayList<>();
					for (final IBreakpoint breakpoint : breakpoints) {
						final int lineno = breakpoint.getMarker().getAttribute(IMarker.LINE_NUMBER, -1);
						pythonBreakpoints.add(new PythonBreakpoint(filename, lineno));
					}
					return pythonBreakpoints;
				}
			}
		}

		return Collections.<PythonBreakpoint> emptyList();

	}

	@Override
	protected void suspend(IDebuggerEvent event) {
		fTraceFilter.suspend();
		super.suspend(event);

	}

	@Override
	protected void resume(int resumeType, Object thread) {
		super.resume(resumeType, thread);
		fTraceFilter.resume(resumeType);
	}

	@Override
	public Object execute(Script script) {
		try {
			String reference = script.getTitle();
			final IPythonScriptRegistry registry = getScriptRegistry();
			if (registry != null) {
				registry.put(script);
				reference = registry.getReference(script);
			}
			return fTraceFilter.run(script, reference);

		} catch (final Exception e) {
			/*
			 * When terminating, #handleEvent sets resume type to STEP_END, which causes #traceDispatch to raise an ExitException. The ExitException is
			 * propagated back to python, and eventually ends up back here at Exit method. However Py4J does not support propogating the same type of exception
			 * across the Python/Java barrier, so what ends up being thrown is a Py4JException instead.
			 *
			 * Therefore we catch that case and re-raise the expected ExitException()
			 */
			if (getThreadState(getThread()).fResumeType == DebugEvent.STEP_END) {
				throw new ExitException("Debug aborted by user");
			}
			throw e;
		}
	}

	@Override
	protected void breakpointAdded(final Script script, final IBreakpoint breakpoint) {
		final IPythonScriptRegistry registry = getScriptRegistry();
		if (registry == null) {
			return;
		}

		final String reference = registry.getReference(script);
		if (reference != null) {
			final int linenumber = breakpoint.getMarker().getAttribute(IMarker.LINE_NUMBER, -1);

			if (linenumber != -1) {
				fTraceFilter.setBreakpoint(new PythonBreakpoint(reference, linenumber));
			}
		}
	}

	@Override
	protected void breakpointRemoved(final Script script, final IBreakpoint breakpoint) {
		final IPythonScriptRegistry registry = getScriptRegistry();
		if (registry == null) {
			return;
		}

		final String reference = registry.getReference(script);
		if (reference != null) {
			final int linenumber = breakpoint.getMarker().getAttribute(IMarker.LINE_NUMBER, -1);

			if (linenumber != -1) {
				fTraceFilter.removeBreakpoint(new PythonBreakpoint(reference, linenumber));
			}
		}
	}
}
