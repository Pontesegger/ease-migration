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

package org.eclipse.ease.lang.unittest.ui.editor;

import java.util.Map.Entry;

import org.eclipse.ease.lang.unittest.UnitTestHelper;
import org.eclipse.ease.lang.unittest.definition.Flag;
import org.eclipse.ease.lang.unittest.definition.IDefinitionFactory;
import org.eclipse.ease.lang.unittest.definition.IDefinitionPackage;
import org.eclipse.ease.lang.unittest.definition.impl.FlagToStringMap;
import org.eclipse.ease.lang.unittest.runtime.ITestContainer;
import org.eclipse.ease.lang.unittest.runtime.ITestFile;
import org.eclipse.ease.lang.unittest.runtime.ITestSuite;
import org.eclipse.ease.lang.unittest.ui.Activator;
import org.eclipse.ease.lang.unittest.ui.dialogs.HTMLContentDialog;
import org.eclipse.ease.lang.unittest.ui.views.SuiteRuntimeInformation;
import org.eclipse.ease.lang.unittest.ui.views.UnitTestView;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

public class OverviewPage extends AbstractEditorPage {
	private Text fTxtThreads;
	private Browser fBrowser;
	private Button fChkPromoteFailuresToErrors;
	private Button fChkStopSuiteOnError;
	private Button fChkRunTeardownOnError;
	private ComboViewer fEngineComboViewer;
	private Label fLblTestFilesCount;
	private Label fLblDefinedVariablesCount;
	private Label fLblDisabledFilesCount;
	private Label fLblExpectedRuntime;
	private Label fLblUsesSetupTeardown;
	private Hyperlink fHprlnkSetupTeardownCode;

	public OverviewPage(FormEditor editor, String id, String title) {
		super(editor, id, title);
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		super.createFormContent(managedForm);

		final FormToolkit toolkit = managedForm.getToolkit();
		final ScrolledForm form = managedForm.getForm();
		final Composite body = form.getBody();
		toolkit.decorateFormHeading(form.getForm());
		toolkit.paintBordersFor(body);
		managedForm.getForm().getBody().setLayout(new GridLayout(2, true));

		final Section sctnDescription = managedForm.getToolkit().createSection(managedForm.getForm().getBody(),
				ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR);
		sctnDescription.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		managedForm.getToolkit().paintBordersFor(sctnDescription);
		sctnDescription.setText("Description");
		sctnDescription.setExpanded(true);

		final Composite composite = new Composite(sctnDescription, SWT.NONE);
		managedForm.getToolkit().adapt(composite);
		managedForm.getToolkit().paintBordersFor(composite);
		sctnDescription.setClient(composite);
		composite.setLayout(new GridLayout(1, false));

		fBrowser = new Browser(composite, SWT.NONE);
		fBrowser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		managedForm.getToolkit().adapt(fBrowser);
		managedForm.getToolkit().paintBordersFor(fBrowser);

		final Hyperlink hprlnkEdit = managedForm.getToolkit().createHyperlink(composite, "edit", SWT.NONE);
		hprlnkEdit.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		hprlnkEdit.addHyperlinkListener(new HyperlinkAdapter() {

			@Override
			public void linkActivated(HyperlinkEvent e) {
				final HTMLContentDialog htmlContentDialog = new HTMLContentDialog(getEditorSite().getShell(), getTestSuiteDefinition().getDescription());
				if (Window.OK == htmlContentDialog.open()) {
					updateDescription(htmlContentDialog.getContent());
					fBrowser.setText(htmlContentDialog.getContent());
				}
			}
		});
		managedForm.getToolkit().paintBordersFor(hprlnkEdit);

		final Section sctnSummary = managedForm.getToolkit().createSection(managedForm.getForm().getBody(),
				ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR);
		sctnSummary.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		managedForm.getToolkit().paintBordersFor(sctnSummary);
		sctnSummary.setText("Summary");
		sctnSummary.setExpanded(true);

		final Composite composite_1 = managedForm.getToolkit().createComposite(sctnSummary, SWT.NONE);
		managedForm.getToolkit().paintBordersFor(composite_1);
		sctnSummary.setClient(composite_1);
		composite_1.setLayout(new GridLayout(3, false));

		final Label lblNewLabel = managedForm.getToolkit().createLabel(composite_1, "Test files", SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));

