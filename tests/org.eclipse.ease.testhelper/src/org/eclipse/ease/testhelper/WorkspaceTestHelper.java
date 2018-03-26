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

package org.eclipse.ease.testhelper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.ease.tools.StringTools;

public class WorkspaceTestHelper {

	public static final IWorkspaceRoot getWorkspace() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	public static final IProject createProject(String name) throws CoreException {
		final IWorkspaceRoot workspace = getWorkspace();

		final IProject project = workspace.getProject(name);
		if (!project.exists())
			project.create(null);

		if (!project.isOpen())
			project.open(null);

		return project;
	}

	public static final IFolder createFolder(String name, IContainer parent) throws CoreException {
		final IFolder folder = parent.getFolder(new Path(name));
		if (!folder.exists())
			folder.create(0, true, null);

		return folder;
	}

	public static final IFile createFile(String name, String content, IContainer parent) throws UnsupportedEncodingException, CoreException {
		final IFile file = parent.getFile(new Path(name));
		if (!file.exists())
			file.create(new ByteArrayInputStream(content.getBytes("UTF-8")), false, null);
		else
			file.setContents(new ByteArrayInputStream(content.getBytes("UTF-8")), false, false, null);

		return file;
	}

	public static final String readResource(String plugin, String location) throws IOException {
		String output = null;

		final URL url = new URL("platform:/plugin/" + plugin + location);
		final InputStream inputStream = url.openConnection().getInputStream();
		output = StringTools.toString(inputStream);
		inputStream.close();

		return output;
	}
}
