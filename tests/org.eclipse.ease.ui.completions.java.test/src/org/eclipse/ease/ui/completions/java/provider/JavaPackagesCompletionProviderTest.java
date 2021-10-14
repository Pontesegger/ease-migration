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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;

import org.eclipse.ease.ui.completion.BasicContext;
import org.eclipse.ease.ui.completion.ScriptCompletionProposal;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class JavaPackagesCompletionProviderTest {

	@BeforeAll
	public static void beforeAll() {
		// preload java packages

		while (!JavaResources.getInstance().getPackages().contains("java.io")) {
			try {
				Thread.sleep(300);
			} catch (final InterruptedException e) {
			}
		}
	}

	private JavaPackagesCompletionProvider fProvider;

	@BeforeEach
	public void beforeEach() {
		fProvider = new JavaPackagesCompletionProvider();
	}

	@ParameterizedTest(name = "for context ''{0}''")
	@DisplayName("isActive() = true")
	@ValueSource(strings = { "", "j", "java", "java.", "java.i", "java.lang.ref", "foo(", "foo(co", "java.io.File(", "java.io.File(jav",
			"java.io.File().exists(", "java.io.File().exists(org", "new java", "f=java" })
	public void isActive_equals_true(String input) {
		assertTrue(fProvider.isActive(createContext(input)));
	}

	@ParameterizedTest(name = "for context ''{0}''")
	@DisplayName("isActive() = false")
	@ValueSource(strings = { "foo()", "foo().bar", "java.io.File", "include(\"" })
	public void isActive_equals_false(String input) {
		assertFalse(fProvider.isActive(createContext(input)));
	}

	@ParameterizedTest(name = "''{0}''")
	@DisplayName("prepareProposals() contains root package")
	@ValueSource(strings = { "java", "com", "org" })
	public void prepareProposals_contains_root_package(String rootPackage) {
		final Collection<ScriptCompletionProposal> proposals = fProvider.getProposals(createContext(""));

		final ScriptCompletionProposal proposal = findProposal(proposals, rootPackage);
		assertNotNull(proposal);
	}

	@Test
	@DisplayName("prepareProposals() contains 'java.io'")
	public void prepareProposals_instance_contains_sub_package() {
		final Collection<ScriptCompletionProposal> proposals = fProvider.getProposals(createContext("java."));

		final ScriptCompletionProposal proposal = findProposal(proposals, "java.io");
		assertNotNull(proposal);
	}

	@Test
	@DisplayName("prepareProposals() contains 'java.lang.reflect'")
	public void prepareProposals_instance_contains_3rd_level() {
		final Collection<ScriptCompletionProposal> proposals = fProvider.getProposals(createContext("java.lang."));

		final ScriptCompletionProposal proposal = findProposal(proposals, "java.lang.reflect");
		assertNotNull(proposal);
	}

	@Test
	@DisplayName("prepareProposals() contains 'java' after 'new' keyword")
	public void prepareProposals_instance_contains_root_package_after_new_keyword() {
		final Collection<ScriptCompletionProposal> proposals = fProvider.getProposals(createContext("new java"));

		final ScriptCompletionProposal proposal = findProposal(proposals, "java");
		assertNotNull(proposal);
	}

	@Test
	@DisplayName("proposal 'java' replaces filter")
	public void proposal_java_replaces_filter() {
		final Collection<ScriptCompletionProposal> proposals = fProvider.getProposals(createContext("ja"));

		final ScriptCompletionProposal proposal = findProposal(proposals, "java");
		assertEquals("java.", proposal.getContent());
		assertEquals(5, proposal.getCursorPosition());
	}

	@Test
	@DisplayName("proposal 'java' appends to prefix")
	public void proposal_java_appends_to_prefix() {
		final Collection<ScriptCompletionProposal> proposals = fProvider.getProposals(createContext("new ja"));

		final ScriptCompletionProposal proposal = findProposal(proposals, "java");
		assertEquals("new java.", proposal.getContent());
		assertEquals(9, proposal.getCursorPosition());
	}

	@Test
	@DisplayName("proposal 'java' preserves suffix")
	public void proposal_java_preserves_suffix() {
		final Collection<ScriptCompletionProposal> proposals = fProvider.getProposals(new BasicContext(null, null, "jaio.File", 2));

		final ScriptCompletionProposal proposal = findProposal(proposals, "java");
		assertEquals("java.io.File", proposal.getContent());
		assertEquals(5, proposal.getCursorPosition());
	}

	@Test
	@DisplayName("prepareProposals() is empty for context 'java.io.notthere'")
	public void prepareProposals_is_empty_for_exact_class_context() {
		final Collection<ScriptCompletionProposal> proposals = fProvider.getProposals(createContext("java.io.notthere"));

		assertTrue(proposals.isEmpty());
	}

	private ScriptCompletionProposal findProposal(Collection<ScriptCompletionProposal> proposals, String displayString) {
		return proposals.stream().filter(p -> p.getDisplayString().startsWith(displayString)).findFirst().orElseGet(() -> null);
	}

	private BasicContext createContext(String input) {
		return new BasicContext(null, null, input, input.length());
	}
}
