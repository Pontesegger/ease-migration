/*******************************************************************************
 * Copyright (c) 2018 Christian Pontesegger and others.
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

package org.eclipse.ease.lang.javascript.nashorn;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.IScriptEngineLaunchExtension;
import org.eclipse.ease.Script;
import org.eclipse.ease.modules.EnvironmentModule;

public class NashornBootstrapper implements IScriptEngineLaunchExtension {

	@Override
	public void createEngine(final IScriptEngine engine) {
		// seems nashorn cannot call static methods on a class instance
		engine.execute(new Script("Bootloader", "Packages." + EnvironmentModule.class.getName() + ".bootstrap();"));
	}
}
