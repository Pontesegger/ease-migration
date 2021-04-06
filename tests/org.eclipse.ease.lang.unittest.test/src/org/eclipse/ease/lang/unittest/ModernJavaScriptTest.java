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

package org.eclipse.ease.lang.unittest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.concurrent.ExecutionException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ease.lang.unittest.runtime.ITestContainer;
import org.eclipse.ease.lang.unittest.runtime.ITestEntity;
import org.eclipse.ease.lang.unittest.runtime.ITestSuite;
import org.eclipse.ease.lang.unittest.runtime.TestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ModernJavaScriptTest extends TestBase {

	private static final String JAVASCRIPT_MODERN_SUITE_FILENAME = "JS Modern Testsuite.suite";
	private ITestSuite fTestSuite;

	private ITestEntity getTestEntity(IPath path) {
		return getTestEntity(fTestSuite, path);
	}

	@BeforeEach
	public void runTestSuite() throws ExecutionException {
		fTestSuite = runSuite(TEST_PROJECT.getFile(JAVASCRIPT_MODERN_SUITE_FILENAME));
	}

	@Test
	public void suiteStatus() {
		assertEquals(TestStatus.ERROR, fTestSuite.getStatus());
	}

	@Test
	public void folderStatus() {
		ITestContainer test = (ITestContainer) getTestEntity(new Path("/workspace/UnitTest/tests/JavaScript/Classic"));

		while (test != null) {
			assertEquals(TestStatus.ERROR, test.getStatus());
			test = test.getParent();
		}
	}

	@Test
	public void passTestClassStatus() {
		final ITestEntity test = getTestEntity(new Path("/workspace/UnitTest/tests/JavaScript/Modern/Pass.js"));
		assertEquals(TestStatus.PASS, test.getStatus());
	}

	@Test
	public void ignoreTestClassStatus() {
		final ITestEntity test = getTestEntity(new Path("/workspace/UnitTest/tests/JavaScript/Modern/Ignore Class.js"));
		assertEquals(TestStatus.DISABLED, test.getStatus());
	}

	@Test
	public void ignoreTestStatus() {
		final ITestEntity test = getTestEntity(new Path("/workspace/UnitTest/tests/JavaScript/Modern/Ignore Test.js"));
		assertEquals(TestStatus.DISABLED, test.getStatus());
	}

	@Test
	public void failureTestClassStatus() {
		final ITestEntity test = getTestEntity(new Path("/workspace/UnitTest/tests/JavaScript/Modern/Failure.js"));
		assertEquals(TestStatus.FAILURE, test.getStatus());
	}

	@Test
	public void errorTestClassStatus() {
		final ITestEntity test = getTestEntity(new Path("/workspace/UnitTest/tests/JavaScript/Modern/Error.js"));
		assertEquals(TestStatus.ERROR, test.getStatus());
	}

	@Test
	public void verifyOutput() {
		assertFalse(getTestOutput().contains("Never to be reached"));
	}
}
