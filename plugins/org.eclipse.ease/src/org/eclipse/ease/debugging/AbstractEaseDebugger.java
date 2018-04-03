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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.ease.Activator;
import org.eclipse.ease.IDebugEngine;
import org.eclipse.ease.IExecutionListener;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Logger;
import org.eclipse.ease.Script;
import org.eclipse.ease.debugging.dispatcher.EventDispatchJob;
import org.eclipse.ease.debugging.dispatcher.IEventProcessor;
import org.eclipse.ease.debugging.events.AbstractEvent;
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

	public class ThreadState {
		/** Stacktrace is not set entirely, but incrementally updated by clients. */
		public ScriptStackTrace fStacktrace = new ScriptStackTrace();

		public ScriptStackTrace fExceptionStacktrace = null;

		public int fResumeType;

		public int fResumeLineNumber = 0;

		public boolean fSuspended = false;

		public ScriptStackTrace fResumeStack = new ScriptStackTrace();
	}

	private EventDispatchJob fDispatcher;

	private IDebugEngine fEngine;

	private final Map<Script, List<IBreakpoint>> fBreakpoints = new HashMap<>();

	private final boolean fShowDynamicCode;

	private boolean fTerminated = false;

	private final Map<Object, ThreadState> fThreadStates = new HashMap<>();

	/** Requests to evaluate expressions. */
	private final List<AbstractEvent> fEvaluationRequests = Collections.synchronizedList(new ArrayList<>());

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
	public void fireDispatchEvent(final IDebuggerEvent event) {
		if (fDispatcher != null)
			fDispatcher.addEvent(event);
	}

	@Override
	public void handleEvent(final IDebugEvent event) {
		if (!fTerminated) {
			DebugTracer.debug("Debugger", "process " + event);

			if (event instanceof ResumeRequest) {
				resume(((ResumeRequest) event).getType(), ((ResumeRequest) event).getThread());

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

			} else if (event instanceof TerminateRequest) {
				fBreakpoints.clear();
				getEngine().terminate();
				fireDispatchEvent(new EngineTerminatedEvent());
				setDispatcher(null);

			} else if (event instanceof AbstractEvent) {
				fEvaluationRequests.add((AbstractEvent) event);
				final ThreadState threadState = getThreadState(((AbstractEvent) event).getThread());
				synchronized (threadState) {
					threadState.notify();
				}
			}
		}
	}

	protected void suspend(final IDebuggerEvent event) {

		// only suspend if there possibly exists someone to wake us up again
		if (fDispatcher != null) {
			final ThreadState threadState = getThreadState(getThread());
			synchronized (threadState) {
				// need to fire event in synchronized code to avoid getting a resume event too soon
				threadState.fSuspended = true;
				fireDispatchEvent(event);

				DebugTracer.debug("Debugger", "\t engine suspended on state: " + threadState);

				try {
					while (threadState.fSuspended) {

						synchronized (fEvaluationRequests) {
							for (final AbstractEvent request : fEvaluationRequests) {
								final ThreadState threadStateForRequest = getThreadState(request.getThread());
								if (threadState.equals(threadStateForRequest)) {

									if (request instanceof EvaluateExpressionRequest) {
										try {
											final EaseDebugStackFrame scope = ((EvaluateExpressionRequest) request).getContext();
											final Object result = scope.getDebugFrame().inject(((EvaluateExpressionRequest) request).getExpression());
											fireDispatchEvent(new EvaluateExpressionEvent(((EvaluateExpressionRequest) request).getExpression(), result, null,
													((EvaluateExpressionRequest) request).getListener()));
										} catch (final Throwable e) {
											fireDispatchEvent(new EvaluateExpressionEvent(((EvaluateExpressionRequest) request).getExpression(), null, e,
													((EvaluateExpressionRequest) request).getListener()));
										}

									} else if (request instanceof GetStackFramesRequest) {
										fireDispatchEvent(new StackFramesEvent(getStacktrace(), getThread()));

									} else if (request instanceof GetVariablesRequest) {
										final EaseDebugStackFrame requestor = ((GetVariablesRequest) request).getRequestor();
										final Collection<EaseDebugVariable> variables = getEngine().getVariables(requestor.getDebugFrame());
										fireDispatchEvent(new VariablesEvent(requestor, variables));

									} else if (request instanceof SetVariablesRequest) {
										final IDebugElement requestor = ((SetVariablesRequest) request).getRequestor();
										try {
											if (requestor instanceof EaseDebugStackFrame) {

												// evaluate expression
												final Object result = getEngine().inject(((SetVariablesRequest) request).getExpression());
												// set variable
												((EaseDebugStackFrame) requestor).getDebugFrame()
														.setVariable(((SetVariablesRequest) request).getVariable().getName(), result);

												// re-fetch variables for current stackframe
												final Collection<EaseDebugVariable> variables = getEngine()
														.getVariables(((EaseDebugStackFrame) requestor).getDebugFrame());
												fireDispatchEvent(new VariablesEvent((EaseDebugStackFrame) requestor, variables));
											}

										} catch (final Throwable e) {
											Logger.error(Activator.PLUGIN_ID,
													"Could not change variable <" + ((SetVariablesRequest) event).getVariable().getName() + "> to \""
															+ ((SetVariablesRequest) event).getExpression() + "\"",
													e);
										}
									}
								}
							}
						}

						threadState.wait();
					}

				} catch (final InterruptedException e) {
					threadState.fSuspended = false;
				}

				DebugTracer.debug("Debugger", "\t engine resumed");

				fireDispatchEvent(new ResumedEvent(getThread(), getThreadState(getThread()).fResumeType));
			}
		}
	}

	protected void resume(final int resumeType, Object thread) {
		// called by dispatcher thread

		final ThreadState threadState = fThreadStates.get(thread);

		// UNSPECIFIED is sent by the debug target if execution is resumed automatically, so stay with last user resume request
		if (resumeType != DebugEvent.UNSPECIFIED) {
			threadState.fResumeType = resumeType;
			threadState.fResumeStack = threadState.fStacktrace.clone();
			threadState.fResumeLineNumber = (threadState.fResumeStack.size() > 0) ? threadState.fStacktrace.get(0).getLineNumber() : 0;
		}

		synchronized (threadState) {
			threadState.fSuspended = false;
			threadState.notifyAll();
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
			fThreadStates.clear();
			fBreakpoints.clear();
			break;

		case SCRIPT_START:
			// fall through
		case SCRIPT_INJECTION_START:
			// new script
			if (isTrackedScript(script))
				suspend(new ScriptReadyEvent(script, getThread(), getThreadState(getThread()).fStacktrace.isEmpty()));

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

	/**
	 * Get current stack trace. If the current trace is empty (script not started or terminated) we get an optional exception stack trace. This is by default
	 * empty and will only be filled when an exception is thrown.
	 *
	 * @return
	 */
	public ScriptStackTrace getStacktrace() {
		return getThreadState(getThread()).fStacktrace;
	}

	protected void setStacktrace(final ScriptStackTrace stacktrace) {
		getThreadState(getThread()).fStacktrace = stacktrace;
	}

	public ScriptStackTrace getExceptionStacktrace() {
		return getExceptionStacktrace(getThread());
	}

	public ScriptStackTrace getExceptionStacktrace(Object thread) {
		return getThreadState(thread).fExceptionStacktrace;
	}

	protected void setExceptionStacktrace(final ScriptStackTrace exceptionStacktrace) {
		getThreadState(getThread()).fExceptionStacktrace = exceptionStacktrace;
	}

	protected Object getThread() {
		return Thread.currentThread();
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

		final Object thread = getThread();

		// check breakpoints
		if (isActiveBreakpoint(script, lineNumber)) {
			suspend(new SuspendedEvent(DebugEvent.BREAKPOINT, thread, getStacktrace()));
			return;
		}

		// no breakpoint, check for step events
		switch (getThreadState(thread).fResumeType) {
		case DebugEvent.STEP_INTO:
			suspend(new SuspendedEvent(DebugEvent.STEP_END, thread, getStacktrace()));
			break;

		case DebugEvent.STEP_OVER:
			if (getThreadState(thread).fResumeStack.size() > getStacktrace().size()) {
				// stacktrace got smaller, we stepped out of a function or file
				if (!getStacktrace().isEmpty())
					suspend(new SuspendedEvent(DebugEvent.STEP_END, thread, getStacktrace()));

			} else if (getThreadState(thread).fResumeStack.size() == getStacktrace().size()) {
				// same stacktrace, check if line number changed
				if (getThreadState(thread).fResumeLineNumber != getStacktrace().get(0).getLineNumber())
					suspend(new SuspendedEvent(DebugEvent.STEP_END, thread, getStacktrace()));
			}

			break;

		case DebugEvent.STEP_RETURN:
			if (getThreadState(thread).fResumeStack.size() > getStacktrace().size())
				suspend(new SuspendedEvent(DebugEvent.STEP_END, thread, getStacktrace()));

			break;

		default:
			// either user did not request anything yet or "RESUME" was triggered
		}
	}

	protected boolean isActiveBreakpoint(Script script, int lineNumber) {
		final IBreakpoint breakpoint = getBreakpoint(script, lineNumber);
		return breakpoint != null;
	}

	protected ThreadState getThreadState(Object thread) {
		if (!fThreadStates.containsKey(thread))
			fThreadStates.put(thread, new ThreadState());

		return fThreadStates.get(thread);
	}
}
