/*******************************************************************************
 * Copyright (c) 2018 Martin Kloesch and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Martin Kloesch - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.python.debugger;

import org.eclipse.ease.Script;
import org.eclipse.ease.debugging.IScriptRegistry;
import org.eclipse.ease.debugging.dispatcher.EventDispatchJob;
import org.eclipse.ease.debugging.dispatcher.IEventProcessor;

/**
 * Extension of {@link EventDispatchJob} using {@link IPythonScriptRegistry} rather than {@link IScriptRegistry}.
 */
public class PythonEventDispatchJob extends EventDispatchJob implements IPythonScriptRegistry {
	/**
	 * {@link IPythonScriptRegistry} to be used instead of parent's {@link IScriptRegistry}.
	 */
	private final IPythonScriptRegistry fScriptRegistry;

	/**
	 * Constructor creating new {@link PythonScriptRegistry} for overload.
	 *
	 * @see EventDispatchJob#EventDispatchJob(IEventProcessor, IEventProcessor, IScriptRegistry)
	 */
	public PythonEventDispatchJob(IEventProcessor host, IEventProcessor debugger) {
		super(host, debugger, new PythonScriptRegistry());
		fScriptRegistry = (IPythonScriptRegistry) getScriptRegistry();
	}

	@Override
	public Script getScript(String reference) {
		return fScriptRegistry.getScript(reference);
	}

	@Override
	public String getReference(Script script) {
		return fScriptRegistry.getReference(script);
	}

}
