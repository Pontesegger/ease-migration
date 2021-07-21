/*******************************************************************************
 * Copyright (c) 2021 Christian Pontesegger and others.
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

package org.eclipse.ease.tools;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

public class PlatformExtension {

	public static Collection<PlatformExtension> createFor(String extensionPoint) {
		final IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(extensionPoint);

		return Arrays.asList(config).stream().map(e -> new PlatformExtension(e)).collect(Collectors.toSet());
	}

	public static Collection<PlatformExtension> createForName(String extensionPoint, String name) {
		final Collection<PlatformExtension> extensions = createFor(extensionPoint);

		return extensions.stream().filter(e -> Objects.equals(name, e.getConfigurationElement().getName())).collect(Collectors.toSet());
	}

	private final IConfigurationElement fConfigurationElement;

	public PlatformExtension(IConfigurationElement configurationElement) {
		fConfigurationElement = configurationElement;
	}

	public <T> T createInstance(String name, Class<T> clazz) throws CoreException {
		final Object instance = getConfigurationElement().createExecutableExtension(name);
		if (clazz.isAssignableFrom(instance.getClass()))
			return (T) instance;

		throw new ClassCastException(String.format("Could not cast class '%s' to '%s'", instance.getClass().getName(), clazz.getName()));
	}

	public IConfigurationElement getConfigurationElement() {
		return fConfigurationElement;
	}

	public String getAttribute(String name) {
		return getConfigurationElement().getAttribute(name);
	}
}
