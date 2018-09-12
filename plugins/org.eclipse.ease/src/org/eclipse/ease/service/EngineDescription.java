/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.service;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.ease.AbstractScriptEngine;
import org.eclipse.ease.Activator;
import org.eclipse.ease.IDebugEngine;
import org.eclipse.ease.IReplEngine;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.IScriptEngineLaunchExtension;
import org.eclipse.ease.Logger;
import org.eclipse.ease.classloader.EaseClassLoader;

public class EngineDescription {

	private static final String CLASS = "class";

	private static final String PRIORITY = "priority";

	private static final String BINDING = "binding";

	private static final String TYPE = "scriptType";

	private static final String ID = "id";

	private static final String NAME = "name";

	private final IConfigurationElement fConfigurationElement;

	private List<ScriptType> fTypes = null;

	public EngineDescription(final IConfigurationElement configurationElement) {
		fConfigurationElement = configurationElement;
	}

	// public Collection<String> getSupportedScriptTypesNames() {
	// return Collections2.transform(getSupportedScriptTypes(), new ScriptType.ToScriptType());
	// }
	//
	public List<ScriptType> getSupportedScriptTypes() {
		if (fTypes == null) {
			fTypes = new ArrayList<>();
			final IScriptService scriptService = ScriptService.getService();

			for (final IConfigurationElement child : fConfigurationElement.getChildren(BINDING)) {
				final String scriptTypeID = child.getAttribute(TYPE);

				if (scriptTypeID != null) {
					final ScriptType scriptType = scriptService.getAvailableScriptTypes().get(scriptTypeID);
					if (scriptType == null)
						Logger.error(Activator.PLUGIN_ID, "Unknow scriptType " + scriptTypeID);
					else
						fTypes.add(scriptType);
				}
			}
		}

		return fTypes;
	}

	public int getPriority() {
		try {
			return Integer.parseInt(fConfigurationElement.getAttribute(PRIORITY));
		} catch (final Throwable e) {
			// ignore
		}

		return 0;
	}

	/**
	 * Create a dedicated script engine.
	 *
	 * @return script engine or <code>null</code>
	 */
	public IScriptEngine createEngine() {
		try {
			final Object object = fConfigurationElement.createExecutableExtension(CLASS);
			if (object instanceof IScriptEngine) {
				// configure engine
				if (object instanceof AbstractScriptEngine)
					((AbstractScriptEngine) object).setEngineDescription(this);

				// engine loaded, now load launch extensions
				final IScriptService scriptService = ScriptService.getService();
				for (final IScriptEngineLaunchExtension extension : scriptService.getLaunchExtensions(this))
					extension.createEngine((IScriptEngine) object);

				return (IScriptEngine) object;
			}

		} catch (final CoreException e) {
			Logger.error(Activator.PLUGIN_ID, "Could not create script engine: " + getID(), e);
		}

		return null;
	}

	public String getID() {
		return fConfigurationElement.getAttribute(ID);
	}

	public String getName() {
		final String name = fConfigurationElement.getAttribute(NAME);
		return (name != null) ? name : getID();
	}

	public boolean supports(final String scriptType) {
		for (final ScriptType type : getSupportedScriptTypes()) {
			if (type.getName().equals(scriptType))
				return true;
		}
		return false;
	}

	public boolean supports(final IFile file) {

		// try to resolve by content type
		try {
			final IContentType fileContentType = file.getContentDescription().getContentType();

			for (final ScriptType type : getSupportedScriptTypes()) {
				for (final String contentType : type.getContentTypes())
					if (contentType.equals(fileContentType.getId()))
						return true;
			}

		} catch (final CoreException e) {
			// did not work. Fall back to file extension
			for (final ScriptType type : getSupportedScriptTypes()) {
				if (file.getFileExtension().equalsIgnoreCase(type.getDefaultExtension()))
					return true;
			}
		}

		return false;
	}

	@Override
	public String toString() {
		return getName();
	}

	public boolean supportsDebugging() {
		return IDebugEngine.class.isAssignableFrom(getEngineClass());
	}

	public boolean isReplShell() {
		return IReplEngine.class.isAssignableFrom(getEngineClass());
	}

	private Class<?> getEngineClass() {
		final String className = fConfigurationElement.getAttribute(CLASS);
		try {
			return new EaseClassLoader().loadClass(className);
		} catch (final ClassNotFoundException e) {
			return createEngine().getClass();
		}
	}
}
