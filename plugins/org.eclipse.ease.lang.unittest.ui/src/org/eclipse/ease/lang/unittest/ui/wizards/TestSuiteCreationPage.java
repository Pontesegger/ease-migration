/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.lang.unittest.ui.wizards;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.ease.Logger;
import org.eclipse.ease.lang.unittest.UnitTestHelper;
import org.eclipse.ease.lang.unittest.definition.IDefinitionFactory;
import org.eclipse.ease.lang.unittest.definition.ITestSuiteDefinition;
import org.eclipse.ease.lang.unittest.ui.Activator;
import org.eclipse.ease.tools.StringTools;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

public class TestSuiteCreationPage extends WizardNewFileCreationPage {

	public TestSuiteCreationPage(final String pageName, final IStructuredSelection selection) {
		super(pageName, selection);
	}

	@Override
	protected InputStream getInitialContents() {
		final ITestSuiteDefinition testSuite = IDefinitionFactory.eINSTANCE.createTestSuiteDefinition();
		testSuite.setVersion("1.0");
		testSuite.setIncludeFilter("project://**.js" + StringTools.LINE_DELIMITER + "project://**.py");

		try {
			return new ByteArrayInputStream(UnitTestHelper.serializeTestSuite(testSuite));

		} catch (final IOException e) {
			Logger.error(Activator.PLUGIN_ID, "Could not create initial testsuite data");
			return null;
		}
	}
}
