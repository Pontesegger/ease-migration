/*******************************************************************************
 * Copyright (c) 2018 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.javascript.nashorn;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.IScriptEngineLaunchExtension;
import org.eclipse.ease.Script;
import org.eclipse.ease.modules.EnvironmentModule;

public class NashornBootstrapper implements IScriptEngineLaunchExtension {

	@Override
	public void createEngine(final IScriptEngine engine) {
		// seems nashorn cannot call static methods on a class instance
		engine.executeAsync(new Script("Bootloader", "Packages." + EnvironmentModule.class.getName() + ".bootstrap();"));
	}
}
