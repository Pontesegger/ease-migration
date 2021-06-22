/*******************************************************************************
 * Copyright (c) 2016 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License_Identifier: EPL-2.0
 *
 * Contributors:
 *     Kichwa Coders - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.javascript;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class JavaScriptCodeFactoryTest {
	private JavaScriptCodeFactory fFactory;

	@BeforeEach
	public void beforeEach() {
		fFactory = new JavaScriptCodeFactory();
	}

	@Test
	@DisplayName("createCommentedString() creates single line comment")
	public void createCommentedString_creates_single_line_comment() {
		assertEquals("// Comment", fFactory.createCommentedString("Comment", false));
	}

	@Test
	@DisplayName("createCommentedString() creates multi line comment")
	public void createCommentedString_creates_multi_line_comment() {
		assertEquals(String.format("// Multi%n// Line%n// Comment"), fFactory.createCommentedString("Multi\nLine\nComment", false));
		assertEquals(String.format("/**%n * Multi%n * Line%n * Comment%n */%n"), fFactory.createCommentedString("Multi\nLine\nComment", true));
	}

	@Test
	@DisplayName("createCommentedString() creates block comment")
	public void createCommentedString_creates_block_comment() {
		assertEquals(String.format("/**%n * Multi%n * Line%n * Comment%n */%n"), fFactory.createCommentedString("Multi\nLine\nComment", true));
	}

	@Test
	@DisplayName("getNullString() == 'null'")
	public void getNullString_equals_null() {
		assertEquals("null", fFactory.getNullString());
	}

	@Test
	@DisplayName("classInstantiation() creates default instance")
	public void classInstantiation_creates_default_instance() {
		assertEquals(String.format("new Packages.%s()", getClass().getName()), fFactory.classInstantiation(getClass(), null));
	}

	@Test
	@DisplayName("classInstantiation() creates instance with parameters")
	public void classInstantiation_creates_instance_with_parameters() {
		final String[] parameters = new String[] { "one", "2" };
		assertEquals(String.format("new Packages.%s(%s, %s)", getClass().getName(), parameters[0], parameters[1]),
				fFactory.classInstantiation(getClass(), parameters));
	}

	@Test
	@DisplayName("getSaveVariableName() does not alter save name")
	public void getSaveVariableName_does_not_alter_save_name() {
		assertEquals("this_is_2021", fFactory.getSaveVariableName("this_is_2021"));
	}

	@Test
	@DisplayName("getSaveVariableName() replaces invalid characters")
	public void getSaveVariableName_replaces_invalid_characters() {
		assertEquals("this_is_2021__", fFactory.getSaveVariableName("this is-2021 :"));
	}

	@Test
	@DisplayName("getSaveVariableName() prefixes numbers with _")
	public void getSaveVariableName_prefixes_numbers_with_underscore() {
		assertEquals("_123", fFactory.getSaveVariableName("123"));
	}

	@Test
	@DisplayName("getSaveVariableName() creates random string for invalid characters")
	public void getSaveVariableName_creates_random_string_for_invalid_characters() {
		assertFalse(fFactory.getSaveVariableName("!§%&/()=?").isEmpty());
	}

	@Test
	@DisplayName("getSaveVariableName() prefixes keywords with _")
	public void getSaveVariableName_prefixes_keywords_with_underscore() {
		assertEquals("_for", fFactory.getSaveVariableName("for"));
	}
}
