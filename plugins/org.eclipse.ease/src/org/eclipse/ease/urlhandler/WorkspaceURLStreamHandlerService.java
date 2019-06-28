/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.urlhandler;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.osgi.service.url.AbstractURLStreamHandlerService;

public class WorkspaceURLStreamHandlerService extends AbstractURLStreamHandlerService {

	public WorkspaceURLStreamHandlerService() {
	}

	@Override
	public URLConnection openConnection(final URL u) throws IOException {
		return new WorkspaceURLConnection(u);
	}
}
