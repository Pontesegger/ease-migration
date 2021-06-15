/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
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
package org.eclipse.ease.lang.unittest.ui.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.ease.lang.unittest.definition.ITestSuiteDefinition;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.part.FileEditorInput;

public abstract class AbstractEditorPage extends FormPage {

	/**
	 * Create the form page.
	 *
	 * @param id
	 * @param title
	 */
	public AbstractEditorPage(final String id, final String title) {
		super(id, title);
	}

	/**
	 * Create the form page.
	 *
	 * @param editor
	 * @param id
	 * @param title
	 */
	public AbstractEditorPage(final FormEditor editor, final String id, final String title) {
		super(editor, id, title);
	}

	/**
	 * Create contents of the form.
	 *
	 * @param managedForm
	 * @return
	 */
	@Override
	protected void createFormContent(final IManagedForm managedForm) {
		final FormToolkit toolkit = managedForm.getToolkit();
		final ScrolledForm form = managedForm.getForm();
		final Composite body = form.getBody();

		toolkit.decorateFormHeading(form.getForm());
		toolkit.paintBordersFor(body);

		form.setText(getPageTitle());
		form.setImage(getTitleImage());

		// add menu items
		final ToolBarManager manager = (ToolBarManager) form.getToolBarManager();
		final IMenuService menuService = getSite().getService(IMenuService.class);
		menuService.populateContributionManager(manager, "toolbar:" + TestSuiteEditor.EDITOR_ID);
		manager.update(true);
	}

	abstract protected String getPageTitle();

	/**
	 * Populate page content with data from the testsuite.
	 */
	abstract protected void populateContent();

	protected ITestSuiteDefinition getTestSuiteDefinition() {
		return getEditor().getTestSuite();
	}

	@Override
	public TestSuiteEditor getEditor() {
		return (TestSuiteEditor) super.getEditor();
	}

	@Override
	public IEditorInput getEditorInput() {
		return getEditor().getEditorInput();
	}

	protected AdapterFactoryEditingDomain getEditingDomain() {
		return getEditor().getEditingDomain();
	}

	protected IFile getFile() {
		final IEditorInput input = getEditorInput();
		if (input instanceof FileEditorInput)
			return ((FileEditorInput) input).getFile();

		return null;
	}
}
