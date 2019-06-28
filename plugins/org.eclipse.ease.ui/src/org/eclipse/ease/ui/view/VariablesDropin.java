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
package org.eclipse.ease.ui.view;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.ease.IExecutionListener;
import org.eclipse.ease.IReplEngine;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Script;
import org.eclipse.ease.debugging.model.EaseDebugLastExecutionResult;
import org.eclipse.ease.debugging.model.EaseDebugTarget;
import org.eclipse.ease.debugging.model.EaseDebugVariable;
import org.eclipse.ease.modules.EnvironmentModule;
import org.eclipse.ease.ui.Messages;
import org.eclipse.ease.ui.debugging.model.AbstractEaseDebugModelPresentation;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IWorkbenchPartSite;

public class VariablesDropin implements IShellDropin, IExecutionListener {

	private TreeViewer fVariablesTree = null;
	private IScriptEngine fEngine;

	@Override
	public void setScriptEngine(IScriptEngine engine) {
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

		fVariablesTree.setFilters(new ViewerFilter[] {

				// filter modules
				new ViewerFilter() {

					@Override
					public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
						if (element instanceof EaseDebugVariable)
							return !((EaseDebugVariable) element).getName().startsWith(EnvironmentModule.EASE_CODE_PREFIX);

						return true;
					}
				} });

		fVariablesTree.setComparator(new ViewerComparator() {
			@Override
			public int category(Object element) {
				return (element instanceof EaseDebugLastExecutionResult) ? 1 : 2;
			}
		});

		fVariablesTree.setContentProvider(new ITreeContentProvider() {

			@Override
			public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
			}

			@Override
			public void dispose() {
			}

			@Override
			public boolean hasChildren(final Object element) {
				return getChildren(element).length > 0;
			}

			@Override
			public Object getParent(final Object element) {
				return null;
			}

			@Override
			public Object[] getElements(final Object inputElement) {
				if (inputElement instanceof IReplEngine) {
					final EaseDebugTarget debugTarget = new EaseDebugTarget(null, false, false, false) {
						@Override
						protected IBreakpoint[] getBreakpoints(Script script) {
							return null;
						}

						@Override
						public boolean supportsBreakpoint(IBreakpoint breakpoint) {
							return false;
						}
					};

					// get variables
					final Collection<EaseDebugVariable> variables = new HashSet<>(((IReplEngine) inputElement).getDefinedVariables());

					// add last execution result
					final EaseDebugVariable lastScriptResult = ((IReplEngine) inputElement).getLastExecutionResult();
					variables.add(lastScriptResult);

					for (final EaseDebugVariable entry : variables)
						entry.setParent(debugTarget);

					return variables.toArray();
				}

				return new Object[0];
			}

			@Override
			public Object[] getChildren(final Object parentElement) {
				if (parentElement instanceof EaseDebugVariable)
					return ((EaseDebugVariable) parentElement).getValue().getVariables();

				return new Object[0];
			}
		});

		final TreeViewerColumn treeViewerColumn = new TreeViewerColumn(fVariablesTree, SWT.NONE);
		final TreeColumn column = treeViewerColumn.getColumn();
		treeColumnLayout.setColumnData(column, new ColumnWeightData(1));
		column.setText(Messages.VariablesDropin_name);
		treeViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			private final AbstractEaseDebugModelPresentation fDebugModelPresentation = new AbstractEaseDebugModelPresentation() {
			};

			@Override
			public String getText(final Object element) {
				if (element instanceof EaseDebugVariable)
					return ((EaseDebugVariable) element).getName();

				return super.getText(element);
			}

			@Override
			public Image getImage(final Object element) {
				return fDebugModelPresentation.getImage(element);
			}
		});

		final TreeViewerColumn treeViewerColumn2 = new TreeViewerColumn(fVariablesTree, SWT.NONE);
		final TreeColumn column2 = treeViewerColumn2.getColumn();
		treeColumnLayout.setColumnData(column2, new ColumnWeightData(1));
		column2.setText(Messages.VariablesDropin_value);
		treeViewerColumn2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof EaseDebugVariable)
					return ((EaseDebugVariable) element).getValue().getValueString();

				return super.getText(element);
			}
		});

		fVariablesTree.setInput(fEngine);

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
		}
	}
}
