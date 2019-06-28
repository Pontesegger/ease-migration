/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.unittest.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class HTMLContentDialog extends Dialog {
	private Text fTxtInput;
	private String fContent;

	public HTMLContentDialog(Shell parentShell, String initialContent) {
		super(parentShell);

		fContent = (initialContent != null) ? initialContent : "";
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new FillLayout(SWT.HORIZONTAL));

		final SashForm sashForm = new SashForm(container, SWT.VERTICAL);

		fTxtInput = new Text(sashForm, SWT.BORDER | SWT.WRAP | SWT.MULTI);

		final Browser fBrowserPreview = new Browser(sashForm, SWT.NONE);
		sashForm.setWeights(new int[] { 1, 1 });

		fTxtInput.setText(fContent);
		fBrowserPreview.setText(fContent);

		fTxtInput.addModifyListener(e -> fBrowserPreview.setText(fTxtInput.getText()));

		return container;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(600, 500);
	}

	@Override
	protected void okPressed() {
		fContent = fTxtInput.getText();

		super.okPressed();
	}

	public String getContent() {
		return fContent;
	}
}
