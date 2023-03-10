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
import org.eclipse.ease.ui.view.ScriptShell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

public class SpawnShell extends AbstractHandler implements IHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		IWorkbenchPage page = HandlerUtil.getActivePart(event).getSite().getPage();

		// create dynamic secondary ID
		int maxID = 0;
		for (IViewReference reference : page.getViewReferences()) {
			if (ScriptShell.VIEW_ID.equals(reference.getId())) {
				try {
					int secondaryID = Integer.parseInt(reference.getSecondaryId());
					maxID = Math.max(maxID, secondaryID);
				} catch (NumberFormatException e) {
					// ignore
				}
			}
		}

		// open view
		try {
			IViewPart view = page.showView(ScriptShell.VIEW_ID, Integer.toString(maxID + 1), IWorkbenchPage.VIEW_ACTIVATE);
			if (view instanceof ScriptShell) {
				// TODO set new engine
			}

		} catch (PartInitException e) {
			// TODO handle this exception (but for now, at least know it
			// happened)
			throw new RuntimeException(e);
		}

		return null;
	}
}
