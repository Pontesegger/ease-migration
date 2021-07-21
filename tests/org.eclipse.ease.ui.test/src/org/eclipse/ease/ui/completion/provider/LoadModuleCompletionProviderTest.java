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

import java.util.Collection;

import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.ui.completion.BasicContext;
import org.eclipse.ease.ui.completion.ScriptCompletionProposal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LoadModuleCompletionProviderTest {

	@Test
	@DisplayName("isActive() = true for loadModule(|")
	public void isActive_equals_true_for_method() {
		assertTrue(new LoadModuleCompletionProvider().isActive(getContext("loadModule(")));
	}

	@Test
	@DisplayName("isActive() = true for loadModule(\"Plat|")
	public void isActive_equals_true_for_partial_module_name() {
		assertTrue(new LoadModuleCompletionProvider().isActive(getContext("loadModule(\"Plat")));
	}

	@Test
	@DisplayName("isActive() = false for loadModul(|")
	public void isActive_equals_true_for_wrong_method_name() {
		assertFalse(new LoadModuleCompletionProvider().isActive(getContext("loadModul(")));
	}

	@Test
	@DisplayName("isActive() = false for loadModule(\"\", |")
	public void isActive_equals_true_for_wrong_method_parameter() {
		assertFalse(new LoadModuleCompletionProvider().isActive(getContext("loadModule(\"\",")));
	}

	@Test
	@DisplayName("getProposals() contains root entry for loadModule(")
	public void getProposals_contains_root_entry() {
		final Collection<ScriptCompletionProposal> proposals = new LoadModuleCompletionProvider().getProposals(getContext("loadModule("));
		assertNotNull(findProposal(proposals, "Test Root"));
	}

	@Test
	@DisplayName("getProposals() contains root entry for loadModule(\"Te")
	public void getProposals_contains_root_entry_with_relative_filter() {
		final Collection<ScriptCompletionProposal> proposals = new LoadModuleCompletionProvider().getProposals(getContext("loadModule(\"Te"));
		assertNotNull(findProposal(proposals, "Test Root"));
	}

	@Test
	@DisplayName("getProposals() contains root entry for loadModule(\"/Te")
	public void getProposals_contains_root_entry_with_absolute_filter() {
		final Collection<ScriptCompletionProposal> proposals = new LoadModuleCompletionProvider().getProposals(getContext("loadModule(\"/Te"));
		assertNotNull(findProposal(proposals, "Test Root"));
	}

	@Test
	@DisplayName("getProposals() does not contain root entry for loadModule(\"/Foo")
	public void getProposals_does_not_contain_root_entry_on_filter_mismatch() {
		final Collection<ScriptCompletionProposal> proposals = new LoadModuleCompletionProvider().getProposals(getContext("loadModule(\"/foo"));
		assertNull(findProposal(proposals, "Test Root"));
	}

	private ScriptCompletionProposal findProposal(Collection<ScriptCompletionProposal> proposals, String displayString) {
		return proposals.stream().filter(p -> p.getDisplayString().startsWith(displayString)).findFirst().orElseGet(() -> null);
	}

	private ICompletionContext getContext(String input) {
		return new BasicContext(null, null, input, input.length());
	}
}
