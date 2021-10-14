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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;

import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.ui.completion.BasicContext;
import org.eclipse.ease.ui.completion.ScriptCompletionProposal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class LoadedModuleCompletionProviderTest {

	@ParameterizedTest(name = "for ''{0}''")
	@DisplayName("isActive() = true")
	@ValueSource(strings = { "", "myMethod", "call(", "call(myMethod", "call(param, ", "call(param, myMe" })
	public void isActive_equals_true(String input) {
		assertTrue(new LoadedModuleCompletionProvider().isActive(createContext(input)));
	}

	@ParameterizedTest(name = "for ''{0}''")
	@DisplayName("isActive() = false")
	@ValueSource(strings = { "java.", "java.io", "call(java.io", "call(\"myMethod" })
	public void isActive_equals_false(String input) {
		assertFalse(new LoadedModuleCompletionProvider().isActive(createContext(input)));
	}

	@Test
	@DisplayName("getProposals() contains method proposals")
	public void getProposals_contains_module_methods() {
		final Collection<ScriptCompletionProposal> proposals = new LoadedModuleCompletionProvider().getProposals(createContext("loadModule(\"/Test Root\")\n"));
		assertNotNull(findProposal(proposals, "testMethod"));
	}

	@Test
	@DisplayName("getProposals() contains constant proposals")
	public void getProposals_contains_constant_methods() {
		final Collection<ScriptCompletionProposal> proposals = new LoadedModuleCompletionProvider().getProposals(createContext("loadModule(\"/Test Root\")\n"));
		assertNotNull(findProposal(proposals, "TEST_CONSTANT"));
	}

	@Test
	@DisplayName("method proposal contains full method call when method has no parameters")
	public void method_proposal_contains_full_method_call_when_method_has_no_parameters() {
		final Collection<ScriptCompletionProposal> proposals = new LoadedModuleCompletionProvider().getProposals(createContext("loadModule(\"/Test Root\")\n"));
		final ScriptCompletionProposal proposal = findProposal(proposals, "testMethod");
		assertEquals("testMethod()", proposal.getContent().split("\n")[1]);
	}

	@Test
	@DisplayName("method proposal contains full method call when method has only optional parameters")
	public void method_proposal_contains_full_method_call_when_method_has_only_optional_parameters() {
		final Collection<ScriptCompletionProposal> proposals = new LoadedModuleCompletionProvider().getProposals(createContext("loadModule(\"/Test Root\")\n"));
		final ScriptCompletionProposal proposal = findProposal(proposals, "testWithOptionalParameters");
		assertEquals("testWithOptionalParameters()", proposal.getContent().split("\n")[1]);
	}

	@Test
	@DisplayName("method proposal contains opening bracket when method has mandatory parameters")
	public void method_proposal_contains_opening_bracket_when_method_has_mandatory_parameters() {
		final Collection<ScriptCompletionProposal> proposals = new LoadedModuleCompletionProvider().getProposals(createContext("loadModule(\"/Test Root\")\n"));
		final ScriptCompletionProposal proposal = findProposal(proposals, "testWithMandatoryParameters");
		assertEquals("testWithMandatoryParameters(", proposal.getContent().split("\n")[1]);
	}

	private ScriptCompletionProposal findProposal(Collection<ScriptCompletionProposal> proposals, String displayString) {
		return proposals.stream().filter(p -> p.getDisplayString().startsWith(displayString)).findFirst().orElseGet(() -> null);
	}

	private ICompletionContext createContext(String input) {
		return new BasicContext(null, null, input, input.length());
	}
}
