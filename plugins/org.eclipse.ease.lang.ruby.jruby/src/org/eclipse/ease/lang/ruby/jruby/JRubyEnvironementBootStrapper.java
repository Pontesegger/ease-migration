/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.lang.ruby.jruby;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.IScriptEngineLaunchExtension;

/**
 * JRuby loader. Loads initial environment module.
 */
public class JRubyEnvironementBootStrapper implements IScriptEngineLaunchExtension {

	@Override
	public void createEngine(final IScriptEngine engine) {
		// load environment module
		engine.executeAsync("java_import org.eclipse.ease.modules.EnvironmentModule");
		engine.executeAsync("org.eclipse.ease.modules.EnvironmentModule.new().bootstrap()");
	}
}
