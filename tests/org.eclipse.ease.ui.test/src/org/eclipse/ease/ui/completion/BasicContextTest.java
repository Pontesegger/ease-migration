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

package org.eclipse.ease.ui.completion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.modules.EnvironmentModule;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.ui.test.SubModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class BasicContextTest {

	@Test
	@DisplayName("getFilter() is empty for 'java.io.File()'")
	public void getFilter_is_empty() {
		final BasicContext context = createContext("java.io.File()");

		assertEquals("", context.getFilter());
	}

	@Test
	@DisplayName("getFilter() is empty for ''")
	public void getFilter_is_empty_for_empty_input() {
		final BasicContext context = createContext("");

		assertEquals("", context.getFilter());
	}

	@Test
	@DisplayName("getFilter() = 12 for 'java.io.File().exists(12'")
	public void getFilter_contains_parameter_prefix() {
		final BasicContext context = createContext("java.io.File().exists(12");

		assertEquals("12", context.getFilter());
	}

	@Test
	@DisplayName("getFilter() = 123 for 'java.io.File().exists(\"123'")
	public void getFilter_removes_string_literal_start() {
		final BasicContext context = createContext("java.io.File().exists(\"123");

		assertEquals("123", context.getFilter());
	}

	@Test
	@DisplayName("getFilter() = 123 abc for 'java.io.File().exists(\"123 abc'")
	public void getFilter_keeps_spaces_in_string_literal() {
		final BasicContext context = createContext("java.io.File().exists(\"123 abc");

		assertEquals("123 abc", context.getFilter());
	}

	@Test
	@DisplayName("getScriptEngine() returns engine")
	public void getScriptEngine_returns_engine() {
		final IScriptEngine scriptEngine = createEngine();

		assertEquals(scriptEngine, new BasicContext(scriptEngine, "", 0).getScriptEngine());
	}

	@Test
	@DisplayName("getLoadedModules() contains environment on resource")
	public void getLoadedModules_contains_environment_on_resource() {

		final IScriptEngine scriptEngine = createEngine();
		when(scriptEngine.getVariables()).thenReturn(Collections.emptyMap());

		final List<ModuleDefinition> modules = createContext("").getLoadedModules();
		assertEquals(1, modules.size());
		assertEquals(EnvironmentModule.MODULE_NAME, modules.get(0).getPath().toString());
	}

	@Test
	@DisplayName("getLoadedModules() does not load environment twice on resource")
	public void getLoadedModules_does_not_contain_environment_twice_on_resource() {

		final IScriptEngine scriptEngine = createEngine();
		when(scriptEngine.getVariables()).thenReturn(Collections.emptyMap());

		final List<ModuleDefinition> modules = createContext("loadModule(\"/Environment\")\n").getLoadedModules();
		assertEquals(1, modules.size());
		assertEquals(EnvironmentModule.MODULE_NAME, modules.get(0).getPath().toString());
	}

	@Test
	@DisplayName("getLoadedModules() detects loadModule() commands")
	public void getLoadedModules_detects_loadModule_commands() {

		final IScriptEngine scriptEngine = createEngine();
		when(scriptEngine.getVariables()).thenReturn(Collections.emptyMap());

		final List<ModuleDefinition> modules = createContext("loadModule(\"/Testing/Test Sub\")\n").getLoadedModules();
		assertEquals(2, modules.size());
		assertEquals("/Testing/Test Sub", modules.get(0).getPath().toString());
		assertEquals(EnvironmentModule.MODULE_NAME, modules.get(1).getPath().toString());
	}

	@Test
	@DisplayName("getLoadedModules() detects modules from ScriptEngine")
	public void getLoadedModules_detects_modules_from_ScriptEngine() {

		final Map<String, Object> variables = new HashMap<>();
		variables.put(IEnvironment.MODULE_PREFIX + "_one", new SubModule());

		final IScriptEngine scriptEngine = createEngine();
		when(scriptEngine.getVariables()).thenReturn(variables);

		final List<ModuleDefinition> modules = new BasicContext(scriptEngine, "", 0).getLoadedModules();
		assertEquals(1, modules.size());
		assertEquals("/Testing/Test Sub", modules.get(0).getPath().toString());
	}

	@Test
	@DisplayName("isValid() = true")
	public void isValid_equals_true() {
		final BasicContext context = createContext("foo('");
		assertTrue(context.isValid());
	}

	@Test
	@DisplayName("isValid() = false")
	public void isInvalid_equals_true() {
		final BasicContext context = createContext("foo(''");
		assertFalse(context.isValid());
	}

	private IScriptEngine createEngine() {
		final EngineDescription engineDescription = mock(EngineDescription.class);
		when(engineDescription.getSupportedScriptTypes()).thenReturn(Arrays.asList(new ScriptType(null)));

		final IScriptEngine engine = mock(IScriptEngine.class);
		when(engine.getDescription()).thenReturn(engineDescription);

		return engine;
	}

	private BasicContext createContext(String input) {
		return new BasicContext(null, null, input, input.length());
	}
}
