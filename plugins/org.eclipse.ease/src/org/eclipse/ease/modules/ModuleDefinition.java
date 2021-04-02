/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.modules;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ease.Activator;
import org.eclipse.ease.Logger;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;
import org.eclipse.ease.tools.ContributionTools;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.Bundle;
import org.osgi.service.prefs.Preferences;

public class ModuleDefinition {

	public static ModuleDefinition forInstance(Object element) {
		final List<ModuleDefinition> modules = new ArrayList<>(ScriptService.getInstance().getAvailableModules());

		return modules.stream().filter(d -> Objects.equals(d.getModuleClass(), element.getClass())).findAny().orElse(null);
	}

	public static class ModuleDependency {

		/** Module dependency parameter name. */
		private static final String CONFIG_DEPENDENCY_ID = "module";

		private final IConfigurationElement fElement;

		public ModuleDependency(IConfigurationElement element) {
			fElement = element;
		}

		public String getId() {
			return fElement.getAttribute(CONFIG_DEPENDENCY_ID);
		}

		public ModuleDefinition getDefinition() {
			final IScriptService scriptService = ScriptService.getInstance();
			return scriptService.getModuleDefinition(getId());
		}
	}

	/** Module name parameter. */
	private static final String NAME = "name";

	/** Module class parameter. */
	private static final String CLASS = "class";

	/** Module visibility parameter. */
	private static final String VISIBLE = "visible";

	/** Module category parameter. */
	private static final String CATEGORY = "category";

	/** Module dependency node. */
	private static final String DEPENDENCY = "dependency";

	/** Module id parameter name. */
	private static final String ID = "id";

	/** Module icon parameter name. */
	private static final String ICON = "icon";

	/**
	 * Retrieve the module definition for a given module instance.
	 *
	 * @param module
	 *            module instance to look up
	 * @return module definition or <code>null</code>
	 */
	public static ModuleDefinition getDefinition(final Object module) {
		final IScriptService scriptService = ScriptService.getService();
		for (final ModuleDefinition definition : scriptService.getAvailableModules()) {
			if (definition.getModuleClass().equals(module.getClass()))
				return definition;
		}

		return null;
	}

	/** Main configuration element for module. */
	private final IConfigurationElement fConfig;

	private IPath fPath = null;

	public ModuleDefinition(final IConfigurationElement config) {
		fConfig = config;
	}

	public String getName() {
		return fConfig.getAttribute(NAME);
	}

	/**
	 * Get module dependencies.
	 *
	 * @return required dependencies
	 */
	public List<ModuleDependency> getDependencies() {
		final List<ModuleDependency> dependencies = new ArrayList<>();

		for (final IConfigurationElement element : fConfig.getChildren(DEPENDENCY))
			dependencies.add(new ModuleDependency(element));

		return dependencies;
	}

	/**
	 * Get the class definition of the provided module. Will not (by default) create an instance of this class, but look up the class definition directly.
	 *
	 * @return class definition of module contribution
	 */
	public Class<?> getModuleClass() {
		final Bundle bundle = Platform.getBundle(fConfig.getDeclaringExtension().getContributor().getName());
		if (bundle != null) {
			try {
				final String className = fConfig.getAttribute(CLASS);
				return Platform.getBundle(fConfig.getDeclaringExtension().getContributor().getName()).loadClass(className);
			} catch (final InvalidRegistryObjectException e) {
				// ignore
			} catch (final ClassNotFoundException e) {
				// ignore
			}
		}

		// we could not locate the class, try to create instance
		final Object instance = createModuleInstance();
		if (instance != null)
			return createModuleInstance().getClass();

		return null;
	}

	/**
	 * Create a new instance of the module.
	 *
	 * @return module instance
	 */
	public Object createModuleInstance() {
		try {
			return fConfig.createExecutableExtension(CLASS);
		} catch (final CoreException e) {
			// could not create class, ignore
			Logger.error(Activator.PLUGIN_ID, "Could not create module instance", e);
		}

		return null;
	}

	/**
	 * Get visibility status of module. Modules have a default visibility stored in its definition. Users may override this setting using preferences. Invisible
	 * modules may still be used in scripts. However they are not visible in the UI.
	 *
	 * @return <code>true</code> when visible
	 */
	public boolean isVisible() {
		final IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		final Preferences node = prefs.node("modules");
		return node.getBoolean(getPath().toString(), Boolean.parseBoolean(fConfig.getAttribute(VISIBLE)));
	}

	/**
	 * Sets visibility status of module in preferences
	 *
	 * @param visible
	 *            <code>true</code> to make visible
	 */
	public void setVisible(final boolean visible) {
		final IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		final Preferences node = prefs.node("modules");
		node.putBoolean(getPath().toString(), visible);
	}

	/**
	 * Reset visibility to defaults.
	 */
	public void resetVisible() {
		final IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		final Preferences node = prefs.node("modules");
		node.remove(getPath().toString());
	}

	/**
	 * Get the full module name. The full name consists of optional parent categories and the module name itself.
	 *
	 * @return absolute path of this module definition
	 */
	public IPath getPath() {
		if (fPath == null) {
			final IScriptService scriptService = ScriptService.getService();

			fPath = new Path(getName());

			String categoryID = fConfig.getAttribute(CATEGORY);
			while (categoryID != null) {
				final ModuleCategoryDefinition definition = scriptService.getAvailableModuleCategories().get(categoryID);
				if (definition != null) {
					fPath = new Path(definition.getName()).append(fPath);
					categoryID = definition.getParentId();
				} else {
					// invalid category detected
					Logger.error(Activator.PLUGIN_ID, "Invalid category \"" + categoryID + "\" detected for module \"" + getName() + "\"");
					categoryID = null;
				}
			}

			fPath = fPath.makeAbsolute();
		}

		return fPath;
	}

	public String getId() {
		return (fConfig.getAttribute(ID) != null) ? fConfig.getAttribute(ID) : "";
	}

	public ImageDescriptor getImageDescriptor() {
		return ContributionTools.getImageDescriptor(fConfig, ICON);
	}

	public String getBundleID() {
		return fConfig.getContributor().getName();
	}

	public List<Method> getMethods() {
		return ModuleHelper.getMethods(getModuleClass());
	}

	public List<Field> getFields() {
		return ModuleHelper.getFields(getModuleClass());
	}

	/**
	 * Provide the help location for a given topic. Returns the help URI needed to open the according help page.
	 *
	 * @param topic
	 *            help topic within module
	 * @return link to help
	 */
	public String getHelpLocation(final String topic) {
		return "/" + getBundleID() + "/help/module_" + getId().replace(' ', '_').toLowerCase() + ".html" + ((topic != null) ? "#" + topic : "");
	}

	/**
	 * Check deprecation status of module.
	 *
	 * @return <code>true</code> when module is deprecated
	 */
	public boolean isDeprecated() {
		return getModuleClass().getAnnotation(Deprecated.class) != null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ModuleDefinition other = (ModuleDefinition) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}
}
