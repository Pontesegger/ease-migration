/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.launching;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class FileExtensionFilter extends ViewerFilter {

	private final List<String> extensions;

	public FileExtensionFilter(String[] extensions) {
		this.extensions = Arrays.asList(extensions);
	}

	@Override
	public boolean select(Viewer viewer, Object parent, Object element) {
		if (element instanceof IFile)
			return isFileValid(((IFile) element).getProjectRelativePath());

		if (element instanceof IContainer) {
			try {
				if (!((IContainer) element).isAccessible())
					return false;
				final IResource[] resources = ((IContainer) element).members();
				for (final IResource resource : resources) {
					if (select(viewer, parent, resource))
						return true;
				}
			} catch (final CoreException e) {
			}
		}
		return false;
	}

	private boolean isFileValid(IPath path) {
		final String ext = path.getFileExtension();
		if ((ext != null) && !ext.trim().equals("") && isProperExtension(ext))
			return true;
		return false;
	}

	private boolean isProperExtension(String ext) {
		if (extensions != null) {
			return extensions.contains(ext);
		}
		return false;
	}
}
