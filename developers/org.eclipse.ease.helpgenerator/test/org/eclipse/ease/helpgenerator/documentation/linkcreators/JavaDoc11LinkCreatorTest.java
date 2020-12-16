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

package org.eclipse.ease.helpgenerator.documentation.linkcreators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class JavaDoc11LinkCreatorTest {

	private ILinkCreator fLinkCreator;

	@BeforeEach
	public void beforeEach() {
		fLinkCreator = new JavaDoc11LinkCreator(getRemoteDocumentationLocation(), getRemoteSiteContent(), className -> className);
	}

	protected String getRemoteDocumentationLocation() {
		return "https://docs.oracle.com/en/java/javase/11/docs/api";
	}

	private List<String> getRemoteSiteContent() {
		return Arrays.asList("module:java.base", "java.io", "java.lang", "module:java.compiler");
	}

	@Test
	@DisplayName("createLink() throws for @module references")
	public void createThrowsForModuleReferences() {
		assertThrows(IOException.class, () -> fLinkCreator.createLink("{@module #someMethod()}"));
	}

	@Test
	@DisplayName("createLink() throws for invalid references")
	public void createThrowsForInvalidReferences() {
		assertThrows(IOException.class, () -> fLinkCreator.createLink("{@link-not-available}"));
	}

	@Test
	@DisplayName("createLink() throws for unknown package")
	public void createThrowsForUnknownPackage() {
		assertThrows(IOException.class, () -> fLinkCreator.createLink("{@link java.notavailable.SomeClass}"));
	}

	@Test
	@DisplayName("createLink() for {@link java.lang.String#String(byte[])}")
	public void createLinkForConstructor() throws IOException {
		assertEquals("<a href='" + getRemoteDocumentationLocation() + "/java.base/java/lang/String.html#%3Cinit%3E(byte%5B%5D)'>new String(byte[])</a>",
				fLinkCreator.createLink("{@link java.lang.String#String(byte[])}"));
	}

	@Test
	@DisplayName("createLink() for {@link java.lang.String#getBytes()}")
	public void createLinkForMethodWithoutParameter() throws IOException {
		assertEquals("<a href='" + getRemoteDocumentationLocation() + "/java.base/java/lang/String.html#getBytes()'>String.getBytes()</a>",
				fLinkCreator.createLink("{@link java.lang.String#getBytes()}"));
	}

	@Test
	@DisplayName("createLink() for {@link java.lang.String#copyValueOf(char[], int, int)}")
	public void createLinkForMethodWithParameters() throws IOException {
		assertEquals(
				"<a href='" + getRemoteDocumentationLocation()
						+ "/java.base/java/lang/String.html#copyValueOf(char%5B%5D,int,int)'>String.copyValueOf(char[], int, int)</a>",
				fLinkCreator.createLink("{@link java.lang.String#copyValueOf(char[], int, int)}"));
	}

	@Test
	@DisplayName("createLink() for {@link java.io.File#pathSeparator}")
	public void createLinkForConstant() throws IOException {
		assertEquals("<a href='" + getRemoteDocumentationLocation() + "/java.base/java/io/File.html#pathSeparator'>File.pathSeparator</a>",
				fLinkCreator.createLink("{@link java.io.File#pathSeparator}"));
	}

	@Test
	@DisplayName("createLink() for {@link java.io.File}")
	public void createLinkForClass() throws IOException {
		assertEquals("<a href='" + getRemoteDocumentationLocation() + "/java.base/java/io/File.html'>File</a>",
				fLinkCreator.createLink("{@link java.io.File}"));
	}
}
