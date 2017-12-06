/*******************************************************************************
 * Copyright (c) 2016 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jonah Graham (Kichwa Coders) - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.python.py4j.internal.ui;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.ease.lang.python.py4j.internal.Activator;
import org.eclipse.ease.lang.python.py4j.internal.Py4JScriptEnginePrefConstants;
import org.eclipse.jface.preference.IPreferenceStore;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		final IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		preferenceStore.setDefault(Py4JScriptEnginePrefConstants.INTERPRETER, Py4JScriptEnginePrefConstants.DEFAULT_INTERPRETER);
		preferenceStore.setDefault(Py4JScriptEnginePrefConstants.IGNORE_PYTHON_ENV_VARIABLES,
				Py4JScriptEnginePrefConstants.DEFAULT_IGNORE_PYTHON_ENV_VARIABLES);
	}

}
