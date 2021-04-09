/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.lang.javascript.rhino.debugger.actions;

import org.eclipse.ease.debugging.model.EaseDebugVariable;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.jface.viewers.Viewer;

public class ModuleFilterAction extends ViewFilterAction {

	@Override
	protected String getPreferenceKey() {
		return "org.eclipse.ease.ui.show_modules";
	}

	@Override
	public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
		if (element instanceof EaseDebugVariable) {
			if (((EaseDebugVariable) element).getName().startsWith(IEnvironment.MODULE_PREFIX))
				return getValue();
		}

		return true;
	}
}
