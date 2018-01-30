/*******************************************************************************
 * Copyright (c) 2018 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.python;

import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.modules.ModuleCategoryDefinition;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;

public class Pep302ModuleImporter {

	/**
	 * Verify if a given path is a subpath of an EASE module.
	 *
	 * @param candidate
	 *            module path name candidate
	 * @return <code>true</code> when candidate is a path to an EASE module
	 */
	public static boolean isModulePath(String candidate) {
		// do not alter signature as this is called from python code directly.
		candidate = candidate.toLowerCase();

		if ("eclipse".equals(candidate))
			return true;

		if (candidate.startsWith("eclipse.")) {
			// remove eclipse prefix
			candidate = candidate.substring("eclipse".length());

			final String candidateA = candidate.replace('.', '/');
			final String candidateB = candidateA.replace('_', ' ');

			final IScriptService scriptService = ScriptService.getService();
			for (final ModuleCategoryDefinition category : scriptService.getAvailableModuleCategories().values()) {
				if (category.getFullName().toLowerCase().equals(candidateA))
					return true;

				if (category.getFullName().toLowerCase().equals(candidateB))
					return true;
			}
		}

		return false;
	}

	public static boolean isModule(String candidate) {
		// do not alter signature as this is called from python code directly.
		return getModuleDefinition(candidate) != null;
	}

	private static ModuleDefinition getModuleDefinition(String candidate) {
		candidate = candidate.toLowerCase();

		if (candidate.startsWith("eclipse"))
			candidate = candidate.substring("eclipse".length());

		final String candidateA = candidate.replace('.', '/');
		final String candidateB = candidateA.replace('_', ' ');

		final IScriptService scriptService = ScriptService.getService();

		for (final ModuleDefinition definition : scriptService.getAvailableModules()) {
			if (definition.getPath().toString().toLowerCase().equals(candidateA))
				return definition;

			if (definition.getPath().toString().toLowerCase().equals(candidateB))
				return definition;
		}

		return null;
	}

	public static String getCode(String moduleName, IEnvironment enviromentModule) {
		// do not alter signature as this is called from python code directly.
		final ModuleDefinition definition = getModuleDefinition(moduleName);
		if (definition != null) {
			final Object instance = enviromentModule.createModuleInstance(definition);

			return "print(\"some EASE module\")";

		} else
			throw new RuntimeException("Module <" + moduleName + "> could not be found");
	}
}
