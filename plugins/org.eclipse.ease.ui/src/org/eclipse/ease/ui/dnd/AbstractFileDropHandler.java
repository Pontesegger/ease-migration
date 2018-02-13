/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.dnd;

import java.io.File;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.tools.ResourceTools;

/**
 * Helper class for file drop handler. Accepts files with certain file extensions and allows to encode their path.
 */
public abstract class AbstractFileDropHandler extends AbstractModuleDropHandler {

	@Override
	public boolean accepts(final IScriptEngine scriptEngine, final Object element) {
		for (String extension : getAcceptedFileExtensions()) {
			if (!extension.startsWith("."))
				extension = "." + extension;

			if (element instanceof IFile)
				return ((IFile) element).getName().toLowerCase().endsWith(extension.toLowerCase());

			else if (element instanceof File)
				return ((File) element).getName().toLowerCase().endsWith(extension.toLowerCase());
		}

		return false;
	}

	/**
	 * Encode the file instance to an URI. As we are accepting files only, we do not need to resolve relative paths here.
	 *
	 * @param element
	 *            {@link File} or {@link IFile} instance
	 * @return
	 */
	protected String getFileURI(final Object element) {
		return ResourceTools.toAbsoluteLocation(element, null);
	}

	/**
	 * Get accepted file extensions. File extensions are considered to be the text after a '.'. Even files with multiple extensions in the file name can be
	 * considered (eg 'ascii.txt'). File extensions are compared case insensitive.
	 *
	 * @return accepted file extensions
	 */
	protected abstract Collection<String> getAcceptedFileExtensions();
}
