/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
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
package org.eclipse.ease.ui.dnd;

import java.util.concurrent.ExecutionException;

import org.eclipse.ease.ICodeFactory;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Logger;
import org.eclipse.ease.modules.EnvironmentModule;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.modules.ModuleHelper;
import org.eclipse.ease.service.ScriptService;
import org.eclipse.ease.ui.Activator;

/**
 * Helper class for drop handlers. Adds convenience methods to keep dedicated handlers simpler.
 */
public abstract class AbstractModuleDropHandler implements IShellDropHandler {

	/**
	 * Load a dedicated module if it is not already loaded.
	 *
	 * @param scriptEngine
	 *            script engine to load module
	 * @param moduleID
	 *            moduleID to look for
	 * @param force
	 *            if set to <code>false</code> load only when not already loaded
	 * @return module instance or <code>null</code>
	 */
	protected Object loadModule(final IScriptEngine scriptEngine, final String moduleID, final boolean force) {

		if ((force) || (getLoadedModule(scriptEngine, moduleID) == null)) {
			if (ModuleHelper.resolveModuleName(moduleID) != null) {
				try {
					final ICodeFactory codeFactory = ScriptService.getCodeFactory(scriptEngine);
					final String functionCall = codeFactory.createFunctionCall(EnvironmentModule.class.getMethod("loadModule", String.class, boolean.class),
							moduleID, false);
					return scriptEngine.execute(functionCall).get();

				} catch (final NoSuchMethodException e) {
					Logger.error(Activator.PLUGIN_ID, "Method loadModule() not found", e);
				} catch (final SecurityException e) {
					Logger.error(Activator.PLUGIN_ID, "Method loadModule() not accessible", e);
				} catch (final ExecutionException e) {
					Logger.error(Activator.PLUGIN_ID, "Method loadModule() failed to execute script code", e);
				}

			} else
				Logger.error(Activator.PLUGIN_ID, "Module \"" + moduleID + "\" cannot be found");
		}

		return getLoadedModule(scriptEngine, moduleID);
	}

	private Object getLoadedModule(IScriptEngine scriptEngine, String moduleID) {
		final IEnvironment environment = IEnvironment.getEnvironment(scriptEngine);
		if (environment != null)
			return environment.getModule(moduleID);

		return null;
	}
}
