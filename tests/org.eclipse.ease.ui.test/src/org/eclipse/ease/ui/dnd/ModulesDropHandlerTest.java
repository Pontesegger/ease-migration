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

package org.eclipse.ease.ui.dnd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.ease.ICodeFactory;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.ScriptResult;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.ScriptService;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.ui.modules.ui.ModulesTools.ModuleEntry;
import org.eclipse.ease.ui.test.RootModule;
import org.eclipse.swt.dnd.DND;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

public class ModulesDropHandlerTest {

	private static final String LOAD_MODULE_SCRIPT = "loadModule";

	@Test
	@DisplayName("accepts() == true when element instanceOf ModuleDefinition")
	public void accepts_true_for_ModuleDefinition() {
		final ModuleDefinition mock = mock(ModuleDefinition.class);
		assertTrue(new ModulesDropHandler().accepts(null, mock));
	}

	@Test
	@DisplayName("accepts() == true when element instanceOf ModuleEntry")
	public void accepts_true_for_ModuleEntry() {
		final ModuleEntry<?> mock = mock(ModuleEntry.class);
		assertTrue(new ModulesDropHandler().accepts(null, mock));
	}

	@Test
	@DisplayName("performDrop(ModuleDefinition) loads module")
	public void performDrop_of_ModuleDefinition_loads_module() {
		final ICodeFactory codeFactory = setupMockedCodeFactory();
		final IScriptEngine scriptEngine = setupMockedScriptEngine(codeFactory);

		final ModuleDefinition moduleDefinition = ScriptService.getInstance().getModuleDefinition(RootModule.MODULE_ID);
		new ModulesDropHandler().performDrop(scriptEngine, moduleDefinition, 0);

		verify(codeFactory).createFunctionCall(any(), ArgumentMatchers.eq(RootModule.MODULE_ID), any());
		verify(scriptEngine).execute(ArgumentMatchers.eq(LOAD_MODULE_SCRIPT));
	}

	@Test
	@DisplayName("performDrop(ModuleEntry) loads module")
	public void performDrop_of_ModuleEntry_loads_module() {
		final ICodeFactory codeFactory = setupMockedCodeFactory();
		final IScriptEngine scriptEngine = setupMockedScriptEngine(codeFactory);

		final ModuleEntry<?> moduleEntry = mock(ModuleEntry.class);
		when(moduleEntry.getModuleDefinition()).thenReturn(ScriptService.getInstance().getModuleDefinition(RootModule.MODULE_ID));

		new ModulesDropHandler().performDrop(scriptEngine, moduleEntry, 0);

		verify(codeFactory).createFunctionCall(any(), ArgumentMatchers.eq(RootModule.MODULE_ID), any());
		verify(scriptEngine).execute(ArgumentMatchers.eq(LOAD_MODULE_SCRIPT));
	}

	@Test
	@DisplayName("performDrop(Method) executes with default parameters on DROP_COPY")
	public void performDrop_of_Method_executes_with_default_parameters_on_DROP_COPY() throws NoSuchMethodException {
		final ICodeFactory codeFactory = setupMockedCodeFactory();
		final IScriptEngine scriptEngine = setupMockedScriptEngine(codeFactory);

		final Method method = RootModule.class.getMethod("testWithOptionalParameters", String.class);

		final ModuleEntry<Method> moduleEntry = mock(ModuleEntry.class);
		when(moduleEntry.getModuleDefinition()).thenReturn(ScriptService.getInstance().getModuleDefinition(RootModule.MODULE_ID));
		when(moduleEntry.getEntry()).thenReturn(method);

		new ModulesDropHandler().performDrop(scriptEngine, moduleEntry, DND.DROP_COPY);

		final ArgumentCaptor<Method> methodCaptor = ArgumentCaptor.forClass(Method.class);

		verify(codeFactory, times(2)).createFunctionCall(methodCaptor.capture(), any());
		verify(scriptEngine, times(2)).execute(any());

		assertEquals(method, methodCaptor.getAllValues().get(1));
	}

	private ICodeFactory setupMockedCodeFactory() {
		final ICodeFactory codeFactory = mock(ICodeFactory.class);
		when(codeFactory.createFunctionCall(any(), any(), any())).thenReturn(LOAD_MODULE_SCRIPT);

		return codeFactory;
	}

	private IScriptEngine setupMockedScriptEngine(ICodeFactory codeFactory) {
		final ScriptType scriptType = mock(ScriptType.class);
		when(scriptType.getCodeFactory()).thenReturn(codeFactory);

		final EngineDescription engineDescription = mock(EngineDescription.class);
		when(engineDescription.getSupportedScriptTypes()).thenReturn(List.of(scriptType));

		final IScriptEngine scriptEngine = mock(IScriptEngine.class);
		when(scriptEngine.getDescription()).thenReturn(engineDescription);
		final ScriptResult executionResult = new ScriptResult();
		executionResult.setResult(LOAD_MODULE_SCRIPT);
		when(scriptEngine.execute(any())).thenReturn(executionResult);

		return scriptEngine;
	}
}
