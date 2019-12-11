/*******************************************************************************
 * Copyright (c) 2019 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.helpgenerator;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Test;

public class IntegrationTestJava5API {

	private static int buildDocs(boolean failOnHtmlError, boolean failOnMissingDocs, String packageName) {
		// @formatter:off
		return com.sun.tools.javadoc.Main.execute(new String[] {
				"-sourcepath", new File("./resources/org.eclipse.ease.helpgenerator.testproject/src").getAbsolutePath(),
				"-root", new File("./resources/org.eclipse.ease.helpgenerator.testproject").getAbsolutePath(),
				"-doclet", V8ModuleDoclet.class.getName(),
				"-docletpath",  new File("./target/classes").getAbsolutePath(),

				"-failOnHTMLError", Boolean.toString(failOnHtmlError),
				"-failOnMissingDocs", Boolean.toString(failOnMissingDocs),

				"-link", "https://docs.oracle.com/javase/8/docs/api",

				packageName
		});
		// @formatter:on
	}

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
	public void verifyContent() throws IOException {
		buildDocs(true, true, "org.eclipse.ease.helpgenerator.testproject.valid");
		String expected = new String(
				Files.readAllBytes(new File("./resources/expected_module_org.eclipse.ease.helpgenerator.testproject.module1.html").toPath()));
		expected = expected.replaceAll("\r", "");

		final String actual = new String(Files.readAllBytes(
				new File("./resources/org.eclipse.ease.helpgenerator.testproject/help/module_org.eclipse.ease.helpgenerator.testproject.module1.html")
						.toPath()));

		assertEquals(expected, actual);
	}
}
