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
package org.eclipse.ease.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ease.ui.view.ScriptShell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Toggle display of the dropins section.
 */
public class ToggleDropinsSection extends AbstractHandler implements IHandler {

	@Override
	public final Object execute(final ExecutionEvent event) throws ExecutionException {

		final IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part instanceof ScriptShell)
			((ScriptShell) part).toggleDropinsPane();

		return null;
	}
}
