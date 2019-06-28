/*******************************************************************************
 * Copyright (c) 2016 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ease.IExecutionListener;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Script;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.Messages;
import org.eclipse.ease.ui.modules.ui.ModulesDragListener;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;

public class ModuleStackDropin implements IShellDropin, IExecutionListener {

	private TableViewer fModulesTable;
	private IScriptEngine fEngine;

	@Override
	public void setScriptEngine(IScriptEngine engine) {
		if (fEngine != null)
			fEngine.removeExecutionListener(this);

		fEngine = engine;

		if (fEngine != null)
			fEngine.addExecutionListener(this);

		// set tree input
		if (fModulesTable != null) {
			fModulesTable.setInput(fEngine);
			Display.getDefault().asyncExec(() -> fModulesTable.refresh());
		}
	}

	@Override
	public Composite createPartControl(IWorkbenchPartSite site, Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		final TableColumnLayout tableColumnLayout = new TableColumnLayout();
		composite.setLayout(tableColumnLayout);

		fModulesTable = new TableViewer(composite, SWT.BORDER);
		final Table table = fModulesTable.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		fModulesTable.setContentProvider((IStructuredContentProvider) inputElement -> {
			if (inputElement instanceof IScriptEngine) {
				final IEnvironment environment = IEnvironment.getEnvironment((IScriptEngine) inputElement);
				if (environment != null) {
					final List<Object> loadedModules = new ArrayList<>();

					for (final Object instance : new ArrayList<>(environment.getModules())) {
						final ModuleDefinition definition = getDefinition(instance);
						loadedModules.add((definition != null) ? definition : instance);
					}

					return loadedModules.toArray();
				}
			}

			return new Object[0];
		});

		final TableViewerColumn tableViewerColumn = new TableViewerColumn(fModulesTable, SWT.NONE);
		final TableColumn column = tableViewerColumn.getColumn();
		tableColumnLayout.setColumnData(column, new ColumnWeightData(1));
		column.setText(Messages.ModuleStackDropin_module);
		tableViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof ModuleDefinition)
					return ((ModuleDefinition) element).getName();

				if (element != null)
					return element.getClass().getCanonicalName();

				return super.getText(element);
			}

			@Override
			public Image getImage(final Object element) {
				if (element instanceof ModuleDefinition) {
					final ImageDescriptor icon = ((ModuleDefinition) element).getImageDescriptor();
					if (icon != null)
						return icon.createImage();

					return Activator.getImage(Activator.PLUGIN_ID, "/icons/eobj16/module.png", true);
				}

				if (element != null)
					return Activator.getImage(Activator.PLUGIN_ID, "/icons/eobj16/debug_java_class.png", true);

				return super.getImage(element);
			}
		});

		fModulesTable.setInput(fEngine);

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

	/**
	 * Get a module definition for a given module instance.
	 *
	 * @param element
	 *            module instance
	 * @return module definition or <code>null</code>
	 */
	private ModuleDefinition getDefinition(Object element) {
		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);
		final List<ModuleDefinition> modules = new ArrayList<>(scriptService.getAvailableModules());

		for (final ModuleDefinition definition : modules) {
			if (definition.getModuleClass().equals(element.getClass())) {
				return definition;
			}
		}

		return null;
	}

	@Override
	public String getTitle() {
		return Messages.ModuleStackDropin_moduleStack;
	}

	@Override
	public void notify(IScriptEngine engine, Script script, int status) {
		switch (status) {
		case IExecutionListener.SCRIPT_END:
		case IExecutionListener.SCRIPT_INJECTION_END:
			Display.getDefault().asyncExec(() -> fModulesTable.refresh());
			break;

		case IExecutionListener.ENGINE_END:
			engine.removeExecutionListener(this);
			break;
		}
	}
}
