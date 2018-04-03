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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.ease.Script;
import org.eclipse.ease.debugging.dispatcher.EventDispatchJob;
import org.eclipse.ease.debugging.dispatcher.IEventProcessor;
import org.eclipse.ease.debugging.events.IDebugEvent;
import org.eclipse.ease.debugging.events.debugger.ScriptReadyEvent;
import org.eclipse.ease.debugging.events.debugger.ThreadCreatedEvent;
import org.eclipse.ease.debugging.events.debugger.ThreadTerminatedEvent;
import org.eclipse.ease.debugging.events.model.BreakpointRequest;
import org.eclipse.ease.debugging.events.model.BreakpointRequest.Mode;
import org.eclipse.ease.debugging.events.model.ResumeRequest;
import org.eclipse.ease.debugging.events.model.TerminateRequest;

public class EaseDebugProcess extends EaseDebugElement implements IProcess, IEventProcessor {

	private final Map<String, String> fAttributes = new HashMap<>();

	private final List<EaseDebugThread> fThreads = new ArrayList<>();

	public EaseDebugProcess(final EaseDebugTarget target) {
		super(target);
	}

	@Override
	public String getLabel() {
		return "virtual Script Process";
	}

	@Override
	public IStreamsProxy getStreamsProxy() {
		return null;
	}

	@Override
	public void setAttribute(final String key, final String value) {
		fAttributes.put(key, value);
	}

	@Override
	public String getAttribute(final String key) {
		return fAttributes.get(key);
	}

	@Override
	public int getExitValue() throws DebugException {
		return 0;
	}

	@Override
	public String toString() {
		return getLabel();
	}

	// ************************************************************
	// IEventProcessor
	// ************************************************************
	@Override
	public synchronized void handleEvent(final IDebugEvent event) {
		if (event instanceof ScriptReadyEvent) {

			// find existing DebugThread
			EaseDebugThread debugThread = findDebugThread(((ScriptReadyEvent) event).getThread());

			if (debugThread == null) {
				// thread does not exist, create new one
				debugThread = createDebugThread(((ScriptReadyEvent) event).getThread());

			} else {
				// stack frames changed
				debugThread.fireChangeEvent(DebugEvent.CONTENT);
			}

			// set deferred breakpoints
			setDeferredBreakpoints(((ScriptReadyEvent) event).getScript());

			if (getDebugTarget().isSuspendOnScriptLoad())
				debugThread.stepInto();

			else if ((((ScriptReadyEvent) event).isRoot()) && (getDebugTarget().isSuspendOnStartup()))
				debugThread.stepInto();

			// default resume request
			debugThread.resume();

		} else if (event instanceof ThreadCreatedEvent) {
			EaseDebugThread debugThread = findDebugThread(((ThreadCreatedEvent) event).getThread());

			if (debugThread == null) {
				// thread does not exist, create new one
				debugThread = createDebugThread(((ThreadCreatedEvent) event).getThread());
			}

		} else if (event instanceof ThreadTerminatedEvent) {
			final EaseDebugThread debugThread = findDebugThread(((ThreadTerminatedEvent) event).getThread());

			if (debugThread != null)
				debugThread.handleEvent(event);
		}
	}

	public EaseDebugThread createDebugThread(Object thread) {
		final EaseDebugThread debugThread = new EaseDebugThread(getDebugTarget(), thread);
		fThreads.add(debugThread);
		debugThread.fireCreationEvent();

		return debugThread;
	}

	@Override
	public void setDispatcher(EventDispatchJob dispatcher) {
		// noting to do
	}

	public EaseDebugThread findDebugThread(final Object thread) {
		for (final EaseDebugThread debugThread : fThreads) {
			if (thread.equals(debugThread.getThread()))
				return debugThread;
		}

		return null;
	}

	private void setDeferredBreakpoints(final Script script) {

		final Object file = script.getFile();
		if (file instanceof IResource) {
			final IBreakpoint[] breakpoints = getDebugTarget().getBreakpoints(script);

			for (final IBreakpoint breakpoint : breakpoints) {
				if (file.equals(breakpoint.getMarker().getResource()))
					getDebugTarget().fireDispatchEvent(new BreakpointRequest(script, breakpoint, BreakpointRequest.Mode.ADD));
			}
		}
	}

	public EaseDebugThread[] getThreads() {
		return fThreads.toArray(new EaseDebugThread[fThreads.size()]);
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
		getDebugTarget().fireDispatchEvent(new TerminateRequest());
	}

	@Override
	public boolean isTerminated() {
		return State.TERMINATED == getState();
	}

	public void setTerminated() {
		setState(State.TERMINATED);
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
		getDebugTarget().fireDispatchEvent(new BreakpointRequest(Mode.REMOVE));

		// resume interpreter
		for (final EaseDebugThread thread : getThreads()) {
			if (thread.isSuspended())
				getDebugTarget().fireDispatchEvent(new ResumeRequest(DebugEvent.CLIENT_REQUEST, thread.getThread()));
		}

		// cleanup removes dispatcher instance. Needs to be called after all events have been sent
		getDebugTarget().cleanupOnTermination();
	}

	@Override
	public boolean isDisconnected() {
		return isTerminated();
	}
}
