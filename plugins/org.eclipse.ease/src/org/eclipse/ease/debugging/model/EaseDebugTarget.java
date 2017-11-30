/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.debugging.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.ease.Script;
import org.eclipse.ease.debugging.DebugTracer;
import org.eclipse.ease.debugging.IScriptDebugFrame;
import org.eclipse.ease.debugging.dispatcher.EventDispatchJob;
import org.eclipse.ease.debugging.dispatcher.IEventProcessor;
import org.eclipse.ease.debugging.events.IDebugEvent;
import org.eclipse.ease.debugging.events.debugger.EngineStartedEvent;
import org.eclipse.ease.debugging.events.debugger.EngineTerminatedEvent;
import org.eclipse.ease.debugging.events.debugger.EvaluateExpressionEvent;
import org.eclipse.ease.debugging.events.debugger.ResumedEvent;
import org.eclipse.ease.debugging.events.debugger.ScriptReadyEvent;
import org.eclipse.ease.debugging.events.debugger.StackFramesEvent;
import org.eclipse.ease.debugging.events.debugger.SuspendedEvent;
import org.eclipse.ease.debugging.events.debugger.VariablesEvent;
import org.eclipse.ease.debugging.events.model.BreakpointRequest;
import org.eclipse.ease.debugging.events.model.BreakpointRequest.Mode;
import org.eclipse.ease.debugging.events.model.IModelRequest;
import org.eclipse.ease.debugging.events.model.ResumeRequest;
import org.eclipse.ease.debugging.events.model.SuspendRequest;
import org.eclipse.ease.debugging.events.model.TerminateRequest;

public abstract class EaseDebugTarget extends EaseDebugElement implements IDebugTarget, IEventProcessor {

	private enum State {
		NOT_STARTED, SUSPENDED, RESUMED, STEPPING, TERMINATED, DISCONNECTED
	}

