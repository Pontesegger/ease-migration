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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.modules.EnvironmentModule;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.ui.views.shell.dropins.variables.VariablesContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ModulesStackContentProviderTest {

	@Test
	@DisplayName("getElements() returns found modules")
	public void getElements_returns_modules() {

		final List<Object> mockedModules = Arrays.asList(new EnvironmentModule());

		final IEnvironment environment = mock(IEnvironment.class);
		when(environment.getModules()).thenReturn(mockedModules);

		final Map<String, Object> variables = new HashMap<>();
		variables.put("env", environment);

		final IScriptEngine engine = mock(IScriptEngine.class);
		when(engine.getVariables()).thenReturn(variables);

		final IStructuredContentProvider contentProvider = new ModulesStackContentProvider();

		assertTrue(contentProvider.getElements(engine)[0] instanceof ModuleDefinition);
		assertEquals("Environment", ((ModuleDefinition) contentProvider.getElements(engine)[0]).getName());
	}

	@Test
	@DisplayName("getElements() returns empty list when no environment exists")
	public void getElements_returns_empty_list_without_environment() {

		final IScriptEngine engine = mock(IScriptEngine.class);
		when(engine.getVariables()).thenReturn(new HashMap<>());

		final ITreeContentProvider contentProvider = new VariablesContentProvider();
		assertEquals(0, contentProvider.getElements(engine).length);
	}
}
