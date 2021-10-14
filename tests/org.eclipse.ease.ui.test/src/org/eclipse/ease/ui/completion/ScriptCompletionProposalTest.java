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

package org.eclipse.ease.ui.completion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.ease.ICompletionContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ScriptCompletionProposalTest {

	@Test
	@DisplayName("getContent() sets replacement text")
	public void getContent_sets_replacement_text() {
		final ScriptCompletionProposal proposal = new ScriptCompletionProposal(createContext(""), "foo", "foo()", null, 0, null);

		assertEquals("foo()", proposal.getContent());
	}

	@Test
	@DisplayName("getContent() appends replacement text")
	public void getContent_appends_replacement_text() {
		final ScriptCompletionProposal proposal = new ScriptCompletionProposal(createContext("bar."), "foo", "foo()", null, 0, null);

		assertEquals("bar.foo()", proposal.getContent());
	}

	@Test
	@DisplayName("getContent() inserts replacement text")
	public void getContent_inserts_replacement_text() {
		final ScriptCompletionProposal proposal = new ScriptCompletionProposal(new BasicContext(null, null, "AAA.BBB", 4), "foo", "foo()", null, 0, null);

		assertEquals("AAA.foo()BBB", proposal.getContent());
	}

	@Test
	@DisplayName("getCursorPosition() = end of set text")
	public void getCursorPosition_equals_end_of_set_text() {
		final ScriptCompletionProposal proposal = new ScriptCompletionProposal(createContext(""), "foo", "foo()", null, 0, null);

		assertEquals(5, proposal.getCursorPosition());
	}

	@Test
	@DisplayName("getCursorPosition() = end of appended text")
	public void getCursorPosition_equals_end_of_appended_text() {
		final ScriptCompletionProposal proposal = new ScriptCompletionProposal(createContext("bar."), "foo", "foo()", null, 0, null);

		assertEquals(9, proposal.getCursorPosition());
	}

	@Test
	@DisplayName("getCursorPosition() = end of inserted text")
	public void getCursorPosition_equals_end_of_inserted_text() {
		final ScriptCompletionProposal proposal = new ScriptCompletionProposal(new BasicContext(null, null, "AAA.BBB", 4), "foo", "foo()", null, 0, null);

		assertEquals(9, proposal.getCursorPosition());
	}

	@Test
	@DisplayName("compareTo() lists higher priority first")
	public void compareTo_lists_higher_priority_first() {
		final ScriptCompletionProposal highPrio = new ScriptCompletionProposal(createContext(""), "foo", "foo()", null, 100, null);
		final ScriptCompletionProposal lowPrio = new ScriptCompletionProposal(createContext(""), "bar", "bar()", null, 0, null);

		assertTrue(highPrio.compareTo(lowPrio) < 0);
	}

	@Test
	@DisplayName("compareTo() lists same prio in alphabetic order")
	public void compareTo_lists_same_prio_in_alphabetic_order() {
		final ScriptCompletionProposal alphabeticSecond = new ScriptCompletionProposal(createContext(""), "foo", "foo()", null, 0, null);
		final ScriptCompletionProposal alphabeticFirst = new ScriptCompletionProposal(createContext(""), "bar", "bar()", null, 0, null);

		assertTrue(alphabeticFirst.compareTo(alphabeticSecond) < 0);
	}

	private ICompletionContext createContext(String input) {
		return new BasicContext(null, null, input, input.length());
	}
}
