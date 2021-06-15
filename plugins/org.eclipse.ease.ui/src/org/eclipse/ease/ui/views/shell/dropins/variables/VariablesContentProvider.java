/*******************************************************************************
 * Copyright (c) 2021 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License_Identifier: EPL-2.0
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.views.shell.dropins.variables;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.ease.IReplEngine;
import org.eclipse.ease.debugging.model.EaseDebugVariable;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class VariablesContentProvider implements ITreeContentProvider {

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		// older eclipse versions do not have a default implementation
	}

	@Override
	public void dispose() {
		// older eclipse versions do not have a default implementation
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

		final Collection<EaseDebugVariable> variables = new HashSet<>();

		if (inputElement instanceof IReplEngine) {
			// get variables
			variables.addAll(((IReplEngine) inputElement).getDefinedVariables());

			// add last execution result
			variables.add(((IReplEngine) inputElement).getLastExecutionResult());
		}

		return variables.toArray();
	}

	@Override
	public Object[] getChildren(final Object parentElement) {
		if (parentElement instanceof EaseDebugVariable)
			return ((EaseDebugVariable) parentElement).getValue().getVariables();

		return new Object[0];
	}
}
