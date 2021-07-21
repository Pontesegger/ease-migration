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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.ui.completion.BasicContext;
import org.eclipse.ease.ui.completion.ScriptCompletionProposal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class VariablesCompletionProviderTest {

	private VariablesCompletionProvider fProvider;

	@BeforeEach
	public void beforeEach() {
		fProvider = new VariablesCompletionProvider();
	}

	@ParameterizedTest(name = "for context ''{0}''")
	@DisplayName("isActive() = true")
	@ValueSource(strings = { "", "foo", "Foo", "method(", "method(45,", "method(45, foo", "java.io.File(", "java.io.File().exists(" })
	public void isActive_equals_true(String input) {
		assertTrue(fProvider.isActive(createContext(input)));
	}

	@ParameterizedTest(name = "for context ''{0}''")
	@DisplayName("isActive() = false")
	@ValueSource(strings = { "com.", "method()" })
	public void isActive_equals_false(String input) {
		assertFalse(fProvider.isActive(createContext(input)));
	}

	@Test
	@DisplayName("prepareProposals() contains string variable")
	public void prepareProposals_instance_contains_string_variable() {
		final Collection<ScriptCompletionProposal> proposals = fProvider.getProposals(createContext(""));

		final ScriptCompletionProposal proposal = findProposal(proposals, "myString");
		assertNotNull(proposal);
	}

	@Test
	@DisplayName("prepareProposals() contains string variable for filter input")
	public void prepareProposals_instance_contains_string_variable_for_filter_input() {
		final Collection<ScriptCompletionProposal> proposals = fProvider.getProposals(createContext("myStr"));

		final ScriptCompletionProposal proposal = findProposal(proposals, "myString");
		assertNotNull(proposal);
	}

	@Test
	@DisplayName("prepareProposals() contains string variable for filter input (case insensitive))")
	public void prepareProposals_instance_contains_string_variable_for_filter_input_case_insensitive() {
		final Collection<ScriptCompletionProposal> proposals = fProvider.getProposals(createContext("MYSTR"));

		final ScriptCompletionProposal proposal = findProposal(proposals, "myString");
		assertNotNull(proposal);
	}

	@Test
	@DisplayName("prepareProposals() contains integer variable")
	public void prepareProposals_instance_contains_integer_variable() {
		final Collection<ScriptCompletionProposal> proposals = fProvider.getProposals(createContext(""));

		final ScriptCompletionProposal proposal = findProposal(proposals, "myNumber");
		assertNotNull(proposal);
	}

	@Test
	@DisplayName("prepareProposals() does not contain internal variable")
	public void prepareProposals_does_not_contain_internal_variable() {
		final Collection<ScriptCompletionProposal> proposals = fProvider.getProposals(createContext(""));

		final ScriptCompletionProposal proposal = findProposal(proposals, IEnvironment.MODULE_PREFIX + "SOMETHING");
		assertNull(proposal);
	}

	private ScriptCompletionProposal findProposal(Collection<ScriptCompletionProposal> proposals, String displayString) {
		return proposals.stream().filter(p -> p.getDisplayString().startsWith(displayString)).findFirst().orElseGet(() -> null);
	}

	private BasicContext createContext(String input) {
		final Map<String, Object> variables = new HashMap<>();
		variables.put("myString", "Hello world");
		variables.put("myNumber", 42);
		variables.put(IEnvironment.MODULE_PREFIX + "SOMETHING", "not visible");

		final EngineDescription description = mock(EngineDescription.class);
		when(description.getSupportedScriptTypes()).thenReturn(Collections.singletonList(null));

		final IScriptEngine scriptEngine = mock(IScriptEngine.class);
		when(scriptEngine.getVariables()).thenReturn(variables);
		when(scriptEngine.getDescription()).thenReturn(description);

		return new BasicContext(scriptEngine, input, input.length());
	}
}