	private static final boolean DEBUG = org.eclipse.ease.Activator.getDefault().isDebugging()
			&& "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.ease/debug/debuggerEvents"));;

	private EventDispatchJob fDispatcher;

	private EaseDebugProcess fProcess = null;

	private final List<EaseDebugThread> fThreads = new ArrayList<>();

	private final ILaunch fLaunch;

	private State fState = State.NOT_STARTED;

	private final boolean fSuspendOnStartup;

	private final boolean fSuspendOnScriptLoad;

	private final boolean fShowDynamicCode;

	/** Unique IDs for each variable displayed during breakpoints. */
	private final List<Integer> fUniqueVariableIds = new ArrayList<>();

	public EaseDebugTarget(final ILaunch launch, final boolean suspendOnStartup, final boolean suspendOnScriptLoad, final boolean showDynamicCode) {
		super(null);
		fLaunch = launch;
		fSuspendOnStartup = suspendOnStartup;
		fSuspendOnScriptLoad = suspendOnScriptLoad;
		fShowDynamicCode = showDynamicCode;

		// subscribe for breakpoint changes
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);

		fireCreationEvent();
	}

	@Override
	public String getName() throws DebugException {
		return "EASE Debugger";
	}

	@Override
	public EaseDebugTarget getDebugTarget() {
		return this;
	}

	@Override
	public ILaunch getLaunch() {
		return fLaunch;
	}

	@Override
	public IProcess getProcess() {
		return fProcess;
	}

	@Override
	public EaseDebugThread[] getThreads() {
		return fThreads.toArray(new EaseDebugThread[fThreads.size()]);
	}

	@Override
	public boolean hasThreads() throws DebugException {
		return !fThreads.isEmpty();
	}

	public void fireDispatchEvent(final IModelRequest event) {
		if (fDispatcher != null)
			fDispatcher.addEvent(event);
	}

	// ************************************************************
	// IEventProcessor
	// ************************************************************

	@Override
	public void setDispatcher(final EventDispatchJob dispatcher) {
		fDispatcher = dispatcher;
	}

	@Override
	public synchronized void handleEvent(final IDebugEvent event) {
		if (fDispatcher != null) {

			DebugTracer.debug("Model", "process " + event);

			if (event instanceof EngineStartedEvent) {
				fProcess = new EaseDebugProcess(this);

				fProcess.fireCreationEvent();

			} else if (event instanceof ScriptReadyEvent) {
				// find existing DebugThread
				EaseDebugThread debugThread = findDebugThread(((ScriptReadyEvent) event).getThread());

				if (debugThread == null) {
					// thread does not exist, create new one
					debugThread = new EaseDebugThread(getDebugTarget(), ((ScriptReadyEvent) event).getThread());
					fThreads.add(debugThread);

					debugThread.fireCreationEvent();

				} else {
					// stack frames changed
					debugThread.fireChangeEvent(DebugEvent.CONTENT);
				}

				// set deferred breakpoints
				setDeferredBreakpoints(((ScriptReadyEvent) event).getScript());

				// tell framework we are suspended
				fState = State.SUSPENDED;
				debugThread.fireSuspendEvent(DebugEvent.CLIENT_REQUEST);

				// by default resume execution
				int stepType = DebugEvent.UNSPECIFIED;
				if (fSuspendOnScriptLoad)
					// suspend on any script load event
					stepType = DebugEvent.STEP_INTO;

				else if ((((ScriptReadyEvent) event).isRoot()) && (fSuspendOnStartup))
					// suspend on script startup event
					stepType = DebugEvent.STEP_INTO;

				// send resume request
				fireDispatchEvent(new ResumeRequest(stepType, debugThread.getThread()));

			} else if (event instanceof SuspendedEvent) {
				final EaseDebugThread debugThread = findDebugThread(((SuspendedEvent) event).getThread());
				debugThread.setStackFrames(filterFrames(((SuspendedEvent) event).getDebugFrames()));

				fState = State.SUSPENDED;
				debugThread.fireSuspendEvent(((SuspendedEvent) event).getType());

			} else if (event instanceof StackFramesEvent) {
				// stackframe refresh
				final EaseDebugThread debugThread = findDebugThread(((StackFramesEvent) event).getThread());
				debugThread.setStackFrames(filterFrames(((StackFramesEvent) event).getDebugFrames()));

				// stack frames changed
				debugThread.fireChangeEvent(DebugEvent.CONTENT);

			} else if (event instanceof VariablesEvent) {
				// variables refresh
				final EaseDebugStackFrame requestor = ((VariablesEvent) event).getRequestor();
				requestor.setVariables(((VariablesEvent) event).getVariables());

			} else if (event instanceof EvaluateExpressionEvent) {
				if (getThreads().length > 0) {
					((EvaluateExpressionEvent) event).getListener().watchEvaluationFinished(((EvaluateExpressionEvent) event).getWatchExpressionResult(this));
				}

			} else if (event instanceof ResumedEvent) {
				fState = State.RESUMED;
				final EaseDebugThread debugThread = findDebugThread(((ResumedEvent) event).getThread());
				debugThread.fireResumeEvent(((ResumedEvent) event).getType());

			} else if (event instanceof EngineTerminatedEvent) {
				cleanupOnTermination();
			}
		}
	}

	private void cleanupOnTermination() {
		// allow for garbage collection
		fDispatcher = null;

		// unsubscribe from breakpoint changes
		final DebugPlugin debugPlugin = DebugPlugin.getDefault();
		if (debugPlugin != null)
			debugPlugin.getBreakpointManager().removeBreakpointListener(this);

		fState = State.TERMINATED;
		fireTerminateEvent();
	}

	/**
	 * Remove dynamic code fragments in case they are disabled by the debug target.
	 *
	 * @param frames
	 *            frames to be filtered
	 * @return filtered frames
	 */
	private List<IScriptDebugFrame> filterFrames(final List<IScriptDebugFrame> frames) {
		if (fShowDynamicCode)
			return frames;

		return frames.stream().filter(frame -> !frame.getScript().isDynamic()).collect(Collectors.toList());
	}

	private EaseDebugThread findDebugThread(final Thread thread) {
		for (final EaseDebugThread debugThread : getThreads()) {
			if (thread.equals(debugThread.getThread()))
				return debugThread;
		}

		return null;
	}

	public int getUniqueVariableId(Object value) {
		final int hashCode = System.identityHashCode(value);

		int index = fUniqueVariableIds.indexOf(hashCode);
		if (index == -1) {
			fUniqueVariableIds.add(hashCode);
			index = fUniqueVariableIds.indexOf(hashCode);
		}

		return index;
	}

	private void setDeferredBreakpoints(final Script script) {

		final Object file = script.getFile();
		if (file instanceof IResource) {
			final IBreakpoint[] breakpoints = getBreakpoints(script);

			for (final IBreakpoint breakpoint : breakpoints) {
				if (file.equals(breakpoint.getMarker().getResource()))
					fireDispatchEvent(new BreakpointRequest(script, breakpoint, BreakpointRequest.Mode.ADD));
			}
		}
	}

	protected abstract IBreakpoint[] getBreakpoints(Script script);

	// ************************************************************
	// IBreakpointListener
	// ************************************************************

	@Override
	public void breakpointAdded(final IBreakpoint breakpoint) {
		handleBreakpointChange(breakpoint, BreakpointRequest.Mode.ADD);
	}

	@Override
	public void breakpointRemoved(final IBreakpoint breakpoint, final IMarkerDelta delta) {
		handleBreakpointChange(breakpoint, BreakpointRequest.Mode.REMOVE);
	}

	@Override
	public void breakpointChanged(final IBreakpoint breakpoint, final IMarkerDelta delta) {
		breakpointRemoved(breakpoint, delta);
		breakpointAdded(breakpoint);
	}

	private synchronized void handleBreakpointChange(final IBreakpoint breakpoint, final Mode mode) {
		final IResource affectedResource = breakpoint.getMarker().getResource();

		// see if we are affected by this breakpoint
		for (final EaseDebugThread thread : getThreads()) {
			for (final IStackFrame frame : thread.getStackFrames()) {
				if (frame instanceof EaseDebugStackFrame) {
					final Script script = ((EaseDebugStackFrame) frame).getScript();
					if (affectedResource.equals(script.getFile())) {
						// we need to deal with this breakpoint
						fireDispatchEvent(new BreakpointRequest(script, breakpoint, mode));
					}
				}
			}
		}
	}

	// ************************************************************
	// IMemoryBlockRetrieval
	// ************************************************************

	@Override
	public boolean supportsStorageRetrieval() {
		return false;
	}

	@Override
	public IMemoryBlock getMemoryBlock(final long startAddress, final long length) throws DebugException {
		// FIXME add correct plugin id
		throw new DebugException(new Status(IStatus.ERROR, "Activator.PLUGIN_ID", "getMemoryBlock() not supported by " + getName()));
	}

	// ************************************************************
	// ITerminate
	// ************************************************************

	@Override
	public boolean canTerminate() {
		return !isTerminated();
	}

	@Override
	public synchronized void terminate() {
		fireDispatchEvent(new TerminateRequest());
	}

	@Override
	public boolean isTerminated() {
		return State.TERMINATED == fState;
	}

	// ************************************************************
	// ISuspendResume
	// ************************************************************

	@Override
	public boolean canResume() {
		return isSuspended();
	}

	@Override
	public boolean canSuspend() {
		return !isSuspended();
	}

	@Override
	public synchronized void resume() {
		// FIXME do we need the thread here?
		final EaseDebugThread[] threads = getThreads();
		if (threads.length == 1)
			fireDispatchEvent(new ResumeRequest(DebugEvent.CLIENT_REQUEST, threads[0].getThread()));
	}

	@Override
	public synchronized void suspend() {
		fireDispatchEvent(new SuspendRequest());
	}

	@Override
	public boolean isSuspended() {
		return State.SUSPENDED == fState;
	}

	// ************************************************************
	// IDisconnect
	// ************************************************************

	@Override
	public boolean canDisconnect() {
		return canTerminate();
	}

	@Override
	public synchronized void disconnect() {
		// remove all breakpoints
		fireDispatchEvent(new BreakpointRequest(Mode.REMOVE));

		// resume interpreter
		fireDispatchEvent(new ResumeRequest(DebugEvent.CLIENT_REQUEST, null));

		// cleanup removes dispatcher instance. Needs to be called after all events have been sent
		cleanupOnTermination();
	}

	@Override
	public boolean isDisconnected() {
		return isTerminated();
	}

	// ************************************************************
	// IStep
	// ************************************************************

	@Override
	public boolean canStepInto() {
		return isSuspended();
	}

	@Override
	public boolean canStepOver() {
		return isSuspended();
	}

	@Override
	public boolean canStepReturn() {
		return isSuspended();
	}

	@Override
	public synchronized void stepInto() {
		final EaseDebugThread[] threads = getDebugTarget().getThreads();
		if (threads.length == 1)
			fireDispatchEvent(new ResumeRequest(DebugEvent.STEP_INTO, threads[0].getThread()));
	}

	@Override
	public synchronized void stepOver() {
		final EaseDebugThread[] threads = getDebugTarget().getThreads();
		if (threads.length == 1)
			fireDispatchEvent(new ResumeRequest(DebugEvent.STEP_OVER, threads[0].getThread()));
	}

	@Override
	public synchronized void stepReturn() {
		final EaseDebugThread[] threads = getDebugTarget().getThreads();
		if (threads.length == 1)
			fireDispatchEvent(new ResumeRequest(DebugEvent.STEP_RETURN, threads[0].getThread()));
	}

	@Override
	public boolean isStepping() {
		return State.STEPPING == fState;
	}
}
