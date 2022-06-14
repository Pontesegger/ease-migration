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

package org.eclipse.ease.lang.python.py4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.ScriptResult;
import org.eclipse.ease.lang.python.py4j.internal.Py4jScriptEngine;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.ScriptService;
import org.eclipse.ease.testhelper.WorkspaceTestHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class Py4jScriptEngineTest {

	@Test
	@DisplayName("PYTHONPATH contains <Py4J Plugin>/src")
	public void PYTHONPATH_contains_py4j_plugin_src() throws ExecutionException {
		pathTest(Pattern.compile(".*py4j-python.*src"));
	}

	@Test
	@DisplayName("PYTHONPATH contains <ease.lang.python Plugin>/src")
	public void PYTHONPATH_contains_ease_lang_python_plugin_src() throws ExecutionException {
		pathTest(Pattern.compile(".*org.eclipse.ease.lang.python.*src"));
	}

	@Test
	@DisplayName("PYTHONPATH contains project root folder")
	public void PYTHONPATH_contains_project_root_folder() throws ExecutionException, CoreException, URISyntaxException, IOException {
		final IProject project = WorkspaceTestHelper.importProject("org.eclipse.ease.lang.python.py4j.test", "resources/pythonproject");
		pathTest(Pattern.compile(".*resources.testproject"), project.getFile("hello.py"));
	}

	@Test
	@DisplayName("toString(VOID) is null")
	public void toString_of_VOID_is_null() throws ExecutionException, CoreException, URISyntaxException, IOException {
		assertNull(new Py4jScriptEngine().toString(ScriptResult.VOID));
	}

	@Test
	@DisplayName("toString(null) is null")
	public void toString_of_null_is_null() throws ExecutionException, CoreException, URISyntaxException, IOException {
		assertNull(new Py4jScriptEngine().toString(null));
	}

	@Test
	@DisplayName("toString(Object) is not empty")
	public void toString_of_Object_is_not_empty() throws ExecutionException, CoreException, URISyntaxException, IOException {
		assertEquals("foo", new Py4jScriptEngine().toString("foo"));
	}

	private void pathTest(Pattern pathPattern) throws ExecutionException {
		pathTest(pathPattern, null);
	}

	private void pathTest(Pattern pathPattern, IFile file) throws ExecutionException {
		final EngineDescription engineDescription = ScriptService.getInstance().getEngineByID(Py4jScriptEngine.ENGINE_ID);
		final IScriptEngine engine = engineDescription.createEngine();

		if (Objects.nonNull(file))
			engine.execute(file);

		final ScriptResult result = engine.execute("import sys\nsys.path");
		engine.schedule();

		final Object syspath = result.get();
		if (syspath instanceof List<?>)
			assertPathContains((List<?>) syspath, pathPattern);
		else
			fail(String.format("Script return value is not a list, got %s", syspath.getClass()));
	}

	private void assertPathContains(List<?> pythonPath, Pattern searchPattern) {
		for (final Object path : pythonPath) {
			if (searchPattern.matcher(String.valueOf(path)).matches())
				return;
		}

		fail(String.format("PYTHONPATH does not contain '%s'", searchPattern));
	}
}
