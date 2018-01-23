/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.scripts.repository.impl;

import java.io.File;

import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.ease.ui.scripts.repository.IRepositoryService;
import org.eclipse.ease.ui.scripts.repository.IScriptLocation;
import org.eclipse.ui.PlatformUI;

public class FileSystemParser extends WorkspaceParser {

	public void parse(final File file, final IScriptLocation entry) {
		if (file.isDirectory()) {
			// containment, parse children
			for (final File child : file.listFiles()) {
				if ((child.isFile()) || (entry.isRecursive()))
					parse(child, entry);
			}

		} else {
			// try to locate registered script
			final String location = ResourceTools.toAbsoluteLocation(file, null);

			final IRepositoryService repositoryService = PlatformUI.getWorkbench().getService(IRepositoryService.class);
			repositoryService.updateLocation(entry, location, file.lastModified());
		}
	}
}
