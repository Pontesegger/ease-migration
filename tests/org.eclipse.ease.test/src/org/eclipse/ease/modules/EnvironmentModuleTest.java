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

import org.eclipse.core.runtime.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class EnvironmentModuleTest {

	@Test
	@DisplayName("getModuleDefinition(env) returns environment definition")
	public void getModuleDefinition_returns_environment_definition() {
		final EnvironmentModule module = new EnvironmentModule();

		assertEquals(new Path(EnvironmentModule.MODULE_NAME).lastSegment(), module.getModuleDefinition(module).getName());
	}
}
