/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.debugging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.ease.AbstractScriptEngine;
import org.eclipse.ease.IDebugEngine;
import org.eclipse.ease.IExecutionListener;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Script;
import org.eclipse.ease.debugging.dispatcher.EventDispatchJob;
import org.eclipse.ease.debugging.dispatcher.IEventProcessor;
import org.eclipse.ease.debugging.events.IDebugEvent;
import org.eclipse.ease.debugging.events.debugger.EngineStartedEvent;
import org.eclipse.ease.debugging.events.debugger.EngineTerminatedEvent;
import org.eclipse.ease.debugging.events.debugger.EvaluateExpressionEvent;
import org.eclipse.ease.debugging.events.debugger.IDebuggerEvent;
import org.eclipse.ease.debugging.events.debugger.ResumedEvent;
import org.eclipse.ease.debugging.events.debugger.ScriptReadyEvent;
import org.eclipse.ease.debugging.events.debugger.StackFramesEvent;
import org.eclipse.ease.debugging.events.debugger.SuspendedEvent;
import org.eclipse.ease.debugging.events.debugger.VariablesEvent;
import org.eclipse.ease.debugging.events.model.BreakpointRequest;
import org.eclipse.ease.debugging.events.model.EvaluateExpressionRequest;
import org.eclipse.ease.debugging.events.model.GetStackFramesRequest;
import org.eclipse.ease.debugging.events.model.GetVariablesRequest;
import org.eclipse.ease.debugging.events.model.ResumeRequest;
import org.eclipse.ease.debugging.events.model.SetVariablesRequest;
import org.eclipse.ease.debugging.events.model.TerminateRequest;
import org.eclipse.ease.debugging.model.EaseDebugStackFrame;
import org.eclipse.ease.debugging.model.EaseDebugVariable;

public abstract class AbstractEaseDebugger implements IEventProcessor, IExecutionListener {

	private EventDispatchJob fDispatcher;

	private IDebugEngine fEngine;

	private boolean fSuspended = false;

	private final Map<Script, List<IBreakpoint>> fBreakpoints = new HashMap<>();

	private final boolean fShowDynamicCode;

	private int fResumeType;

	/** Stacktrace is not set entirely, but incrementally updated by clients. */
	private ScriptStackTrace fStacktrace = new ScriptStackTrace();

	private ScriptStackTrace fExceptionStacktrace = null;

	private int fResumeStackSize = 0;

	private boolean fTerminated = false;

	private int fResumeLineNumber = 0;

	public AbstractEaseDebugger(final IDebugEngine engine, final boolean showDynamicCode) {
		fEngine = engine;
		fShowDynamicCode = showDynamicCode;

		fEngine.addExecutionListener(this);
	}

	/**
	 * Setter method for dispatcher.
	 *
	 * @param dispatcher
	 *            dispatcher for communication between debugger and debug target.
	 */
	@Override
	public void setDispatcher(final EventDispatchJob dispatcher) {
		fDispatcher = dispatcher;
	}

	/**
	 * Helper method to raise event via dispatcher.
	 *
	 * @param event
	 *            Debug event to be raised.
	 */
	protected void fireDispatchEvent(final IDebuggerEvent event) {
		if (fDispatcher != null)
			fDispatcher.addEvent(event);
	}

	protected void suspend(final IDebuggerEvent event) {

		// only suspend if there possibly exists someone to wake us up again
		if (fDispatcher != null) {
			synchronized (fEngine) {
				// need to fire event in synchronized code to avoid getting a resume event too soon
				fSuspended = true;
				fireDispatchEvent(event);

				DebugTracer.debug("Debugger", "\t engine suspended");

				try {
					while (fSuspended)
						fEngine.wait();

				} catch (final InterruptedException e) {
					fSuspended = false;
				}

				DebugTracer.debug("Debugger", "\t engine resumed");

				fireDispatchEvent(new ResumedEvent(Thread.currentThread(), getResumeType()));
			}
		}
	}

	protected void resume(final int resumeType) {
		// UNSPECIFIED is sent by the debug target if execution is resumed automatically, so stay with last user resume request
		if (resumeType != DebugEvent.UNSPECIFIED) {
			fResumeType = resumeType;
			fResumeStackSize = getStacktrace().size();
			fResumeLineNumber = (fResumeStackSize > 0) ? getStacktrace().get(0).getLineNumber() : 0;
		}

		synchronized (fEngine) {
			fSuspended = false;
			fEngine.notifyAll();
		}
	}

