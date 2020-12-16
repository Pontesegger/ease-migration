/*******************************************************************************
 * Copyright (c) 2020 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.helpgenerator.documentation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ClassNameResolverTest {

	private ClassNameResolver fResolver;

	@BeforeEach
	public void beforeEach() {
		fResolver = new ClassNameResolver(Arrays.asList("org.test.Foo", "com.example.Bar"));
	}

	@Test
	@DisplayName("resolveClassName() resolves FQ class name")
	public void resolveFQN() {
		assertEquals("java.io.File", fResolver.resolveClassName("java.io.File"));
	}

	@Test
	@DisplayName("resolveClassName() does not alter unknown class name")
	public void resolveUnknownClassName() {
		assertEquals("UnknownClass", fResolver.resolveClassName("UnknownClass"));
	}

	@Test
	@DisplayName("resolveClassName() detects imported classes")
	public void resolveImportedClasses() {
		assertEquals("org.test.Foo", fResolver.resolveClassName("Foo"));
		assertEquals("com.example.Bar", fResolver.resolveClassName("Bar"));
	}

	@Test
	@DisplayName("resolveClassName() detects classes from java.lang")
	public void resolveSystemClasses() {
		assertEquals("java.lang.Object", fResolver.resolveClassName("Object"));
		assertEquals("java.lang.Math", fResolver.resolveClassName("Math"));
		assertEquals("java.lang.String", fResolver.resolveClassName("String"));
	}
}
