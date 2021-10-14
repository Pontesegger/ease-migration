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
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class InputTokenizerTest {

	private static final String COMMA = ",";
	private static final String DOT = ".";
	private static final String BRACKETS = "()";
	private static final String OPEN = "(";

	@ParameterizedTest(name = "for ''{0}''")
	@DisplayName("isDelimiter() = true")
	@ValueSource(strings = { DOT, COMMA, OPEN, BRACKETS, ")", "\"", "'" })
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
	@ValueSource(strings = { DOT, OPEN, BRACKETS, ")", COMMA })
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
	@DisplayName("foo => foo")
	public void getTokens_1() {
		final InputTokenizer tokenizer = new InputTokenizer();

		assertArrayEquals(new Object[] { "foo" }, tokenizer.getTokens("foo").toArray());
	}

	@Test
	@DisplayName("java.i => java .i")
	public void getTokens_detects_package_fragments() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { "java", DOT, "i" }, tokenizer.getTokens("java.i").toArray());
	}

	@Test
	@DisplayName("java.io => P(java.io)")
	public void getTokens_detects_package() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { Package.getPackage("java.io") }, tokenizer.getTokens("java.io").toArray());
	}

	@Test
	@DisplayName("java.io. => P(java.io) .")
	public void getTokens_detects_incomplete_package() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { Package.getPackage("java.io"), DOT }, tokenizer.getTokens("java.io.").toArray());
	}

	@Test
	@DisplayName("java.io.foo => P(java.io) . foo")
	public void getTokens_detects_partial_package() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { Package.getPackage("java.io"), DOT, "foo" }, tokenizer.getTokens("java.io.foo").toArray());
	}

	@Test
	@DisplayName("java.io.File => C(java.io.File)")
	public void getTokens_detects_class() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { File.class }, tokenizer.getTokens("java.io.File").toArray());
	}

	@Test
	@DisplayName("java.io.ObjectInputStream.GetField => C(java.io.ObjectInputStream.GetField)")
	public void getTokens_detects_inner_class() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { ObjectInputStream.GetField.class }, tokenizer.getTokens("java.io.ObjectInputStream.GetField").toArray());
	}

	@Test
	@DisplayName("java.io.File. => C(java.io.File) .")
	public void getTokens_detects_class_2() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { File.class, DOT }, tokenizer.getTokens("java.io.File.").toArray());
	}

	@Test
	@DisplayName("java.io.File() => C(java.io.File) ()")
	public void getTokens_detects_class_instantiation() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { File.class, BRACKETS }, tokenizer.getTokens("java.io.File()").toArray());
	}

	@Test
	@DisplayName("java.io.File(42, 23) => C(java.io.File) ()")
	public void getTokens_detects_class_instantiation_2() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { File.class, BRACKETS }, tokenizer.getTokens("java.io.File(42, 23)").toArray());
	}

	@Test
	@DisplayName("java.io.File(). => C(java.io.File) () .")
	public void getTokens_detects_class_instantiation_3() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { File.class, BRACKETS, DOT }, tokenizer.getTokens("java.io.File().").toArray());
	}

	@Test
	@DisplayName("java.io.Fi => P(java.io) . Fi")
	public void getTokens_detects_package_with_class_prefix() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { Package.getPackage("java.io"), DOT, "Fi" }, tokenizer.getTokens("java.io.Fi").toArray());
	}

	@Test
	@DisplayName("java.io.File.createTempFile() => C(java.io.File) M(createTempFile)")
	public void getTokens_detects_static_method_call() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { File.class, getMethod(File.class, "createTempFile") }, tokenizer.getTokens("java.io.File.createTempFile()").toArray());
	}

	@Test
	@DisplayName("java.io.File.createTempFile(). => C(java.io.File) M(createTempFile) .")
	public void getTokens_detects_static_method_call_with_dot_prefix() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { File.class, getMethod(File.class, "createTempFile"), DOT },
				tokenizer.getTokens("java.io.File.createTempFile().").toArray());
	}

	@Test
	@DisplayName("java.io.File.createTempFile().getName() => C(java.io.File) M(createTempFile) M(getName)")
	public void getTokens_detects_static_method_call_chain() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { File.class, getMethod(File.class, "createTempFile"), getMethod(File.class, "getName") },
				tokenizer.getTokens("java.io.File.createTempFile().getName()").toArray());
	}

	@Test
	@DisplayName("java.io.File().getName() => C(java.io.File) () M(getName)")
	public void getTokens_detects_method_call() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { File.class, BRACKETS, getMethod(File.class, "getName") }, tokenizer.getTokens("java.io.File().getName()").toArray());
	}

	@Test
	@DisplayName("java.io.File().getName().getBytes() => C(java.io.File) () M(getName) M(getBytes)")
	public void getTokens_detects_method_call_chain() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { File.class, BRACKETS, getMethod(File.class, "getName"), getMethod(String.class, "getBytes") },
				tokenizer.getTokens("java.io.File().getName().getBytes()").toArray());
	}

	@Test
	@DisplayName("java.io.File().getName( => C(java.io.File) () M(getName) (")
	public void getTokens_detects_method_call_parameter() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { File.class, BRACKETS, getMethod(File.class, "getName"), OPEN },
				tokenizer.getTokens("java.io.File().getName(").toArray());
	}

	@Test
	@DisplayName("java.io.File().getName(1, 2, => C(java.io.File) () M(getName) ( , ,")
	public void getTokens_detects_method_call_parameters() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { File.class, BRACKETS, getMethod(File.class, "getName"), OPEN, COMMA, COMMA },
				tokenizer.getTokens("java.io.File().getName(1, 2,").toArray());
	}

	@Test
	@DisplayName("new java => java")
	public void getTokens_removes_new_operator() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { "java" }, tokenizer.getTokens("new java").toArray());
	}

	@Test
	@DisplayName("foo=java => java")
	public void getTokens_removes_left_hand_side_of_assignment() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { "java" }, tokenizer.getTokens("foo=java").toArray());
	}

	@Test
	@DisplayName("java.io.File().get => C(java.io.File) () . get")
	public void getTokens_detects_method_call_request_with_filter() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { File.class, BRACKETS, DOT, "get" }, tokenizer.getTokens("java.io.File().get").toArray());
	}

	@Test
	@DisplayName("java.io.File().getName(1, 2, 3 => C(java.io.File) () M(getName) ( , , 3")
	public void getTokens_detects_method_call_parameters_with_prefix() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { File.class, BRACKETS, getMethod(File.class, "getName"), OPEN, COMMA, COMMA, "3" },
				tokenizer.getTokens("java.io.File().getName(1, 2, 3").toArray());
	}

	@Test
	@DisplayName("java.io.File().getName(foo => C(java.io.File) () M(getName) ( foo")
	public void getTokens_detects_method_call_parameter_prefix() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { File.class, BRACKETS, getMethod(File.class, "getName"), OPEN, "foo" },
				tokenizer.getTokens("java.io.File().getName(foo").toArray());
	}

	@Test
	@DisplayName("java.io.File().getName(). => C(java.io.File) () M(getName) .")
	public void getTokens_detects_completed_method() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { File.class, BRACKETS, getMethod(File.class, "getName"), DOT },
				tokenizer.getTokens("java.io.File().getName().").toArray());
	}

	@Test
	@DisplayName("java.io.File().getName().trim() => C(java.io.File) () M(getName) M(trim)")
	public void getTokens_detects_method_chaining() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { File.class, BRACKETS, getMethod(File.class, "getName"), getMethod(String.class, "trim") },
				tokenizer.getTokens("java.io.File().getName().trim()").toArray());
	}

	@Test
	@DisplayName("foo() => foo ()")
	public void getTokens_detects_javascript_method_call() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { "foo", BRACKETS }, tokenizer.getTokens("foo()").toArray());
	}

	@Test
	@DisplayName("JavaScript() => JavaScript ()")
	public void getTokens_detects_javascript_class_instantiation() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { "JavaScript", BRACKETS }, tokenizer.getTokens("JavaScript()").toArray());
	}

	@Test
	@DisplayName("myVar.foo() => myVar . foo ()")
	public void getTokens_detects_javascript_object_call() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { "myVar", DOT, "foo", BRACKETS }, tokenizer.getTokens("myVar.foo()").toArray());
	}

	@Test
	@DisplayName("myVar => C(java.lang.String) ()")
	public void getTokens_detects_resolved_javascript_variable() {
		final InputTokenizer tokenizer = new InputTokenizer(v -> null, v -> "myVar".equals(v) ? String.class : null);
		assertArrayEquals(new Object[] { String.class, BRACKETS }, tokenizer.getTokens("myVar").toArray());
	}

	@Test
	@DisplayName("myVar. => C(java.lang.String) () .")
	public void getTokens_detects_resolved_javascript_variable_2() {
		final InputTokenizer tokenizer = new InputTokenizer(v -> null, v -> "myVar".equals(v) ? String.class : null);
		assertArrayEquals(new Object[] { String.class, BRACKETS, DOT }, tokenizer.getTokens("myVar.").toArray());
	}

	@Test
	@DisplayName("myVar.startsWith(x) => C(java.lang.String) () M(startsWith)")
	public void getTokens_detects_resolved_javascript_variable_with_method() {
		final InputTokenizer tokenizer = new InputTokenizer(v -> null, v -> "myVar".equals(v) ? String.class : null);
		assertArrayEquals(new Object[] { String.class, BRACKETS, getMethod(String.class, "startsWith") }, tokenizer.getTokens("myVar.startsWith(x)").toArray());
	}

	@Test
	@DisplayName("getName(). => M(getName) .")
	public void getTokens_detects_module_methods() {
		final InputTokenizer tokenizer = new InputTokenizer(v -> "getName".equals(v) ? getMethod(File.class, "getName") : null, v -> null);
		assertArrayEquals(new Object[] { getMethod(File.class, "getName"), DOT }, tokenizer.getTokens("getName().").toArray());
	}

	@Test
	@DisplayName("getName().trim(). => M(getName) M(trim) .")
	public void getTokens_detects_module_methods_call_chain() {
		final InputTokenizer tokenizer = new InputTokenizer(v -> "getName".equals(v) ? getMethod(File.class, "getName") : null, v -> null);
		assertArrayEquals(new Object[] { getMethod(File.class, "getName"), getMethod(String.class, "trim"), DOT },
				tokenizer.getTokens("getName().trim().").toArray());
	}

	@Test
	@DisplayName("getName( => M(getName) (")
	public void getTokens_detects_opened_module_method() {
		final InputTokenizer tokenizer = new InputTokenizer(v -> "getName".equals(v) ? getMethod(File.class, "getName") : null, v -> null);
		assertArrayEquals(new Object[] { getMethod(File.class, "getName"), OPEN }, tokenizer.getTokens("getName(").toArray());
	}

	@Test
	@DisplayName("getName('Foo => M(getName) ( 'Foo")
	public void getTokens_detects_opened_module_method_with_filter() {
		final InputTokenizer tokenizer = new InputTokenizer(v -> "getName".equals(v) ? getMethod(File.class, "getName") : null, v -> null);
		assertArrayEquals(new Object[] { getMethod(File.class, "getName"), OPEN, "\"", "Foo" }, tokenizer.getTokens("getName(\"Foo").toArray());
	}

	@Test
	@DisplayName("getName(2, 'Foo => M(getName) ( , 'Foo")
	public void getTokens_detects_opened_module_method_with_parameter() {
		final InputTokenizer tokenizer = new InputTokenizer(v -> "getName".equals(v) ? getMethod(File.class, "getName") : null, v -> null);
		assertArrayEquals(new Object[] { getMethod(File.class, "getName"), OPEN, COMMA, "\"", "Foo" }, tokenizer.getTokens("getName(2, \"Foo").toArray());
	}

	@Test
	@DisplayName("getName('foo' => (invalid)")
	public void getTokens_is_empty_after_string_literal() {
		final InputTokenizer tokenizer = new InputTokenizer(v -> "getName".equals(v) ? getMethod(File.class, "getName") : null, v -> null);
		assertArrayEquals(new Object[] { InputTokenizer.INVALID }, tokenizer.getTokens("getName('foo'").toArray());
	}

	@Test
	@DisplayName("foo('\"String )Literal', => foo ( ,")
	public void getTokens_simplifies_string_literal_1() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { "foo", OPEN, COMMA }, tokenizer.getTokens("foo('\"String )Literal',").toArray());
	}

	@Test
	@DisplayName("foo(\"'String )Literal\", => foo ( ,")
	public void getTokens_simplifies_string_literal_2() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { "foo", OPEN, COMMA }, tokenizer.getTokens("foo(\"'String )Literal\",").toArray());
	}

	@Test
	@DisplayName("foo(1,2,3) => foo ()")
	public void getTokens_simplifies_brackets() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { "foo", BRACKETS }, tokenizer.getTokens("foo(1,2,3)").toArray());
	}

	@Test
	@DisplayName("foo(1, bar(a,b) ,3) => foo ()")
	public void getTokens_simplifies_brackets_recursively() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { "foo", BRACKETS }, tokenizer.getTokens("foo(1, bar(a,b) ,3)").toArray());
	}

	@Test
	@DisplayName("var a = foo( => foo (")
	public void getTokens_simplifies_assignments() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { "foo", OPEN }, tokenizer.getTokens("var a = foo(").toArray());
	}

	@Test
	@DisplayName("bar + foo( => foo (")
	public void getTokens_simplifies_plus_operations() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { "foo", OPEN }, tokenizer.getTokens("bar + foo(").toArray());
	}

	@Test
	@DisplayName("bar - foo( => foo (")
	public void getTokens_simplifies_minus_operations() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { "foo", OPEN }, tokenizer.getTokens("bar - foo(").toArray());
	}

	@Test
	@DisplayName("bar * foo( => foo (")
	public void getTokens_simplifies_multiply_operations() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { "foo", OPEN }, tokenizer.getTokens("bar * foo(").toArray());
	}

	@Test
	@DisplayName("bar / foo( => foo (")
	public void getTokens_simplifies_divide_operations() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { "foo", OPEN }, tokenizer.getTokens("bar / foo(").toArray());
	}

	@Test
	@DisplayName("for(var i=3; foo( => foo (")
	public void getTokens_simplifies_for_loop() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { "foo", OPEN }, tokenizer.getTokens("for(var i=3; foo(").toArray());
	}

	@Test
	@DisplayName("\"test => \" test")
	public void getTokens_splits_string_literal() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { "\"", "test" }, tokenizer.getTokens("\"test").toArray());
	}

	@Test
	@DisplayName("\"file://with/../special () characters => \" file://with/../special () characters")
	public void getTokens_does_not_split_within_string_literal() {
		final InputTokenizer tokenizer = new InputTokenizer();
		assertArrayEquals(new Object[] { "\"", "file://with/../special () characters" },
				tokenizer.getTokens("\"file://with/../special () characters").toArray());
	}

	private static Method getMethod(Class<?> clazz, String methodName) {
		return Arrays.asList(clazz.getMethods()).stream().filter(m -> methodName.equals(m.getName())).findFirst().get();
	}
}
