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

import org.eclipse.ease.IReplEngine;
import org.eclipse.ease.ui.Messages;
import org.eclipse.ease.ui.view.VariablesDragListener;
import org.eclipse.ease.ui.views.shell.dropins.AbstractDropin;
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
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IWorkbenchPartSite;

public class VariablesDropin extends AbstractDropin {

	private TreeViewer fVariablesTree = null;

	@Override
	public void setScriptEngine(IReplEngine engine) {
		super.setScriptEngine(engine);

		// set tree input
		if (fVariablesTree != null) {
			fVariablesTree.setInput(engine);

			update();
		}
	}

	@Override
	public Composite createComposite(final IWorkbenchPartSite site, final Composite parent) {

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

		fVariablesTree.addDragSupport(DND.DROP_MOVE | DND.DROP_COPY, new Transfer[] { LocalSelectionTransfer.getTransfer(), TextTransfer.getInstance() },
				new VariablesDragListener(fVariablesTree));

		return composite;
	}

	@Override
	public String getTitle() {
		return Messages.VariablesDropin_variables;
	}

	@Override
	public void updateUI() {
		fVariablesTree.refresh();
	};
}
