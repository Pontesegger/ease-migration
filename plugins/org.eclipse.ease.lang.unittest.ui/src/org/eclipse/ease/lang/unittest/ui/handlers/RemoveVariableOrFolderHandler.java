/*******************************************************************************
 * Copyright (c) 2016 Madalina Hodorog and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Madalina Hodorog - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.lang.unittest.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ease.lang.unittest.definition.IDefinitionPackage;
import org.eclipse.ease.lang.unittest.definition.IVariable;
import org.eclipse.ease.lang.unittest.ui.editor.TestSuiteEditor;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class RemoveVariableOrFolderHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		final IEditorPart editorWindow = HandlerUtil.getActiveEditor(event);
		if (editorWindow instanceof TestSuiteEditor) {
			final ISelection selection = HandlerUtil.getActiveMenuSelection(event);
			if (selection instanceof IStructuredSelection) {
				final CompoundCommand compoundCommand = new CompoundCommand();

				for (final Object element : ((IStructuredSelection) selection).toList()) {
					if (element instanceof IVariable) {
						final Command command = RemoveCommand.create(((TestSuiteEditor) editorWindow).getEditingDomain(),
								((TestSuiteEditor) editorWindow).getTestSuite(), IDefinitionPackage.Literals.TEST_SUITE_DEFINITION__VARIABLES, element);
						compoundCommand.append(command);

					} else if (element instanceof IPath) {
						for (final IVariable variable : ((TestSuiteEditor) editorWindow).getTestSuite().getVariables()) {
							if (((IPath) element).isPrefixOf(variable.getFullName())) {
								final Command command = RemoveCommand.create(((TestSuiteEditor) editorWindow).getEditingDomain(),
										((TestSuiteEditor) editorWindow).getTestSuite(), IDefinitionPackage.Literals.TEST_SUITE_DEFINITION__VARIABLES,
										variable);
								compoundCommand.append(command);
							}
						}
					}
				}

				((TestSuiteEditor) editorWindow).executeCommand(compoundCommand);
			}
		}

		return null;
	}
}
