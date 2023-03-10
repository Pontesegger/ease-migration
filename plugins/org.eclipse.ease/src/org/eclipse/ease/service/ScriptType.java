/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
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
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ease.Activator;
import org.eclipse.ease.ICodeFactory;
import org.eclipse.ease.ICodeParser;
import org.eclipse.ease.Logger;

public class ScriptType {

	private static final String NAME = "name";
	private static final String DEFAULT_EXTENSION = "defaultExtension";
	private static final String BINDING = "binding";
	private static final String CODE_PARSER = "codeParser";
	private static final String CODE_FACTORY = "codeFactory";
	private static final String CONTENT_TYPE = "contentType";

	private final IConfigurationElement fConfigurationElement;

	public ScriptType(final IConfigurationElement configurationElement) {
		fConfigurationElement = configurationElement;
	}

	public String getName() {
		return fConfigurationElement.getAttribute(NAME);
	}

	public String getDefaultExtension() {
		return fConfigurationElement.getAttribute(DEFAULT_EXTENSION);
	}

	public Collection<String> getContentTypes() {
		final Collection<String> result = new HashSet<>();

		for (final IConfigurationElement binding : fConfigurationElement.getChildren(BINDING))
			result.add(binding.getAttribute(CONTENT_TYPE));

		return result;
	}

	public ICodeParser getCodeParser() {
		try {
			final Object parser = fConfigurationElement.createExecutableExtension(CODE_PARSER);
			if (parser instanceof ICodeParser)
				return (ICodeParser) parser;

		} catch (final CoreException e) {
			// could not instantiate class
			Logger.error(Activator.PLUGIN_ID, "Could not instantiate code parser", e);
		}

		return null;
	}

	public ICodeFactory getCodeFactory() {
		try {
			final Object factory = fConfigurationElement.createExecutableExtension(CODE_FACTORY);
			if (factory instanceof ICodeFactory)
				return (ICodeFactory) factory;

		} catch (final CoreException e) {
			// could not instantiate class
			Logger.error(Activator.PLUGIN_ID, "Could not instantiate code factory", e);
		}

		return null;
	}

	/**
	 * Get available engines. Returns available script engine descriptions sorted by priority (highest first).
	 *
	 * @return available engines
	 */
	public List<EngineDescription> getEngines() {
		final List<EngineDescription> engines = new ArrayList<>();

		final IScriptService scriptService = ScriptService.getService();
		for (final EngineDescription description : scriptService.getEngines()) {
			if (description.getSupportedScriptTypes().contains(this))
				engines.add(description);
		}

		// sort engines to report highest priority first
		Collections.sort(engines, (o1, o2) -> o2.getPriority() - o1.getPriority());

		return engines;
	}

	public EngineDescription getEngine() {
		final List<EngineDescription> engines = getEngines();
		return (engines.isEmpty()) ? null : engines.get(0);
	}

	public EngineDescription getDebugEngine() {
		for (final EngineDescription description : getEngines()) {
			if (description.supportsDebugging())
				return description;
		}

		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ScriptType)
			return getName().equals(((ScriptType) obj).getName());

		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((getName() == null) ? 0 : getName().hashCode());
		result = (prime * result) + ((getDefaultExtension() == null) ? 0 : getDefaultExtension().hashCode());
		return result;
	}
}
