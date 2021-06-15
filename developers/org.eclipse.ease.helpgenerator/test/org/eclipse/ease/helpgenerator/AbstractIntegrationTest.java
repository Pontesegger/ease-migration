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

package org.eclipse.ease.helpgenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

public abstract class AbstractIntegrationTest {

	protected abstract int buildDocs(boolean failOnHtmlError, boolean failOnMissingDocs, String packageName);

	@Test
	public void validModule() {
		assertEquals(0, buildDocs(true, true, "org.eclipse.ease.helpgenerator.testproject.valid"));
	}

	@Test
	public void invalidXMLIgnoreErrors() {
		assertEquals(0, buildDocs(false, false, "org.eclipse.ease.helpgenerator.testproject.invalidxml"));
	}

	@Test
	public void invalidXMLShouldFail() {
		assertEquals(1, buildDocs(true, false, "org.eclipse.ease.helpgenerator.testproject.invalidxml"));
	}

	@Test
	public void missingDocsIgnoreErrors() {
		assertEquals(0, buildDocs(false, false, "org.eclipse.ease.helpgenerator.testproject.missingdocs"));
	}

	@Test
	public void missingDocsShouldFail() {
		assertEquals(1, buildDocs(false, true, "org.eclipse.ease.helpgenerator.testproject.missingdocs"));
	}

	@Test
	public void verifyValidContent() throws IOException {
		buildDocs(true, true, "org.eclipse.ease.helpgenerator.testproject.valid");

		String expectedContent = new String(
				Files.readAllBytes(new File("./resources/expected_module_org.eclipse.ease.helpgenerator.testproject.module1.html").toPath()));
		expectedContent = expectedContent.replaceAll("\r", "");

		final String actualContent = new String(Files.readAllBytes(
				new File("./resources/org.eclipse.ease.helpgenerator.testproject/help/module_org.eclipse.ease.helpgenerator.testproject.module1.html")
						.toPath()));

		assertEquals(expectedContent, actualContent);
	}

	@Test
	public void verifyDeprecatedContent() throws IOException {
		buildDocs(true, true, "org.eclipse.ease.helpgenerator.testproject.valid");

		String expectedContent = new String(
				Files.readAllBytes(new File("./resources/expected_module_org.eclipse.ease.helpgenerator.testproject.deprecatedModule.html").toPath()));
		expectedContent = expectedContent.replaceAll("\r", "");

		final String actualContent = new String(Files.readAllBytes(
				new File("./resources/org.eclipse.ease.helpgenerator.testproject/help/module_org.eclipse.ease.helpgenerator.testproject.deprecatedmodule.html")
						.toPath()));

		assertEquals(expectedContent, actualContent);
	}
}
