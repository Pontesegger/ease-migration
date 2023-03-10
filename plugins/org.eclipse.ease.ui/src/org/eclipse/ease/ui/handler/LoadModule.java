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
package org.eclipse.ease.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ease.IScriptEngineProvider;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Pulldown handler to load/ list modules.
 */
public class LoadModule extends AbstractHandler implements IHandler {

	public static final String COMMAND_ID = "org.eclipse.ease.commands.scriptShell.loadModule";
	public static final String PARAMETER_NAME = COMMAND_ID + ".moduleID";

	@Override
	public final Object execute(final ExecutionEvent event) throws ExecutionException {

		final IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part instanceof IScriptEngineProvider) {
			final String moduleID = event.getParameter(PARAMETER_NAME);

			if (moduleID != null)
				// specific module selected
				((IScriptEngineProvider) part).getScriptEngine().execute("loadModule('" + moduleID + "');");

			else
				// button was clicked, no module selected
				((IScriptEngineProvider) part).getScriptEngine().execute("listModules();");
		}

		return null;
	}
}
