/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.debugging.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.ease.debugging.IScriptDebugFrame;
import org.eclipse.ease.debugging.ScriptStackTrace;
import org.eclipse.ease.debugging.dispatcher.EventDispatchJob;
import org.eclipse.ease.debugging.dispatcher.IEventProcessor;
import org.eclipse.ease.debugging.events.IDebugEvent;
import org.eclipse.ease.debugging.events.debugger.ResumedEvent;
import org.eclipse.ease.debugging.events.debugger.StackFramesEvent;
import org.eclipse.ease.debugging.events.debugger.SuspendedEvent;
import org.eclipse.ease.debugging.events.debugger.ThreadTerminatedEvent;
import org.eclipse.ease.debugging.events.model.GetStackFramesRequest;
import org.eclipse.ease.debugging.events.model.ResumeRequest;
import org.eclipse.ease.debugging.events.model.SuspendRequest;

public class EaseDebugThread extends EaseDebugElement implements IThread, IEventProcessor {

	private final Object fThread;

	private final List<EaseDebugStackFrame> fStackFrames = new ArrayList<>();

	public EaseDebugThread(final EaseDebugTarget target, final Object thread) {
		super(target);

		fThread = thread;
	}

	@Override
	public String getName() {
		if (fThread instanceof Thread)
			return "Thread: " + ((Thread) fThread).getName();

		return "Thread: " + fThread.toString();
	}

	@Override
	public synchronized IStackFrame[] getStackFrames() {
		if ((fStackFrames.isEmpty()) && (isSuspended())) {
			getDebugTarget().fireDispatchEvent(new GetStackFramesRequest(getThread()));
			return new IStackFrame[0];
		}

		return fStackFrames.toArray(new IStackFrame[fStackFrames.size()]);
	}

	@Override
	public boolean hasStackFrames() {
		return getStackFrames().length > 0;
	}

	@Override
	public synchronized EaseDebugStackFrame getTopStackFrame() {
		if (hasStackFrames())
			return fStackFrames.get(0);

		return null;
	}

	@Override
	public int getPriority() throws DebugException {
		return 0;
	}

	@Override
	public IBreakpoint[] getBreakpoints() {
		return new IBreakpoint[0];
	}

	public Object getThread() {
		return fThread;
	}

	public synchronized void setStackFrames(final List<IScriptDebugFrame> debugFrames) {
		// update stack frames
		final List<EaseDebugStackFrame> oldStackFrames = new ArrayList<>(fStackFrames);
		fStackFrames.clear();

		for (final IScriptDebugFrame debugFrame : debugFrames) {
			// find existing StackFrame
			EaseDebugStackFrame stackFrame = null;
			for (final EaseDebugStackFrame oldStackFrame : oldStackFrames) {
				if (debugFrame.equals(oldStackFrame.getDebugFrame())) {
					stackFrame = oldStackFrame;
					fStackFrames.add(stackFrame);
					oldStackFrame.setDirty();

					break;
				}
			}

			if (stackFrame == null) {
				stackFrame = new EaseDebugStackFrame(this, debugFrame);
				fStackFrames.add(stackFrame);
				stackFrame.fireCreationEvent();

			} else
				oldStackFrames.remove(stackFrame);
		}

		for (final EaseDebugStackFrame stackFrame : oldStackFrames)
			stackFrame.fireTerminateEvent();

		fireChangeEvent(DebugEvent.CONTENT);
	}

	@Override
	public String toString() {
		return getName();
	}

	// ************************************************************
	// IEventProcessor
	// ************************************************************
	@Override
	public synchronized void handleEvent(final IDebugEvent event) {
		if (event instanceof SuspendedEvent) {
			setStackFrames(filterFrames(((SuspendedEvent) event).getDebugFrames()));

			setSuspended();

		} else if (event instanceof ResumedEvent) {
			setResumed(((ResumedEvent) event).getType());

		} else if (event instanceof StackFramesEvent) {
			setStackFrames(filterFrames(((StackFramesEvent) event).getDebugFrames()));

			// stack frames changed
			fireChangeEvent(DebugEvent.CONTENT);

		} else if (event instanceof ThreadTerminatedEvent) {
			setStackFrames(new ScriptStackTrace());
			setState(State.TERMINATED);

			fireChangeEvent(DebugEvent.CONTENT);
		}
	}

	@Override
	public void setDispatcher(EventDispatchJob dispatcher) {
		// noting to do
	}

	/**
	 * Remove dynamic code fragments in case they are disabled by the debug target.
	 *
	 * @param frames
	 *            frames to be filtered
	 * @return filtered frames
	 */
	private List<IScriptDebugFrame> filterFrames(final List<IScriptDebugFrame> frames) {
		if (getDebugTarget().isShowDynamicCode())
			return frames;

		return frames.stream().filter(frame -> (frame.getScript() != null) && (!frame.getScript().isDynamic())).collect(Collectors.toList());
	}

	// ************************************************************
	// ITerminate
	// ************************************************************

	@Override
	public boolean canTerminate() {
		if (getState() != State.TERMINATED) {
			if (getDebugTarget().getProcess().getThreads().length == 1) {
				return getDebugTarget().getProcess().canTerminate();
			}
		}

		return false;
	}

	@Override
	public synchronized void terminate() {
		getDebugTarget().getProcess().terminate();
	}

	@Override
	public boolean isTerminated() {
		return getState() == State.TERMINATED;
	}

	// ************************************************************
	// ISuspendResume
	// ************************************************************

	@Override
	public boolean canSuspend() {
		return (!isTerminated()) && (!isSuspended());
	}

	@Override
	public boolean canResume() {
		return (!isTerminated()) && (isSuspended());
	}

	@Override
	public boolean isSuspended() {
		return State.SUSPENDED == getState();
	}

	@Override
	public void resume() {
		getDebugTarget().fireDispatchEvent(new ResumeRequest(DebugEvent.CLIENT_REQUEST, getThread()));

	}

	@Override
	public void suspend() {
		getDebugTarget().fireDispatchEvent(new SuspendRequest(getThread()));
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
	public boolean isStepping() {
		return State.STEPPING == getState();
	}

	@Override
	public void stepInto() {
		getDebugTarget().fireDispatchEvent(new ResumeRequest(DebugEvent.STEP_INTO, getThread()));
	}

	@Override
	public void stepOver() {
		getDebugTarget().fireDispatchEvent(new ResumeRequest(DebugEvent.STEP_OVER, getThread()));
	}

	@Override
	public void stepReturn() {
		getDebugTarget().fireDispatchEvent(new ResumeRequest(DebugEvent.STEP_RETURN, getThread()));
	}
}
