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
package org.eclipse.ease.modules;

import org.eclipse.ease.IScriptEngine;

/**
 * Base class to be used for modules. Handles retrieval of script engine and environment module.
 */
public abstract class AbstractScriptModule implements IScriptModule {

	/** Script engine instance. */
	private IScriptEngine fEngine = null;

	/** Environment module instance. */
	private IEnvironment fEnvironment = null;

	@Override
	public void initialize(final IScriptEngine engine, final IEnvironment environment) {
		fEngine = engine;
		fEnvironment = environment;
	}

	/**
	 * Get the current script engine.
	 * 
	 * @return script engine
	 */
	public IScriptEngine getScriptEngine() {
		return fEngine;
	}

	/**
	 * Get the current environment module.
	 * 
	 * @return environment module
	 */
	protected IEnvironment getEnvironment() {
		return fEnvironment;
	}
}
