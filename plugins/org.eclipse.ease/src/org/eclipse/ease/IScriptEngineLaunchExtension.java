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
package org.eclipse.ease;

/**
 * Interface for script engine launch extensions. An extension gets the chance to modify a script engine before scripts are fed to the engine.
 */
public interface IScriptEngineLaunchExtension {

	/**
	 * Called upon a script engine creation. As there might be multiple launch extensions, this might not be the only contribution to the script engine.
	 *
	 * @param engine
	 *            engine just created
	 */
	void createEngine(IScriptEngine engine);
}
