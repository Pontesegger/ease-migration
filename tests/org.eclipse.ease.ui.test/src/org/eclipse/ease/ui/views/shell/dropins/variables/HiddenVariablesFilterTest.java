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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.ease.debugging.model.EaseDebugVariable;
import org.eclipse.ease.modules.EnvironmentModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class HiddenVariablesFilterTest {

	@Test
	@DisplayName("select() accepts anything != EaseDebugVariable")
	public void select_accepts_unknown_classes() {
		final HiddenVariablesFilter filter = new HiddenVariablesFilter();

		assertTrue(filter.select(null, null, "test"));
	}

	@Test
	@DisplayName("select() accepts script variables")
	public void select_accepts_script_variables() {
		final EaseDebugVariable variable = mock(EaseDebugVariable.class);
		when(variable.getName()).thenReturn("foo");

		final HiddenVariablesFilter filter = new HiddenVariablesFilter();

		assertTrue(filter.select(null, null, variable));
	}

	@Test
	@DisplayName("select() declines hidden script variables")
	public void select_declines_hidden_script_variables() {
		final EaseDebugVariable variable = mock(EaseDebugVariable.class);
		when(variable.getName()).thenReturn(EnvironmentModule.EASE_CODE_PREFIX + "foo");

		final HiddenVariablesFilter filter = new HiddenVariablesFilter();

		assertFalse(filter.select(null, null, variable));
	}
}
