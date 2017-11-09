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

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ease.lang.unittest.UnitTestHelper;
import org.eclipse.ease.lang.unittest.definition.IDefinitionPackage;
import org.eclipse.ease.lang.unittest.runtime.ITestContainer;
import org.eclipse.ease.lang.unittest.runtime.ITestEntity;
import org.eclipse.ease.lang.unittest.runtime.ITestFile;
import org.eclipse.ease.lang.unittest.runtime.ITestFolder;
import org.eclipse.ease.lang.unittest.ui.Activator;
import org.eclipse.ease.lang.unittest.ui.views.TestSuiteLabelProvider;
import org.eclipse.ease.lang.unittest.ui.views.UnitTestView;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.jface.resource.DeviceResourceException;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

public class FileSelectionPage extends AbstractEditorPage {

	// copied from org.eclipse.ui.internal.forms.widgets.FormFonts
	private class StyledFontDescriptor extends FontDescriptor {
		private final FontData[] fFontData;

		private StyledFontDescriptor(Font font, int style) {
			fFontData = font.getFontData();
			for (final FontData element : fFontData) {
				element.setStyle(element.getStyle() | style);
			}
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof StyledFontDescriptor) {
				final StyledFontDescriptor desc = (StyledFontDescriptor) obj;
				if (desc.fFontData.length != fFontData.length)
					return false;
				for (int i = 0; i < fFontData.length; i++)
					if (!fFontData[i].equals(desc.fFontData[i]))
						return false;
				return true;
			}
			return false;
		}

		@Override
		public int hashCode() {
			int hash = 0;
			for (final FontData element : fFontData)
				hash = (hash * 7) + element.hashCode();
			return hash;
		}

		@Override
		public Font createFont(Device device) throws DeviceResourceException {
			return new Font(device, fFontData);
		}

