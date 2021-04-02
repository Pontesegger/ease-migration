/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.views.shell.dropins.variables;

import org.eclipse.ease.IExecutionListener;
import org.eclipse.ease.IReplEngine;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Script;
import org.eclipse.ease.ui.Messages;
import org.eclipse.ease.ui.view.VariablesDragListener;
import org.eclipse.ease.ui.views.shell.dropins.IShellDropin;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IWorkbenchPartSite;

public class VariablesDropin implements IShellDropin, IExecutionListener {

	private TreeViewer fVariablesTree = null;
	private IReplEngine fEngine;

	@Override
	public void setScriptEngine(IReplEngine engine) {
		if (fEngine != null)
			fEngine.removeExecutionListener(this);

		fEngine = engine;

		if (fEngine != null)
			fEngine.addExecutionListener(this);

		// set tree input
		if (fVariablesTree != null) {
			fVariablesTree.setInput(engine);
			Display.getDefault().asyncExec(() -> fVariablesTree.refresh());
		}
	}

	@Override
	public Composite createPartControl(final IWorkbenchPartSite site, final Composite parent) {

		final Composite composite = new Composite(parent, SWT.NONE);
		final TreeColumnLayout treeColumnLayout = new TreeColumnLayout();
		composite.setLayout(treeColumnLayout);

		fVariablesTree = new TreeViewer(composite, SWT.BORDER);
		final Tree tree = fVariablesTree.getTree();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);

		fVariablesTree.setFilters(new ViewerFilter[] { new HiddenVariablesFilter() });
		fVariablesTree.setComparator(new VariablesComparator());
		fVariablesTree.setContentProvider(new VariablesContentProvider());

		final TreeViewerColumn treeViewerColumn = new TreeViewerColumn(fVariablesTree, SWT.NONE);
		final TreeColumn column = treeViewerColumn.getColumn();
		treeColumnLayout.setColumnData(column, new ColumnWeightData(1));
		column.setText(Messages.VariablesDropin_name);
		treeViewerColumn.setLabelProvider(new VariablesLabelProvider());

		final TreeViewerColumn treeViewerColumn2 = new TreeViewerColumn(fVariablesTree, SWT.NONE);
		final TreeColumn column2 = treeViewerColumn2.getColumn();
		treeColumnLayout.setColumnData(column2, new ColumnWeightData(1));
		column2.setText(Messages.VariablesDropin_value);
		treeViewerColumn2.setLabelProvider(new ContentLabelProvider());

		fVariablesTree.setInput(fEngine);

		fVariablesTree.addDragSupport(DND.DROP_MOVE | DND.DROP_COPY, new Transfer[] { LocalSelectionTransfer.getTransfer(), TextTransfer.getInstance() },
				new VariablesDragListener(fVariablesTree));

		return composite;
	}

	@Override
	public String getTitle() {
		return Messages.VariablesDropin_variables;
	}

	@Override
	public void notify(IScriptEngine engine, Script script, int status) {
		switch (status) {
		case IExecutionListener.SCRIPT_END:
			Display.getDefault().asyncExec(() -> fVariablesTree.refresh());
			break;

		case IExecutionListener.ENGINE_END:
			engine.removeExecutionListener(this);
			break;

		default:
			// nothing to do;
			break;
		}
	}
}
