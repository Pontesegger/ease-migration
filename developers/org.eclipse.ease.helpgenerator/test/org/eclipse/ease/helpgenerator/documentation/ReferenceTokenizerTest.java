/*******************************************************************************
 * Copyright (c) 2020 Christian Pontesegger and others.
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

package org.eclipse.ease.helpgenerator.documentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ReferenceTokenizerTest {

	@Test
	@DisplayName("getSimpleClassName('java.lang.String') returns last part of FQN")
	public void getSimpleClassNameReturnsLastPartOfFQN() {
		assertEquals("String", ReferenceTokenizer.getSimpleClassName("java.lang.String"));
	}

	@Test
	@DisplayName("getSimpleClassName('String') returns simple name")
	public void getSimpleClassNameReturnsSimpleString() {
		assertEquals("String", ReferenceTokenizer.getSimpleClassName("String"));
	}

	@Test
	@DisplayName("getPackageName('java.lang.String') returns 'java.lang'")
	public void getPackageNameReturnsPackage() {
		assertEquals("java.lang", ReferenceTokenizer.getPackageName("java.lang.String"));
	}

	@Test
	@DisplayName("getPackageName('String') returns empty string")
	public void getPackageNameReturnsEmptyString() {
		assertEquals("", ReferenceTokenizer.getPackageName("String"));
	}

	@Test
	@DisplayName("getPackageName('String') + getSimpleClassName() return FQN")
	public void getPackageNameAndGetSimpleNameConBeCombined() {
		assertEquals("java.lang.String",
				ReferenceTokenizer.getPackageName("java.lang.String") + "." + ReferenceTokenizer.getSimpleClassName("java.lang.String"));
	}

	@Test
	@DisplayName("tokenize throws on invalid reference")
	public void tokenizeInvalidReference() {
		assertThrows(IOException.class, () -> new ReferenceTokenizer("{@dead}"));
	}

	@Test
	@DisplayName("tokenize '{@link java.io.File}'")
	public void tokenizeClassLink() throws IOException {
		final ReferenceTokenizer tokenizer = new ReferenceTokenizer("{@link java.io.File}");

		assertTrue(tokenizer.isLink());
		assertEquals("java.io.File", tokenizer.getQualifiedName());
		assertNull(tokenizer.getMethod());
		assertNull(tokenizer.getParameters());

		assertFalse(tokenizer.isConstructor());
		assertFalse(tokenizer.isMethod());
		assertFalse(tokenizer.isField());
		assertTrue(tokenizer.isClass());
	}

	@Test
	@DisplayName("tokenize '{@link java.io.File#pathSeparator}'")
	public void tokenizeClassField() throws IOException {
		final ReferenceTokenizer tokenizer = new ReferenceTokenizer("{@link java.io.File#pathSeparator}");

		assertTrue(tokenizer.isLink());
		assertEquals("java.io.File", tokenizer.getQualifiedName());
		assertEquals("pathSeparator", tokenizer.getMethod());
		assertNull(tokenizer.getParameters());

		assertFalse(tokenizer.isConstructor());
		assertFalse(tokenizer.isMethod());
		assertTrue(tokenizer.isField());
		assertFalse(tokenizer.isClass());
	}

	@Test
	@DisplayName("tokenize '{@link java.lang.String#getBytes()}'")
	public void tokenizeClassMethodWithoutParameters() throws IOException {
		final ReferenceTokenizer tokenizer = new ReferenceTokenizer("{@link java.lang.String#getBytes()}");

		assertTrue(tokenizer.isLink());
		assertEquals("java.lang.String", tokenizer.getQualifiedName());
		assertEquals("getBytes", tokenizer.getMethod());
		assertEquals("", tokenizer.getParameters());

		assertFalse(tokenizer.isConstructor());
		assertTrue(tokenizer.isMethod());
		assertFalse(tokenizer.isField());
		assertFalse(tokenizer.isClass());
	}

	@Test
	@DisplayName("tokenize '{@link #getBytes()}'")
	public void tokenizeLocalClassMethod() throws IOException {
		final ReferenceTokenizer tokenizer = new ReferenceTokenizer("{@link #getBytes()}");

		assertTrue(tokenizer.isLink());
		assertEquals("", tokenizer.getQualifiedName());
		assertEquals("getBytes", tokenizer.getMethod());
		assertEquals("", tokenizer.getParameters());

		assertFalse(tokenizer.isConstructor());
		assertTrue(tokenizer.isMethod());
		assertFalse(tokenizer.isField());
		assertFalse(tokenizer.isClass());
	}

	@Test
	@DisplayName("tokenize '{@link #pathSeparator}'")
	public void tokenizeLocalClassField() throws IOException {
		final ReferenceTokenizer tokenizer = new ReferenceTokenizer("{@link #pathSeparator}");

		assertTrue(tokenizer.isLink());
		assertEquals("", tokenizer.getQualifiedName());
		assertEquals("pathSeparator", tokenizer.getMethod());
		assertNull(tokenizer.getParameters());

		assertFalse(tokenizer.isConstructor());
		assertFalse(tokenizer.isMethod());
		assertTrue(tokenizer.isField());
		assertFalse(tokenizer.isClass());
	}

	@Test
	@DisplayName("tokenize '{@link java.lang.String#copyValueOf(char[], int, int)}'")
	public void tokenizeClassMethodWithParameters() throws IOException {
		final ReferenceTokenizer tokenizer = new ReferenceTokenizer("{@link java.lang.String#copyValueOf(char[], int, int)}");

		assertTrue(tokenizer.isLink());
		assertEquals("java.lang.String", tokenizer.getQualifiedName());
		assertEquals("copyValueOf", tokenizer.getMethod());
		assertEquals("char[], int, int", tokenizer.getParameters());

		assertFalse(tokenizer.isConstructor());
		assertTrue(tokenizer.isMethod());
		assertFalse(tokenizer.isField());
		assertFalse(tokenizer.isClass());
	}

	@Test
	@DisplayName("tokenize '{@link java.lang.String#String(byte[])}'")
	public void tokenizeClassConstructor() throws IOException {
		final ReferenceTokenizer tokenizer = new ReferenceTokenizer("{@link java.lang.String#String(byte[])}");

		assertTrue(tokenizer.isLink());
		assertEquals("java.lang.String", tokenizer.getQualifiedName());
		assertEquals("String", tokenizer.getMethod());
		assertEquals("byte[]", tokenizer.getParameters());

		assertTrue(tokenizer.isConstructor());
		assertFalse(tokenizer.isMethod());
		assertFalse(tokenizer.isField());
		assertFalse(tokenizer.isClass());
	}

	@Test
	@DisplayName("tokenize '{@module #methodName()}'")
	public void tokenizeLocalModuleMethod() throws IOException {
		final ReferenceTokenizer tokenizer = new ReferenceTokenizer("{@module #methodName()}");

		assertTrue(tokenizer.isModule());
		assertEquals("", tokenizer.getQualifiedName());
		assertEquals("methodName", tokenizer.getMethod());
		assertEquals("", tokenizer.getParameters());

		assertFalse(tokenizer.isConstructor());
		assertTrue(tokenizer.isMethod());
		assertFalse(tokenizer.isField());
		assertFalse(tokenizer.isClass());
	}

	@Test
	@DisplayName("tokenize '{@module #fieldName}'")
	public void tokenizeModuleConstant() throws IOException {
		final ReferenceTokenizer tokenizer = new ReferenceTokenizer("{@module #fieldName}");

		assertTrue(tokenizer.isModule());
		assertEquals("", tokenizer.getQualifiedName());
		assertEquals("fieldName", tokenizer.getMethod());
		assertNull(tokenizer.getParameters());

		assertFalse(tokenizer.isConstructor());
		assertFalse(tokenizer.isMethod());
		assertTrue(tokenizer.isField());
		assertFalse(tokenizer.isClass());
	}

	@Test
	@DisplayName("tokenize '{@module module.id.of.target.module#methodName()}'")
	public void tokenizeExternalModuleMethod() throws IOException {
		final ReferenceTokenizer tokenizer = new ReferenceTokenizer("{@module module.id.of.target.module#methodName()}");

		assertTrue(tokenizer.isModule());
		assertEquals("module.id.of.target.module", tokenizer.getQualifiedName());
		assertEquals("methodName", tokenizer.getMethod());
		assertEquals("", tokenizer.getParameters());

		assertFalse(tokenizer.isConstructor());
		assertTrue(tokenizer.isMethod());
		assertFalse(tokenizer.isField());
		assertFalse(tokenizer.isClass());
	}

	@Test
	@DisplayName("tokenize '{@module module.id.of.target.module}'")
	public void tokenizeExternalModule() throws IOException {
		final ReferenceTokenizer tokenizer = new ReferenceTokenizer("{@module module.id.of.target.module}");

		assertTrue(tokenizer.isModule());
		assertEquals("module.id.of.target.module", tokenizer.getQualifiedName());
		assertNull(tokenizer.getMethod());
		assertNull(tokenizer.getParameters());

		assertFalse(tokenizer.isConstructor());
		assertFalse(tokenizer.isMethod());
		assertFalse(tokenizer.isField());
		assertTrue(tokenizer.isClass());
	}

	@Test
	@DisplayName("tokenize '{@link String#getBytes()}'")
	public void tokenizeSimpleClassName() throws IOException {
		final ReferenceTokenizer tokenizer = new ReferenceTokenizer("{@link String#getBytes()}", name -> "java.lang." + name);

		assertTrue(tokenizer.isLink());
		assertEquals("java.lang.String", tokenizer.getQualifiedName());
		assertEquals("getBytes", tokenizer.getMethod());
		assertEquals("", tokenizer.getParameters());

		assertFalse(tokenizer.isConstructor());
		assertTrue(tokenizer.isMethod());
		assertFalse(tokenizer.isField());
		assertFalse(tokenizer.isClass());
	}
}
