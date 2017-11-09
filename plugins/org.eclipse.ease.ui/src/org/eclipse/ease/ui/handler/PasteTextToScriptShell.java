/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ease.IScriptEngineProvider;
import org.eclipse.ease.ui.view.ScriptShell;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class PasteTextToScriptShell extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof ITextSelection) {
			final IViewReference viewReference = findViewReference(ScriptShell.VIEW_ID);
			if (viewReference != null) {
				final IViewPart view = viewReference.getView(true);
				if (view instanceof IScriptEngineProvider) {
					((IScriptEngineProvider) view).getScriptEngine().executeAsync(((ITextSelection) selection).getText());
				}
			}
		}

		return null;
	}

	public static IViewReference findViewReference(String idOrTitle) {
		for (final IWorkbenchWindow workbenchWindow : PlatformUI.getWorkbench().getWorkbenchWindows()) {
			for (final IWorkbenchPage page : workbenchWindow.getPages()) {
				for (final IViewReference viewReference : page.getViewReferences()) {
					if ((idOrTitle.equals(viewReference.getId())) || (idOrTitle.equals(viewReference.getTitle())))
						return viewReference;
				}
			}
		}

		return null;
	}

}
