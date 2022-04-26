/*******************************************************************************
 * Copyright (c) 2022 Christian Pontesegger and others.
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

package org.eclipse.ease.ui.views.shell.dropins;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ease.Logger;
import org.eclipse.ease.ui.Activator;

public final class DropinTools {

	private static final String EXTENSION_SHELL_ID = "org.eclipse.ease.ui.shell";

	private static final String EXTENSION_DROPIN_ID = "dropin";

	private static final String PROPERTY_DROPIN_CLASS = "class";

	@Deprecated
	private DropinTools() {
		throw new UnsupportedOperationException("Constructor of helper class shall not be called");
	}

	public static Collection<IShellDropin> getAvailableDropins() {
		final Collection<IShellDropin> dropins = new ArrayList<>();

		final IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_SHELL_ID);
		for (final IConfigurationElement e : config) {
			if (EXTENSION_DROPIN_ID.equals(e.getName())) {
				try {
					final Object dropin = e.createExecutableExtension(PROPERTY_DROPIN_CLASS);
					if (dropin instanceof IShellDropin) {
						// TODO sort by priorities
						dropins.add((IShellDropin) dropin);
					}

				} catch (final CoreException e1) {
					Logger.error(Activator.PLUGIN_ID, "Invalid shell dropin detected: " + e.getAttribute(PROPERTY_DROPIN_CLASS), e1);
				}
			}
		}

		return dropins;
	}
}
