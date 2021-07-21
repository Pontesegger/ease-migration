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

package org.eclipse.ease.ui.completion.tokenizer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class BracketMatcherTest {

	@Test
	@DisplayName("getBrackets() is empty for 'some text'")
	public void getBrackets_is_empty() {
		assertTrue(new BracketMatcher("some text").getBrackets().isEmpty());
	}

	@Test
	@DisplayName("getBrackets() detects 'single()'")
	public void getBrackets_detects_1() {
		assertArrayEquals(new Object[] { new Bracket(6, 7) }, new BracketMatcher("single()").getBrackets().toArray());
	}

	@Test
	@DisplayName("getBrackets() detects 'single().inText'")
	public void getBrackets_detects_2() {
		assertArrayEquals(new Object[] { new Bracket(6, 7) }, new BracketMatcher("single().inText").getBrackets().toArray());
	}

	@Test
	@DisplayName("getBrackets() detects 'one().two().three'")
	public void getBrackets_detects_3() {
		assertArrayEquals(new Object[] { new Bracket(9, 10), new Bracket(3, 4) }, new BracketMatcher("one().two().three").getBrackets().toArray());
	}

	@Test
	@DisplayName("getBrackets() detects 'outer(inner())'")
	public void getBrackets_detects_nested() {
		assertArrayEquals(new Object[] { new Bracket(11, 12), new Bracket(5, 13) }, new BracketMatcher("outer(inner())").getBrackets().toArray());
	}

	@Test
	@DisplayName("getBrackets() detects 'outer(inner('")
	public void getBrackets_detects_open_brackets() {
		assertArrayEquals(new Object[] { new Bracket(11, -1), new Bracket(5, -1) }, new BracketMatcher("outer(inner(").getBrackets().toArray());
	}

	@Test
	@DisplayName("hasOpenBrackets() = false for 'outer(inner())'")
	public void hasOpenBrackets_equals_false() {
		assertFalse(new BracketMatcher("outer(inner())").hasOpenBrackets());
	}

	@Test
	@DisplayName("hasOpenBrackets() = true for 'outer(inner(), text()'")
	public void hasOpenBrackets_equals_true() {
		assertTrue(new BracketMatcher("outer(inner(), text()").hasOpenBrackets());
	}

	@Test
	@DisplayName("getOpenBrackets() detects 'outer(inner(), text('")
	public void getOpenBrackets_returns_open_brackets_only() {
		assertArrayEquals(new Object[] { new Bracket(19, -1), new Bracket(5, -1) }, new BracketMatcher("outer(inner(), text(").getOpenBrackets().toArray());
	}
}
