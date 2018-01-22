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

package org.eclipse.ease.lang.unittest.ui.editor;

import org.eclipse.emf.edit.ui.action.EditingDomainActionBarContributor;
import org.eclipse.emf.edit.ui.action.RedoAction;
import org.eclipse.emf.edit.ui.action.UndoAction;
import org.eclipse.ui.actions.TextActionHandler;
import org.eclipse.ui.forms.editor.IFormPage;

public class TestSuiteEditorActionBarContributor extends EditingDomainActionBarContributor {

	@Override
	protected UndoAction createUndoAction() {
		return new UndoAction() {
			@Override
			public void run() {
				super.run();

				updateActivePage();
			}

		};
	}

	@Override
	protected RedoAction createRedoAction() {
		return new RedoAction() {
			@Override
			public void run() {
				super.run();

				updateActivePage();
			}
		};
	}

	@Override
	public TestSuiteEditor getActiveEditor() {
		return (TestSuiteEditor) super.getActiveEditor();
	}

	private void updateActivePage() {
		final IFormPage activePageInstance = getActiveEditor().getActivePageInstance();
		if (activePageInstance instanceof AbstractEditorPage)
			((AbstractEditorPage) activePageInstance).populateContent();
	}

	@Override
	public void activate() {
		super.activate();

		getActiveEditor().getEditingDomain().getCommandStack().addCommandStackListener(event -> update());

		// add support for 'classic' cut/copy/paste handlers
		new TextActionHandler(getActionBars());
	}
}
