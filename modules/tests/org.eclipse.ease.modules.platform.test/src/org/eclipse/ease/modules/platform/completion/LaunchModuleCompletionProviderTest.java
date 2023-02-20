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

package org.eclipse.ease.modules.platform.completion;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.modules.platform.debug.LaunchModule;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.ui.completion.BasicContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class LaunchModuleCompletionProviderTest {
	@ParameterizedTest(name = "for ''{0}''")
	@DisplayName("isActive() = true")
	@ValueSource(strings = { "launch(\"", "launchUI(\"", "getLaunchConfiguration(\"", "launch(1,\"", "launchUI(1,\"" })
	public void isActive_equals_true(String input) {
		assertTrue(new LaunchModuleCompletionProvider().isActive(createContext(input)));
	}

	@ParameterizedTest(name = "for ''{0}''")
	@DisplayName("isActive() = false")
	@ValueSource(strings = { "", "getLaunchConfiguration(1,\"" })
	public void isActive_equals_false(String input) {
		assertFalse(new LaunchModuleCompletionProvider().isActive(createContext(input)));
	}

	private ICompletionContext createContext(String input) {
		return new BasicContext(createEngine(), input, input.length());
	}

	private IScriptEngine createEngine() {
		final EngineDescription engineDescription = mock(EngineDescription.class);
		when(engineDescription.getSupportedScriptTypes()).thenReturn(Arrays.asList(new ScriptType(null)));

		final Map<String, Object> variables = new HashMap<>();
		variables.put(IEnvironment.MODULE_PREFIX + "Launch", new LaunchModule());

		final IScriptEngine engine = mock(IScriptEngine.class);
		when(engine.getDescription()).thenReturn(engineDescription);
		when(engine.getVariables()).thenReturn(variables);

		return engine;
	}
}
