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
package org.eclipse.ease.modules;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;

public class ModuleCategoryDefinition {

	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String PARENT = "parent";

	private final IConfigurationElement fConfig;

	public ModuleCategoryDefinition(IConfigurationElement config) {
		fConfig = config;
	}

	public String getId() {
		return (fConfig.getAttribute(ID) != null) ? fConfig.getAttribute(ID) : "";
	}

	public String getParentId() {
		return fConfig.getAttribute(PARENT);
	}

	public String getName() {
		return fConfig.getAttribute(NAME);
	}

	public ModuleCategoryDefinition getParentDefinition() {
		if (getParentId() != null) {
			final IScriptService scriptService = ScriptService.getService();
			;
			return scriptService.getAvailableModuleCategories().get(getParentId());
		}

		return null;
	}

	/**
	 * Get full name including parent category names. Categories are separated by slashes.
	 *
	 * @return full name, eg /parentCategory/CategoryName
	 */
	public String getFullName() {
		final ModuleCategoryDefinition parentDefinition = getParentDefinition();
		if (parentDefinition != null)
			return parentDefinition.getFullName() + "/" + getName();

		return "/" + getName();
	}
}
