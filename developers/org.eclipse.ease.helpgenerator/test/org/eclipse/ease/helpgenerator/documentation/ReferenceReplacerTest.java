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

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ReferenceReplacerTest {

	public static String replace(String reference) {
		return reference.startsWith("{@link") ? "{@link}" : "{@module}";
	}

	@Test
	@DisplayName("replaceReferences() on empty string")
	public void findLinkReferenceOnEmptyString() {
		final ReferenceReplacer replacer = new ReferenceReplacer();
		replacer.addLinkCreator(ReferenceReplacerTest::replace);

		assertEquals("", replacer.replaceReferences(""));
	}

	@Test
	@DisplayName("replaceReferences() on string without reference")
	public void findLinkReferenceOnGenericString() {
		final ReferenceReplacer replacer = new ReferenceReplacer();
		replacer.addLinkCreator(ReferenceReplacerTest::replace);

		assertEquals("this text does not contain a reference", replacer.replaceReferences("this text does not contain a reference"));
	}

	@Test
	@DisplayName("replaceReferences() on string with 1 link reference")
	public void findLinkReferenceOn1ReferenceString() {
		final ReferenceReplacer replacer = new ReferenceReplacer();
		replacer.addLinkCreator(ReferenceReplacerTest::replace);

		assertEquals("See {@link} method", replacer.replaceReferences("See {@link java.lang.String#String(byte[])} method"));
	}

	@Test
	@DisplayName("replaceReferences() on string with 2 link references")
	public void findLinkReferenceOn2ReferenceStrings() {
		final ReferenceReplacer replacer = new ReferenceReplacer();
		replacer.addLinkCreator(ReferenceReplacerTest::replace);

		assertEquals("See {@link} method, or {@link} for a reference",
				replacer.replaceReferences("See {@link java.lang.String#String(byte[])} method, or {@link java.io.File#pathSeparator} for a reference"));
	}

	@Test
	@DisplayName("replaceReferences() on string with 2 link references and 1 module reference")
	public void findLinkReferenceOn3ReferenceStrings() {
		final ReferenceReplacer replacer = new ReferenceReplacer();
		replacer.addLinkCreator(ReferenceReplacerTest::replace);

		assertEquals("See {@link} method, {@module} or {@link} for a reference", replacer.replaceReferences(
				"See {@link java.lang.String#String(byte[])} method, {@module #sibling()} or {@link java.io.File#pathSeparator} for a reference"));
	}

	@Test
	@DisplayName("replaceReferences() uses static text when linkCreator fails")
	public void replaceReferencesUsesStaticTextAsFallback() {
		final ReferenceReplacer replacer = new ReferenceReplacer();
		replacer.addLinkCreator(reference -> {
			throw new IOException("not implementd");
		});

		assertEquals("See java.io.File", replacer.replaceReferences("See {@link java.io.File}"));
	}
}
