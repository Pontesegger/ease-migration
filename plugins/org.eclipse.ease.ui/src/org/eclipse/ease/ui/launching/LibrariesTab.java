/*******************************************************************************
* Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.launching;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.ease.ui.Activator;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class LibrariesTab extends AbstractLaunchConfigurationTab implements ILaunchConfigurationTab {

	private final List<String> fLibraries = new ArrayList<>();
	private TableViewer tableViewer;
	private Button fBtnRemove;

	@Override
	public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(LaunchConstants.LIBRARIES, "");
	}

	@Override
	public void initializeFrom(final ILaunchConfiguration configuration) {
		fLibraries.clear();

		try {
			fLibraries.addAll(LaunchConstants.getLibraries(configuration));
		} catch (final CoreException e) {
		}

		tableViewer.refresh();
	}

	@Override
	public void performApply(final ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(LaunchConstants.LIBRARIES, LaunchConstants.serializeLibraries(fLibraries));
	}

	@Override
	public String getMessage() {
		return "Please select JAR files to load within the interpreter.";
	}

	@Override
	public String getName() {
		return "Libraries";
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	public void createControl(final Composite parent) {
		final Composite topControl = new Composite(parent, SWT.NONE);
		topControl.setLayout(new GridLayout(2, false));

		final Label lblStartupCode = new Label(topControl, SWT.NONE);
		lblStartupCode.setText("Additional li&braries:");
		lblStartupCode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

		tableViewer = new TableViewer(topControl, SWT.BORDER | SWT.V_SCROLL);
		final Table table = tableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtonStatus();
			}
		});
		tableViewer.setLabelProvider(new JarTableProvider());
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setInput(fLibraries);

		final Composite composite = new Composite(topControl, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		final FillLayout fl_composite = new FillLayout(SWT.VERTICAL);
		fl_composite.spacing = 10;
		composite.setLayout(fl_composite);

		final Button btnAddJar = new Button(composite, SWT.NONE);
		btnAddJar.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				addWorkspaceJar();
			}
		});
		btnAddJar.setText("Add &JARs...");

		final Button btnAddExternalJar = new Button(composite, SWT.NONE);
		btnAddExternalJar.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				addExternalJar();
			}
		});
		btnAddExternalJar.setText("Add E&xternal JARs...");

		fBtnRemove = new Button(composite, SWT.NONE);
		fBtnRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				removeJar();
			}
		});
		fBtnRemove.setText("R&emove");

		updateButtonStatus();
		setControl(topControl);
	}

	private void refresh() {
		tableViewer.refresh();
		updateLaunchConfigurationDialog();
		updateButtonStatus();
	}

	private void updateButtonStatus() {
		final boolean isEnable = !tableViewer.getStructuredSelection().isEmpty();
		fBtnRemove.setEnabled(isEnable);
	}

	private void addWorkspaceJar() {

		final ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), new WorkbenchLabelProvider(), new WorkbenchContentProvider());
		dialog.setTitle("Select JAR file");
		dialog.setMessage("Select the JAR file to include:");
		dialog.addFilter(new FileExtensionFilter(new String[] { "jar" }));

		dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
		if (dialog.open() == Window.OK) {
			fLibraries.add("workspace:/" + ((IFile) dialog.getFirstResult()).getFullPath().toPortableString());
			refresh();
		}
	}

	private void addExternalJar() {

		final FileDialog dialog = new FileDialog(getShell(), SWT.OPEN | SWT.MULTI);
		dialog.setFilterExtensions(new String[] { "*.jar" });
		dialog.setFilterNames(new String[] { "Java Archives (*.jar)" });
		dialog.setText("JAR Selection");
		final String filePath = dialog.open();
		if (filePath != null) {
			for (final String filename : dialog.getFileNames()) {
				final String path = dialog.getFilterPath() + File.separator + filename;
				fLibraries.add(new File(path).toURI().toString());
			}
			refresh();
		}
	}

	private void removeJar() {

		final IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
		final Object element = selection.getFirstElement();
		if (element instanceof String) {
			fLibraries.remove(element);
			refresh();
		}
	}

	private class JarTableProvider extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			if (element instanceof String) {
				switch (columnIndex) {
				case 0:
					return Activator.getImage(Activator.PLUGIN_ID, "/icons/eobj16/jar_entry.png", true);
				}
			}
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof String) {
				switch (columnIndex) {
				case 0:
					return element.toString();
				}
			}
			return "";
		}
	}

	@Override
	public Image getImage() {
		return Activator.getImage(Activator.PLUGIN_ID, "/icons/eobj16/library_tab.gif", true);
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		try {
			final Collection<String> jarList = LaunchConstants.getLibraries(launchConfig);
			for (final String jarPath : jarList) {
				if (!ResourceTools.exists(jarPath)) {
					setErrorMessage("One of the JAR file(s) not exists");
					return false;
				}
			}
		} catch (final CoreException e) {
		}
		return true;
	}
}
