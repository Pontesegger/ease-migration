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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.eclipse.ease.IReplEngine;
import org.eclipse.ease.debugging.model.EaseDebugValue;
import org.eclipse.ease.debugging.model.EaseDebugVariable;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class VariablesContentProviderTest {

	@Test
	@DisplayName("hasChildren() == false when element has no child variables")
	public void hasChildren_false_when_no_child_nodes_are_available() {

		final EaseDebugValue value = mock(EaseDebugValue.class);
		when(value.getVariables()).thenReturn(new EaseDebugVariable[0]);

		final EaseDebugVariable variable = mock(EaseDebugVariable.class);
		when(variable.getValue()).thenReturn(value);

		final ITreeContentProvider contentProvider = new VariablesContentProvider();
		assertFalse(contentProvider.hasChildren(variable));
	}

	@Test
	@DisplayName("hasChildren() == true when element has child variables")
	public void hasChildren_true_when_child_nodes_are_available() {

		final EaseDebugValue value = mock(EaseDebugValue.class);
		when(value.getVariables()).thenReturn(new EaseDebugVariable[] { mock(EaseDebugVariable.class) });

		final EaseDebugVariable variable = mock(EaseDebugVariable.class);
		when(variable.getValue()).thenReturn(value);

		final ITreeContentProvider contentProvider = new VariablesContentProvider();
		assertTrue(contentProvider.hasChildren(variable));
	}

	@Test
	@DisplayName("getChildren() is empty when element has no child variables")
	public void getChildren_is_empty_when_no_child_nodes_are_available() {

		final EaseDebugValue value = mock(EaseDebugValue.class);
		when(value.getVariables()).thenReturn(new EaseDebugVariable[0]);

		final EaseDebugVariable variable = mock(EaseDebugVariable.class);
		when(variable.getValue()).thenReturn(value);

		final ITreeContentProvider contentProvider = new VariablesContentProvider();
		assertArrayEquals(new EaseDebugVariable[0], contentProvider.getChildren(variable));
	}

	@Test
	@DisplayName("getChildren() contains nodes when element has child variables")
	public void getChildren_not_empty_when_child_nodes_are_available() {

		final EaseDebugValue value = mock(EaseDebugValue.class);
		when(value.getVariables()).thenReturn(new EaseDebugVariable[] { mock(EaseDebugVariable.class) });

		final EaseDebugVariable variable = mock(EaseDebugVariable.class);
		when(variable.getValue()).thenReturn(value);

		final ITreeContentProvider contentProvider = new VariablesContentProvider();
		assertEquals(1, contentProvider.getChildren(variable).length);
	}

	@Test
	@DisplayName("getElements() contains global scope variables of engine")
	public void getElements_contains_variables_from_engine() {

		final IReplEngine engine = mock(IReplEngine.class);
		when(engine.getDefinedVariables()).thenReturn(Arrays.asList(mock(EaseDebugVariable.class), mock(EaseDebugVariable.class)));
		when(engine.getLastExecutionResult()).thenReturn(mock(EaseDebugVariable.class));

		final ITreeContentProvider contentProvider = new VariablesContentProvider();
		assertEquals(3, contentProvider.getElements(engine).length);
	}
}
