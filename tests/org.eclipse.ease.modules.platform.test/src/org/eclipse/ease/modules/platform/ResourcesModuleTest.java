/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.modules.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ease.IScriptEngine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ResourcesModuleTest extends AbstractModuleTest {

	private File tempFile;
	private ResourcesModule module;

	@Override
	protected Object getModuleClass() {
		return ResourcesModule.class;
	}

	@Override
	protected Object getModuleID() {
		return ResourcesModule.MODULE_ID;
	}

	@BeforeEach
	public void createTempFile() throws IOException {
		tempFile = File.createTempFile("ease_unittest_", "");
		tempFile.deleteOnExit(); // ensure cleanup

		// mocked script engine
		final IScriptEngine mockEngine = mock(IScriptEngine.class);
		when(mockEngine.getExecutedFile()).thenReturn(tempFile);

		// initialize module
		module = new ResourcesModule();
		module.initialize(mockEngine, null);
	}

	@AfterEach
	public void cleanUp() {
		tempFile.delete();
	}

	@Test
	public void getFile() throws IOException {
		final Object file = module.getFile(tempFile.toString(), true);
		assertEquals(tempFile, file);
	}

	@Test
	public void copyBinaryFile() throws Exception {
		// GIVEN
		// use bytes that are not ASCII
		final byte[] data = new byte[] { -128, -12, 123, 52, 0, 12, 127 };

		final File sourceFile = File.createTempFile("ease_unittest_copyFile_sourceFile", "");
		sourceFile.deleteOnExit();

		try (FileOutputStream out = new FileOutputStream(sourceFile)) {
			out.write(data);
		}

		final File targetFile = File.createTempFile("ease_unittest_copyFile_target", "");
		targetFile.delete();
		targetFile.deleteOnExit();

		// WHEN
		module.copyFile(sourceFile, targetFile);

		// THEN
		// compare content with the written bytes
		try (final BufferedInputStream in = new BufferedInputStream(new FileInputStream(targetFile))) {
			final byte[] read = new byte[data.length];
			in.read(read, 0, data.length);

			assertTrue(Arrays.equals(data, read), "The content of the copied file is not the same");
		}

		targetFile.delete();
		sourceFile.delete();
	}

	@Test
	public void copyBinaryIFile() throws Exception {

		// GIVEN
		// use bytes that are not ASCII
		final byte[] data = new byte[] { -128, -12, 123, 52, 0, 12, 127 };

		final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("ease_test_project_copyBinaryIFile");
		project.create(null);
		project.open(null);

		final IFile sourceIFile = project.getFile("ease_unittest_sourceIFile");
		sourceIFile.create(new ByteArrayInputStream(data), true, null);

		final IFile targetIFile = project.getFile("ease_unittest_targetIFile");

		// WHEN
		module.copyFile(sourceIFile, targetIFile);

		// THEN
		// compare content with the written bytes
		try (final BufferedInputStream in = new BufferedInputStream(targetIFile.getContents())) {
			final byte[] read = new byte[data.length];
			in.read(read, 0, data.length);

			assertTrue(Arrays.equals(data, read), "The content of the copied file is not the same");
		}
	}
}
