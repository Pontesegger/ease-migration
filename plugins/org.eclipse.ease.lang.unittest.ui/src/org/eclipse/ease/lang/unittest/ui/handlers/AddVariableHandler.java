/*******************************************************************************
 * Copyright (c) 2016 Madalina Hodorog and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
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
import org.eclipse.core.runtime.Path;
import org.eclipse.ease.lang.unittest.definition.IDefinitionFactory;
import org.eclipse.ease.lang.unittest.definition.IDefinitionPackage;
import org.eclipse.ease.lang.unittest.definition.IVariable;
import org.eclipse.ease.lang.unittest.ui.editor.TestSuiteEditor;
import org.eclipse.ease.lang.unittest.ui.editor.VariablesPage;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.handlers.HandlerUtil;

public class AddVariableHandler extends AbstractHandler implements IHandler {

	private static final String DEFAULT_VARIABLE_NAME = "myVariable";

	private static boolean isUniqueVariable(IPath path, VariablesPage variablesPage) {

		for (final IVariable variable : variablesPage.getEditor().getTestSuite().getVariables()) {
			if (path.equals(variable.getFullName()))
				return false;
		}

		return true;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		final IEditorPart editorWindow = HandlerUtil.getActiveEditor(event);
		if (editorWindow instanceof TestSuiteEditor) {
			final IFormPage variablesPage = ((TestSuiteEditor) editorWindow).getActivePageInstance();
			if (variablesPage instanceof VariablesPage) {
				final ISelection selection = HandlerUtil.getActiveMenuSelection(event);

				IPath rootPath = null;
				if (selection.isEmpty()) {
					// add new root group
					rootPath = Path.ROOT;

				} else {
					final Object firstElement = ((IStructuredSelection) selection).getFirstElement();
					if (firstElement instanceof IVariable)
						rootPath = ((IVariable) firstElement).getPath();
					else if (firstElement instanceof IPath)
						rootPath = (IPath) firstElement;
				}

				if (rootPath != null) {
					IPath candidate = rootPath.append(DEFAULT_VARIABLE_NAME);
					int index = 1;
					while (!isUniqueVariable(candidate, (VariablesPage) variablesPage))
						candidate = rootPath.append(DEFAULT_VARIABLE_NAME + index++);

					final IVariable variable = IDefinitionFactory.eINSTANCE.createVariable();
					variable.setFullName(candidate.makeAbsolute());

					final Command command = AddCommand.create(((TestSuiteEditor) editorWindow).getEditingDomain(),
							((TestSuiteEditor) editorWindow).getTestSuite(), IDefinitionPackage.Literals.TEST_SUITE_DEFINITION__VARIABLES, variable);

					((TestSuiteEditor) editorWindow).executeCommand(command);
				}
			}
		}

		return null;
	}
}