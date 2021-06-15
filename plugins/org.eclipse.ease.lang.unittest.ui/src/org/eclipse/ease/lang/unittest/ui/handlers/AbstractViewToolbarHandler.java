/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
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

package org.eclipse.ease.lang.unittest.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Helper method to access a view even if the view does not have focus.
 */
public abstract class AbstractViewToolbarHandler extends AbstractHandler {

	@SuppressWarnings("unchecked")
	public static <T> T getView(ExecutionEvent event, Class<T> viewClazz) {
		final IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part.getClass().isAssignableFrom(viewClazz))
			return (T) part;

		for (final IViewReference viewReference : PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences()) {
			final IViewPart view = viewReference.getView(false);
			if ((view != null) && (view.getClass().isAssignableFrom(viewClazz)))
				return (T) view;
		}

		return null;
	}
}
