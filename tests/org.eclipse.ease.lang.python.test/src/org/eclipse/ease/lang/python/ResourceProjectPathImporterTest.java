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

package org.eclipse.ease.lang.python;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.ease.IExecutionListener;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Script;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class ResourceProjectPathImporterTest {

	@Test
	@DisplayName("createEngine() registers engine execution listener")
	public void createEngine_registers_engine_execution_listener() {
		final IScriptEngine engine = mock(IScriptEngine.class);

		final ResourceProjectPathImporter importer = new ResourceProjectPathImporter();
		importer.createEngine(engine);

		verify(engine, times(1)).addExecutionListener(any());
	}

	@Test
	@DisplayName("notify() registers project paths")
	public void notify_registers_project_paths() throws ExecutionException, CoreException {
		final IScriptEngine engine = mock(IScriptEngine.class);
		final Script script = setupScript("/one/two");

		final ResourceProjectPathImporter importer = new ResourceProjectPathImporter();
		importer.notify(engine, script, IExecutionListener.SCRIPT_START);

		final ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
		verify(engine).inject(codeCaptor.capture(), eq(false));
		assertTrue(codeCaptor.getValue().contains("sys.path.append('/one/two')"));
	}

	@Test
	@DisplayName("notify() escapes path on windows")
	public void notify_escapes_path_on_windows() throws ExecutionException, CoreException {
		final IScriptEngine engine = mock(IScriptEngine.class);
		final Script script = setupScript("C:\\one");

		final ResourceProjectPathImporter importer = new ResourceProjectPathImporter();
		importer.notify(engine, script, IExecutionListener.SCRIPT_START);

		final ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
		verify(engine).inject(codeCaptor.capture(), eq(false));
		assertTrue(codeCaptor.getValue().contains("sys.path.append('C:\\\\one')"));
	}

	@Test
	@DisplayName("notify() registers project paths for enigne executed file")
	public void notify_registers_project_paths_for_engine_executed_file() throws ExecutionException, CoreException {

		final IResource resource = mock(IResource.class);
		final IProject project = mock(IProject.class);

		when(resource.getProject()).thenReturn(project);
		when(project.getLocation()).thenReturn(new Path("/path/to/file"));

		final IScriptEngine engine = mock(IScriptEngine.class);
		when(engine.getExecutedFile()).thenReturn(resource);

		final Script script = mock(Script.class);

		final ResourceProjectPathImporter importer = new ResourceProjectPathImporter();
		importer.notify(engine, script, IExecutionListener.SCRIPT_START);

		final ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
		verify(engine).inject(codeCaptor.capture(), eq(false));
		assertTrue(codeCaptor.getValue().contains("sys.path.append('/path/to/file')"));
	}

	private Script setupScript(String path) throws CoreException {
		final Script script = mock(Script.class);

		final IResource resource = mock(IResource.class);
		final IProject project = mock(IProject.class);

		when(script.getFile()).thenReturn(resource);
		when(resource.getProject()).thenReturn(project);
		when(project.getLocation()).thenReturn(new Path(path));
		return script;
	}
}
