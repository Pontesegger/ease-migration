/*******************************************************************************
 * Copyright (c) 2016 Christian Pontesegger and others.
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

package org.eclipse.ease.ui.completion.provider;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ease.tools.ResourceTools;

/**
 * Resolves file location completion proposals. Helper for file location completion provider.
 */
public class LocationResolver {

	/**
	 * Detect windows operating system.
	 *
	 * @return <code>true</code> when executed on a windows based OS
	 */
	public static final boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().startsWith("windows");
	}

	private final String fLocation;
	private final Object fParent;
	private Object fResolvedFolder;
	private boolean fProcessed = false;
	private String fFilterPart = "";

	public LocationResolver(final String location, final Object parent) {
		fLocation = location;
		fParent = parent;
	}

	/**
	 * Detect part of location that denotes the filter component. This is the text string following the last delimiter (eg '/').
	 *
	 * @return filter part of location
	 */
	public String getFilterPart() {
		process();
		return fFilterPart;
	}

	/**
	 * Get base folder this location refers to.
	 *
	 * @return base folder of this location
	 */
	public Object getParentFolder() {
		process();
		return fResolvedFolder;
	}

	/**
	 * Get all children of this location. Returns the content of {@link #getParentFolder()}.
	 *
	 * @return content of location base folder
	 */
	public Collection<? extends Object> getChildren() {
		process();

		try {
			if (fResolvedFolder instanceof IContainer)
				return Arrays.asList(((IContainer) fResolvedFolder).members());

		} catch (final CoreException e) {
			// TODO handle this exception (but for now, at least know it happened)
			throw new RuntimeException(e);
		}

		if (fResolvedFolder instanceof File) {
			if ((isWindows()) && (ResourceTools.VIRTUAL_WINDOWS_ROOT.equals(fResolvedFolder)))
				return Arrays.asList(File.listRoots());

			final File[] files = ((File) fResolvedFolder).listFiles();
			if (files != null)
				return Arrays.asList(files);
		}

		return Collections.emptySet();
	}

	/**
	 * Get a string representation of the resolved location folder.
	 *
	 * @return location base folder representation
	 */
	public String getParentString() {
		process();

		if (ResourceTools.isAbsolute(fLocation))
			return ResourceTools.toAbsoluteLocation(getParentFolder(), null);

		String parentString = fLocation.substring(0, fLocation.length() - getFilterPart().length());
		while (parentString.endsWith("/"))
			parentString = parentString.substring(0, parentString.length() - 1);

		return parentString;
	}

	/**
	 * Resolve the given location.
	 */
	private void process() {
		if (!fProcessed) {

			// direct resolve: works if the given location is already
			fResolvedFolder = ResourceTools.resolve(fLocation, fParent);

			if ((!ResourceTools.isFolder(fResolvedFolder)) && (!ResourceTools.VIRTUAL_WINDOWS_ROOT.equals(fResolvedFolder))) {
				// did not work, we quite likely have some partial file/folder name which acts as a filter
				// calculate filter part
				if ((isWindows()) && (fLocation.contains("\\")))
					fFilterPart = fLocation.substring(fLocation.lastIndexOf('\\') + 1);

				else if (fLocation.contains("/"))
					fFilterPart = fLocation.substring(fLocation.lastIndexOf('/') + 1);

				else
					// no delimiter found, the whole content acts as filter
					fFilterPart = fLocation;

				// truncate filter part
				final String locationStr = fLocation.substring(0, fLocation.length() - fFilterPart.length());

				// resolve without filter part
				fResolvedFolder = ResourceTools.resolve(locationStr, fParent);
			}

			if ((!ResourceTools.isFolder(fResolvedFolder)) && (!ResourceTools.VIRTUAL_WINDOWS_ROOT.equals(fResolvedFolder))) {
				// we should have a file instance here, resolve to its parent folder (which should be a file instance)
				if (fParent instanceof IFile)
					fResolvedFolder = ((IFile) fParent).getParent();

				else if ((fParent instanceof File) && (((File) fParent).isDirectory()))
					fResolvedFolder = ((File) fParent).getParentFile();
			}

			fProcessed = true;
		}
	}
}
