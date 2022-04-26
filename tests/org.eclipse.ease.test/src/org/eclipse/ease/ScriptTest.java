/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
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

package org.eclipse.ease;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ease.tools.ResourceTools;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ScriptTest {

	private static final String SAMPLE_CODE = "print('Hello world');";
	private IFile fFile;

	@BeforeEach
	public void setUp() throws Exception {
		final IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();

		final IProject project = workspace.getProject("Sample project");
		if (!project.exists())
			project.create(null);

		if (!project.isOpen())
			project.open(null);

		fFile = project.getFile("Script.js");
		if (!fFile.exists())
			fFile.create(new ByteArrayInputStream(SAMPLE_CODE.getBytes("UTF-8")), false, null);
	}

	@Test
	public void codeFromString() throws IOException {
		final Script script = new Script(SAMPLE_CODE);
		assertEquals(SAMPLE_CODE, script.getCode());
	}

	@Test
	public void codeFromInputStream() throws IOException {
		final Script script = new Script(new ByteArrayInputStream(SAMPLE_CODE.getBytes()));
		assertEquals(SAMPLE_CODE, script.getCode());
	}

	@Test
	public void codeFromReader() throws IOException {
		final Script script = new Script(new StringReader(SAMPLE_CODE));
		assertEquals(SAMPLE_CODE, script.getCode());
	}

	@Test
	public void codeFromScriptable() throws IOException {
		final Script script = new Script((IScriptable) () -> new ByteArrayInputStream(SAMPLE_CODE.getBytes()));
		assertEquals(SAMPLE_CODE, script.getCode());
	}

	@Test
	public void codeFromWorkspaceFile() throws IOException {
		final Script script = new Script(fFile);
		assertEquals(SAMPLE_CODE, script.getCode());
	}

	@Test
	public void codeFromFilesystemFile() throws IOException {
		final Script script = new Script(fFile.getLocation().toFile());
		assertEquals(SAMPLE_CODE, script.getCode());
	}

	@Test
	public void codeFromStringBuilder() throws IOException {
		final Script script = new Script(new StringBuilder(SAMPLE_CODE));
		assertEquals(SAMPLE_CODE, script.getCode());
	}

	@Test
	public void getCodeStream() throws IOException {
		final Script script = new Script(new ByteArrayInputStream(SAMPLE_CODE.getBytes()));
		assertEquals(SAMPLE_CODE, ResourceTools.toString(script.getCodeStream()));

		// try a second time to see that stream is recreated
		assertEquals(SAMPLE_CODE, ResourceTools.toString(script.getCodeStream()));
	}

	@Test
	public void getCommand() {
		final Script script = new Script(fFile);
		assertEquals(fFile, script.getCommand());
	}

	@Test
	public void getFile() {
		Script script = new Script(fFile);
		assertEquals(fFile, script.getFile());

		script = new Script(fFile.getLocation().toFile());
		assertEquals(fFile.getLocation().toFile(), script.getFile());

		script = new Script(new ByteArrayInputStream(SAMPLE_CODE.getBytes()));
		assertNull(script.getFile());
	}

	@Test
	public void getTitle() {
		Script script = new Script(fFile);
		assertEquals(fFile.getName(), script.getTitle());

		script = new Script("Script title", fFile);
		assertEquals("Script title", script.getTitle());
	}

	@Test
	public void getResult() throws ExecutionException {
		final Script script = new Script(fFile);
		assertNotNull(script.getResult());

		script.setResult("result");
		assertEquals("result", script.getResult().get());
	}

	@Test
	public void getException() {
		final Script script = new Script(fFile);

		script.setException(new ScriptExecutionException());
		assertThrows(ScriptExecutionException.class, () -> script.getResult().get());
	}
}
