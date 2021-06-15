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

import org.eclipse.ease.debugging.model.EaseDebugLastExecutionResult;
import org.eclipse.jface.viewers.ViewerComparator;

public class VariablesComparator extends ViewerComparator {

	@Override
	public int category(Object element) {
		return (element instanceof EaseDebugLastExecutionResult) ? 1 : 2;
	}
}
