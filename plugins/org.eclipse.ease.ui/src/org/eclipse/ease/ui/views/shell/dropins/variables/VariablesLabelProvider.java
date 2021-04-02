/*******************************************************************************
 * Copyright (c) 2021 Christian Pontesegger and others.
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

import org.eclipse.ease.debugging.model.EaseDebugVariable;
import org.eclipse.ease.ui.debugging.model.AbstractEaseDebugModelPresentation;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;

public class VariablesLabelProvider extends ColumnLabelProvider {

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
}
