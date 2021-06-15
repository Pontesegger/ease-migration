/*******************************************************************************
 * Copyright (c) 2019 Christian Pontesegger and others.
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

package org.eclipse.ease.helpgenerator.model;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.ease.helpgenerator.IMemento;

public class ModuleDefinition {
	private final IMemento fDefinition;

	public ModuleDefinition(IMemento definition) {
		fDefinition = definition;
	}

	public String getId() {
		return fDefinition.getString("id");
	}

	public String getName() {
		return fDefinition.getString("name");
	}

	public String getCategoryId() {
		return fDefinition.getString("category");
	}

	public String getClassName() {
		return fDefinition.getString("class");
	}

	public boolean hasDependencies() {
		return !getDependencies().isEmpty();
	}

	public Collection<ModuleDefinition> getDependencies() {
		final Collection<ModuleDefinition> dependencies = new HashSet<>();
		for (final IMemento node : fDefinition.getChildren("dependency"))
			dependencies.add(new ModuleDefinition(null) {

				@Override
				public String getId() {
					return node.getString("module");
				}
			});

		return dependencies;
	}
}
