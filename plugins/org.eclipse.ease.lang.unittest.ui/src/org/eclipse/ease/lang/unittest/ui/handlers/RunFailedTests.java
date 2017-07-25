/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.unittest.ui.handlers;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.ease.lang.unittest.FilteredTestCommand;
import org.eclipse.ease.lang.unittest.runtime.ITestContainer;
import org.eclipse.ease.lang.unittest.runtime.ITestEntity;
import org.eclipse.ease.lang.unittest.runtime.ITestSuite;
import org.eclipse.ease.lang.unittest.runtime.TestStatus;

public class RunFailedTests extends RunAllTests {

	private static Collection<ITestEntity> addTestsWithErrors(ITestContainer container, Collection<ITestEntity> containedTests) {
		if ((TestStatus.ERROR.equals(container.getStatus())) || (TestStatus.FAILURE.equals(container.getStatus())))
			containedTests.add(container);

		for (final ITestContainer child : container.getChildContainers())
			addTestsWithErrors(child, containedTests);

		return containedTests;
	}

	@Override
	protected Object getTestRoot(ITestSuite testSuite) {
		return new FilteredTestCommand(testSuite, addTestsWithErrors(testSuite, new HashSet<>()));
	}
}
