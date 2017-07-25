/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.lang.unittest.ui.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.ease.lang.unittest.definition.ICode;
import org.eclipse.ease.lang.unittest.definition.IDefinitionFactory;
import org.eclipse.ease.lang.unittest.definition.IDefinitionPackage;
import org.eclipse.ease.lang.unittest.definition.ITestSuiteDefinition;
import org.eclipse.ease.lang.unittest.ui.Activator;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;

public class CustomCodePage extends AbstractEditorPage {

	private static String getDefaultText(final String location) {
		if (ITestSuiteDefinition.CODE_LOCATION_TESTSUITE_SETUP.equals(location))
			return "// Testsuite setup is run once before test files are run.\n" + "// To raise errors use the failure(\"Reason\") function.\n"
					+ "// Variables are already available in setup code.\n";

		if (ITestSuiteDefinition.CODE_LOCATION_TESTSUITE_TEARDOWN.equals(location))
			return "// Testsuite teardown is run once after all test files are finished.";

		if (ITestSuiteDefinition.CODE_LOCATION_TESTFILE_SETUP.equals(location))
			return "// Testfile setup is run at the beginning of a test file,\n" + "// right before the script code is executed.\n"
					+ "// Variables are available in setup code.\n";

		if (ITestSuiteDefinition.CODE_LOCATION_TESTFILE_TEARDOWN.equals(location))
			return "// Testfile teardown is run at the end of each test file.\n";

		return "";
	}

	/** Indicator when to track changes in the text field. */
	private boolean fEnableChangeTracker = true;

	// UI elements
	private Text fTxtCode;
	private ComboViewer fCmbCodeLocation;

	public CustomCodePage(final FormEditor editor, final String id, final String title) {
		super(editor, id, title);
	}

	@Override
	protected void createFormContent(final IManagedForm managedForm) {
		super.createFormContent(managedForm);
		managedForm.getForm().getBody().setLayout(new GridLayout(2, false));

		final Label lblCustomCodeUsed = new Label(managedForm.getForm().getBody(), SWT.NONE);
		lblCustomCodeUsed.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		managedForm.getToolkit().adapt(lblCustomCodeUsed, true, true);
		lblCustomCodeUsed
				.setText("Custom code used to setup test suites, sets and tests. To run custom code actions use executeUserCode(<location>); in your scripts.");

		final Label label = new Label(managedForm.getForm().getBody(), SWT.NONE);
		managedForm.getToolkit().adapt(label, true, true);
		new Label(managedForm.getForm().getBody(), SWT.NONE);

		final Label lblLocation = new Label(managedForm.getForm().getBody(), SWT.NONE);
		lblLocation.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		managedForm.getToolkit().adapt(lblLocation, true, true);
		lblLocation.setText("Location:");

		final CCombo combo_1 = new CCombo(managedForm.getForm().getBody(), SWT.READ_ONLY | SWT.FLAT);
		final GridData gd_combo_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_combo_1.widthHint = 300;
		combo_1.setLayoutData(gd_combo_1);
		combo_1.setEditable(true);
		combo_1.setVisibleItemCount(10);
		combo_1.setLayoutData(gd_combo_1);
		fCmbCodeLocation = new ComboViewer(combo_1);
		managedForm.getToolkit().paintBordersFor(combo_1);
		fCmbCodeLocation.setContentProvider(ArrayContentProvider.getInstance());
		fCmbCodeLocation.setInput(getLocations());

		fCmbCodeLocation.addSelectionChangedListener(event -> {
			final String location = event.getStructuredSelection().getFirstElement().toString();
			final ICode customCode = getTestSuitDefinition().getCustomCode(location);
			final String code = (customCode != null) ? customCode.getContent() : null;

			fEnableChangeTracker = false;
			fTxtCode.setText((code != null) ? code : getDefaultText(location));
			fEnableChangeTracker = true;
		});

		fTxtCode = new Text(managedForm.getForm().getBody(), SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		fTxtCode.addModifyListener(e -> {
			if (fEnableChangeTracker) {

				final Object element = fCmbCodeLocation.getStructuredSelection().getFirstElement();
				if (element != null) {
					final String location = element.toString();

					final ICode code = getTestSuitDefinition().getCustomCode(location);
					if (code != null) {
						// change existing code location
						final Command command = SetCommand.create(getEditingDomain(), code, IDefinitionPackage.Literals.CODE__CONTENT, fTxtCode.getText());
						command.execute();

					} else {
						// new code location
						final ICode customCode = IDefinitionFactory.eINSTANCE.createCode();
						customCode.setLocation(location);
						customCode.setContent(fTxtCode.getText());

						final Command command = AddCommand.create(getEditingDomain(), getTestSuitDefinition(),
								IDefinitionPackage.Literals.TEST_SUITE_DEFINITION__CUSTOM_CODE, customCode);
						command.execute();
					}
				}
			}
		});

		final GridData gd_txtCode = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gd_txtCode.verticalIndent = 10;
		fTxtCode.setLayoutData(gd_txtCode);
		managedForm.getToolkit().adapt(fTxtCode, true, true);
	}

	private Collection<String> getLocations() {
		final List<String> result = new ArrayList<>();

		result.add(ITestSuiteDefinition.CODE_LOCATION_TESTSUITE_SETUP);
		result.add(ITestSuiteDefinition.CODE_LOCATION_TESTFILE_SETUP);
		result.add(ITestSuiteDefinition.CODE_LOCATION_TESTFILE_TEARDOWN);
		result.add(ITestSuiteDefinition.CODE_LOCATION_TESTSUITE_TEARDOWN);

		for (final ICode code : getTestSuitDefinition().getCustomCode()) {
			if (!result.contains(code.getLocation()))
				result.add(code.getLocation());
		}

		return result;
	}

	@Override
	protected String getPageTitle() {
		return "Custom Code";
	}

	@Override
	protected void populateContent() {
		fCmbCodeLocation.setInput(getLocations());
	}

	@Override
	public Image getTitleImage() {
		return Activator.getImage(Activator.PLUGIN_ID, "/icons/eobj16/custom_code.png", true);
	}
}
