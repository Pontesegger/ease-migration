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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.ease.AbstractScriptEngine;
import org.eclipse.ease.Activator;
import org.eclipse.ease.ICodeFactory;
import org.eclipse.ease.ICodeParser;
import org.eclipse.ease.IExecutionListener;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.IScriptEngineLaunchExtension;
import org.eclipse.ease.Logger;
import org.eclipse.ease.Script;
import org.eclipse.ease.ScriptResult;
import org.eclipse.ease.modules.ModuleCategoryDefinition;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

public final class ScriptService implements IScriptService, BundleListener, IExecutionListener {

	private static final String ENGINE = "engine";

	private static final String ENGINE_ID = "engineID";

	private static final Object EXTENSION_MODULE = "module";

	private static final Object EXTENSION_CATEGORY = "category";

	private static final String EXTENSION_LANGUAGE_ID = "org.eclipse.ease.language";

	private static final String EXTENSION_MODULES_ID = "org.eclipse.ease.modules";

	private static final String EXTENSION_SCRIPTTYPE_ID = "org.eclipse.ease.scriptType";

	private static final String SCRIPTTYPE_NAME = "name";

	private static final String LAUNCH_EXTENSION = "launchExtension";

	private static ScriptService fInstance = null;

	public static IScriptService getService() {
		try {
			return PlatformUI.getWorkbench().getService(IScriptService.class);
		} catch (final IllegalStateException e) {
			// workbench has not been created yet, might be running in headless mode
			return ScriptService.getInstance();
		}
	}

	public static synchronized ScriptService getInstance() {
		if (fInstance == null)
			fInstance = new ScriptService();

		return fInstance;
	}

	private Collection<ModuleDefinition> fAvailableModules = null;

	private Map<String, EngineDescription> fEngineDescriptions = null;

	private Map<String, ScriptType> fScriptTypes = null;

	private Map<String, ModuleCategoryDefinition> fAvailableModuleCategories = null;

	private final Collection<IScriptEngine> fRunningEngines = new ArrayList<>();

	private final ListenerList<IScriptEngineLaunchExtension> fEngineListeners = new ListenerList<>();

	private ScriptService() {
		Activator.getDefault().getContext().addBundleListener(this);
	}

	@Override
	public void addEngineListener(IScriptEngineLaunchExtension listener) {
		if (listener != null)
			fEngineListeners.add(listener);
	}

	@Override
	public void removeEngineListener(IScriptEngineLaunchExtension listener) {
		fEngineListeners.remove(listener);
	}

	@Override
	public EngineDescription getEngineByID(final String engineID) {
		return getEngineDescriptions().get(engineID);
	}

	@Override
	public synchronized Collection<ModuleDefinition> getAvailableModules() {
		if (fAvailableModules == null) {
			fAvailableModules = new HashSet<>();
			final IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_MODULES_ID);
			for (final IConfigurationElement e : config) {
				if (e.getName().equals(EXTENSION_MODULE)) {
					// module extension detected
					final ModuleDefinition definition = new ModuleDefinition(e);
					if (definition.getModuleClass() != null)
						fAvailableModules.add(definition);

					else
						Logger.warning(Activator.PLUGIN_ID,
								"Module <" + definition.getName() + "> in plugin <" + definition.getBundleID() + "> could not be located!");
				}
			}
		}

