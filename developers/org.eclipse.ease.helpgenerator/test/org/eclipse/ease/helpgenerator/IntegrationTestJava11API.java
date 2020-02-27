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

import java.io.File;

import javax.tools.DocumentationTool;
import javax.tools.ToolProvider;

public class IntegrationTestJava11API extends AbstractIntegrationTest {

	@Override
	protected int buildDocs(boolean failOnHtmlError, boolean failOnMissingDocs, String packageName) {

		final DocumentationTool documentationTool = ToolProvider.getSystemDocumentationTool();
		return documentationTool.run(System.in, System.out, System.err,

				"-sourcepath", new File("./resources/org.eclipse.ease.helpgenerator.testproject/src").getAbsolutePath(), "-root",
				new File("./resources/org.eclipse.ease.helpgenerator.testproject").getAbsolutePath(), "-doclet", V9ModuleDoclet.class.getName(), "-docletpath",
				new File("./target/classes").getAbsolutePath(),

				"-failOnHTMLError", Boolean.toString(failOnHtmlError), "-failOnMissingDocs", Boolean.toString(failOnMissingDocs),

				"-link", "https://docs.oracle.com/javase/8/docs/api",

				packageName);
	}
}
