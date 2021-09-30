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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
import org.eclipse.ease.IExecutionListener;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Script;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.python.pydev.core.IPythonPathNature;
import org.python.pydev.plugin.nature.PythonNature;

public class PyDevPathImporterTest {

	@Test
	@DisplayName("createEngine() registers engine execution listener")
	public void createEngine_registers_engine_execution_listener() {
		final IScriptEngine engine = mock(IScriptEngine.class);

		final PyDevPathImporter importer = new PyDevPathImporter();
		importer.createEngine(engine);

		verify(engine, times(1)).addExecutionListener(any());
	}

	@Test
	@DisplayName("notify() registers project paths")
	public void notify_registers_project_paths() throws ExecutionException, CoreException {
		final IScriptEngine engine = mock(IScriptEngine.class);
		final Script script = mock(Script.class);

		final IResource resource = mock(IResource.class);
		final IProject project = mock(IProject.class);
		final PythonNature pythonNature = mock(PythonNature.class);
		final IPythonPathNature pythonPathNature = mock(IPythonPathNature.class);

		when(script.getFile()).thenReturn(resource);
		when(resource.getProject()).thenReturn(project);
		when(project.isOpen()).thenReturn(true);
		when(project.getNature(any())).thenReturn(pythonNature);
		when(pythonNature.getPythonPathNature()).thenReturn(pythonPathNature);
		when(pythonPathNature.getOnlyProjectPythonPathStr(true)).thenReturn("one|two");

		final ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

		final PyDevPathImporter importer = new PyDevPathImporter();
		importer.notify(engine, script, IExecutionListener.SCRIPT_START);

		verify(engine).inject(codeCaptor.capture(), eq(false));

		assertTrue(codeCaptor.getValue().contains("sys.path.append('one')"));
		assertTrue(codeCaptor.getValue().contains("sys.path.append('two')"));
	}

	@Test
	@DisplayName("notify() does not throw for non-PyDev project")
	public void notify_does_not_throw_for_non_PyDev_project() throws ExecutionException, CoreException {
		final IScriptEngine engine = mock(IScriptEngine.class);
		final Script script = mock(Script.class);

		final IResource resource = mock(IResource.class);
		final IProject project = mock(IProject.class);

		when(script.getFile()).thenReturn(resource);
		when(resource.getProject()).thenReturn(project);
		when(project.isOpen()).thenReturn(true);
		when(project.getNature(any())).thenReturn(null);

		final PyDevPathImporter importer = new PyDevPathImporter();
		assertDoesNotThrow(() -> importer.notify(engine, script, IExecutionListener.SCRIPT_START));
	}

	@Test
	@DisplayName("notify() does not throw when PyDev plugins are not availablew")
	public void notify_does_not_throwwhen_PyDev_plugins_are_not_available() throws ExecutionException, CoreException {
		final IScriptEngine engine = mock(IScriptEngine.class);
		final Script script = mock(Script.class);

		final IResource resource = mock(IResource.class);
		final IProject project = mock(IProject.class);

		when(script.getFile()).thenReturn(resource);
		when(resource.getProject()).thenReturn(project);
		when(project.isOpen()).thenReturn(true);
		when(project.getNature(any())).thenThrow(new NoClassDefFoundError());

		final PyDevPathImporter importer = new PyDevPathImporter();
		assertDoesNotThrow(() -> importer.notify(engine, script, IExecutionListener.SCRIPT_START));
	}
}
