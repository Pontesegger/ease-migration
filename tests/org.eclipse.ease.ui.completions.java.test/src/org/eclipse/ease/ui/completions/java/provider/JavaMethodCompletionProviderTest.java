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

package org.eclipse.ease.ui.completions.java.provider;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;

import org.eclipse.ease.ui.completion.BasicContext;
import org.eclipse.ease.ui.completion.ScriptCompletionProposal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class JavaMethodCompletionProviderTest {

	@ParameterizedTest(name = "for context ''{0}''")
	@DisplayName("isActive() = true")
	@ValueSource(strings = { "java.io.File()", "java.io.File().", "java.io.File().exis", "java.io.File", "java.io.File.", "java.io.File.exis" })
	public void isActive_equals_true(String input) {
		assertTrue(new JavaMethodCompletionProvider().isActive(createContext(input)));
	}

	@ParameterizedTest(name = "for context ''{0}''")
	@DisplayName("isActive() = false")
	@ValueSource(strings = { "java.io", "java.io.File(", "File()" })
	public void isActive_equals_false(String input) {
		assertFalse(new JavaMethodCompletionProvider().isActive(createContext(input)));
	}

	@Test
	@DisplayName("prepareProposals() instance contains instance methods")
	public void prepareProposals_instance_contains_instance_methods() {
		final Collection<ScriptCompletionProposal> proposals = new JavaMethodCompletionProvider().getProposals(createContext("java.io.File()."));

		final ScriptCompletionProposal proposal = findProposal(proposals, "exists()");
		assertNotNull(proposal);
	}

	@Test
	@DisplayName("prepareProposals() instance does not contain static methods")
	public void prepareProposals_instance_does_not_contain_static_methods() {
		final Collection<ScriptCompletionProposal> proposals = new JavaMethodCompletionProvider().getProposals(createContext("java.io.File()."));

		final ScriptCompletionProposal proposal = findProposal(proposals, "createTempFile(");
		assertNull(proposal);
	}

	@Test
	@DisplayName("prepareProposals() class contains static methods")
	public void prepareProposals_class_contains_static_methods() {
		final Collection<ScriptCompletionProposal> proposals = new JavaMethodCompletionProvider().getProposals(createContext("java.io.File."));

		final ScriptCompletionProposal proposal = findProposal(proposals, "createTempFile(");
		assertNotNull(proposal);
	}

	@Test
	@DisplayName("prepareProposals() class does not contain instance methods")
	public void prepareProposals_class_does_not_contain_isntance_methods() {
		final Collection<ScriptCompletionProposal> proposals = new JavaMethodCompletionProvider().getProposals(createContext("java.io.File."));

		final ScriptCompletionProposal proposal = findProposal(proposals, "exists()");
		assertNull(proposal);
	}

	@Test
	@DisplayName("prepareProposals() instance does not contain static fields")
	public void prepareProposals_instance_does_not_contain_static_fields() {
		final Collection<ScriptCompletionProposal> proposals = new JavaMethodCompletionProvider().getProposals(createContext("java.io.File()."));

		final ScriptCompletionProposal proposal = findProposal(proposals, "pathSeparator");
		assertNull(proposal);
	}

	@Test
	@DisplayName("prepareProposals() class contains static fields")
	public void prepareProposals_class_contains_static_fields() {
		final Collection<ScriptCompletionProposal> proposals = new JavaMethodCompletionProvider().getProposals(createContext("java.io.File."));

		final ScriptCompletionProposal proposal = findProposal(proposals, "pathSeparator");
		assertNotNull(proposal);
	}

	@Test
	@DisplayName("prepareProposals() instance contains method with filter")
	public void prepareProposals_instance_contains_method_with_filter() {
		final Collection<ScriptCompletionProposal> proposals = new JavaMethodCompletionProvider().getProposals(createContext("java.io.File().exi"));

		final ScriptCompletionProposal proposal = findProposal(proposals, "exists()");
		assertNotNull(proposal);
	}

	private ScriptCompletionProposal findProposal(Collection<ScriptCompletionProposal> proposals, String displayString) {
		return proposals.stream().filter(p -> p.getDisplayString().startsWith(displayString)).findFirst().orElseGet(() -> null);
	}

	private BasicContext createContext(String input) {
		return new BasicContext(null, null, input, input.length());
	}
}
