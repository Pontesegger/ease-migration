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

package org.eclipse.ease.ui.dnd;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.ease.ICodeFactory;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.ScriptType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ResourceDropHandlerTest {

	private IScriptEngine fScriptEngine;

	@BeforeEach
	public void beforeEach() {
		final ICodeFactory codeFactory = setupMockedCodeFactory();
		fScriptEngine = setupMockedScriptEngine(codeFactory);
	}

	@Test
	@DisplayName("accepts() = false for String input")
	public void accepts_returns_false_for_String_input() {
		assertFalse(new ResourceDropHandler().accepts(fScriptEngine, "test"));
	}

	@Test
	@DisplayName("performDrop() includes File instance")
	public void performDrop_includes_File_instance() {

		new ResourceDropHandler().performDrop(fScriptEngine, mock(File.class));

		verify(fScriptEngine).execute(any());
	}

	@Test
	@DisplayName("performDrop() includes IFile instance")
	public void performDrop_includes_IFile_instance() {
		final IFile file = mock(IFile.class);
		when(file.getFullPath()).thenReturn(new Path("myFile.txt"));
		new ResourceDropHandler().performDrop(fScriptEngine, file);

		verify(fScriptEngine).execute(any());
	}

	@Test
	@DisplayName("performDrop() includes URI instance")
	public void performDrop_includes_URI_instance() throws URISyntaxException {
		new ResourceDropHandler().performDrop(fScriptEngine, new URI("http://eclipse.org/test.js"));

		verify(fScriptEngine).execute(any());
	}

	private ICodeFactory setupMockedCodeFactory() {
		final ICodeFactory codeFactory = mock(ICodeFactory.class);
		when(codeFactory.createFunctionCall(any(), any())).thenReturn("include");

		return codeFactory;
	}

	private IScriptEngine setupMockedScriptEngine(ICodeFactory codeFactory) {
		final ScriptType scriptType = mock(ScriptType.class);
		when(scriptType.getCodeFactory()).thenReturn(codeFactory);

		final EngineDescription engineDescription = mock(EngineDescription.class);
		when(engineDescription.getSupportedScriptTypes()).thenReturn(List.of(scriptType));

		final IScriptEngine scriptEngine = mock(IScriptEngine.class);
		when(scriptEngine.getDescription()).thenReturn(engineDescription);

		return scriptEngine;
	}
}
