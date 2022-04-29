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

package org.eclipse.ease;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collection;

import org.eclipse.ease.ICodeFactory.Parameter;
import org.eclipse.ease.modules.WrapToScript;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class AbstractCodeFactoryTest {

	private static final String SINGLELINE_HEADER = "This is a single line header.";
	private static final String MULTILINE_HEADER = "This is a\n" + "multiline header.";
	private static final String KEYWORDS = "keyword1        : value1\n" + "keyword2        : value2";

	private AbstractCodeFactory fFactory;

	@BeforeEach
	public void beforeEach() {
		fFactory = mock(AbstractCodeFactory.class, Mockito.CALLS_REAL_METHODS);
	}

	@Test
	@DisplayName("getMethodNames() returns method name")
	public void getMethodNames_returns_method_name() throws NoSuchMethodException {
		final Collection<String> names = AbstractCodeFactory.getMethodNames(AbstractCodeFactoryTest.class.getMethod("methodWithoutAliases"));

		assertEquals(1, names.size());
		assertTrue(names.contains("methodWithoutAliases"));
	}

	@Test
	@DisplayName("getMethodNames() returns method name & aliases")
	public void getMethodNames_returns_method_name_and_aliases() throws NoSuchMethodException {
		final Collection<String> names = AbstractCodeFactory.getMethodNames(AbstractCodeFactoryTest.class.getMethod("methodWithAliases"));

		assertEquals(3, names.size());
		assertTrue(names.contains("methodWithAliases"));
		assertTrue(names.contains("one"));
		assertTrue(names.contains("two"));
	}

	@Test
	@DisplayName("getMethodNames() filters empty aliases")
	public void getMethodNames_filters_empty_aliases() throws NoSuchMethodException {
		final Collection<String> names = AbstractCodeFactory.getMethodNames(AbstractCodeFactoryTest.class.getMethod("methodWithEmptyAliases"));

		assertEquals(3, names.size());
		assertTrue(names.contains("methodWithEmptyAliases"));
		assertTrue(names.contains("one"));
		assertTrue(names.contains("two"));
	}

	@Test
	@DisplayName("getMethodAliases() is empty without annotation")
	public void getMethodAliases_is_empty_without_annotation() throws NoSuchMethodException {
		final Collection<String> names = AbstractCodeFactory.getMethodAliases(AbstractCodeFactoryTest.class.getMethod("methodWithoutAliases"));

		assertTrue(names.isEmpty());
	}

	@Test
	@DisplayName("getMethodAliases() returns method aliases")
	public void getMethodAliases_returns_method_aliases() throws NoSuchMethodException {
		final Collection<String> names = AbstractCodeFactory.getMethodAliases(AbstractCodeFactoryTest.class.getMethod("methodWithAliases"));

		assertEquals(2, names.size());
		assertTrue(names.contains("one"));
		assertTrue(names.contains("two"));
	}

	@Test
	@DisplayName("getMethodAliases() filters empty aliases")
	public void getMethodAliases_filters_empty_aliases() throws NoSuchMethodException {
		final Collection<String> names = AbstractCodeFactory.getMethodAliases(AbstractCodeFactoryTest.class.getMethod("methodWithEmptyAliases"));

		assertEquals(2, names.size());
		assertTrue(names.contains("one"));
		assertTrue(names.contains("two"));
	}

	@Test
	@DisplayName("createFunctionCall() for foo()")
	public void createFunctionCall_for_method_without_parameters() throws NoSuchMethodException {
		assertEquals("foo();", fFactory.createFunctionCall(AbstractCodeFactoryTest.class.getMethod("foo")));
	}

	@Test
	@DisplayName("createFunctionCall() for foo('test')")
	public void createFunctionCall_for_method_with_String_parameter() throws NoSuchMethodException {
		assertEquals("foo(\"test\");", fFactory.createFunctionCall(AbstractCodeFactoryTest.class.getMethod("foo", String.class), "test"));
	}

	@Test
	@DisplayName("createFunctionCall() for foo(null)")
	public void createFunctionCall_for_method_with_null_parameter() throws NoSuchMethodException {
		when(fFactory.getNullString()).thenReturn("null");

		assertEquals("foo(null);", fFactory.createFunctionCall(AbstractCodeFactoryTest.class.getMethod("foo", String.class), (String) null));
	}

	@Test
	@DisplayName("createFunctionCall() for foo(1)")
	public void createFunctionCall_for_method_with_primitive_parameter() throws NoSuchMethodException {
		assertEquals("foo(1);", fFactory.createFunctionCall(AbstractCodeFactoryTest.class.getMethod("foo", int.class), 1));
	}

	@Test
	@DisplayName("createFunctionCall() for foo(Object)")
	public void createFunctionCall_for_method_with_Object_parameter() throws NoSuchMethodException {
		assertEquals("foo(/);", fFactory.createFunctionCall(AbstractCodeFactoryTest.class.getMethod("foo", File.class), new File("/")));
	}

	@Test
	@DisplayName("createFunctionCall() for foo(true)")
	public void createFunctionCall_for_method_with_true_parameter() throws NoSuchMethodException {
		assertEquals("foo(true);", fFactory.createFunctionCall(AbstractCodeFactoryTest.class.getMethod("foo", boolean.class), true));
	}

	@Test
	@DisplayName("createFunctionCall() for foo(false)")
	public void createFunctionCall_for_method_with_false_parameter() throws NoSuchMethodException {
		assertEquals("foo(false);", fFactory.createFunctionCall(AbstractCodeFactoryTest.class.getMethod("foo", boolean.class), false));
	}

	@Test
	@DisplayName("createFunctionCall() for foo('test', null, false)")
	public void createFunctionCall_for_method_with_multiple_parameters() throws NoSuchMethodException {
		when(fFactory.getNullString()).thenReturn("NULL");

		assertEquals("foo(\"test\", NULL, true);",
				fFactory.createFunctionCall(AbstractCodeFactoryTest.class.getMethod("foo", String.class, Object.class, boolean.class), "test", null, true));
	}

	@Test
	@DisplayName("getDefaultValue(null) = null string")
	public void getDefaultValue_for_null() throws NoSuchMethodException {
		when(fFactory.getNullString()).thenReturn("NULL");

		assertEquals("NULL", fFactory.getDefaultValue(new ICodeFactory.Parameter()));
	}

	@Test
	@DisplayName("getDefaultValue(int) = 1")
	public void getDefaultValue_for_int() throws NoSuchMethodException {
		final Parameter parameter = new ICodeFactory.Parameter();
		parameter.setClass(int.class);
		parameter.setDefault("1");
		assertEquals("1", fFactory.getDefaultValue(parameter));
	}

	@Test
	@DisplayName("getDefaultValue(int) throws for invalid default")
	public void getDefaultValue_for_int_throws_for_invalid_default() throws NoSuchMethodException {
		final Parameter parameter = new ICodeFactory.Parameter();
		parameter.setClass(int.class);
		parameter.setDefault("");
		assertThrows(IllegalArgumentException.class, () -> fFactory.getDefaultValue(parameter));
	}

	@Test
	@DisplayName("getDefaultValue(Integer) = 1")
	public void getDefaultValue_for_Integer() throws NoSuchMethodException {
		final Parameter parameter = new ICodeFactory.Parameter();
		parameter.setClass(Integer.class);
		parameter.setDefault("1");
		assertEquals("1", fFactory.getDefaultValue(parameter));
	}

	@Test
	@DisplayName("getDefaultValue(Integer) throws for invalid default")
	public void getDefaultValue_for_Integer_throws_for_invalid_default() throws NoSuchMethodException {
		final Parameter parameter = new ICodeFactory.Parameter();
		parameter.setClass(Integer.class);
		parameter.setDefault("");
		assertThrows(IllegalArgumentException.class, () -> fFactory.getDefaultValue(parameter));
	}

	@Test
	@DisplayName("getDefaultValue(long) = 1")
	public void getDefaultValue_for_long() throws NoSuchMethodException {
		final Parameter parameter = new ICodeFactory.Parameter();
		parameter.setClass(long.class);
		parameter.setDefault("1");
		assertEquals("1", fFactory.getDefaultValue(parameter));
	}

	@Test
	@DisplayName("getDefaultValue(long) throws for invalid default")
	public void getDefaultValue_for_long_throws_for_invalid_default() throws NoSuchMethodException {
		final Parameter parameter = new ICodeFactory.Parameter();
		parameter.setClass(long.class);
		parameter.setDefault("");
		assertThrows(IllegalArgumentException.class, () -> fFactory.getDefaultValue(parameter));
	}

	@Test
	@DisplayName("getDefaultValue(Long) = 1")
	public void getDefaultValue_for_Long() throws NoSuchMethodException {
		final Parameter parameter = new ICodeFactory.Parameter();
		parameter.setClass(Long.class);
		parameter.setDefault("1");
		assertEquals("1", fFactory.getDefaultValue(parameter));
	}

	@Test
	@DisplayName("getDefaultValue(Long) throws for invalid default")
	public void getDefaultValue_for_Long_throws_for_invalid_default() throws NoSuchMethodException {
		final Parameter parameter = new ICodeFactory.Parameter();
		parameter.setClass(Long.class);
		parameter.setDefault("");
		assertThrows(IllegalArgumentException.class, () -> fFactory.getDefaultValue(parameter));
	}

	@Test
	@DisplayName("getDefaultValue(byte) = 1")
	public void getDefaultValue_for_byte() throws NoSuchMethodException {
		final Parameter parameter = new ICodeFactory.Parameter();
		parameter.setClass(byte.class);
		parameter.setDefault("1");
		assertEquals("1", fFactory.getDefaultValue(parameter));
	}

	@Test
	@DisplayName("getDefaultValue(byte) throws for invalid default")
	public void getDefaultValue_for_byte_throws_for_invalid_default() throws NoSuchMethodException {
		final Parameter parameter = new ICodeFactory.Parameter();
		parameter.setClass(byte.class);
		parameter.setDefault("");
		assertThrows(IllegalArgumentException.class, () -> fFactory.getDefaultValue(parameter));
	}

	@Test
	@DisplayName("getDefaultValue(Byte) = 1")
	public void getDefaultValue_for_Byte() throws NoSuchMethodException {
		final Parameter parameter = new ICodeFactory.Parameter();
		parameter.setClass(Byte.class);
		parameter.setDefault("1");
		assertEquals("1", fFactory.getDefaultValue(parameter));
	}

	@Test
	@DisplayName("getDefaultValue(Byte) throws for invalid default")
	public void getDefaultValue_for_Byte_throws_for_invalid_default() throws NoSuchMethodException {
		final Parameter parameter = new ICodeFactory.Parameter();
		parameter.setClass(Byte.class);
		parameter.setDefault("");
		assertThrows(IllegalArgumentException.class, () -> fFactory.getDefaultValue(parameter));
	}

	@Test
	@DisplayName("getDefaultValue(float) = 1.1")
	public void getDefaultValue_for_float() throws NoSuchMethodException {
		final Parameter parameter = new ICodeFactory.Parameter();
		parameter.setClass(float.class);
		parameter.setDefault("1.1");
		assertEquals("1.1", fFactory.getDefaultValue(parameter));
	}

	@Test
	@DisplayName("getDefaultValue(float) throws for invalid default")
	public void getDefaultValue_for_float_throws_for_invalid_default() throws NoSuchMethodException {
		final Parameter parameter = new ICodeFactory.Parameter();
		parameter.setClass(float.class);
		parameter.setDefault("");
		assertThrows(IllegalArgumentException.class, () -> fFactory.getDefaultValue(parameter));
	}

	@Test
	@DisplayName("getDefaultValue(Float) = 1.1")
	public void getDefaultValue_for_Float() throws NoSuchMethodException {
		final Parameter parameter = new ICodeFactory.Parameter();
		parameter.setClass(Float.class);
		parameter.setDefault("1.1");
		assertEquals("1.1", fFactory.getDefaultValue(parameter));
	}

	@Test
	@DisplayName("getDefaultValue(Float) throws for invalid default")
	public void getDefaultValue_for_Float_throws_for_invalid_default() throws NoSuchMethodException {
		final Parameter parameter = new ICodeFactory.Parameter();
		parameter.setClass(Float.class);
		parameter.setDefault("");
		assertThrows(IllegalArgumentException.class, () -> fFactory.getDefaultValue(parameter));
	}

	@Test
	@DisplayName("getDefaultValue(double) = 1.1")
	public void getDefaultValue_for_double() throws NoSuchMethodException {
		final Parameter parameter = new ICodeFactory.Parameter();
		parameter.setClass(double.class);
		parameter.setDefault("1.1");
		assertEquals("1.1", fFactory.getDefaultValue(parameter));
	}

	@Test
	@DisplayName("getDefaultValue(double) throws for invalid default")
	public void getDefaultValue_for_double_throws_for_invalid_default() throws NoSuchMethodException {
		final Parameter parameter = new ICodeFactory.Parameter();
		parameter.setClass(double.class);
		parameter.setDefault("");
		assertThrows(IllegalArgumentException.class, () -> fFactory.getDefaultValue(parameter));
	}

	@Test
	@DisplayName("getDefaultValue(Double) = 1.1")
	public void getDefaultValue_for_Double() throws NoSuchMethodException {
		final Parameter parameter = new ICodeFactory.Parameter();
		parameter.setClass(Double.class);
		parameter.setDefault("1.1");
		assertEquals("1.1", fFactory.getDefaultValue(parameter));
	}

	@Test
	@DisplayName("getDefaultValue(Double) throws for invalid default")
	public void getDefaultValue_for_Double_throws_for_invalid_default() throws NoSuchMethodException {
		final Parameter parameter = new ICodeFactory.Parameter();
		parameter.setClass(Double.class);
		parameter.setDefault("");
		assertThrows(IllegalArgumentException.class, () -> fFactory.getDefaultValue(parameter));
	}

	@Test
	@DisplayName("getDefaultValue(boolean) = true")
	public void getDefaultValue_for_boolean_is_true() throws NoSuchMethodException {
		final Parameter parameter = new ICodeFactory.Parameter();
		parameter.setClass(boolean.class);
		parameter.setDefault("true");
		assertEquals("true", fFactory.getDefaultValue(parameter));
	}

	@Test
	@DisplayName("getDefaultValue(boolean) = false")
	public void getDefaultValue_for_boolean_is_false() throws NoSuchMethodException {
		final Parameter parameter = new ICodeFactory.Parameter();
		parameter.setClass(boolean.class);
		parameter.setDefault("false");
		assertEquals("false", fFactory.getDefaultValue(parameter));
	}

	@Test
	@DisplayName("getDefaultValue(boolean), invalid = false")
	public void getDefaultValue_for_boolean_invalid_is_false() throws NoSuchMethodException {
		final Parameter parameter = new ICodeFactory.Parameter();
		parameter.setClass(boolean.class);
		parameter.setDefault("");
		assertEquals("false", fFactory.getDefaultValue(parameter));
	}

	@Test
	@DisplayName("getDefaultValue(Boolean) = true")
	public void getDefaultValue_for_Boolean_is_true() throws NoSuchMethodException {
		final Parameter parameter = new ICodeFactory.Parameter();
		parameter.setClass(Boolean.class);
		parameter.setDefault("true");
		assertEquals("true", fFactory.getDefaultValue(parameter));
	}

	@Test
	@DisplayName("getDefaultValue(Boolean) = false")
	public void getDefaultValue_for_Boolean_is_false() throws NoSuchMethodException {
		final Parameter parameter = new ICodeFactory.Parameter();
		parameter.setClass(Boolean.class);
		parameter.setDefault("false");
		assertEquals("false", fFactory.getDefaultValue(parameter));
	}

	@Test
	@DisplayName("getDefaultValue(Boolean), invalid = false")
	public void getDefaultValue_for_Boolean_invalid_is_false() throws NoSuchMethodException {
		final Parameter parameter = new ICodeFactory.Parameter();
		parameter.setClass(Boolean.class);
		parameter.setDefault("");
		assertEquals("false", fFactory.getDefaultValue(parameter));
	}

	@Test
	@DisplayName("getDefaultValue(char) = 'a'")
	public void getDefaultValue_for_char() throws NoSuchMethodException {
		final Parameter parameter = new ICodeFactory.Parameter();
		parameter.setClass(char.class);
		parameter.setDefault("a");
		assertEquals("'a'", fFactory.getDefaultValue(parameter));
	}

	@Test
	@DisplayName("getDefaultValue(char) takes first character")
	public void getDefaultValue_for_char_takes_first_character() throws NoSuchMethodException {
		final Parameter parameter = new ICodeFactory.Parameter();
		parameter.setClass(char.class);
		parameter.setDefault("abc");
		assertEquals("'a'", fFactory.getDefaultValue(parameter));
	}

	@Test
	@DisplayName("getDefaultValue(char) throws on empty default")
	public void getDefaultValue_for_char_throws_on_empty_default() throws NoSuchMethodException {
		final Parameter parameter = new ICodeFactory.Parameter();
		parameter.setClass(char.class);
		parameter.setDefault("");
		assertThrows(IllegalArgumentException.class, () -> fFactory.getDefaultValue(parameter));
	}

	@Test
	@DisplayName("getDefaultValue(Character) = 'a'")
	public void getDefaultValue_for_Character() throws NoSuchMethodException {
		final Parameter parameter = new ICodeFactory.Parameter();
		parameter.setClass(Character.class);
		parameter.setDefault("a");
		assertEquals("'a'", fFactory.getDefaultValue(parameter));
	}

	@Test
	@DisplayName("getDefaultValue(Character) takes first character")
	public void getDefaultValue_for_Character_takes_first_character() throws NoSuchMethodException {
		final Parameter parameter = new ICodeFactory.Parameter();
		parameter.setClass(Character.class);
		parameter.setDefault("abc");
		assertEquals("'a'", fFactory.getDefaultValue(parameter));
	}

	@Test
	@DisplayName("getDefaultValue(Character) throws on empty default")
	public void getDefaultValue_for_Character_throws_on_empty_default() throws NoSuchMethodException {
		final Parameter parameter = new ICodeFactory.Parameter();
		parameter.setClass(Character.class);
		parameter.setDefault("");
		assertThrows(IllegalArgumentException.class, () -> fFactory.getDefaultValue(parameter));
	}

	@Test
	@DisplayName("getDefaultValue(String)")
	public void getDefaultValue_for_String() throws NoSuchMethodException {
		final Parameter parameter = new ICodeFactory.Parameter();
		parameter.setClass(String.class);
		parameter.setDefault("abc");
		assertEquals("\"abc\"", fFactory.getDefaultValue(parameter));
	}

	@WrapToScript
	public void methodWithoutAliases() {
		// dummy method for alias tests; do not change the method name!
	}

	@WrapToScript(alias = "one;two")
	public void methodWithAliases() {
		// dummy method for alias tests; do not change the method name!
	}

	@WrapToScript(alias = " one ; ;; two;")
	public void methodWithEmptyAliases() {
		// dummy method for alias tests; do not change the method name!
	}

	public void foo() {
		// dummy method for createFunctionCall() tests
	}

	public void foo(String bar) {
		// dummy method for createFunctionCall() tests
	}

	public void foo(String bar, Object another, boolean doIt) {
		// dummy method for createFunctionCall() tests
	}

	public void foo(int bar) {
		// dummy method for createFunctionCall() tests
	}

	public void foo(boolean bar) {
		// dummy method for createFunctionCall() tests
	}

	public void foo(File bar) {
		// dummy method for createFunctionCall() tests
	}
}
