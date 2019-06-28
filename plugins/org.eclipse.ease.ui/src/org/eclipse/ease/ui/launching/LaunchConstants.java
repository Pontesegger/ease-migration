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
package org.eclipse.ease.ui.launching;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

public interface LaunchConstants {

	public String FILE_LOCATION = "File location";

	public String SUSPEND_ON_STARTUP = "Suspend on startup";

	public String DISPLAY_DYNAMIC_CODE = "Display dynamic code";

	public String LIBRARIES = "Libraries";

	public String SCRIPT_ENGINE = "Script engine";

	public String STARTUP_PARAMETERS = "Startup parameters";

	public String SUSPEND_ON_SCRIPT_LOAD = "Suspend on script load";

	public static Collection<String> getLibraries(final ILaunchConfiguration configuration) throws CoreException {
		final String librariesString = configuration.getAttribute(LIBRARIES, "");
		return unserializeLibraries(librariesString);
	}

	public static Collection<String> unserializeLibraries(final String libraries) {
		final String[] elements = libraries.split(File.pathSeparator);
		final List<String> result = new ArrayList<>(elements.length);
		for (final String element : elements)
			if (!element.trim().isEmpty())
				result.add(element.trim());

		return result;
	}

	public static String serializeLibraries(final List<String> libraries) {
		final StringBuilder result = new StringBuilder();
		for (final String filePath : libraries) {
			result.append(File.pathSeparator);
			result.append(filePath);
		}

		if (result.length() > 0)
			result.delete(0, File.pathSeparator.length());

		return result.toString();
	}
}
