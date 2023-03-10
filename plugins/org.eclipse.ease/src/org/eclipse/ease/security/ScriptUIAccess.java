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

package org.eclipse.ease.security;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ease.Activator;
import org.eclipse.ease.ISecurityCheck;
import org.osgi.service.prefs.Preferences;

public final class ScriptUIAccess implements ISecurityCheck {

	/** Singleton instance. */
	private static final ISecurityCheck INSTANCE = new ScriptUIAccess();

	public static ISecurityCheck getInstance() {
		return INSTANCE;
	}

	private ScriptUIAccess() {
		// hide constructor from public
	}

	@Override
	public boolean doIt(ActionType action, Object... data) {
		if ((ActionType.INJECT_CODE == action) && (data.length >= 2) && (data[1] instanceof Boolean) && ((Boolean) data[1])) {
			final Preferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).node(Activator.PREFERENCES_NODE_SCRIPTS);
			final boolean allowUIAccess = prefs.getBoolean(Activator.SCRIPTS_ALLOW_UI_ACCESS, Activator.DEFAULT_SCRIPTS_ALLOW_UI_ACCESS);
			if (!allowUIAccess)
				throw new SecurityException("Script UI access disabled by user preferences.");
		}

		return true;
	}
}
