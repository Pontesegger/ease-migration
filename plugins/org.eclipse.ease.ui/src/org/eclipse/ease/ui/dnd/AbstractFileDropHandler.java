/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
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

			if (element instanceof IFile) {
				final boolean accept = ((IFile) element).getName().toLowerCase().endsWith(extension.toLowerCase());
				if (accept)
					return true;
			}

			else if (element instanceof File) {
				final boolean accept = ((File) element).getName().toLowerCase().endsWith(extension.toLowerCase());
				if (accept)
					return true;
			}
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
