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

package org.eclipse.ease.modules.platform.resources;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

public final class ResourceTestHelper {

	@Deprecated
	private ResourceTestHelper() {
		throw new IllegalArgumentException("Not expected to be instantiated");
	}

	public static IProject createTestProject(String projectName) throws CoreException {
		final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (project.exists()) {
			project.delete(true, new NullProgressMonitor());
		}

		project.create(new NullProgressMonitor());
		project.open(new NullProgressMonitor());

		final IFile file = project.getFile("Test file.txt");
		file.create(new ByteArrayInputStream("Hello world!".getBytes()), false, null);

		project.getFolder("existingFolder").create(true, true, new NullProgressMonitor());

		return project;
	}

	public static File createTempFile(String prefix) throws IOException {
		final File file = File.createTempFile(prefix, "");
		file.deleteOnExit();

		try (OutputStream out = Files.newOutputStream(file.toPath())) {
			out.write("Hello world!".getBytes());
		}

		return file;
	}

	public static File createTempFolder(String prefix) throws IOException {
		final File file = File.createTempFile(prefix, "");

		final File folder = new File(file.getParent() + File.separator + prefix + "testFolder");
		folder.mkdirs();

		Files.delete(file.toPath());

		return folder;
	}

	public static void cleanupTempFiles(String prefix) throws IOException {
		deleteFiles(File.createTempFile(prefix, "").getParentFile().listFiles(file -> (file.getName().startsWith(prefix))));
	}

	private static void deleteFiles(File[] files) throws IOException {
		for (final File file : files) {
			if (file.isDirectory())
				deleteFiles(file.listFiles());

			Files.delete(file.toPath());
		}
	}
}
