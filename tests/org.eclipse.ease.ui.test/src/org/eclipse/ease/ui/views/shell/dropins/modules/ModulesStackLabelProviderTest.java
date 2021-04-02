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

package org.eclipse.ease.ui.views.shell.dropins.modules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.ease.modules.ModuleDefinition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ModulesStackLabelProviderTest {

	@Test
	@DisplayName("getText(definition) returns definition name")
	public void getText_returns_definition_name() {
		final ModuleDefinition definition = mock(ModuleDefinition.class);
		when(definition.getName()).thenReturn("myModule");

		assertEquals("myModule", new ModulesStackLabelProvider().getText(definition));
	}

	@Test
	@DisplayName("getText(unknownClass) returns full class name name")
	public void getText_returns_full_class_name_name() {

		assertEquals("java.lang.String", new ModulesStackLabelProvider().getText("dummy"));
	}

	@Test
	@DisplayName("getText(null) returns ''")
	public void getText_null_returns_empty_string() {
		assertEquals("", new ModulesStackLabelProvider().getText(null));
	}
}
