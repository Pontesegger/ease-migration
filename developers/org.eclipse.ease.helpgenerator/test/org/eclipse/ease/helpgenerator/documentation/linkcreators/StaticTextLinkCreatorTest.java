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

package org.eclipse.ease.helpgenerator.documentation.linkcreators;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class StaticTextLinkCreatorTest {

	@Test
	@DisplayName("createLink('{@link java.lang.String#String(byte[])}') -> java.lang.String(byte[])")
	public void createLinkForConstructor() throws IOException {
		final StaticTextLinkCreator linkCreator = new StaticTextLinkCreator();

		assertEquals("new java.lang.String(byte[])", linkCreator.createLink("{@link java.lang.String#String(byte[])}"));
	}

	@Test
	@DisplayName("createLink('{@link java.lang.String#getBytes()}') -> java.lang.String.getBytes()")
	public void createLinkForMethodWithoutParameters() throws IOException {
		final StaticTextLinkCreator linkCreator = new StaticTextLinkCreator();

		assertEquals("java.lang.String.getBytes()", linkCreator.createLink("{@link java.lang.String#getBytes()}"));
	}

	@Test
	@DisplayName("createLink('{@link java.lang.String#copyValueOf(char[], int, int)}') -> java.lang.String.copyValueOf(char[], int, int)")
	public void createLinkForMethodWithParameters() throws IOException {
		final StaticTextLinkCreator linkCreator = new StaticTextLinkCreator();

		assertEquals("java.lang.String.copyValueOf(char[], int, int)", linkCreator.createLink("{@link java.lang.String#copyValueOf(char[], int, int)}"));
	}

	@Test
	@DisplayName("createLink('{@link java.io.File#pathSeparator}') -> java.io.File.pathSeparator")
	public void createLinkForConstant() throws IOException {
		final StaticTextLinkCreator linkCreator = new StaticTextLinkCreator();

		assertEquals("java.io.File.pathSeparator", linkCreator.createLink("{@link java.io.File#pathSeparator}"));
	}
}
