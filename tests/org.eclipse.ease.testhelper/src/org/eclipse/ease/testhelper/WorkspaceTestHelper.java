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

package org.eclipse.ease.testhelper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ease.tools.StringTools;
import org.osgi.framework.Bundle;

public class WorkspaceTestHelper {

	public static IWorkspaceRoot getWorkspace() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	public static IProject createProject(String name) throws CoreException {
		final IWorkspaceRoot workspace = getWorkspace();

		final IProject project = workspace.getProject(name);
		if (!project.exists())
			project.create(null);

		if (!project.isOpen())
			project.open(null);

		return project;
	}

	public static IFolder createFolder(String name, IContainer parent) throws CoreException {
		final IFolder folder = parent.getFolder(new Path(name));
		if (!folder.exists())
			folder.create(0, true, null);

		return folder;
	}

	public static IFile createFile(String name, String content, IContainer parent) throws UnsupportedEncodingException, CoreException {
		final IFile file = parent.getFile(new Path(name));
		if (!file.exists())
			file.create(new ByteArrayInputStream(content.getBytes("UTF-8")), false, null);
		else
			file.setContents(new ByteArrayInputStream(content.getBytes("UTF-8")), false, false, null);

		return file;
	}

	public static String readResource(String plugin, String location) throws IOException {
		String output = null;

		final URL url = new URL("platform:/plugin/" + plugin + location);
		final InputStream inputStream = url.openConnection().getInputStream();
		output = StringTools.toString(inputStream);
		inputStream.close();

		return output;
	}

	public static IProject importProject(String plugin, String projectFolder) throws CoreException, URISyntaxException, IOException {
		final Bundle bundle = Platform.getBundle(plugin);
		final URL fileURL = bundle.getEntry(projectFolder);
		final File sourceFolder = new File(FileLocator.resolve(fileURL).toURI());

		// create project that links to /this/resources/UnitTest
		final Path projectPath = new Path(sourceFolder.getAbsoluteFile() + File.separator + ".project");
		final IProjectDescription projectDescription = ResourcesPlugin.getWorkspace().loadProjectDescription(projectPath);
		projectDescription.setLocation(new Path(sourceFolder.getAbsolutePath()));

		final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectDescription.getName());
		if (!project.exists()) {
			project.create(projectDescription, new NullProgressMonitor());
			project.open(new NullProgressMonitor());
		}

		return project;
	}
}