		@Override
		public void destroyFont(Font previouslyCreatedFont) {
			previouslyCreatedFont.dispose();
		}
	}

	private class FileSelectionTreeProvider implements ITreeContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			return ((List<?>) inputElement).toArray();
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof ITestContainer)
				return ((ITestContainer) parentElement).getChildren().toArray();

			return null;
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof ITestContainer)
				return !((ITestContainer) element).getChildren().isEmpty();

			return false;
		}
	}

	private class FileSelectionLabelProvider extends TestSuiteLabelProvider implements IFontProvider, IColorProvider {

		public FileSelectionLabelProvider(LocalResourceManager resourceManager) {
			super(resourceManager);
		}

		@Override
		public Font getFont(Object element) {
			if (isRootElement(element))
				return getEditor().getResourceManager().createFont(new StyledFontDescriptor(fTestFilesTreeViewer.getTree().getFont(), SWT.BOLD));

			if (element instanceof ITestFile) {
				if (fFilteredFiles.keySet().contains(((ITestFile) element).getResource()))
					return getEditor().getResourceManager().createFont(new StyledFontDescriptor(fTestFilesTreeViewer.getTree().getFont(), SWT.ITALIC));
			}

			return null;
		}

		@Override
		public Color getForeground(Object element) {
			if (element instanceof ITestFile) {
				if (fFilteredFiles.keySet().contains(((ITestFile) element).getResource()))
					return Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
			}

			return null;
		}

		@Override
		public Color getBackground(Object element) {
			return null;
		}
	}

	private ContainerCheckedTreeViewer fTestFilesTreeViewer;

	private Text fTxtIncludeFilters;
	private Text fTxtExcludeFilters;
	private Map<Object, String> fFilteredFiles;

	public FileSelectionPage(FormEditor editor, String id, String title) {
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
		managedForm.getForm().getBody().setLayout(new FillLayout(SWT.HORIZONTAL));

		final Composite composite = managedForm.getToolkit().createComposite(managedForm.getForm().getBody(), SWT.NONE);
		managedForm.getToolkit().paintBordersFor(composite);
		composite.setLayout(new GridLayout(1, false));

		final Section sctnInclusionexclusionPatterns = managedForm.getToolkit().createSection(composite,
				ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR);
		final GridData gd_sctnInclusionexclusionPatterns = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		sctnInclusionexclusionPatterns.setLayoutData(gd_sctnInclusionexclusionPatterns);
		managedForm.getToolkit().paintBordersFor(sctnInclusionexclusionPatterns);
		sctnInclusionexclusionPatterns.setText("Inclusion/Exclusion Patterns");
		sctnInclusionexclusionPatterns.setExpanded(false);
		sctnInclusionexclusionPatterns.addExpansionListener(new IExpansionListener() {

			@Override
			public void expansionStateChanging(ExpansionEvent e) {
			}

			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				gd_sctnInclusionexclusionPatterns.heightHint = e.getState() ? 200 : -1;
				body.layout(true, true);
			}
		});

		final Composite composite_1 = managedForm.getToolkit().createComposite(sctnInclusionexclusionPatterns, SWT.NONE);
		managedForm.getToolkit().paintBordersFor(composite_1);
		sctnInclusionexclusionPatterns.setClient(composite_1);
		composite_1.setLayout(new GridLayout(2, false));

		managedForm.getToolkit().createLabel(composite_1, "Include patterns", SWT.NONE);

		managedForm.getToolkit().createLabel(composite_1, "Exclude patterns", SWT.NONE);

		fTxtIncludeFilters = managedForm.getToolkit().createText(composite_1, "", SWT.MULTI);
		fTxtIncludeFilters.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		fTxtExcludeFilters = managedForm.getToolkit().createText(composite_1, "", SWT.MULTI);
		fTxtExcludeFilters.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		final Label lblAddAntStyle = managedForm.getToolkit().createLabel(sctnInclusionexclusionPatterns,
				"Add ANT style patterns to include and exclude files. First a list of included files will be generated, then exclude patterns get applied to that list.\n\nYou may use relative paths (relative to the suite file) and URIs (file://, workspace://, project://). Further * and ** wildcards are allowed.",
				SWT.WRAP);
		sctnInclusionexclusionPatterns.setDescriptionControl(lblAddAntStyle);

		final Section sctnTestFileSelection = managedForm.getToolkit().createSection(composite, ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR);
		sctnTestFileSelection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		managedForm.getToolkit().paintBordersFor(sctnTestFileSelection);
		sctnTestFileSelection.setText("Test File Selection");
		sctnTestFileSelection.setExpanded(true);

		final Tree tree = managedForm.getToolkit().createTree(sctnTestFileSelection, SWT.BORDER | SWT.CHECK);
		managedForm.getToolkit().paintBordersFor(tree);
		sctnTestFileSelection.setClient(tree);
		fTestFilesTreeViewer = new ContainerCheckedTreeViewer(tree);
		fTestFilesTreeViewer.setContentProvider(new FileSelectionTreeProvider());
		fTestFilesTreeViewer.setLabelProvider(new FileSelectionLabelProvider(getEditor().getResourceManager()));
		fTestFilesTreeViewer.setComparator(new ViewerComparator() {
			@Override
			public int category(Object element) {
				return (element instanceof ITestFolder) ? 0 : 1;
			}
		});

		fTestFilesTreeViewer.addDoubleClickListener(event -> {
			final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
			if (!selection.isEmpty()) {
				final Object element = selection.getFirstElement();
				if (element instanceof ITestFile) {
					final Object resource = ((ITestFile) element).getResource();
					UnitTestView.openEditor(resource, 0);
				}
			}
		});

		fTestFilesTreeViewer.setInput(null);

		final Label lblUncheckFilesTo = managedForm.getToolkit().createLabel(sctnTestFileSelection, "Uncheck files to disable them for the test run.",
				SWT.NONE);
		sctnTestFileSelection.setDescriptionControl(lblUncheckFilesTo);

		// apply model data
		populateContent();

		// modify listeners
		fTxtIncludeFilters.addFocusListener(new FocusAdapter() {
			private String fInitialContent;

			@Override
			public void focusGained(FocusEvent e) {
				// store previous input to detect changes correctly
				fInitialContent = fTxtIncludeFilters.getText();
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (!fTxtIncludeFilters.getText().equals(fInitialContent)) {
					final Command command = SetCommand.create(getEditingDomain(), getTestSuitDefinition(),
							IDefinitionPackage.Literals.TEST_SUITE_DEFINITION__INCLUDE_FILTER, fTxtIncludeFilters.getText());
					getEditor().executeCommand(command);

					updateTestFiles();
				}
			}
		});

		fTxtExcludeFilters.addFocusListener(new FocusAdapter() {
			private String fInitialContent;

			@Override
			public void focusGained(FocusEvent e) {
				// store previous input to detect changes correctly
				fInitialContent = fTxtExcludeFilters.getText();
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (!fTxtExcludeFilters.getText().equals(fInitialContent)) {
					final Command command = SetCommand.create(getEditingDomain(), getTestSuitDefinition(),
							IDefinitionPackage.Literals.TEST_SUITE_DEFINITION__EXCLUDE_FILTER, fTxtExcludeFilters.getText());
					getEditor().executeCommand(command);

					updateTestFiles();
				}
			}
		});

		fTestFilesTreeViewer.addCheckStateListener(event -> {
			final Object element = event.getElement();
			if (element instanceof ITestEntity) {
				final CompoundCommand compoundCommand = new CompoundCommand();

				populateModelUpdateForUncheckedTests(element, event.getChecked(), compoundCommand);

				getEditor().executeCommand(compoundCommand);
			}
		});
	}

	/**
	 * Create commands to update the list of disabled test files. Does not actually update the model but just populate the provided compoundCommand.
	 *
	 * @param element
	 *            element to query
	 * @param checked
	 *            checked state of element
	 * @param compoundCommand
	 *            command to store actions in
	 */
	private void populateModelUpdateForUncheckedTests(Object element, boolean checked, CompoundCommand compoundCommand) {
		if (element instanceof ITestContainer) {
			final IPath entityPath = ((ITestContainer) element).getFullPath();

			// update resource state in testsuite
			if ((getTestSuitDefinition().getDisabledResources().contains(entityPath)) && (checked)) {
				// remove resource from list of disabled resources
				final Command command = RemoveCommand.create(getEditingDomain(), getTestSuitDefinition(),
						IDefinitionPackage.Literals.TEST_SUITE_DEFINITION__DISABLED_RESOURCES, entityPath);
				compoundCommand.append(command);
			} else if ((!getTestSuitDefinition().getDisabledResources().contains(entityPath)) && (!checked)) {
				// remove resource from list of disabled resources
				final Command command = AddCommand.create(getEditingDomain(), getTestSuitDefinition(),
						IDefinitionPackage.Literals.TEST_SUITE_DEFINITION__DISABLED_RESOURCES, entityPath);
				compoundCommand.append(command);
			}

			for (final ITestEntity child : ((ITestContainer) element).getChildren())
				populateModelUpdateForUncheckedTests(child, checked, compoundCommand);
		}
	}

	@Override
	protected void populateContent() {
		fTxtIncludeFilters.setText(getTestSuitDefinition().getIncludeFilter());
		fTxtExcludeFilters.setText(getTestSuitDefinition().getExcludeFilter());

		updateTestFiles();
	}

	private void setCheckedState(ITestContainer element) {
		final IPath entityPath = element.getFullPath();
		fTestFilesTreeViewer.setChecked(element, !getTestSuitDefinition().getDisabledResources().contains(entityPath));

		for (final ITestEntity child : element.getChildren()) {
			if (child instanceof ITestContainer)
				setCheckedState((ITestContainer) child);
		}
	}

	private void updateTestFiles() {
		final String[] includeFilters = fTxtIncludeFilters.getText().split("\r?\n");
		final String[] excludeFilters = fTxtExcludeFilters.getText().split("\r?\n");

		final Map<Object, String> acceptedFiles = UnitTestHelper.getTestFilesFromFilter(includeFilters, getFile());
		fFilteredFiles = UnitTestHelper.getTestFilesFromFilter(excludeFilters, getFile());

		acceptedFiles.keySet().removeAll(fFilteredFiles.keySet());

		final List<ITestEntity> testStructure = UnitTestHelper.createTestStructure(acceptedFiles);

		fTestFilesTreeViewer.setInput(testStructure);
		fTestFilesTreeViewer.refresh();

		fTestFilesTreeViewer.expandAll();

		for (final ITestEntity element : testStructure) {
			if (element instanceof ITestContainer)
				setCheckedState((ITestContainer) element);
		}
	}

	@Override
	protected String getPageTitle() {
		return "Test Selection";
	}

	@Override
	public Image getTitleImage() {
		return Activator.getImage(Activator.PLUGIN_ID, "/icons/eobj16/test_selection.png", true);
	}
}
