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

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.DebugElement;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.core.model.IStep;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.core.model.ITerminate;

public abstract class EaseDebugElement extends DebugElement implements ITerminate, ISuspendResume, IDisconnect, IStep {

	public enum State {
		NOT_STARTED, SUSPENDED, RESUMED, STEPPING, TERMINATED, DISCONNECTED
	}

	private State fState = State.NOT_STARTED;

	public EaseDebugElement(final EaseDebugTarget target) {
		super(target);
	}

	@Override
	public EaseDebugTarget getDebugTarget() {
		return (EaseDebugTarget) super.getDebugTarget();
	}

	@Override
	public String getModelIdentifier() {
		return getDebugTarget().getModelIdentifier();
	}

	protected State getState() {
		return fState;
	}

	protected void setState(State state) {
		fState = state;
	}

	// ************************************************************
	// ITerminate
	// ************************************************************

	@Override
	public boolean canTerminate() {
		if (getDebugTarget().getProcess() != null)
			return getDebugTarget().getProcess().canTerminate();

		return false;
	}

	@Override
	public synchronized void terminate() {
		if (getDebugTarget().getProcess() != null)
			getDebugTarget().getProcess().terminate();
	}

	@Override
	public boolean isTerminated() {
		if (getDebugTarget().getProcess() != null)
			return getDebugTarget().getProcess().isTerminated();

		return false;
	}

	// ************************************************************
	// IDisconnect
	// ************************************************************

	@Override
	public boolean canDisconnect() {
		if (getDebugTarget().getProcess() != null)
			return getDebugTarget().getProcess().canDisconnect();
		return false;
	}

	@Override
	public void disconnect() {
		if (getDebugTarget().getProcess() != null)
			getDebugTarget().getProcess().disconnect();
	}

	@Override
	public boolean isDisconnected() {
		if (getDebugTarget().getProcess() != null)
			return getDebugTarget().getProcess().isDisconnected();
		return true;
	}

	// ************************************************************
	// ISuspendResume
	// ************************************************************

	@Override
	public boolean canResume() {
		final EaseDebugThread[] threads = getDebugTarget().getThreads();
		if (threads.length == 1)
			return threads[0].canResume();

		return false;
	}

	@Override
	public boolean canSuspend() {
		final EaseDebugThread[] threads = getDebugTarget().getThreads();
		if (threads.length == 1)
			return threads[0].canSuspend();

		return false;
	}

	@Override
	public boolean isSuspended() {
		final EaseDebugThread[] threads = getDebugTarget().getThreads();
		if (threads.length == 1)
			return threads[0].isSuspended();

		return State.SUSPENDED == getState();
	}

	@Override
	public void resume() {
		final EaseDebugThread[] threads = getDebugTarget().getThreads();
		if (threads.length == 1)
			threads[0].resume();
	}

	@Override
	public void suspend() {
		final EaseDebugThread[] threads = getDebugTarget().getThreads();
		if (threads.length == 1)
			threads[0].suspend();
	}

	public void setSuspended() {
		fState = State.SUSPENDED;
		fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
	}

	public void setResumed(int type) {
		fState = State.RESUMED;
		fireResumeEvent(type);
	}

	// ************************************************************
	// IStep
	// ************************************************************

	@Override
	public boolean canStepInto() {
		final EaseDebugThread[] threads = getDebugTarget().getThreads();
		if (threads.length == 1)
			return threads[0].canStepInto();

		return false;
	}

	@Override
	public boolean canStepOver() {
		final EaseDebugThread[] threads = getDebugTarget().getThreads();
		if (threads.length == 1)
			return threads[0].canStepOver();

		return false;
	}

	@Override
	public boolean canStepReturn() {
		final EaseDebugThread[] threads = getDebugTarget().getThreads();
		if (threads.length == 1)
			return threads[0].canStepReturn();

		return false;
	}

	@Override
	public boolean isStepping() {
		final EaseDebugThread[] threads = getDebugTarget().getThreads();
		if (threads.length == 1)
			return threads[0].isStepping();

		return false;
	}

	@Override
	public synchronized void stepInto() {
		final EaseDebugThread[] threads = getDebugTarget().getThreads();
		if (threads.length == 1)
			threads[0].stepInto();
	}

	@Override
	public synchronized void stepOver() {
		final EaseDebugThread[] threads = getDebugTarget().getThreads();
		if (threads.length == 1)
			threads[0].stepOver();
	}

	@Override
	public synchronized void stepReturn() {
		final EaseDebugThread[] threads = getDebugTarget().getThreads();
		if (threads.length == 1)
			threads[0].stepReturn();
	}
}
