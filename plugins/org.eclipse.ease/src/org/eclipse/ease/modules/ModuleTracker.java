/*******************************************************************************
 * Copyright (c) 2018 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
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
			pushUp();
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

		public void setWrapped(boolean wrapped) {
			fWrapped = wrapped;
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

	public ModuleState addInstance(Object instance) {
		final ModuleState state = new ModuleState();

		fAvailableModules.add(0, state);
		state.setInstance(instance);

		return state;
	}

	public ModuleState getModuleState(ModuleDefinition definition) {
		for (final ModuleState state : getAvailableModules()) {
			if (state.getModuleDefinition() != null) {
				if (definition.getId().equals(state.getModuleDefinition().getId()))
					return state;
			}
		}

		return null;
	}

	public ModuleState getOrCreateModuleState(ModuleDefinition definition) {
		final ModuleState state = getModuleState(definition);

		return (state != null) ? state : addModule(definition);
	}

	public List<ModuleState> getAvailableModules() {
		return new ArrayList<>(fAvailableModules);
	}
}
