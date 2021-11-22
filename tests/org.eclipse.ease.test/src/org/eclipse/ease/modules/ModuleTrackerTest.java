/*******************************************************************************
 * Copyright (c) 2021 Christian Pontesegger and others.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.ease.modules.ModuleTracker.ModuleState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ModuleTrackerTest {

	private ModuleTracker fTracker;

	@BeforeEach
	public void beforeEach() {
		fTracker = new ModuleTracker();
	}

	@Test
	@DisplayName("new tracker does not contain modules")
	public void new_tracker_does_not_contain_modules() {
		assertTrue(fTracker.getAvailableModules().isEmpty());
	}

	@Test
	@DisplayName("addModule() adds a module state")
	public void addModule_adds_a_module_state() {
		final ModuleDefinition definition = mock(ModuleDefinition.class);

		fTracker.addModule(definition);

		assertEquals(definition, fTracker.getAvailableModules().get(0).getModuleDefinition());
	}

	@Test
	@DisplayName("addModule() adds on top of stack")
	public void addModule_adds_on_top_of_stack() {
		final ModuleDefinition definition1 = mock(ModuleDefinition.class);
		final ModuleDefinition definition2 = mock(ModuleDefinition.class);

		fTracker.addModule(definition1);
		fTracker.addModule(definition2);

		assertEquals(definition2, fTracker.getAvailableModules().get(0).getModuleDefinition());
	}

	@Test
	@DisplayName("getModuleState() = null when module is not available")
	public void getModuleState_is_null_when_module_is_not_available() {
		final ModuleDefinition definition = mock(ModuleDefinition.class);
		when(definition.getId()).thenReturn("notThere");

		assertNull(fTracker.getModuleState(definition));
	}

	@Test
	@DisplayName("getOrCreateModuleState() creates new module state")
	public void getOrCreateModuleState_creates_new_module_state() {
		final ModuleDefinition definition = mock(ModuleDefinition.class);

		final ModuleState state = fTracker.getOrCreateModuleState(definition);

		assertEquals(definition, state.getModuleDefinition());
		assertEquals(definition, fTracker.getAvailableModules().get(0).getModuleDefinition());
	}

	@Test
	@DisplayName("getOrCreateModuleState() returns existing state")
	public void getOrCreateModuleState_returns_existing_state() {
		final ModuleDefinition definition = mock(ModuleDefinition.class);

		final ModuleState addedState = fTracker.addModule(definition);

		final ModuleState detectedState = fTracker.getOrCreateModuleState(definition);

		assertEquals(addedState, detectedState);
		assertEquals(1, fTracker.getAvailableModules().size());
		assertEquals(definition, fTracker.getAvailableModules().get(0).getModuleDefinition());
	}

	@Test
	@DisplayName("State.isLoaded() = false when no instance is set")
	public void state_isLoaded_is_false_when_no_instance_is_set() {
		final ModuleDefinition definition = mock(ModuleDefinition.class);
		final ModuleState state = fTracker.addModule(definition);

		assertFalse(state.isLoaded());
	}

	@Test
	@DisplayName("State.isLoaded() = true when instance is set")
	public void state_isLoaded_is_true_when_instance_is_set() {
		final ModuleDefinition definition = mock(ModuleDefinition.class);
		final ModuleState state = fTracker.addModule(definition);
		state.setInstance("instance");

		assertTrue(state.isLoaded());
	}

	@Test
	@DisplayName("State.isWrapped() = false after definition is loaded")
	public void state_iisWrapped_is_false_after_definition_is_loaded() {
		final ModuleDefinition definition = mock(ModuleDefinition.class);
		final ModuleState state = fTracker.addModule(definition);

		assertFalse(state.isWrapped());
	}

	@Test
	@DisplayName("State.isWrapped() = true after wrapping the module")
	public void state_isWrapped_is_true_after_wrapping_the_module() {
		final ModuleDefinition definition = mock(ModuleDefinition.class);
		final ModuleState state = fTracker.addModule(definition);
		state.setWrapped();

		assertTrue(state.isWrapped());
	}

	@Test
	@DisplayName("State.setWrapped() pushes module on top of stack")
	public void state_setWrapped_pushes_module_on_top_of_stack() {
		final ModuleDefinition definition1 = mock(ModuleDefinition.class);
		final ModuleDefinition definition2 = mock(ModuleDefinition.class);

		final ModuleState state1 = fTracker.addModule(definition1);
		final ModuleState state2 = fTracker.addModule(definition2);

		assertEquals(state2, fTracker.getAvailableModules().get(0));
		assertEquals(state1, fTracker.getAvailableModules().get(1));

		state1.setWrapped();

		assertEquals(state1, fTracker.getAvailableModules().get(0));
		assertEquals(state2, fTracker.getAvailableModules().get(1));
	}
}
