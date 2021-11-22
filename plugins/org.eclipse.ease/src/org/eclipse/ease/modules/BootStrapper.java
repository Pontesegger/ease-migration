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
package org.eclipse.ease.modules;

import org.eclipse.ease.ICodeFactory;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.IScriptEngineLaunchExtension;
import org.eclipse.ease.Script;
import org.eclipse.ease.service.ScriptService;

/**
 * Loads basic module support for script engines. The {@link EnvironmentModule} provides basic functionality to manage modules, include other source files and
 * to print data. It will be loaded automatically when a script engine is started.
 */
public class BootStrapper implements IScriptEngineLaunchExtension {

	@Override
	public void createEngine(final IScriptEngine engine) {
		final ICodeFactory codeFactory = ScriptService.getCodeFactory(engine);
		if (codeFactory != null) {
			final String code = String.format("%s.bootstrap();%n", codeFactory.classInstantiation(EnvironmentModule.class, new String[0]));
			engine.execute(new Script("Bootloader", code));
		}
	}
}
