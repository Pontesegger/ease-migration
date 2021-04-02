/*******************************************************************************
 * Copyright (c) 2021 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.views.shell.dropins.variables;

import org.eclipse.ease.debugging.model.EaseDebugVariable;
import org.eclipse.ease.modules.EnvironmentModule;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class HiddenVariablesFilter extends ViewerFilter {
	@Override
	public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
		if (element instanceof EaseDebugVariable)
			return !((EaseDebugVariable) element).getName().startsWith(EnvironmentModule.EASE_CODE_PREFIX);

		return true;
	}
}
