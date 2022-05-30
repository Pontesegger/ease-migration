/*******************************************************************************
 * Copyright (c) 2022 Christian Pontesegger and others.
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

package org.eclipse.ease.debugging.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Arrays;

import org.eclipse.debug.core.model.IDebugElement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class EaseDebugValueTest {

	@Test
	@DisplayName("getVariables() contains public field")
	public void getVariables_contains_public_field() {
		final IDebugElement debugElement = mock(IDebugElement.class);
		final EaseDebugValue value = new EaseDebugValue(debugElement, new Fields());

		final EaseDebugVariable[] variables = value.getVariables();
		assertEquals(1, Arrays.asList(variables).stream().filter(v -> "fPublicField".equals(v.getName())).count());
	}

	@Test
	@DisplayName("getVariables() contains private field")
	public void getVariables_contains_private_field() {
		final IDebugElement debugElement = mock(IDebugElement.class);
		final EaseDebugValue value = new EaseDebugValue(debugElement, new Fields());

		final EaseDebugVariable[] variables = value.getVariables();
		assertEquals(1, Arrays.asList(variables).stream().filter(v -> "fPrivateField".equals(v.getName())).count());
	}

	public static class Fields {
		public String fPublicField = "";
		private final String fPrivateField = "";
	}
}
