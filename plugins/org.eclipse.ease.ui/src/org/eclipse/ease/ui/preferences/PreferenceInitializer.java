/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
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
package org.eclipse.ease.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.ease.ui.Activator;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/**
	 *
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer# initializeDefaultPreferences()
	 */
	@Override
	public final void initializeDefaultPreferences() {
		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		// shell default settings
		store.setDefault(IPreferenceConstants.SHELL_HISTORY_LENGTH, 20);
		store.setDefault(IPreferenceConstants.SHELL_MODULES_AS_LIST, false);
		store.setDefault(IPreferenceConstants.SHELL_AUTOFOCUS, true);
		store.setDefault(IPreferenceConstants.SHELL_KEEP_COMMAND, false);
		store.setDefault(IPreferenceConstants.SHELL_DEFAULT_ENGINE, "org.eclipse.ease.javascript.rhino");
	}
}
