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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.eclipse.ease.debugging.model.EaseDebugLastExecutionResult;
import org.eclipse.ease.debugging.model.EaseDebugVariable;
import org.eclipse.jface.viewers.ViewerComparator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class VariablesComparatorTest {

	@Test
	@DisplayName("category(EaseDebugLastExecutionResult) < category(EaseDebugVariable)")
	public void lastExecutionResult_comes_before_debugVariable() {
		final ViewerComparator comparator = new VariablesComparator();

		final EaseDebugLastExecutionResult lastExecutionResult = mock(EaseDebugLastExecutionResult.class);
		final EaseDebugVariable variable = mock(EaseDebugVariable.class);

		assertTrue(comparator.category(lastExecutionResult) < comparator.category(variable));
	}
}
