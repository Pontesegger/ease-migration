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

import org.eclipse.debug.core.model.DebugElement;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.core.model.IStep;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.core.model.ITerminate;

public abstract class EaseDebugElement extends DebugElement implements ITerminate, ISuspendResume, IDisconnect, IStep {

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

	// ************************************************************
	// ITerminate
	// ************************************************************

	@Override
	public boolean canTerminate() {
		return !getDebugTarget().isTerminated();
	}

	@Override
	public boolean isTerminated() {
		return getDebugTarget().isTerminated();
	}

	@Override
	public void terminate() {
		getDebugTarget().terminate();
	}

	// ************************************************************
	// ISuspendResume
	// ************************************************************

	@Override
	public boolean canResume() {
		return getDebugTarget().canResume();
	}

	@Override
	public boolean canSuspend() {
		return getDebugTarget().canSuspend();
	}

	@Override
	public boolean isSuspended() {
		return getDebugTarget().isSuspended();
	}

	@Override
	public void resume() {
		getDebugTarget().resume();
	}

	@Override
	public void suspend() {
		getDebugTarget().suspend();
	}

	// ************************************************************
	// IDisconnect
	// ************************************************************

	@Override
	public boolean canDisconnect() {
		return getDebugTarget().canDisconnect();
	}

	@Override
	public void disconnect() {
		getDebugTarget().disconnect();
	}

	@Override
	public boolean isDisconnected() {
		return getDebugTarget().isDisconnected();
	}

	// ************************************************************
	// IStep
	// ************************************************************

	@Override
	public boolean canStepInto() {
		return getDebugTarget().canStepInto();
	}

	@Override
	public boolean canStepOver() {
		return getDebugTarget().canStepOver();
	}

	@Override
	public boolean canStepReturn() {
		return getDebugTarget().canStepReturn();
	}

	@Override
	public boolean isStepping() {
		return getDebugTarget().isStepping();
	}

	@Override
	public void stepInto() {
		getDebugTarget().stepInto();
	}

	@Override
	public void stepOver() {
		getDebugTarget().stepOver();
	}

	@Override
	public void stepReturn() {
		getDebugTarget().stepReturn();
	}
}
