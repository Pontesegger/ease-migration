/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
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

	private IScriptRegistry fScriptRegistry;

	private IDebugEngine fEngine;

	protected final Map<Script, List<IBreakpoint>> fBreakpoints = new HashMap<>();

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
	 * Protected getter for event dispatch job.
	 *
	 * @return {@link #fDispatcher}.
	 */
	protected EventDispatchJob getDispatcher() {
		return fDispatcher;
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

		// TODO: Use setScriptRegistry explicitly
		setScriptRegistry(dispatcher);
	}

	/**
	 * Setter method for script registry for lookups between different types of file identifications.
	 *
	 * @param registry
	 *            Script registry to be used.
	 */
	public void setScriptRegistry(final IScriptRegistry registry) {
		fScriptRegistry = registry;
	}

	/**
	 * Protected getter for subclasses to have access to {@link #fScriptRegistry}.
	 *
	 * @return {@link #fScriptRegistry}
	 */
	protected IScriptRegistry getScriptRegistry() {
		return fScriptRegistry;
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

			if (event instanceof BreakpointRequest) {
				if (((BreakpointRequest) event).isRemoveAllBreakpointsRequest())
					fBreakpoints.clear();

				else {
					final Script script = ((BreakpointRequest) event).getScript();
					if (!fBreakpoints.containsKey(script))
						fBreakpoints.put(script, new ArrayList<IBreakpoint>());

					if (((BreakpointRequest) event).getMode() == BreakpointRequest.Mode.ADD) {
						final IBreakpoint breakpoint = ((BreakpointRequest) event).getBreakpoint();
						fBreakpoints.get(script).add(breakpoint);
						breakpointAdded(script, breakpoint);
					} else {
						final IBreakpoint breakpoint = ((BreakpointRequest) event).getBreakpoint();
						fBreakpoints.get(script).remove(breakpoint);
						breakpointRemoved(script, breakpoint);
					}
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

	/**
	 * Callback triggered when breakpoint has been added.
	 *
	 * No-op implementations can be overridden by subclasses.
	 *
	 * @param script
	 *            Script this breakpoint belongs to.
	 * @param breakpoint
	 *            Breakpoint information.
	 */
	protected void breakpointAdded(final Script script, final IBreakpoint breakpoint) {

	}

	/**
	 * Callback triggered when breakpoint has been removed.
	 *
	 * No-op implementations can be overridden by subclasses.
	 *
	 * @param script
	 *            Script this breakpoint belonged to.
	 * @param breakpoint
	 *            Breakpoint information.
	 */
	protected void breakpointRemoved(final Script script, final IBreakpoint breakpoint) {

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
						threadState.wait();
						// handle events after wait() as events might alter the threadState
						handleEventsInSuspendedState(threadState);
					}

				} catch (final InterruptedException e) {
					threadState.fSuspended = false;
				}

				DebugTracer.debug("Debugger", "\t engine resumed");

				fireDispatchEvent(new ResumedEvent(getThread(), getThreadState(getThread()).fResumeType));
			}
		}
	}

	private void handleEventsInSuspendedState(ThreadState threadState) {
		List<AbstractEvent> requests;
		synchronized (fEvaluationRequests) {
			requests = new ArrayList<>(fEvaluationRequests);
		}

		while (!requests.isEmpty()) {
			final AbstractEvent request = requests.remove(0);
			final ThreadState threadStateForRequest = getThreadState(request.getThread());
			if (threadState.equals(threadStateForRequest)) {

				// we handle this event, remove from queue
				fEvaluationRequests.remove(request);

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
							((EaseDebugStackFrame) requestor).getDebugFrame().setVariable(((SetVariablesRequest) request).getVariable().getName(), result);

							// re-fetch variables for current stackframe
							final Collection<EaseDebugVariable> variables = getEngine().getVariables(((EaseDebugStackFrame) requestor).getDebugFrame());
							fireDispatchEvent(new VariablesEvent((EaseDebugStackFrame) requestor, variables));
						}

					} catch (final Throwable e) {
						Logger.error(Activator.PLUGIN_ID, "Could not change variable <" + ((SetVariablesRequest) request).getVariable().getName() + "> to \""
								+ ((SetVariablesRequest) request).getExpression() + "\"", e);
					}

				} else if (request instanceof ResumeRequest) {
					resume(((ResumeRequest) request).getType(), ((ResumeRequest) request).getThread());
				}
			}
		}
	}

	protected void resume(final int resumeType, Object thread) {
		// called by engine thread

		final ThreadState threadState = fThreadStates.get(thread);

		// UNSPECIFIED is sent by the debug target if execution is resumed automatically, so stay with last user resume request
		if (resumeType != DebugEvent.UNSPECIFIED) {
			threadState.fResumeType = resumeType;
			threadState.fResumeStack = threadState.fStacktrace.clone();
			threadState.fResumeLineNumber = (threadState.fResumeStack.size() > 0) ? threadState.fStacktrace.get(0).getLineNumber() : 0;
		}

		threadState.fSuspended = false;
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

	/**
	 * Checks if the given script is tracked by debugger.
	 *
	 * @param script
	 *            Script to be checked.
	 * @return {@code true} if script is being tracked by debugger.
	 */
	public boolean isTrackedScript(final Script script) {
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
