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

import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ease.lang.unittest.definition.IDefinitionPackage;
import org.eclipse.ease.lang.unittest.definition.ITestSuiteDefinition;
import org.eclipse.ease.lang.unittest.definition.IVariable;
import org.eclipse.ease.lang.unittest.runtime.ITestSuite;
import org.eclipse.ease.ui.tools.AbstractVirtualTreeProvider;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.progress.UIJob;

/**
 * Represents the Variables component in the Test Suite Editor. This class implements the Variables component in Test Suite Editor with a tree structure
 * allowing to classify variables in groups.
 */
public class VariablesPage extends AbstractEditorPage {

	private class VariablesTreeContentProvider extends AbstractVirtualTreeProvider {

		/** Paths added by the user which are currently not populated by a variable. */
		private final Collection<IPath> fAdditionalPaths = new HashSet<>();

		/**
		 * Registers elements in the tree viewer from the test suites variables component.
		 *
		 * @param inputElement
		 *            List of Variables defined for the test suite to be registered in the tree viewer.
		 */
		@Override
		protected void populateElements(Object inputElement) {
			setShowRoot(false);

			if (inputElement instanceof ITestSuiteDefinition) {
				for (final Object element : ((ITestSuiteDefinition) inputElement).getVariables()) {
					if (element instanceof IVariable)
						registerElement(((IVariable) element).getPath(), element);
				}
			}

			for (final IPath additionalPath : fAdditionalPaths)
				registerPath(additionalPath.makeAbsolute());
		}

		public void addPath(IPath path) {
			fAdditionalPaths.add(path);
			registerPath(path);
		}

		public void removePath(IPath path) {
			fAdditionalPaths.remove(path);
		}

		public boolean containsPath(IPath path) {
			return fAdditionalPaths.contains(path);
		}
	}

	private static final String VARIABLES_EDITOR_ID = "org.eclipse.ease.editor.suiteEditor.variables";

	private static final Pattern VARIABLE_NAME_PATTERN = Pattern.compile("([a-zA-Z_$][0-9a-zA-Z_$]*)");

	/**
	 * Verify that a variables name is valid regarding coding rules.
	 *
	 * @param name
	 *            name to be used
	 * @return <code>true</code> when new name is valid
	 */
	private static boolean isValidNamePattern(final String name) {
		return VARIABLE_NAME_PATTERN.matcher(name).matches();
	}

