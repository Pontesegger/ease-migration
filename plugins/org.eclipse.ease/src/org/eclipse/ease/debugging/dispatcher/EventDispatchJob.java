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
package org.eclipse.ease.debugging.dispatcher;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ease.Activator;
import org.eclipse.ease.Logger;
import org.eclipse.ease.Script;
import org.eclipse.ease.debugging.DebugTracer;
import org.eclipse.ease.debugging.IScriptRegistry;
import org.eclipse.ease.debugging.ScriptRegistry;
import org.eclipse.ease.debugging.events.IDebugEvent;
import org.eclipse.ease.debugging.events.debugger.IDebuggerEvent;
import org.eclipse.ease.debugging.events.model.IModelRequest;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;

public class EventDispatchJob extends Job implements IScriptRegistry {

	/** Cached events. */
	private final List<IDebugEvent> fEvents = new ArrayList<>();

	/** Debug host. */
	private final IEventProcessor fModel;

	/** Debug engine. */
	private final IEventProcessor fDebugger;

	/** Script registry for performing script lookups */
	private final IScriptRegistry fScriptRegistry;

	/**
	 * Creates a new dispatcher. The dispatcher automatically attaches to the host and the debugger. Further the job get automatically scheduled.
	 *
	 * @param host
	 *            debug model
	 * @param debugger
	 *            debugger implementation
	 */
	public EventDispatchJob(final IEventProcessor host, final IEventProcessor debugger) {
		this(host, debugger, new ScriptRegistry());
	}

	/**
	 * Protected constructor for externally setting {@link IScriptRegistry}.
	 *
	 * @param host
	 *            debug model
	 * @param debugger
	 *            debugger implementation
	 * @param scriptRegistry
	 *            IScriptRegistry to be used (must not be {@code null})
	 */
	protected EventDispatchJob(final IEventProcessor host, final IEventProcessor debugger, final IScriptRegistry scriptRegistry) {
		super(debugger + " event dispatcher");

		fModel = host;
		fDebugger = debugger;

		fModel.setDispatcher(this);
		fDebugger.setDispatcher(this);

		fScriptRegistry = scriptRegistry;

		setSystem(true);
	}

	/**
	 * Protected getter for the {@link #fScriptRegistry}.
	 */
	protected IScriptRegistry getScriptRegistry() {
		return fScriptRegistry;
	}

	public void addEvent(final IDebugEvent event) {
		DebugTracer.debug("Dispatcher", "[+] " + event);

		synchronized (fEvents) {
			fEvents.add(event);
			schedule();
		}
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {

		final List<IDebugEvent> events = new ArrayList<>();
		synchronized (fEvents) {
			events.addAll(fEvents);
			fEvents.clear();
		}

		for (final IDebugEvent event : events) {
			if (!monitor.isCanceled()) {
				try {
					handleEvent(event);
				} catch (final Throwable e) {
					// do not terminate the dispatcher in case of an error during event handling
					DebugTracer.debug("Dispatcher", "Error detected: " + e.getClass().getName() + ": " + e.getMessage());
					Logger.error(Activator.PLUGIN_ID, "The debug session encountered an error", e);

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

	@Override
	public Script getScript(IResource resource) {
		return fScriptRegistry.getScript(resource);
	}

	@Override
	public IResource getResource(Script script) {
		return fScriptRegistry.getResource(script);
	}

	@Override
	public void put(Script script) {
		fScriptRegistry.put(script);
	}
}
