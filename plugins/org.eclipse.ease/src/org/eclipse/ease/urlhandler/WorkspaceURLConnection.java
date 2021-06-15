/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
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
package org.eclipse.ease.urlhandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

public class WorkspaceURLConnection extends URLConnection {

	public static final String SCHEME = "workspace";

	protected WorkspaceURLConnection(final URL url) {
		super(url);
	}

	@Override
	public void connect() throws IOException {
	}

	@Override
	public InputStream getInputStream() throws IOException {

		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(getURL().getHost() + getURL().getFile()));
		try {
			return file.getContents();
		} catch (CoreException e) {
			throw new IOException(e);
		}
	}

}
