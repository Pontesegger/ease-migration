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
package org.eclipse.ease.lang.python.jython;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.IScriptEngineLaunchExtension;
import org.eclipse.ease.Script;

/**
 * Python loader. Loads initial environment module.
 */
public class PythonEnvironementBootStrapper implements IScriptEngineLaunchExtension {

	@Override
	public void createEngine(final IScriptEngine engine) {

		// load environment module
		final StringBuilder code = new StringBuilder("from org.eclipse.ease.modules import EnvironmentModule\n");
		// register top level packages
		code.append("import java\n");
		code.append("import org\n");
		code.append("import com\n");

		code.append("EnvironmentModule().bootstrap()\n");

		engine.executeAsync(new Script("Python Bootstrapper", code));
	}
}
