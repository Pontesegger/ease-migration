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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.ease.debugging.model.EaseDebugValue;
import org.eclipse.ease.debugging.model.EaseDebugVariable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ContentLabelProviderTest {

	@Test
	@DisplayName("getText() returns variable content")
	public void getText_returns_variable_content() {
		final ContentLabelProvider labelProvider = new ContentLabelProvider();

		final EaseDebugValue value = mock(EaseDebugValue.class);
		when(value.getValueString()).thenReturn("myContent");

		final EaseDebugVariable variable = mock(EaseDebugVariable.class);
		when(variable.getValue()).thenReturn(value);

		assertEquals("myContent", labelProvider.getText(variable));
	}
}