		fLblTestFilesCount = managedForm.getToolkit().createLabel(composite_1, "0", SWT.NONE);
		fLblTestFilesCount.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		final Label lblDisabledFiles = managedForm.getToolkit().createLabel(composite_1, "Disabled files", SWT.NONE);
		lblDisabledFiles.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

		fLblDisabledFilesCount = managedForm.getToolkit().createLabel(composite_1, "0", SWT.NONE);
		fLblDisabledFilesCount.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		final Label lblExpectedExecutionTime = managedForm.getToolkit().createLabel(composite_1, "Expected execution time", SWT.NONE);
		lblExpectedExecutionTime.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

		fLblExpectedRuntime = managedForm.getToolkit().createLabel(composite_1, "unknown", SWT.NONE);
		fLblExpectedRuntime.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		final Hyperlink hprlnkDefinedVariables = managedForm.getToolkit().createHyperlink(composite_1, "Defined variables", SWT.NONE);
		hprlnkDefinedVariables.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		managedForm.getToolkit().paintBordersFor(hprlnkDefinedVariables);
		hprlnkDefinedVariables.addHyperlinkListener(new HyperlinkAdapter() {

			@Override
			public void linkActivated(HyperlinkEvent e) {
				getEditor().setActivePage(TestSuiteEditor.VARIABLES_PAGE);
			}
		});

		fLblDefinedVariablesCount = managedForm.getToolkit().createLabel(composite_1, "0", SWT.NONE);
		fLblDefinedVariablesCount.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		fLblUsesSetupTeardown = managedForm.getToolkit().createLabel(composite_1, "Uses", SWT.NONE);
		fLblUsesSetupTeardown.setVisible(false);

		fHprlnkSetupTeardownCode = managedForm.getToolkit().createHyperlink(composite_1, "Setup/Teardown code", SWT.NONE);
		managedForm.getToolkit().paintBordersFor(fHprlnkSetupTeardownCode);
		fHprlnkSetupTeardownCode.addHyperlinkListener(new HyperlinkAdapter() {

			@Override
			public void linkActivated(HyperlinkEvent e) {
				getEditor().setActivePage(TestSuiteEditor.CUSTOM_CODE_PAGE);
			}
		});
		fHprlnkSetupTeardownCode.setVisible(false);

		new Label(composite_1, SWT.NONE);

		final Section sctnSettings = managedForm.getToolkit().createSection(managedForm.getForm().getBody(),
				ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR);
		sctnSettings.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		managedForm.getToolkit().paintBordersFor(sctnSettings);
		sctnSettings.setText("Settings");
		sctnSettings.setExpanded(true);

		final Composite composite_2 = managedForm.getToolkit().createComposite(sctnSettings, SWT.NONE);
		managedForm.getToolkit().paintBordersFor(composite_2);
		sctnSettings.setClient(composite_2);
		composite_2.setLayout(new GridLayout(2, false));

		final Label lblThreads = new Label(composite_2, SWT.NONE);
		lblThreads.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		managedForm.getToolkit().adapt(lblThreads, true, true);
		lblThreads.setText("Threads:");

		fTxtThreads = managedForm.getToolkit().createText(composite_2, "New Text", SWT.NONE);
		fTxtThreads.setText("1");
		fTxtThreads.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		fTxtThreads.addVerifyListener(e -> e.doit = ((e.character >= '0') && (e.character <= '9')) || (e.character == SWT.DEL) || (e.character == SWT.BS));

