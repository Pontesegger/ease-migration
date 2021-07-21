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

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.eclipse.ease.ui.completion.tokenizer.InputTokenizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class InputTokenizerTest {

	@ParameterizedTest(name = "for ''{0}''")
	@DisplayName("isDelimiter() = true")
	@ValueSource(strings = { ".", ",", "(", "()", ")" })
	public void isDelimiter_equals_true(String input) {
		assertTrue(InputTokenizer.isDelimiter(input));
	}

	@ParameterizedTest(name = "for ''{0}''")
	@DisplayName("isDelimiter() = false")
	@ValueSource(strings = { ". ", "test", "123" })
	public void isDelimiter_equals_false(String input) {
		assertFalse(InputTokenizer.isDelimiter(input));
	}

	@ParameterizedTest(name = "for ''{0}''")
	@DisplayName("isTextFilter() = true")
	@ValueSource(strings = { "foo", "bar" })
	public void isTextFilter_equals_true(String input) {
		assertTrue(InputTokenizer.isTextFilter(input));
	}

	@ParameterizedTest(name = "for ''{0}''")
	@DisplayName("isTextFilter() = false")
	@ValueSource(strings = { ".", "(", "()", ")", "," })
	public void isTextFilter_equals_false(String input) {
		assertFalse(InputTokenizer.isTextFilter(input));
	}

	@Test
	@DisplayName("'' => {}")
	public void getTokens_for_empty_input() {
		final InputTokenizer tokenizer = new InputTokenizer();

		assertArrayEquals(new Object[] {}, tokenizer.getTokens("").toArray());
	}

	@Test
	@DisplayName("'foo' => 'foo'")
	public void getTokens_1() {
		final InputTokenizer tokenizer = new InputTokenizer();

		assertArrayEquals(new Object[] { "foo" }, tokenizer.getTokens("foo").toArray());
	}

	@Test
	@DisplayName("'java.i' => 'java', '.', 'i')")
	public void getTokens_detects_package_fragments() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { "java", ".", "i" }, tokenizer.getTokens("java.i").toArray());
	}

	@Test
	@DisplayName("'java.io' => P(java.io)")
	public void getTokens_detects_package() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { Package.getPackage("java.io") }, tokenizer.getTokens("java.io").toArray());
	}

	@Test
	@DisplayName("'java.io.' => P(java.io), '.'")
	public void getTokens_detects_incomplete_package() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { Package.getPackage("java.io"), "." }, tokenizer.getTokens("java.io.").toArray());
	}

	@Test
	@DisplayName("'java.io.foo' => P(java.io), '.', 'foo'")
	public void getTokens_detects_partial_package() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { Package.getPackage("java.io"), ".", "foo" }, tokenizer.getTokens("java.io.foo").toArray());
	}

	@Test
	@DisplayName("'java.io.File' => C(java.io.File)")
	public void getTokens_detects_class() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { File.class }, tokenizer.getTokens("java.io.File").toArray());
	}

	@Test
	@DisplayName("'java.io.File.' => C(java.io.File), '.'")
	public void getTokens_detects_class_2() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { File.class, "." }, tokenizer.getTokens("java.io.File.").toArray());
	}

	@Test
	@DisplayName("'java.io.File()' => C(java.io.File), '()'")
	public void getTokens_detects_class_instantiation() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { File.class, "()" }, tokenizer.getTokens("java.io.File()").toArray());
	}

	@Test
	@DisplayName("'java.io.File(42, 23)' => C(java.io.File), '()'")
	public void getTokens_detects_class_instantiation_2() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { File.class, "()" }, tokenizer.getTokens("java.io.File(42, 23)").toArray());
	}

	@Test
	@DisplayName("'java.io.File().' => C(java.io.File), '()', '.'")
	public void getTokens_detects_class_instantiation_3() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { File.class, "()", "." }, tokenizer.getTokens("java.io.File().").toArray());
	}

	@Test
	@DisplayName("'java.io.Fi' => P(java.io), '.', 'Fi'")
	public void getTokens_detects_package_with_class_prefix() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { Package.getPackage("java.io"), ".", "Fi" }, tokenizer.getTokens("java.io.Fi").toArray());
	}

	@Test
	@DisplayName("'java.io.File.createTempFile()' => C(java.io.File), M(createTempFile), '()'")
	public void getTokens_detects_static_method_call() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { File.class, getMethod(File.class, "createTempFile"), "()" },
				tokenizer.getTokens("java.io.File.createTempFile()").toArray());
	}

	@Test
	@DisplayName("'java.io.File.createTempFile().getName()' => C(java.io.File), M(createTempFile), '()', M(getName), '()'")
	public void getTokens_detects_static_method_call_chain() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { File.class, getMethod(File.class, "createTempFile"), "()", getMethod(File.class, "getName"), "()" },
				tokenizer.getTokens("java.io.File.createTempFile().getName()").toArray());
	}

	@Test
	@DisplayName("'java.io.File().getName()' => C(java.io.File), '()', M(getName), '()'")
	public void getTokens_detects_method_call() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { File.class, "()", getMethod(File.class, "getName"), "()" }, tokenizer.getTokens("java.io.File().getName()").toArray());
	}

	@Test
	@DisplayName("'java.io.File.getName().getBytes()' => C(java.io.File), '()', M(getName), '()', M(getBytes), '()'")
	public void getTokens_detects_method_call_chain() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { File.class, getMethod(File.class, "getName"), "()", getMethod(String.class, "getBytes"), "()" },
				tokenizer.getTokens("java.io.File.getName().getBytes()").toArray());
	}

	@Test
	@DisplayName("'java.io.File().getName(' => C(java.io.File), '()', M(getName), '('")
	public void getTokens_detects_method_call_parameter() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { File.class, "()", getMethod(File.class, "getName"), "(" }, tokenizer.getTokens("java.io.File().getName(").toArray());
	}

	@Test
	@DisplayName("'java.io.File().getName(1, 2,' => C(java.io.File), '()', M(getName), '(', ',', ','")
	public void getTokens_detects_method_call_parameters() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { File.class, "()", getMethod(File.class, "getName"), "(", ",", "," },
				tokenizer.getTokens("java.io.File().getName(1, 2,").toArray());
	}

	@Test
	@DisplayName("'new java' =>  'java'")
	public void getTokens_removes_new_operator() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { "java" }, tokenizer.getTokens("new java").toArray());
	}

	@Test
	@DisplayName("'foo=java' =>  'java'")
	public void getTokens_removes_left_hand_side_of_assignment() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { "java" }, tokenizer.getTokens("foo=java").toArray());
	}

	@Test
	@DisplayName("'java.io.File().' => C(java.io.File), '()', '.'")
	public void getTokens_detects_method_call_request() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { File.class, "()", "." }, tokenizer.getTokens("java.io.File().").toArray());
	}

	@Test
	@DisplayName("'java.io.File().get' => C(java.io.File), '()', '.', 'get'")
	public void getTokens_detects_method_call_request_with_filter() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { File.class, "()", ".", "get" }, tokenizer.getTokens("java.io.File().get").toArray());
	}

	@Test
	@DisplayName("'java.io.File().getName(1, 2, 3' => C(java.io.File), '()', M(getName), '(', ',', ',', '3'")
	public void getTokens_detects_method_call_parameters_with_prefix() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { File.class, "()", getMethod(File.class, "getName"), "(", ",", ",", "3" },
				tokenizer.getTokens("java.io.File().getName(1, 2, 3").toArray());
	}

	@Test
	@DisplayName("'java.io.File().getName(foo' => C(java.io.File), '()', M(getName), '(', 'foo'")
	public void getTokens_detects_method_call_parameter_prefix() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { File.class, "()", getMethod(File.class, "getName"), "(", "foo" },
				tokenizer.getTokens("java.io.File().getName(foo").toArray());
	}

	@Test
	@DisplayName("'foo()' => 'foo', '()'")
	public void getTokens_detects_javascript_method_call() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { "foo", "()" }, tokenizer.getTokens("foo()").toArray());
	}

	@Test
	@DisplayName("'JavaScript()' => 'JavaScript', '()'")
	public void getTokens_detects_javascript_class_instantiation() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { "JavaScript", "()" }, tokenizer.getTokens("JavaScript()").toArray());
	}

	@Test
	@DisplayName("'myVar' => 'myVar'")
	public void getTokens_detects_javascript_variable() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { "myVar" }, tokenizer.getTokens("myVar").toArray());
	}

	@Test
	@DisplayName("'myVar.foo()' => 'myVar', '.', 'foo', '()'")
	public void getTokens_detects_javascript_object_call() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { "myVar", ".", "foo", "()" }, tokenizer.getTokens("myVar.foo()").toArray());
	}

	@Test
	@DisplayName("'myVar' => C(java.lang.String), '()'")
	public void getTokens_detects_resolved_javascript_variable() {
		final InputTokenizer tokenizer = new InputTokenizer(v -> "myVar".equals(v) ? String.class : null);
		assertArrayEquals(new Object[] { String.class, "()" }, tokenizer.getTokens("myVar").toArray());
	}

	@Test
	@DisplayName("'myVar.' => C(java.lang.String), '()', '.'")
	public void getTokens_detects_resolved_javascript_variable_2() {
		final InputTokenizer tokenizer = new InputTokenizer(v -> "myVar".equals(v) ? String.class : null);
		assertArrayEquals(new Object[] { String.class, "()", "." }, tokenizer.getTokens("myVar.").toArray());
	}

	@Test
	@DisplayName("'myVar.startsWith(x)' => C(java.lang.String), '()', M(startsWith), '()'")
	public void getTokens_detects_resolved_javascript_variable_with_method() {
		final InputTokenizer tokenizer = new InputTokenizer(v -> "myVar".equals(v) ? String.class : null);
		assertArrayEquals(new Object[] { String.class, "()", getMethod(String.class, "startsWith"), "()" },
				tokenizer.getTokens("myVar.startsWith(x)").toArray());
	}

	private static Method getMethod(Class<?> clazz, String methodName) {
		return Arrays.asList(clazz.getMethods()).stream().filter(m -> methodName.equals(m.getName())).findFirst().get();
	}
}
