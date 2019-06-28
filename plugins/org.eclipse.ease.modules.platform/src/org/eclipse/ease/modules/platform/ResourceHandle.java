/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.modules.platform;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public class ResourceHandle extends FilesystemHandle {

	private final IFile fFile;

	public ResourceHandle(final IFile file, final int mode) {
		super(null, mode);

		fFile = file;
	}

	@Override
	protected BufferedReader createReader() throws Exception {
		return new BufferedReader(new InputStreamReader(fFile.getContents()));
	}

	@Override
	public void write(final String data) throws IOException {
		write(data.getBytes());
	}

	@Override
	public void write(final byte[] data) throws IOException {
		try {
			// replace file content or append content
			if ((getMode() & APPEND) == APPEND) {
				// append data
				fFile.appendContents(new ByteArrayInputStream(data), false, false, null);

			} else {
				// replace content
				fFile.setContents(new ByteArrayInputStream(data), false, false, null);
				setMode(getMode() | APPEND);
			}

		} catch (final CoreException e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	@Override
	public Path getPath() {
		return fFile.getLocation().toFile().toPath();
	}

	@Override
	public boolean exists() {
		return fFile.exists();
	}

	@Override
	public boolean createFile(final boolean createHierarchy) throws CoreException {
		if (createHierarchy)
			createFolder(fFile.getParent());

		fFile.create(new ByteArrayInputStream(new byte[0]), false, null);

		return true;
	}

	/**
	 * Create a new container on the workbench.
	 *
	 * @param container
	 *            container to create
	 * @return <code>true</code> on success
	 * @throws CoreException
	 *             thrown when folder cannot be created
	 */
	public static boolean createFolder(final IContainer container) throws CoreException {

		if (!container.isAccessible()) {
			final IContainer parent = container.getParent();

			if (!parent.isAccessible()) {
				if (parent instanceof IFolder)
					createFolder(parent);

				else if (parent instanceof IProject) {
					((IProject) parent).create(null);
					((IProject) parent).open(null);
				}

				else
					return false;
			}

			if (container instanceof IFolder)
				((IFolder) container).create(0, true, null);

			else if (container instanceof IProject) {
				((IProject) container).create(null);
				((IProject) parent).open(null);
			}
		}

		return true;
	}

	@Override
	public Object getFile() {
		return fFile;
	}
}
