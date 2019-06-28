/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.lang.unittest.ui.dialogs;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ease.lang.unittest.reporters.IReportGenerator;
import org.eclipse.ease.lang.unittest.reporters.ReportTools;
import org.eclipse.ease.lang.unittest.ui.Activator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;

public class CreateReportDialog extends Dialog {
	private static final String HISTORY_FILE = "export_history.xml";
	private static final String FILELOCATION = "filelocation";
	private static final String TITLE = "title";
	private static final String DESCRIPTION = "description";
	private static final String TYPE = "type";

	private Text fTxtFileName;
	private Text fTxtTitle;
	private Text fTxtDescription;
	private Button fBtnOpenReportAfter;
	private Combo fCmbType;

	private String fFileName = null;
	private String fTitle = "";
	private String fDescription = "";
	private boolean fOpenAfterSave;
	private String fType;

	public CreateReportDialog(final Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Create Test Report");
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Map<String, String> history = loadHistory();

		final Composite container = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = (GridLayout) container.getLayout();
		gridLayout.numColumns = 3;

		final Label lblType = new Label(container, SWT.NONE);
		lblType.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblType.setText("Type:");

		fCmbType = new Combo(container, SWT.READ_ONLY);
		fCmbType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		fCmbType.setItems(ReportTools.getReportTemplates().toArray(new String[0]));
		if ((history.containsKey(TYPE)) && (history.get(TYPE) != null))
			fTxtFileName.setText(history.get(TYPE));
		else
			fCmbType.setText(fCmbType.getItem(0));

		new Label(container, SWT.NONE);

		final Label lblFile = new Label(container, SWT.NONE);
		lblFile.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblFile.setText("File:");

		fTxtFileName = new Text(container, SWT.BORDER);
		fTxtFileName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if ((history.containsKey(FILELOCATION)) && (history.get(FILELOCATION) != null))
			fTxtFileName.setText(history.get(FILELOCATION));

		final Button btnBrowse = new Button(container, SWT.NONE);
		btnBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
				dialog.setText("Save Report to File");
				dialog.setOverwrite(true);

				// try to set default names & filters
				final IReportGenerator report = ReportTools.getReport(fCmbType.getText());
				if (report != null) {
					dialog.setFileName("report." + report.getDefaultExtension());
					dialog.setFilterExtensions(new String[] { "*." + report.getDefaultExtension() });
				} else {
					dialog.setFileName("report");
					dialog.setFilterExtensions(new String[] { "*.*" });
				}

				final String location = dialog.open();
				if (location != null)
					fTxtFileName.setText(location);
			}
		});
		btnBrowse.setText("Browse...");

		final Label label = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		final Label lblTitle = new Label(container, SWT.NONE);
		lblTitle.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTitle.setText("Title:");

		fTxtTitle = new Text(container, SWT.BORDER);
		fTxtTitle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		if ((history.containsKey(TITLE)) && (history.get(TITLE) != null))
			fTxtTitle.setText(history.get(TITLE));

		final Label lblDescription = new Label(container, SWT.NONE);
		lblDescription.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
		lblDescription.setText("Description:");

		fTxtDescription = new Text(container, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		fTxtDescription.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		if ((history.containsKey(DESCRIPTION)) && (history.get(DESCRIPTION) != null))
			fTxtDescription.setText(history.get(DESCRIPTION));

		new Label(container, SWT.NONE);

		fBtnOpenReportAfter = new Button(container, SWT.CHECK);
		fBtnOpenReportAfter.setSelection(true);
		fBtnOpenReportAfter.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		fBtnOpenReportAfter.setText("Open report after saving");

		return container;
	}

	/**
	 * Create contents of the button bar.
	 *
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(final Composite parent) {
		final Button clearButton = createButton(parent, IDialogConstants.BACK_ID, "Clear", false);
		clearButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				fTxtFileName.setText("");
				fTxtTitle.setText("");
				fTxtDescription.setText("");
				fBtnOpenReportAfter.setSelection(false);
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
			}
		});

		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}

	@Override
	protected void okPressed() {
		fType = fCmbType.getText();
		fFileName = fTxtFileName.getText();
		fTitle = fTxtTitle.getText();
		fDescription = fTxtDescription.getText();
		fOpenAfterSave = fBtnOpenReportAfter.getSelection();

		final Map<String, String> history = new HashMap<>();
		history.put(FILELOCATION, fTxtFileName.getText());
		history.put(TITLE, fTxtTitle.getText());
		history.put(DESCRIPTION, fTxtDescription.getText());
		history.put(TYPE, fCmbType.getText());
		saveHistory(history);

		super.okPressed();
	}

	public String getFileName() {
		return fFileName;
	}

	public String getTitle() {
		return fTitle;
	}

	public String getDescription() {
		return fDescription;
	}

	public boolean isOpenReport() {
		return fOpenAfterSave;
	}

	public IReportGenerator getReport() {
		return ReportTools.getReport(fType);
	}

	private Map<String, String> loadHistory() {
		final Map<String, String> result = new HashMap<>();

		try {
			final IPath location = Activator.getDefault().getStateLocation();
			final File file = location.append(HISTORY_FILE).toFile();
			if (file.exists()) {
				final FileReader reader = new FileReader(file);
				final IMemento memento = XMLMemento.createReadRoot(reader);
				reader.close();

				for (final String key : new String[] { FILELOCATION, TITLE, DESCRIPTION }) {
					final IMemento node = memento.getChild(key);
					if (node != null)
						result.put(key, node.getTextData());
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
			// could not load history, ignore
		}

		return result;
	}

	private void saveHistory(final Map<String, String> data) {
		final XMLMemento memento = XMLMemento.createWriteRoot("history");
		for (final Entry<String, String> entry : data.entrySet())
			memento.createChild(entry.getKey()).putTextData(entry.getValue());

		try {
			final IPath location = Activator.getDefault().getStateLocation();
			final File file = location.append(HISTORY_FILE).toFile();
			final FileWriter writer = new FileWriter(file);
			memento.save(writer);
			writer.close();
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
