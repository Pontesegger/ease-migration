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

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.ease.debugging.IScriptDebugFrame;
import org.eclipse.ease.debugging.events.model.GetStackFramesRequest;

public class EaseDebugThread extends EaseDebugElement implements IThread {

	private final Thread fThread;

	private final List<EaseDebugStackFrame> fStackFrames = new ArrayList<>();

	public EaseDebugThread(final EaseDebugTarget target, final Thread thread) {
		super(target);

		fThread = thread;
	}

	@Override
	public String getName() throws DebugException {
		return "Thread: " + fThread.getName();
	}

	@Override
	public synchronized IStackFrame[] getStackFrames() {
		if (fStackFrames.isEmpty()) {
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

	public Thread getThread() {
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
			}
		}

		fireChangeEvent(DebugEvent.CHANGE);
	}
}
