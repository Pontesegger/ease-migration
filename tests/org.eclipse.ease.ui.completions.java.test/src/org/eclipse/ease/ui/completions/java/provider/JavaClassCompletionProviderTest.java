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

public class JavaClassCompletionProviderTest {

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

	private JavaClassCompletionProvider fProvider;

	@BeforeEach
	public void beforeEach() {
		fProvider = new JavaClassCompletionProvider();
	}

	@ParameterizedTest(name = "for context ''{0}''")
	@DisplayName("isActive() = true")
	@ValueSource(strings = { "Fil", "Foo", "java.io", "java.io.", "java.io.F", "java.io.File" })
	public void isActive_equals_true(String input) {
		assertTrue(fProvider.isActive(createContext(input)));
	}

	@ParameterizedTest(name = "for context ''{0}''")
	@DisplayName("isActive() = false")
	@ValueSource(strings = { "Fi", "java." })
	public void isActive_equals_false(String input) {
		assertFalse(fProvider.isActive(createContext(input)));
	}

	@Test
	@DisplayName("prepareProposals() contains 'FileReader' for package context")
	public void prepareProposals_instance_contains_class_for_package_context() {
		final Collection<ScriptCompletionProposal> proposals = fProvider.getProposals(createContext("java.io."));

		final ScriptCompletionProposal proposal = findProposal(proposals, "FileReader");
		assertNotNull(proposal);
	}

	@Test
	@DisplayName("prepareProposals() contains 'FileReader' for global context")
	public void prepareProposals_instance_contains_class_for_global_context() {
		final Collection<ScriptCompletionProposal> proposals = fProvider.getProposals(createContext("File"));

		final ScriptCompletionProposal proposal = findProposal(proposals, "FileReader");
		assertNotNull(proposal);
	}

	private ScriptCompletionProposal findProposal(Collection<ScriptCompletionProposal> proposals, String displayString) {
		return proposals.stream().filter(p -> p.getDisplayString().startsWith(displayString)).findFirst().orElseGet(() -> null);
	}

	private BasicContext createContext(String input) {
		return new BasicContext(null, null, input, input.length());
	}
}