	/** Job triggered on changes on the underlying EMF model. */
	private final UIJob fUpdateUIJob = new UIJob("Update variables") {

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			fTreeViewer.refresh();
			return Status.OK_STATUS;
		}
	};

	private TreeViewer fTreeViewer;

	public VariablesPage(final FormEditor editor, final String id, final String title) {
		super(editor, id, title);
	}

	@Override
	protected void createFormContent(final IManagedForm managedForm) {
		super.createFormContent(managedForm);

		managedForm.getForm().getBody().setLayout(new GridLayout(1, false));

		final Label lblDefineVariablesThat = new Label(managedForm.getForm().getBody(), SWT.NONE);
		managedForm.getToolkit().adapt(lblDefineVariablesThat, true, true);
		lblDefineVariablesThat.setText("Define variables that will be visible in your scripts.");

		final Composite composite = new Composite(managedForm.getForm().getBody(), SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 4));
		managedForm.getToolkit().adapt(composite);
		managedForm.getToolkit().paintBordersFor(composite);

		final TreeColumnLayout tcl_composite = new TreeColumnLayout();
		composite.setLayout(tcl_composite);

		final Tree tree = new Tree(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		fTreeViewer = new TreeViewer(tree);
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		managedForm.getToolkit().paintBordersFor(tree);

		fTreeViewer.setContentProvider(new VariablesTreeContentProvider());

		fTreeViewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(final Viewer viewer, final Object e1, final Object e2) {
				if ((e1 instanceof IVariable) && (e2 instanceof IVariable))
					return (((IVariable) e1).getName()).compareToIgnoreCase(((IVariable) e2).getName());

				return super.compare(viewer, e1, e2);
			}

			@Override
			public int category(Object element) {
				return (element instanceof IPath) ? 1 : 2;
			}
		});

		final TreeViewerColumn treeViewerColumn = new TreeViewerColumn(fTreeViewer, SWT.NONE);
		final TreeColumn tblclmnVariable = treeViewerColumn.getColumn();
		tcl_composite.setColumnData(tblclmnVariable, new ColumnWeightData(2, ColumnWeightData.MINIMUM_WIDTH, true));
		tblclmnVariable.setText("Variable");
		treeViewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof IVariable)
					return ((IVariable) element).getName();

				if (element instanceof IPath)
					return ((IPath) element).lastSegment();

				return super.getText(element);
			}

			@Override
			public Image getImage(final Object element) {
				if (element instanceof IVariable)
					return DebugUITools.getImage(IDebugUIConstants.IMG_VIEW_VARIABLES);

				else if (element instanceof IPath)
					return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);

				return super.getImage(element);
			}
		});

		treeViewerColumn.setEditingSupport(new EditingSupport(fTreeViewer) {

			@Override
			protected void setValue(Object element, final Object value) {
				if (element instanceof IVariable) {
					final IPath newPath = new Path(value.toString());

					if (newPath.segmentCount() == 1) {
						// simple name provided
						final String oldName = ((IVariable) element).getName();
						final String newName = value.toString();
						if (!oldName.equals(newName)) {
							// check for update
							if ((isValidNamePattern(newName)) && (isUniqueName(newName))) {
								final IPath newFullName = ((IVariable) element).getPath().append(newName);
								final Command command = SetCommand.create(getEditingDomain(), element, IDefinitionPackage.Literals.VARIABLE__FULL_NAME,
										newFullName);

								command.execute();
								fTreeViewer.update(element, null);
							}
						}

					} else {
						// path provided
						final String newName = newPath.lastSegment();
						if ((isValidNamePattern(newName)) && ((isUniqueName(newName)) || (((IVariable) element).getName().equals(newName)))) {
							final IPath newFullName = (newPath.isAbsolute()) ? newPath : ((IVariable) element).getPath().append(newPath);
							final Command command = SetCommand.create(getEditingDomain(), element, IDefinitionPackage.Literals.VARIABLE__FULL_NAME,
									newFullName);

							command.execute();
							fTreeViewer.refresh();
						}
					}

				} else if (element instanceof IPath) {
					final IPath newPath = new Path(value.toString());
					if (!element.equals(newPath)) {
						final IPath replacement = newPath.isAbsolute() ? newPath : ((IPath) element).removeLastSegments(1).append(newPath);

						final CompoundCommand compoundCommand = new CompoundCommand();
						for (final IVariable variable : getTestSuitDefinition().getVariables()) {
							if (((IPath) element).isPrefixOf(variable.getFullName())) {
								final IPath updatedName = replacement.append(variable.getFullName().removeFirstSegments(((IPath) element).segmentCount()));
								final Command command = SetCommand.create(getEditingDomain(), variable, IDefinitionPackage.Literals.VARIABLE__FULL_NAME,
										updatedName);
								compoundCommand.append(command);
							}
						}

						compoundCommand.execute();
						fTreeViewer.refresh();
					}
				}
			}

			@Override
			protected Object getValue(final Object element) {
				if (element instanceof IVariable)
					return ((IVariable) element).getName();

				else if (element instanceof IPath)
					return ((IPath) element).lastSegment();

				return "";
			}

			@Override
			protected CellEditor getCellEditor(final Object element) {
				return new TextCellEditor(fTreeViewer.getTree());
			}

			@Override
			protected boolean canEdit(final Object element) {
				return true;
			}
		});

		final TreeViewerColumn treeViewerColumn_1 = new TreeViewerColumn(fTreeViewer, SWT.NONE);
		final TreeColumn tblclmnContent = treeViewerColumn_1.getColumn();
		tcl_composite.setColumnData(tblclmnContent, new ColumnWeightData(2, ColumnWeightData.MINIMUM_WIDTH, true));
		tblclmnContent.setText("Content");
		treeViewerColumn_1.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof IVariable)
					return ((IVariable) element).getContent();

				if (element instanceof IPath)
					return "";

				return super.getText(element);
			}
		});

		treeViewerColumn_1.setEditingSupport(new EditingSupport(fTreeViewer) {
			@Override
			protected void setValue(final Object element, final Object value) {
				if (element instanceof IVariable) {
					final String oldContent = ((IVariable) element).getContent();
					final String newContent = value.toString();
					if (!oldContent.equals(newContent)) {
						final Command command = SetCommand.create(getEditingDomain(), element, IDefinitionPackage.Literals.VARIABLE__CONTENT, newContent);
						command.execute();

						fTreeViewer.update(element, null);
					}
				}
			}

			@Override
			protected Object getValue(final Object element) {
				if (element instanceof IVariable)
					return ((IVariable) element).getContent();

				return "";
			}

			@Override
			protected CellEditor getCellEditor(final Object element) {
				return new TextCellEditor(fTreeViewer.getTree());
			}

			@Override
			protected boolean canEdit(final Object element) {
				return (element instanceof IVariable);
			}
		});

		final TreeViewerColumn treeViewerColumn_3 = new TreeViewerColumn(fTreeViewer, SWT.NONE);
		final TreeColumn tblclmnDescription = treeViewerColumn_3.getColumn();
		tcl_composite.setColumnData(tblclmnDescription, new ColumnWeightData(3, ColumnWeightData.MINIMUM_WIDTH, true));
		tblclmnDescription.setText("Description");
		treeViewerColumn_3.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof IVariable)
					return ((IVariable) element).getDescription();

				if (element instanceof IPath)
					return "";

				return super.getText(element);
			}
		});
		treeViewerColumn_3.setEditingSupport(new EditingSupport(fTreeViewer) {

			@Override
			protected void setValue(final Object element, final Object value) {
				if (element instanceof IVariable) {
					final String oldDescription = ((IVariable) element).getDescription();
					final String newDescription = value.toString();
					if (!oldDescription.equals(newDescription)) {
						final Command command = SetCommand.create(getEditingDomain(), element, IDefinitionPackage.Literals.VARIABLE__DESCRIPTION,
								newDescription);
						command.execute();

						fTreeViewer.update(element, null);
					}
				}
			}

			@Override
			protected Object getValue(final Object element) {
				if (element instanceof IVariable)
					return ((IVariable) element).getDescription();

				return "";
			}

			@Override
			protected CellEditor getCellEditor(final Object element) {
				return new TextCellEditor(fTreeViewer.getTree());
			}

			@Override
			protected boolean canEdit(final Object element) {
				return (element instanceof IVariable);
			}
		});

		initializeDnD();

		final MenuManager contextMenu = new MenuManager("", VARIABLES_EDITOR_ID);
		final Menu menu = contextMenu.createContextMenu(fTreeViewer.getTree());
		contextMenu.setRemoveAllWhenShown(true);
		fTreeViewer.getTree().setMenu(menu);
		getEditorSite().registerContextMenu(VARIABLES_EDITOR_ID, contextMenu, fTreeViewer, false);

		populateContent();

		// adapter triggering when variables are added or removed (eg from context menu commands)
		getTestSuitDefinition().eAdapters().add(new EContentAdapter() {
			@Override
			public void notifyChanged(Notification notification) {
				super.notifyChanged(notification);

				fUpdateUIJob.schedule(100);
			}

			@Override
			public boolean isAdapterForType(Object type) {
				return (type instanceof ITestSuite);
			}
		});

		getSite().setSelectionProvider(fTreeViewer);
	}

	@Override
	protected void populateContent() {
		fTreeViewer.setInput(getTestSuitDefinition());
		fTreeViewer.refresh();
		fTreeViewer.expandAll();
	}

	@Override
	protected String getPageTitle() {
		return "Variables";
	}

	/**
	 * Provides drag and drop functionality for the tree structure.
	 */
	private void initializeDnD() {
		final int operations = DND.DROP_MOVE;
		final Transfer[] transferTypes = new Transfer[] { LocalSelectionTransfer.getTransfer() };

		fTreeViewer.addDragSupport(operations, transferTypes, new DragSourceAdapter() {
			@Override
			public void dragFinished(DragSourceEvent event) {
				LocalSelectionTransfer.getTransfer().setSelection(null);
			}

			@Override
			public void dragStart(DragSourceEvent event) {
				LocalSelectionTransfer.getTransfer().setSelection(fTreeViewer.getSelection());
			}
		});

		final ViewerDropAdapter dropSupport = new ViewerDropAdapter(fTreeViewer) {

			@Override
			public boolean validateDrop(Object target, int operation, TransferData transferType) {
				return LocalSelectionTransfer.getTransfer().isSupportedType(transferType);
			}

			@Override
			public boolean performDrop(Object data) {
				Object target = getCurrentTarget();

				if (target == null)
					target = Path.ROOT;

				if ((target instanceof IPath) && (data instanceof IStructuredSelection)) {
					final CompoundCommand compoundCommand = new CompoundCommand();

					for (final Object element : ((IStructuredSelection) data).toList()) {
						if (element instanceof IVariable) {
							final IPath newFullName = ((IPath) target).append(((IVariable) element).getName());
							final Command command = SetCommand.create(getEditingDomain(), element, IDefinitionPackage.Literals.VARIABLE__FULL_NAME,
									newFullName);

							compoundCommand.append(command);

						} else if (element instanceof IPath) {
							for (final IVariable variable : getTestSuitDefinition().getVariables()) {
								if (((IPath) element).isPrefixOf(variable.getFullName())) {
									final IPath updatedName = ((IPath) target).append(variable.getName());
									final Command command = SetCommand.create(getEditingDomain(), variable, IDefinitionPackage.Literals.VARIABLE__FULL_NAME,
											updatedName);
									compoundCommand.append(command);
								}
							}
						}
					}

					compoundCommand.execute();
					fTreeViewer.refresh();

					return true;
				}

				return false;
			}
		};

		fTreeViewer.addDropSupport(operations, transferTypes, dropSupport);
	}

	/**
	 * Verify that a variables name is unique.
	 *
	 * @param name
	 *            name to be used
	 * @return <code>true</code> when name is unique (= currently not used)
	 */
	public boolean isUniqueName(final String name) {
		for (final IVariable variable : getTestSuitDefinition().getVariables()) {
			if (name.equals(variable.getName()))
				return false;
		}

		return true;
	}

	public void addPath(IPath path) {
		((VariablesTreeContentProvider) fTreeViewer.getContentProvider()).addPath(path);
		fTreeViewer.refresh();
	}

	public void removePath(IPath path) {
		((VariablesTreeContentProvider) fTreeViewer.getContentProvider()).removePath(path);
		fTreeViewer.refresh();
	}

	public boolean containsPath(IPath path) {
		return ((VariablesTreeContentProvider) fTreeViewer.getContentProvider()).containsPath(path);
	}

	@Override
	public Image getTitleImage() {
		return DebugUITools.getImage(IDebugUIConstants.IMG_VIEW_VARIABLES);
	}

	@Override
	protected Image getDefaultImage() {
		return DebugUITools.getImage(IDebugUIConstants.IMG_VIEW_VARIABLES);
	}
}