	protected IDebugEngine getEngine() {
		return fEngine;
	}

	/**
	 * Notify function called by Eclipse EASE framework.
	 *
	 * Raises according events depending on status
	 */
	@Override
	public void notify(final IScriptEngine engine, final Script script, final int status) {
		switch (status) {
		case ENGINE_START:
			fireDispatchEvent(new EngineStartedEvent());
			break;

		case ENGINE_END:
			fireDispatchEvent(new EngineTerminatedEvent());

			// allow for garbage collection
			fTerminated = true;
			fEngine.removeExecutionListener(this);
			fEngine = null;
			fDispatcher = null;
			fStacktrace.clear();
			fExceptionStacktrace = null;
			fBreakpoints.clear();
			break;

		case SCRIPT_START:
			// fall through
		case SCRIPT_INJECTION_START:
			// new script
			if (isTrackedScript(script))
				suspend(new ScriptReadyEvent(script, Thread.currentThread(), fStacktrace.isEmpty()));

			break;

		case SCRIPT_END:
			// fall through
		case SCRIPT_INJECTION_END:
			// TODO remove script from stack
			break;

		default:
			// unknown event
			break;
		}
	}

	@Override
	public void handleEvent(final IDebugEvent event) {
		if (!fTerminated) {
			DebugTracer.debug("Debugger", "process " + event);

			if (event instanceof ResumeRequest) {
				resume(((ResumeRequest) event).getType());

			} else if (event instanceof BreakpointRequest) {
				if (((BreakpointRequest) event).isRemoveAllBreakpointsRequest())
					fBreakpoints.clear();

				else {
					final Script script = ((BreakpointRequest) event).getScript();
					if (!fBreakpoints.containsKey(script))
						fBreakpoints.put(script, new ArrayList<IBreakpoint>());

					if (((BreakpointRequest) event).getMode() == BreakpointRequest.Mode.ADD)
						fBreakpoints.get(script).add(((BreakpointRequest) event).getBreakpoint());
					else
						fBreakpoints.get(script).remove(((BreakpointRequest) event).getBreakpoint());
				}

			} else if (event instanceof GetStackFramesRequest) {
				fireDispatchEvent(new StackFramesEvent(getStacktrace(), ((AbstractScriptEngine) getEngine()).getThread()));

			} else if (event instanceof GetVariablesRequest) {
				final EaseDebugStackFrame requestor = ((GetVariablesRequest) event).getRequestor();
				final Collection<EaseDebugVariable> variables = getEngine().getVariables(requestor.getDebugFrame());
				fireDispatchEvent(new VariablesEvent(requestor, variables));

			} else if (event instanceof EvaluateExpressionRequest) {
				final IDebugElement scope = ((EvaluateExpressionRequest) event).getContext();
				if (scope instanceof EaseDebugStackFrame) {
					try {
						final Object result = ((EaseDebugStackFrame) scope).getDebugFrame().inject(((EvaluateExpressionRequest) event).getExpression());
						fireDispatchEvent(new EvaluateExpressionEvent(((EvaluateExpressionRequest) event).getExpression(), result, null,
								((EvaluateExpressionRequest) event).getListener()));
					} catch (final Throwable e) {
						fireDispatchEvent(new EvaluateExpressionEvent(((EvaluateExpressionRequest) event).getExpression(), null, e,
								((EvaluateExpressionRequest) event).getListener()));
					}
				}

			} else if (event instanceof SetVariablesRequest) {
				final IDebugElement requestor = ((SetVariablesRequest) event).getRequestor();
				try {
					if (requestor instanceof EaseDebugStackFrame) {

						// evaluate expression
						final Object result = getEngine().inject(((SetVariablesRequest) event).getExpression());
						// set variable
						((EaseDebugStackFrame) requestor).getDebugFrame().setVariable(((SetVariablesRequest) event).getVariable().getName(), result);

						// re-fetch variables for current stackframe
						final Collection<EaseDebugVariable> variables = getEngine().getVariables(((EaseDebugStackFrame) requestor).getDebugFrame());
						fireDispatchEvent(new VariablesEvent((EaseDebugStackFrame) requestor, variables));
					}

				} catch (final Throwable e) {
					// could not set variable
					// FIXME debug output
					e.printStackTrace(System.out);
				}

			} else if (event instanceof TerminateRequest) {
				fBreakpoints.clear();
				getEngine().terminate();
				fireDispatchEvent(new EngineTerminatedEvent());
				setDispatcher(null);
			}
		}
	}

