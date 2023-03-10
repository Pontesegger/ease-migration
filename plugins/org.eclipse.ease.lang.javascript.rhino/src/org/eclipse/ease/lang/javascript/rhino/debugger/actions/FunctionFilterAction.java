/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
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
package org.eclipse.ease.lang.javascript.rhino.debugger.actions;

import org.eclipse.ease.debugging.model.EaseDebugVariable;
import org.eclipse.jface.viewers.Viewer;
import org.mozilla.javascript.Script;

public class FunctionFilterAction extends ViewFilterAction {

	@Override
	protected String getPreferenceKey() {
		return "org.eclipse.ease.ui.rhino.showFunctions";
	}

	@Override
	public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
		if (element instanceof EaseDebugVariable) {
			final Object value = ((EaseDebugVariable) element).getValue().getValue();
			if (value instanceof Script)
				return getValue();
		}

		return true;
	}
}
