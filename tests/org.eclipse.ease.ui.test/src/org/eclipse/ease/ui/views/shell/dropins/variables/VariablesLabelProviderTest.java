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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.ease.debugging.model.EaseDebugVariable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class VariablesLabelProviderTest {

	@Test
	@DisplayName("getText() returns variable name")
	public void getText_returns_variable_name() {
		final VariablesLabelProvider labelProvider = new VariablesLabelProvider();

		final EaseDebugVariable variable = mock(EaseDebugVariable.class);
		when(variable.getName()).thenReturn("myVar");

		assertEquals("myVar", labelProvider.getText(variable));
	}

	@Disabled("Requires an UI thread to execute")
	@DisplayName("getImage() != null")
	public void getImage_returns_not_null() {
		final VariablesLabelProvider labelProvider = new VariablesLabelProvider();

		final EaseDebugVariable variable = mock(EaseDebugVariable.class);
		when(variable.getType()).thenReturn(EaseDebugVariable.Type.NATIVE);

		assertNotNull(labelProvider.getImage(variable));
	}
}
