/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License_Identifier: EPL-2.0
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.debugging.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.ease.Activator;
import org.eclipse.ease.Script;
import org.eclipse.ease.debugging.DebugTracer;
import org.eclipse.ease.debugging.IScriptRegistry;
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
import org.eclipse.ease.debugging.events.debugger.ThreadCreatedEvent;
import org.eclipse.ease.debugging.events.debugger.ThreadTerminatedEvent;
import org.eclipse.ease.debugging.events.debugger.VariablesEvent;
import org.eclipse.ease.debugging.events.model.BreakpointRequest;
import org.eclipse.ease.debugging.events.model.BreakpointRequest.Mode;
import org.eclipse.ease.debugging.events.model.IModelRequest;

public abstract class EaseDebugTarget extends EaseDebugElement implements IDebugTarget, IEventProcessor {

	private EventDispatchJob fDispatcher;

	private IScriptRegistry fScriptRegistry;

	private EaseDebugProcess fProcess = null;

	private final ILaunch fLaunch;

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

	public boolean isSuspendOnStartup() {
		return fSuspendOnStartup;
	}

	public boolean isSuspendOnScriptLoad() {
		return fSuspendOnScriptLoad;
	}

	public boolean isShowDynamicCode() {
		return fShowDynamicCode;
	}

	@Override
	public String getName() {
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
	public EaseDebugProcess getProcess() {
		return fProcess;
	}

	@Override
	public EaseDebugThread[] getThreads() {
		if (getProcess() != null)
			return getProcess().getThreads();

		return new EaseDebugThread[0];
	}

	@Override
	public boolean hasThreads() {
		return getThreads().length > 0;
	}

	public void fireDispatchEvent(final IModelRequest event) {
		if (fDispatcher != null)
			fDispatcher.addEvent(event);
	}

	@Override
	public String toString() {
		return getName();
	}

	// ************************************************************
	// IEventProcessor
	// ************************************************************

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

	@Override
	public synchronized void handleEvent(final IDebugEvent event) {
		if (fDispatcher != null) {

			DebugTracer.debug("Model", "process " + event);

			if (event instanceof EngineStartedEvent) {
				fProcess = new EaseDebugProcess(this);

				fProcess.fireCreationEvent();

			} else if (event instanceof ScriptReadyEvent) {
				fProcess.handleEvent(event);

			} else if (event instanceof SuspendedEvent) {
				EaseDebugThread debugThread = getProcess().findDebugThread(((SuspendedEvent) event).getThread());
				if (debugThread == null) {
					// new debug thread detected
					debugThread = getProcess().createDebugThread(((SuspendedEvent) event).getThread());
				}

				debugThread.handleEvent(event);

			} else if (event instanceof ResumedEvent) {
				final EaseDebugThread debugThread = getProcess().findDebugThread(((ResumedEvent) event).getThread());
				if (debugThread != null)
					debugThread.handleEvent(event);

			} else if (event instanceof StackFramesEvent) {
				final EaseDebugThread debugThread = getProcess().findDebugThread(((StackFramesEvent) event).getThread());
				if (debugThread != null)
					debugThread.handleEvent(event);

			} else if (event instanceof VariablesEvent) {
				// variables refresh
				final EaseDebugStackFrame requestor = ((VariablesEvent) event).getRequestor();
				requestor.setVariables(((VariablesEvent) event).getVariables());

			} else if (event instanceof EvaluateExpressionEvent) {
				if (getThreads().length > 0) {
					((EvaluateExpressionEvent) event).getListener().watchEvaluationFinished(((EvaluateExpressionEvent) event).getWatchExpressionResult(this));
				}

			} else if (event instanceof ThreadCreatedEvent) {
				getProcess().handleEvent(event);

			} else if (event instanceof ThreadTerminatedEvent) {
				getProcess().handleEvent(event);

			} else if (event instanceof EngineTerminatedEvent) {
				cleanupOnTermination();
			}
		}
	}

	public void cleanupOnTermination() {
		// allow for garbage collection
		fDispatcher = null;

		// unsubscribe from breakpoint changes
		final DebugPlugin debugPlugin = DebugPlugin.getDefault();
		if (debugPlugin != null)
			debugPlugin.getBreakpointManager().removeBreakpointListener(this);

		getProcess().setTerminated();

		for (final EaseDebugThread thread : getThreads()) {
			thread.setStackFrames(Collections.emptyList());
			thread.setState(State.TERMINATED);
		}

		fireTerminateEvent();
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

		// Try to perform lookup via script registry (Thread independent)
		if (fScriptRegistry != null) {
			final Script script = fScriptRegistry.getScript(affectedResource);
			if (script != null) {
				fireDispatchEvent(new BreakpointRequest(script, breakpoint, mode));
				return;
			}
		}
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
		throw new DebugException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "getMemoryBlock() not supported by " + getName()));
	}
}
