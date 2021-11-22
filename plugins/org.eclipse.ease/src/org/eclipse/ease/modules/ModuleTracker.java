/*******************************************************************************
 * Copyright (c) 2018 Christian Pontesegger and others.
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

import java.util.ArrayList;
import java.util.List;

public class ModuleTracker {

	public class ModuleState {

		private Object fInstance;
		private ModuleDefinition fModuleDefinition;

		private boolean fWrapped = false;

		public void setInstance(Object instance) {
			fInstance = instance;
		}

		public Object getInstance() {
			return fInstance;
		}

		public void setModuleDefinition(ModuleDefinition moduleDefinition) {
			fModuleDefinition = moduleDefinition;
		}

		public ModuleDefinition getModuleDefinition() {
			return fModuleDefinition;
		}

		public boolean isLoaded() {
			return fInstance != null;
		}

		public boolean isWrapped() {
			return fWrapped;
		}

		public void setWrapped() {
			fWrapped = true;
			pushUp();
		}

		private void pushUp() {
			fAvailableModules.remove(this);
			fAvailableModules.add(0, this);
		}
	}

	private final List<ModuleState> fAvailableModules = new ArrayList<>();

	public ModuleState addModule(ModuleDefinition definition) {
		final ModuleState state = new ModuleState();
		state.setModuleDefinition(definition);

		fAvailableModules.add(0, state);

		return state;
	}

	public ModuleState getModuleState(ModuleDefinition definition) {
		return getAvailableModules().stream().filter(s -> s.getModuleDefinition() != null).filter(s -> s.getModuleDefinition().equals(definition)).findFirst()
				.orElse(null);
	}

	public ModuleState getOrCreateModuleState(ModuleDefinition definition) {
		final ModuleState state = getModuleState(definition);

		return (state != null) ? state : addModule(definition);
	}

	public List<ModuleState> getAvailableModules() {
		return new ArrayList<>(fAvailableModules);
	}
}
