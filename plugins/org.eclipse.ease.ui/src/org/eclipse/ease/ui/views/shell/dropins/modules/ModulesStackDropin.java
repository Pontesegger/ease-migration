/*******************************************************************************
 * Copyright (c) 2016 Christian Pontesegger and others.
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

package org.eclipse.ease.ui.views.shell.dropins.modules;

import org.eclipse.ease.IReplEngine;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.ui.Messages;
import org.eclipse.ease.ui.modules.ui.ModulesDragListener;
import org.eclipse.ease.ui.views.shell.dropins.AbstractDropin;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchPartSite;

public class ModulesStackDropin extends AbstractDropin {

	private TableViewer fModulesTable = null;

	@Override
	public void setScriptEngine(IReplEngine engine) {
		super.setScriptEngine(engine);

		if (fModulesTable != null) {
			fModulesTable.setInput(engine);

			update();
		}
	}

	@Override
	public Composite createComposite(IWorkbenchPartSite site, Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		final TableColumnLayout tableColumnLayout = new TableColumnLayout();
		composite.setLayout(tableColumnLayout);

		fModulesTable = new TableViewer(composite, SWT.BORDER);
		final Table table = fModulesTable.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		fModulesTable.setContentProvider(new ModulesStackContentProvider());

		final TableViewerColumn tableViewerColumn = new TableViewerColumn(fModulesTable, SWT.NONE);
		final TableColumn column = tableViewerColumn.getColumn();
		tableColumnLayout.setColumnData(column, new ColumnWeightData(1));
		column.setText(Messages.ModuleStackDropin_module);
		tableViewerColumn.setLabelProvider(new ModulesStackLabelProvider());

		fModulesTable.addDragSupport(DND.DROP_MOVE | DND.DROP_COPY, new Transfer[] { LocalSelectionTransfer.getTransfer(), TextTransfer.getInstance() },
				new ModulesDragListener(fModulesTable) {
					@Override
					public void dragStart(DragSourceEvent event) {
						super.dragStart(event);

						final Object firstElement = getSelection().getFirstElement();
						event.doit = (firstElement instanceof ModuleDefinition);
					}
				});

		return composite;
	}

	@Override
	public String getTitle() {
		return Messages.ModuleStackDropin_moduleStack;
	}

	@Override
	protected void updateUI() {
		fModulesTable.refresh();
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return fModulesTable;
	}
}
