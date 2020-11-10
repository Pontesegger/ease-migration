/*******************************************************************************
 * Copyright (c) 2020 Christian Pontesegger and others.
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
package org.eclipse.ease.ui.dnd;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.debugging.model.EaseDebugVariable;

public class VariablesDropHandler implements IShellDropHandler {

	@Override
	public boolean accepts(final IScriptEngine scriptEngine, final Object element) {
		return (element instanceof EaseDebugVariable);
	}

	@Override
	public void performDrop(final IScriptEngine scriptEngine, final Object element) {
		scriptEngine.executeAsync(((EaseDebugVariable) element).getName());
	}
}
