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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DropinToolsTest {

	@Test
	@DisplayName("getAvailableDropins() != null")
	public void getAvailableDropins_is_not_null() {
		assertNotNull(DropinTools.getAvailableDropins());
	}

	@Test
	@Disabled("not able to run headless -> No more handles [gtk_init_check() failed]")
	@DisplayName("getAvailableDropins() contains DummyDropin")
	public void getAvailableDropins_contains_DummyDropin() {
		assertEquals(1, DropinTools.getAvailableDropins().stream().filter(d -> d instanceof DummyDropin).count());
	}

	@Test
	@DisplayName("getAvailableDropins() does not contain InvalidDropin")
	public void getAvailableDropins_does_not_contain_InvalidDropin() {
		assertEquals(0, DropinTools.getAvailableDropins().stream().filter(d -> d instanceof InvalidDropin).count());
	}
}
