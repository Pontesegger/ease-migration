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

package org.eclipse.ease.modules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ModuleDefinitionTest {

	@Test
	@DisplayName("forInstance(module) returns definition")
	public void forInstance_returns_definition() {
		final ModuleDefinition definition = ModuleDefinition.forInstance(new EnvironmentModule());

		assertNotNull(definition);
		assertEquals("Environment", definition.getName());
	}

	@Test
	@DisplayName("forInstance(unknown) returns null")
	public void forInstance_returns_null() {
		assertNull(ModuleDefinition.forInstance("foo"));
	}
}