	/**
	 * Get a breakpoint for a given line in a script.
	 *
	 * @param script
	 *            script to look for
	 * @param lineNumber
	 *            line number within script to check
	 * @return {@link IBreakpoint} instance or <code>null</code>
	 */
	protected IBreakpoint getBreakpoint(final Script script, final int lineNumber) {
		final List<IBreakpoint> breakpoints = fBreakpoints.get(script);
		if (breakpoints != null) {
			for (final IBreakpoint breakpoint : breakpoints) {
				try {
					if (breakpoint.isEnabled()) {
						final int breakLocation = breakpoint.getMarker().getAttribute(IMarker.LINE_NUMBER, -1);
						if (lineNumber == breakLocation)
							return breakpoint;
					}
				} catch (final CoreException e) {
					// cannot check enabled state, ignore
				}
			}
		}

		return null;
	}

	protected boolean isTrackedScript(final Script script) {
		return (!script.isDynamic()) || fShowDynamicCode;
	}

	protected int getResumeType() {
		return fResumeType;
	}

	/**
	 * Get current stack trace. If the current trace is empty (script not started or terminated) we get an optional exception stack trace. This is by default
	 * empty and will only be filled when an exception is thrown.
	 *
	 * @return
	 */
	public ScriptStackTrace getStacktrace() {
		return fStacktrace;
	}

	protected void setStacktrace(final ScriptStackTrace stacktrace) {
		fStacktrace = stacktrace;
	}

	public ScriptStackTrace getExceptionStacktrace() {
		return fExceptionStacktrace;
	}

	protected void setExceptionStacktrace(final ScriptStackTrace exceptionStacktrace) {
		fExceptionStacktrace = exceptionStacktrace;
	}

	/**
	 * Called by the debug instance on a line change. Checks for suspend conditions and suspends if necessary.
	 *
	 * @param script
	 * @param lineNumber
	 */
	protected void processLine(final Script script, final int lineNumber) {

		if (!isTrackedScript(script))
			return;

		if (lineNumber < 1)
			return;

		if (getStacktrace().isEmpty())
			return;

		DebugTracer.debug("Debugger", "\t... processing line " + script.getTitle() + ":" + lineNumber);

		// check breakpoints
		final IBreakpoint breakpoint = getBreakpoint(script, lineNumber);
		if (breakpoint != null) {
			suspend(new SuspendedEvent(DebugEvent.BREAKPOINT, ((AbstractScriptEngine) fEngine).getThread(), getStacktrace()));
			return;
		}

		// no breakpoint, check for step events
		switch (getResumeType()) {
		case DebugEvent.STEP_INTO:
			suspend(new SuspendedEvent(DebugEvent.STEP_END, ((AbstractScriptEngine) fEngine).getThread(), getStacktrace()));
			break;

		case DebugEvent.STEP_OVER:
			if (fResumeStackSize > getStacktrace().size()) {
				// stacktrace got smaller, we stepped out of a function or file
				if (!getStacktrace().isEmpty())
					suspend(new SuspendedEvent(DebugEvent.STEP_END, ((AbstractScriptEngine) fEngine).getThread(), getStacktrace()));

			} else if (fResumeStackSize == getStacktrace().size()) {
				// same stacktrace, check if line number changed
				if (fResumeLineNumber != getStacktrace().get(0).getLineNumber())
					suspend(new SuspendedEvent(DebugEvent.STEP_END, ((AbstractScriptEngine) fEngine).getThread(), getStacktrace()));
			}

			break;

		case DebugEvent.STEP_RETURN:
			if (fResumeStackSize > getStacktrace().size())
				suspend(new SuspendedEvent(DebugEvent.STEP_END, ((AbstractScriptEngine) fEngine).getThread(), getStacktrace()));

			break;

		default:
			// either user did not request anything yet or "RESUME" was triggered
		}
	}
}