		final Label lblEngine = managedForm.getToolkit().createLabel(composite_2, "Engine:", SWT.NONE);
		lblEngine.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		final CCombo combo_1 = new CCombo(composite_2, SWT.BORDER);
		combo_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		managedForm.getToolkit().adapt(combo_1);
		managedForm.getToolkit().paintBordersFor(combo_1);
		fEngineComboViewer = new ComboViewer(combo_1);
		fEngineComboViewer.setContentProvider(ArrayContentProvider.getInstance());
		fEngineComboViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof EngineDescription)
					return ((EngineDescription) element).getName();

				return super.getText(element);
			};
		});

		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);
		fEngineComboViewer.setInput(scriptService.getEngines());

		final Label label = new Label(composite_2, SWT.SEPARATOR | SWT.HORIZONTAL);
		final GridData gd_label = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_label.verticalIndent = 10;
		label.setLayoutData(gd_label);
		managedForm.getToolkit().adapt(label, true, true);

		fChkPromoteFailuresToErrors = new Button(composite_2, SWT.CHECK);
		fChkPromoteFailuresToErrors.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		managedForm.getToolkit().adapt(fChkPromoteFailuresToErrors, true, true);
		fChkPromoteFailuresToErrors.setText("Promote Failures to Errors");

		fChkStopSuiteOnError = new Button(composite_2, SWT.CHECK);
		fChkStopSuiteOnError.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		managedForm.getToolkit().adapt(fChkStopSuiteOnError, true, true);
		fChkStopSuiteOnError.setText("Stop suite on error");

		fChkRunTeardownOnError = new Button(composite_2, SWT.CHECK);
		fChkRunTeardownOnError.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		managedForm.getToolkit().adapt(fChkRunTeardownOnError, true, true);
		fChkRunTeardownOnError.setText("Run teardown on error");

		populateContent();

		// add change listeners
		fTxtThreads.addModifyListener(e -> updateFlag(Flag.THREAD_COUNT, fTxtThreads.getText()));

		fEngineComboViewer.addSelectionChangedListener(event -> {
			final Object element = fEngineComboViewer.getStructuredSelection().getFirstElement();
			if (element instanceof EngineDescription)
				updateFlag(Flag.PREFERRED_ENGINE_ID, ((EngineDescription) element).getID());
			else
				updateFlag(Flag.PREFERRED_ENGINE_ID, "");
		});

		fChkPromoteFailuresToErrors.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateFlag(Flag.PROMOTE_FAILURE_TO_ERROR, Boolean.toString(fChkPromoteFailuresToErrors.getSelection()));
			}
		});

		fChkStopSuiteOnError.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateFlag(Flag.STOP_SUITE_ON_ERROR, Boolean.toString(fChkStopSuiteOnError.getSelection()));
			}
		});

		fChkRunTeardownOnError.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateFlag(Flag.RUN_TEARDOWN_ON_ERROR, Boolean.toString(fChkRunTeardownOnError.getSelection()));
			}
		});
	}

	private void updateFlag(Flag flag, String value) {
		final CompoundCommand compoundCommand = new CompoundCommand();

		if (getTestSuiteDefinition().getFlags().containsKey(flag)) {
			// already there, modify
			final Entry<Flag, String> entry = getTestSuiteDefinition().getFlags().get(getTestSuiteDefinition().getFlags().indexOfKey(flag));

			final Command removeCommand = RemoveCommand.create(getEditingDomain(), getTestSuiteDefinition(),
					IDefinitionPackage.Literals.TEST_SUITE_DEFINITION__FLAGS, entry);
			compoundCommand.append(removeCommand);
		}

		final FlagToStringMap newentry = (FlagToStringMap) IDefinitionFactory.eINSTANCE.create(IDefinitionPackage.Literals.FLAG_TO_STRING_MAP);
		newentry.setKey(flag);
		newentry.setValue(value);
		final Command addCommand = AddCommand.create(getEditingDomain(), getTestSuiteDefinition(), IDefinitionPackage.Literals.TEST_SUITE_DEFINITION__FLAGS,
				newentry);
		compoundCommand.append(addCommand);

		getEditor().executeCommand(compoundCommand);
	}

	protected void updateDescription(String content) {
		final Command command = SetCommand.create(getEditingDomain(), getTestSuiteDefinition(), IDefinitionPackage.Literals.TEST_SUITE_DEFINITION__DESCRIPTION,
				content);
		getEditor().executeCommand(command);
	}

	@Override
	protected void populateContent() {
		final String description = getTestSuiteDefinition().getDescription();
		if (description != null)
			fBrowser.setText(description);

		fTxtThreads.setText(Integer.toString(getTestSuiteDefinition().getFlag(Flag.THREAD_COUNT, 1)));

		fChkPromoteFailuresToErrors.setSelection(getTestSuiteDefinition().getFlag(Flag.PROMOTE_FAILURE_TO_ERROR, false));
		fChkStopSuiteOnError.setSelection(getTestSuiteDefinition().getFlag(Flag.STOP_SUITE_ON_ERROR, false));
		fChkRunTeardownOnError.setSelection(getTestSuiteDefinition().getFlag(Flag.RUN_TEARDOWN_ON_ERROR, true));

		final String selectedEngine = getTestSuiteDefinition().getFlag(Flag.PREFERRED_ENGINE_ID, "");
		if (!selectedEngine.isEmpty()) {
			final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);
			final EngineDescription engineDescription = scriptService.getEngineByID(selectedEngine);
			if (engineDescription != null)
				fEngineComboViewer.setSelection(new StructuredSelection(engineDescription));
		}
	}

	@Override
	protected String getPageTitle() {
		final String name = getTestSuiteDefinition().getName();
		return ((name != null) && (!name.isEmpty())) ? name : "Overview";
	}

	@Override
	public Image getTitleImage() {
		return Activator.getImage(Activator.PLUGIN_ID, "/icons/eobj16/testsuite.png", true);
	}

	@Override
	public void setActive(boolean active) {
		super.setActive(active);

		if (active) {
			if ((fLblTestFilesCount != null) && (!fLblTestFilesCount.isDisposed())) {
				// update suite stats
				final ITestSuite testSuite = UnitTestHelper.createRuntimeSuite(getTestSuiteDefinition());

				fLblTestFilesCount.setText(Integer.toString(UnitTestHelper.getTestFiles(testSuite).size()));
				fLblDisabledFilesCount.setText(Integer.toString(getDisabledFilesCount(testSuite)));

				fLblDefinedVariablesCount.setText(Integer.toString(getTestSuiteDefinition().getVariables().size()));

				final long runtime = calculateExpectedRuntime(testSuite);
				if (runtime < 0)
					fLblExpectedRuntime.setText("unknown");
				else
					fLblExpectedRuntime.setText(UnitTestView.getDurationString(runtime));

				fLblUsesSetupTeardown.setVisible(!getTestSuiteDefinition().getCustomCode().isEmpty());
				fHprlnkSetupTeardownCode.setVisible(!getTestSuiteDefinition().getCustomCode().isEmpty());
			}
		}
	}

	private long calculateExpectedRuntime(ITestSuite testSuite) {
		final SuiteRuntimeInformation runtimeInformation = new SuiteRuntimeInformation(testSuite);

		long totalEstimation = 0;
		for (final ITestFile testFile : UnitTestHelper.getTestFiles(testSuite)) {
			final long estimatedTestFileDuration = runtimeInformation.getEstimatedDuration(testFile.getFullPath().makeAbsolute());
			if (estimatedTestFileDuration < 0)
				return -1;

			totalEstimation += estimatedTestFileDuration;
		}

		return totalEstimation;
	}

	private int getDisabledFilesCount(ITestContainer testContainer) {
		int sum = 0;
		for (final ITestFile testFile : UnitTestHelper.getTestFiles(testContainer))
			sum += getTestSuiteDefinition().getDisabledResources().contains(testFile.getFullPath().removeFirstSegments(1).makeAbsolute()) ? 1 : 0;

		return sum;
	}
}
