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
package org.eclipse.ease.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.IScriptEngineLaunchExtension;
import org.eclipse.ease.modules.ModuleCategoryDefinition;
import org.eclipse.ease.modules.ModuleDefinition;

/**
 * Global service to create script engines and to query configuration data from the scripting extensions. To get the service instance use
 *
 * <pre>
 * final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);
 * </pre>
 */
public interface IScriptService {

	/** Trace enablement for the script service. */
	boolean TRACE_SCRIPT_SERVICE = org.eclipse.ease.Activator.getDefault().isDebugging()
			&& "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.ease/debug/scriptService"));

	/**
	 * Get a dedicated engine description. Allows to get an engine description for a dedicated engine identifier.
	 *
	 * @param engineID
	 *            engine identifier
	 * @return script engine description or <code>null</code>
	 */
	EngineDescription getEngineByID(String engineID);

	/**
	 * Get available engine descriptions.
	 *
	 * @return available descriptions or empty collection
	 */
	Collection<EngineDescription> getEngines();

	/**
	 * Get available engine descriptions for a given script type. The resulting list is sorted by priority. Index 0 contains the engine with the highest
	 * priority.
	 *
	 * @param scriptType
	 *            type of script
	 * @return available descriptions or empty collection
	 */
	List<EngineDescription> getEngines(String scriptType);

	/**
	 * Get default engine for a given script type. While there might exist multiple engines, the returned one has the highest priority.
	 *
	 * @param scriptType
	 *            type of script
	 * @return engine description or <code>null</code>
	 */
	EngineDescription getEngine(String scriptType);

	/**
	 * Get a map of available modules. Keys contain the full module name, values contain its descriptor.
	 *
	 * @return available modules or empty map
	 */
	Collection<ModuleDefinition> getAvailableModules();

	/**
	 * Get a map of available module categories. Keys contain the category id, values contain its descriptor.
	 *
	 * @return available modules or empty map
	 */
	Map<String, ModuleCategoryDefinition> getAvailableModuleCategories();

	/**
	 * Get a map of available script tpye. Keys contain the type name, values contain its descriptor.
	 *
	 * @return available script types or empty map
	 */
	Map<String, ScriptType> getAvailableScriptTypes();

	/**
	 * Get Launch extensions for a dedicated script engine.
	 *
	 * @param engineDescription
	 *            engine description for further filtering
	 * @return launch extensions or empty collection
	 */
	Collection<IScriptEngineLaunchExtension> getLaunchExtensions(EngineDescription engineDescription);

	/**
	 * Get the script type for a given resource location.
	 *
	 * @param location
	 *            resource location
	 * @return script type associated with file extension or <code>null</code>
	 */
	ScriptType getScriptType(String location);

	/**
	 * Get the definition of the module with given ID.
	 *
	 * @param moduleId
	 *            id to look for
	 * @return module definition
	 */
	ModuleDefinition getModuleDefinition(String moduleId);

	/**
	 * Execute a given script and returns the execution result.
	 *
	 * @param scriptLocation
	 *            script location to execute
	 * @param engineID
	 *            script engine ID or <code>null</code> to automatically select a matching engine
	 * @param arguments
	 *            script arguments
	 * @return script result
	 * @throws Throwable
	 *             any exception thrown by the script
	 */
	Object executeScript(String scriptLocation, String engineID, String... arguments) throws Throwable;

	/**
	 * Create an engine from a given description.
	 *
	 * @param description
	 *            engine description
	 * @return script engine instance
	 */
	IScriptEngine createEngine(EngineDescription description);

	/**
	 * Get all running script engines.
	 *
	 * @return running engines
	 */
	Collection<IScriptEngine> getRunningEngines();

	/**
	 * Add a listener for engine creations. If already added, nothing will happen.
	 *
	 * @param listener
	 *            listener to add.
	 */
	void addEngineListener(IScriptEngineLaunchExtension listener);

	/**
	 * Remove a listener for engine creations.
	 *
	 * @param listener
	 *            listener to remove
	 */
	void removeEngineListener(IScriptEngineLaunchExtension listener);
}