		return fAvailableModules;
	}

	@Override
	public Collection<EngineDescription> getEngines() {
		return getEngineDescriptions().values();
	}

	@Override
	public List<EngineDescription> getEngines(final String scriptType) {
		final List<EngineDescription> result = new ArrayList<>();

		for (final EngineDescription description : getEngines()) {
			if (description.supports(scriptType))
				result.add(description);
		}

		// sort by priority
		Collections.sort(result, (o1, o2) -> o2.getPriority() - o1.getPriority());

		return result;
	}

	private synchronized Map<String, EngineDescription> getEngineDescriptions() {
		if (fEngineDescriptions == null) {
			fEngineDescriptions = new HashMap<>();
			final IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_LANGUAGE_ID);

			for (final IConfigurationElement e : config) {
				if (ENGINE.equals(e.getName())) {
					final EngineDescription engine = new EngineDescription(e);
					fEngineDescriptions.put(engine.getID(), engine);
				}
			}
		}
		return fEngineDescriptions;
	}

	@Override
	public Collection<IScriptEngineLaunchExtension> getLaunchExtensions(final EngineDescription engineDescription) {
		final String targetEngineID = engineDescription.getID();

		final Collection<IScriptEngineLaunchExtension> extensions = new HashSet<>();
		final IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_LANGUAGE_ID);
		for (final IConfigurationElement e : config) {
			try {
				if (LAUNCH_EXTENSION.equals(e.getName())) {
					// Parse engine ID to backwards compatible regular expression
					String engineID = e.getAttribute(ENGINE_ID);
					if ((engineID == null) || engineID.matches("^\\*?$")) {
						engineID = ".*";
					}
					if (!targetEngineID.matches(engineID)) {
						continue;
					}

					// Check if script type given
					final String scriptType = e.getAttribute("scriptType");
					if ((scriptType != null) && !scriptType.isEmpty()) {
						if (!engineDescription.supports(scriptType)) {
							continue;
						}
					}

					// Create IScriptEngineLaunchExtension based on given string
					final Object extension = e.createExecutableExtension("class");
					if (extension instanceof IScriptEngineLaunchExtension) {
						extensions.add((IScriptEngineLaunchExtension) extension);
					}
				}
			} catch (final InvalidRegistryObjectException e1) {
			} catch (final CoreException e1) {
			}
		}

		return extensions;
	}

	@Override
	public synchronized Map<String, ScriptType> getAvailableScriptTypes() {
		if (fScriptTypes == null) {
			fScriptTypes = new HashMap<>();
			final IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_SCRIPTTYPE_ID);

			for (final IConfigurationElement e : config) {
				if ("scriptType".equals(e.getName()))
					fScriptTypes.put(e.getAttribute(SCRIPTTYPE_NAME), new ScriptType(e));
			}
		}

		return fScriptTypes;
	}

	@Override
	public ScriptType getScriptType(final String location) {
		if (location == null) {
			return null;
		}

		final Object resource = ResourceTools.resolve(location);
		try {
			if (resource instanceof IFile) {
				// try to resolve by content type
				final IContentDescription description = ((IFile) resource).getContentDescription();
				if (description != null) {
					final IContentType contentType = description.getContentType();

					for (final ScriptType scriptType : getAvailableScriptTypes().values()) {
						if (scriptType.getContentTypes().contains(contentType.getId()))
							return scriptType;
					}
				}
			}
		} catch (final CoreException e) {
			// could not retrieve content type, continue using file extension
		}

		// try to resolve by extension
		if (location != null) {
			final int pos = location.lastIndexOf('.');
			if (pos != -1) {
				String extension = location.substring(pos + 1);

				if (extension.contains("?"))
					extension = extension.substring(0, extension.indexOf('?'));

				for (final ScriptType scriptType : getAvailableScriptTypes().values()) {
					if (scriptType.getDefaultExtension().equalsIgnoreCase(extension))
						return scriptType;
				}

				// not found, verify content types
				final IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
				final IContentType[] contentTypes = contentTypeManager.findContentTypesFor("foo." + extension);
				if (contentTypes != null) {
					for (final ScriptType scriptType : getAvailableScriptTypes().values()) {

						// now lets see if one of the content types matches
						for (final String contentTypeIdentifier : scriptType.getContentTypes()) {
							for (final IContentType candidate : contentTypes) {
								if (candidate.getId().equals(contentTypeIdentifier))
									return scriptType;
							}
						}
					}
				}
			}
		}

		return null;
	}

	@Override
	public EngineDescription getEngine(final String scriptType) {
		final List<EngineDescription> engines = getEngines(scriptType);
		if (!engines.isEmpty())
			return engines.get(0);

		return null;
	}

	@Override
	public synchronized Map<String, ModuleCategoryDefinition> getAvailableModuleCategories() {
		if (fAvailableModuleCategories == null) {
			fAvailableModuleCategories = new HashMap<>();
			final IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_MODULES_ID);
			for (final IConfigurationElement e : config) {
				if (e.getName().equals(EXTENSION_CATEGORY)) {
					// module category detected
					final ModuleCategoryDefinition definition = new ModuleCategoryDefinition(e);
					fAvailableModuleCategories.put(definition.getId(), definition);
				}
			}
		}
		return fAvailableModuleCategories;
	}

	@Override
	public ModuleDefinition getModuleDefinition(final String moduleId) {
		for (final ModuleDefinition definition : getAvailableModules()) {

			if ((definition.getId().equals(moduleId)) || (definition.getPath().toString().equals(moduleId)))
				return definition;
		}

		return null;
	}

	/**
	 * Get the default {@link ICodeFactory} for a given script engine.
	 *
	 * @param engine
	 *            script engine to look up
	 * @return code factory or <code>null</code>
	 */
	public static ICodeFactory getCodeFactory(final IScriptEngine engine) {
		final EngineDescription description = engine.getDescription();
		if (description != null) {
			final List<ScriptType> scriptTypes = description.getSupportedScriptTypes();
			if (!scriptTypes.isEmpty())
				return scriptTypes.get(0).getCodeFactory();
		}

		return null;
	}

	/**
	 * Get the default {@link ICodeParser} for a given script engine.
	 *
	 * @param engine
	 *            script engine to look up
	 * @return code factory or <code>null</code>
	 */
	public static ICodeParser getCodeParser(final IScriptEngine engine) {
		final EngineDescription description = engine.getDescription();
		if (description != null) {
			final List<ScriptType> scriptTypes = description.getSupportedScriptTypes();
			if (!scriptTypes.isEmpty())
				return scriptTypes.get(0).getCodeParser();
		}

		return null;
	}

	@Override
	public void bundleChanged(BundleEvent event) {
		final int type = event.getType();
		if ((type == BundleEvent.RESOLVED) || (type == BundleEvent.STARTED) || (type == BundleEvent.STOPPED) || (type == BundleEvent.UPDATED)) {
			synchronized (this) {
				// clear cached entries
				fAvailableModules = null;
				fEngineDescriptions = null;
				fScriptTypes = null;
				fAvailableModuleCategories = null;
			}
		}
	}

	@Override
	public Object executeScript(String scriptLocation, String engineID, String... arguments) throws Throwable {
		// find script engine
		EngineDescription engineDescription = null;
		final IScriptService scriptService = ScriptService.getInstance();

		if (engineID != null)
			// locate engine by ID
			engineDescription = scriptService.getEngineByID(engineID);

		else {
			// locate engine by file extension
			final ScriptType scriptType = scriptService.getScriptType(scriptLocation);
			if (scriptType != null)
				engineDescription = scriptService.getEngine(scriptType.getName());
		}

		if (engineDescription != null) {
			// create engine
			final IScriptEngine engine = createEngine(engineDescription);
			engine.setVariable("argv", arguments);

			// TODO implement better URI handling - eg create URI and pass to script engine
			Object scriptObject = ResourceTools.resolve(scriptLocation);
			if (scriptObject == null)
				// no file available, try to include to resolve URIs
				scriptObject = "include(\"" + scriptLocation + "\")";

			final ScriptResult scriptResult = engine.execute(scriptObject);
			engine.schedule();

			return scriptResult.get();
		}

		throw new IllegalArgumentException("Cannot locate a matching script engine");
	}

	@Override
	public IScriptEngine createEngine(EngineDescription description) {
		IScriptEngine engine;
		try {
			engine = description.createInstance();

			// configure engine
			if (engine instanceof AbstractScriptEngine)
				((AbstractScriptEngine) engine).setEngineDescription(description);

			// register engine locally
			fRunningEngines.add(engine);
			engine.addExecutionListener(this);

			// inform listeners
			for (final IScriptEngineLaunchExtension listener : fEngineListeners)
				listener.createEngine(engine);

			// engine loaded, now load launch extensions
			for (final IScriptEngineLaunchExtension extension : getLaunchExtensions(description))
				extension.createEngine(engine);

			return engine;

		} catch (final CoreException e) {
			Logger.error(Activator.PLUGIN_ID, "Could not create script engine: " + description.getID(), e);
		}

		return null;
	}

	@Override
	public Collection<IScriptEngine> getRunningEngines() {
		return fRunningEngines;
	}

	@Override
	public void notify(IScriptEngine engine, Script script, int status) {
		if (status == IExecutionListener.ENGINE_END)
			fRunningEngines.remove(engine);
	}
}
