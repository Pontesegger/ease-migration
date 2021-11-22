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

package org.eclipse.ease.modules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.ease.ICodeFactory;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Script;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.ScriptType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class BootStrapperTest {

	@Test
	@DisplayName("createEngine() creates wrapper code for EnvironmentModule")
	public void createEngine_creates_wrapper_code_for_EnvironmentModule() throws Exception {

		final ICodeFactory codeFactory = mock(ICodeFactory.class);
		when(codeFactory.classInstantiation(eq(EnvironmentModule.class), any())).thenReturn("EnvironmentModule");

		final ScriptType scriptType = mock(ScriptType.class);
		when(scriptType.getCodeFactory()).thenReturn(codeFactory);

		final EngineDescription description = mock(EngineDescription.class);
		when(description.getSupportedScriptTypes()).thenReturn(List.of(scriptType));

		final IScriptEngine engine = mock(IScriptEngine.class);
		when(engine.getDescription()).thenReturn(description);

		new BootStrapper().createEngine(engine);

		final ArgumentCaptor<Script> scriptCaptor = ArgumentCaptor.forClass(Script.class);
		verify(engine).execute(scriptCaptor.capture());
		assertEquals(String.format("EnvironmentModule.bootstrap();%n"), scriptCaptor.getValue().getCode());
	}
}
