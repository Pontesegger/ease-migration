/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
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
package org.eclipse.ease.lang.javascript.rhino;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ease.ScriptExecutionException;
import org.eclipse.ease.ScriptResult;
import org.eclipse.ease.debugging.IScriptDebugFrame;
import org.eclipse.ease.lang.javascript.rhino.debugger.RhinoDebuggerEngine;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RhinoDebuggerEngineTest extends AbstractRhinoScriptEngineTest {

	private static final String PROJECT_NAME = "Sample project";
	private static final String FOLDER_NAME = "Subfolder";
	private static final String MAIN_FILE_NAME = "mainFile.js";
	private static final String INCLUDE_FILE_NAME = "includeFile.js";

	/**
	 * @formatter:off
	 */
	private static final String MAIN_FILE_CONTENT = "include(\"Subfolder/includeFile.js\")\n" +
			"\n" +
			"function throwOnNull(parameter) {\n" +
			"	parameter.substr();\n" +
			"} \n" +
			"\n" +
			"subCall();\n" +
			"";
	private static final String INCLUDE_FILE_CONTENT = "function subCall() {\n" +
			"	throwOnNull(null);\n" +
			"}\n" +
			"";
	/**
	 * @formatter:on
	 */

	private IProject fProject;
	private IFile fMainFile;
	private IFile fIncludeFile;
	private RhinoDebuggerEngine fEngine;

	@BeforeEach
	public void beforeEach() throws CoreException, UnsupportedEncodingException {

		// create workspace sample project
		final IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();

		fProject = workspace.getProject(PROJECT_NAME);
		if (!fProject.exists())
			fProject.create(null);

		if (!fProject.isOpen())
			fProject.open(null);

		final IFolder folder = fProject.getFolder(FOLDER_NAME);
		if (!folder.exists())
			folder.create(0, true, null);

		fMainFile = fProject.getFile(MAIN_FILE_NAME);
		if (!fMainFile.exists())
			fMainFile.create(new ByteArrayInputStream(MAIN_FILE_CONTENT.getBytes("UTF-8")), false, null);

		fIncludeFile = folder.getFile(INCLUDE_FILE_NAME);
		if (!fIncludeFile.exists())
			fIncludeFile.create(new ByteArrayInputStream(INCLUDE_FILE_CONTENT.getBytes("UTF-8")), false, null);

		// we need to retrieve the service singleton as the workspace is not available in headless tests
		final IScriptService scriptService = ScriptService.getService();
		fEngine = (RhinoDebuggerEngine) scriptService.getEngineByID(RhinoDebuggerEngine.ENGINE_ID).createEngine();
		fEngine.setTerminateOnIdle(false);
	}

	@AfterEach
	public void afterEach() throws CoreException {
		getScriptEngine().terminate();

		if (fProject.exists())
			fProject.delete(true, true, null);
	}

	@Override
	protected RhinoDebuggerEngine getScriptEngine() {
		return fEngine;
	}

	@Test
	@DisplayName("getExceptionStackTrace returns script trace over multiple files")
	public void getExceptionStackTrace_returns_script_trace_over_multiple_files() throws InterruptedException {
		final ScriptResult result = executeCode(fMainFile);
		assertThrows(ScriptExecutionException.class, () -> result.get());

		final List<IScriptDebugFrame> stackTrace = getScriptEngine().getExceptionStackTrace(getScriptEngine().getThread());
		assertNotNull(stackTrace);
		assertEquals(3, stackTrace.size());

		assertEquals(fMainFile, stackTrace.get(0).getScript().getFile());
		assertEquals(4, stackTrace.get(0).getLineNumber());

		assertEquals(fIncludeFile, stackTrace.get(1).getScript().getFile());
		assertEquals(2, stackTrace.get(1).getLineNumber());

		assertEquals(fMainFile, stackTrace.get(2).getScript().getFile());
		assertEquals(7, stackTrace.get(2).getLineNumber());
	}
}
