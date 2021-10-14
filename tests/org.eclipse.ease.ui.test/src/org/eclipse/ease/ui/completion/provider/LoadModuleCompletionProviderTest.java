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

package org.eclipse.ease.ui.completion.provider;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.modules.EnvironmentModule;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.ui.completion.BasicContext;
import org.eclipse.ease.ui.completion.ScriptCompletionProposal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class LoadModuleCompletionProviderTest {

	@ParameterizedTest(name = "for ''{0}''")
	@DisplayName("isActive() = true")
	@ValueSource(strings = { "loadModule('", "loadModule(\"Plat" })
	public void isActive_equals_true(String input) {
		assertTrue(new LoadModuleCompletionProvider().isActive(createContext(input)));
	}

	@ParameterizedTest(name = "for ''{0}''")
	@DisplayName("isActive() = false")
	@ValueSource(strings = { "loadModule(", "loadModul(", "loadModule(\"\", " })
	public void isActive_equals_false(String input) {
		assertFalse(new LoadModuleCompletionProvider().isActive(createContext(input)));
	}

	@Test
	@DisplayName("getProposals() contains root entry for loadModule(")
	public void getProposals_contains_root_entry() {
		final Collection<ScriptCompletionProposal> proposals = new LoadModuleCompletionProvider().getProposals(createContext("loadModule("));
		assertNotNull(findProposal(proposals, "Test Root"));
	}

	@Test
	@DisplayName("getProposals() contains root entry for loadModule(\"Te")
	public void getProposals_contains_root_entry_with_relative_filter() {
		final Collection<ScriptCompletionProposal> proposals = new LoadModuleCompletionProvider().getProposals(createContext("loadModule(\"Te"));
		assertNotNull(findProposal(proposals, "Test Root"));
	}

	@Test
	@DisplayName("getProposals() contains root entry for loadModule(\"/Te")
	public void getProposals_contains_root_entry_with_absolute_filter() {
		final Collection<ScriptCompletionProposal> proposals = new LoadModuleCompletionProvider().getProposals(createContext("loadModule(\"/Te"));
		assertNotNull(findProposal(proposals, "Test Root"));
	}

	@Test
	@DisplayName("getProposals() does not contain root entry for loadModule(\"/Foo")
	public void getProposals_does_not_contain_root_entry_on_filter_mismatch() {
		final Collection<ScriptCompletionProposal> proposals = new LoadModuleCompletionProvider().getProposals(createContext("loadModule(\"/foo"));
		assertNull(findProposal(proposals, "Test Root"));
	}

	@Test
	@DisplayName("getProposals() does not contain entry for loadModule(\"/Foo\"")
	public void getProposals_does_not_contain_entry_on_completed_input() {
		final Collection<ScriptCompletionProposal> proposals = new LoadModuleCompletionProvider().getProposals(createContext("loadModule(\"/foo\""));
		assertTrue(proposals.isEmpty());
	}

	@Test
	@DisplayName("getProposals() contains category for loadModule(\"Te")
	public void getProposals_contains_category_with_relative_filter() {
		final Collection<ScriptCompletionProposal> proposals = new LoadModuleCompletionProvider().getProposals(createContext("loadModule(\"Te"));
		assertNotNull(findProposal(proposals, "/Testing"));
	}

	@Test
	@DisplayName("getProposals() contains child entry for loadModule(\"/Testing/")
	public void getProposals_contains_child_entry_with_absolute_filter() {
		final Collection<ScriptCompletionProposal> proposals = new LoadModuleCompletionProvider().getProposals(createContext("loadModule(\"/Testing/"));
		assertNotNull(findProposal(proposals, "Test Sub"));
	}

	private ScriptCompletionProposal findProposal(Collection<ScriptCompletionProposal> proposals, String displayString) {
		return proposals.stream().filter(p -> p.getDisplayString().startsWith(displayString)).findFirst().orElseGet(() -> null);
	}

	private ICompletionContext createContext(String input) {
		final EngineDescription description = mock(EngineDescription.class);
		when(description.getSupportedScriptTypes()).thenReturn(List.of(new ScriptType(null)));

		final IScriptEngine engine = mock(IScriptEngine.class);
		when(engine.getDescription()).thenReturn(description);
		final Map<String, Object> variables = new HashMap<>();
		variables.put(IEnvironment.MODULE_PREFIX + "environment", new EnvironmentModule());
		when(engine.getVariables()).thenReturn(variables);

		return new BasicContext(engine, input, input.length());
	}
}
