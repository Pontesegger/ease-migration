/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.lang.javascript.rhino;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ease.IDebugEngine;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.debugging.IScriptDebugFrame;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RhinoDebuggerEngineTest {

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
			"	try {\n" +
			"		parameter.substr();\n" +
			"	} catch (e) {\n" +
			"	}\n" +
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

	private IWorkspaceRoot fWorkspace;
	private IProject fProject;
	private IFolder fFolder;
	private IFile fMainFile;
	private IFile fIncludeFile;

	@Before
	public void setUp() throws Exception {

		// create workspace sample project
		fWorkspace = ResourcesPlugin.getWorkspace().getRoot();

		fProject = fWorkspace.getProject(PROJECT_NAME);
		if (!fProject.exists())
			fProject.create(null);

		if (!fProject.isOpen())
			fProject.open(null);

		fFolder = fProject.getFolder(FOLDER_NAME);
		if (!fFolder.exists())
			fFolder.create(0, true, null);

		fMainFile = fProject.getFile(MAIN_FILE_NAME);
		if (!fMainFile.exists())
			fMainFile.create(new ByteArrayInputStream(MAIN_FILE_CONTENT.getBytes("UTF-8")), false, null);

		fIncludeFile = fFolder.getFile(INCLUDE_FILE_NAME);
		if (!fIncludeFile.exists())
			fIncludeFile.create(new ByteArrayInputStream(INCLUDE_FILE_CONTENT.getBytes("UTF-8")), false, null);
	}

	@After
	public void tearDown() throws CoreException {
		if (fProject.exists())
			fProject.delete(true, true, null);
	}

	@Test
	public void getExceptionStackTrace() throws InterruptedException {
		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);
		final IScriptEngine engine = scriptService.getEngineByID("org.eclipse.ease.javascript.rhinoDebugger").createEngine();
		// make sure engine does not terminate automatically (would discard the exception stacktrace)
		engine.setTerminateOnIdle(false);
		engine.executeSync(fMainFile);

		final List<IScriptDebugFrame> stackTrace = ((IDebugEngine) engine).getExceptionStackTrace();
		assertNotNull(stackTrace);
		assertEquals(3, stackTrace.size());

		assertEquals(fMainFile, stackTrace.get(0).getScript().getFile());
		assertEquals(5, stackTrace.get(0).getLineNumber());

		assertEquals(fIncludeFile, stackTrace.get(1).getScript().getFile());
		assertEquals(2, stackTrace.get(1).getLineNumber());

		assertEquals(fMainFile, stackTrace.get(2).getScript().getFile());
		assertEquals(10, stackTrace.get(2).getLineNumber());

		engine.terminate();
	}

}
