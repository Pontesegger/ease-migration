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
package org.eclipse.ease.debugging.dispatcher;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.ease.Activator;
import org.eclipse.ease.debugging.DebugTracer;
import org.eclipse.ease.debugging.events.IDebugEvent;
import org.eclipse.ease.debugging.events.debugger.IDebuggerEvent;
import org.eclipse.ease.debugging.events.model.IModelRequest;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;

public class EventDispatchJob extends Job {

	/** Cached events. */
	private final List<IDebugEvent> fEvents = new ArrayList<>();

	/** Flag indicating this job shall be terminated. */
	private boolean fTerminated = false;

	/** Debug host. */
	private final IEventProcessor fModel;

	/** Debug engine. */
	private final IEventProcessor fDebugger;

	/**
	 * Creates a new dispatcher. The dispatcher automatically attaches to the host and the debugger. Further the job get automatically scheduled.
	 *
	 * @param host
	 *            debug model
	 * @param debugger
	 *            debugger implementation
	 */
	public EventDispatchJob(final IEventProcessor host, final IEventProcessor debugger) {
		super(debugger + " event dispatcher");

		fModel = host;
		fDebugger = debugger;

		fModel.setDispatcher(this);
		fDebugger.setDispatcher(this);

		setSystem(true);
		schedule();
	}

	public void addEvent(final IDebugEvent event) {
		synchronized (fEvents) {
			DebugTracer.debug("Dispatcher", "[+] " + event);

			fEvents.add(event);
			fEvents.notifyAll();
		}
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		while (!fTerminated) {
			IDebugEvent event = null;

			// wait for new events
			synchronized (fEvents) {
				if (fEvents.isEmpty()) {
					try {
						fEvents.wait();
					} catch (final InterruptedException e) {
					}
				}

				if (!fEvents.isEmpty())
					event = fEvents.remove(0);
			}

			final boolean platformRunning = DebugPlugin.getDefault() != null;
			if ((monitor.isCanceled()) || (!platformRunning))
				terminate();

			if (event != null) {
				try {
					handleEvent(event);
				} catch (final Throwable e) {
					// do not terminate the dispatcher in case of an error during event handling
					DebugTracer.debug("Dispatcher", "Error detected: " + e.getClass().getName() + ": " + e.getMessage());

					final Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
							"The debug session encountered an error. We try to gracefully continue the session.", e);

					Display.getDefault().asyncExec(() -> ErrorDialog.openError(Display.getDefault().getActiveShell(), "Debug session error",
							"The debug session encountered an error. We try to gracefully continue the session.", status));
				}
			}
		}

		return Status.OK_STATUS;
	}

	private void handleEvent(final IDebugEvent event) {

		// forward event handling to target
		if (event instanceof IDebuggerEvent) {
			DebugTracer.debug("Dispatcher", "debugger -> " + event + " -> model");
			fModel.handleEvent(event);

		} else if (event instanceof IModelRequest) {
			DebugTracer.debug("Dispatcher", "debugger <- " + event + " <- model");
			fDebugger.handleEvent(event);

		} else
			throw new RuntimeException("Unknown event detected: " + event);
	}

	public void terminate() {
		fTerminated = true;

		// wake up job
		synchronized (fEvents) {
			fEvents.notifyAll();
		}
	}
}
