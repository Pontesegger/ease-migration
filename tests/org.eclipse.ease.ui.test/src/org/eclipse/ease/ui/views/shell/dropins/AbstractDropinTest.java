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

package org.eclipse.ease.ui.views.shell.dropins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.eclipse.ease.IReplEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class AbstractDropinTest {

	private AbstractDropin fDropin;

	@BeforeEach
	public void beforeEach() {
		fDropin = mock(AbstractDropin.class, Mockito.CALLS_REAL_METHODS);
	}

	@Test
	@DisplayName("getSelectionProvider() != null")
	public void getSelectionProvider_is_not_null() {
		assertNotNull(fDropin.getSelectionProvider());
	}

	@Test
	@DisplayName("getSelectionProvider().getSelection() is empty")
	public void getSelectionProvider_getSelection_is_empty() {
		assertTrue(fDropin.getSelectionProvider().getSelection().isEmpty());
	}

	@Test
	@DisplayName("getScriptEngine() returns current script engine")
	public void getScriptEngine_returns_current_script_engine() {
		final IReplEngine engine = mock(IReplEngine.class);

		fDropin.setScriptEngine(engine);
		assertEquals(engine, fDropin.getScriptEngine());
	}
}
