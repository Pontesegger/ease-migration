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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ease.lang.unittest.runtime.ITestContainer;
import org.eclipse.ease.lang.unittest.runtime.ITestEntity;
import org.eclipse.ease.lang.unittest.runtime.ITestSuite;
import org.eclipse.ease.lang.unittest.runtime.TestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClassicJavaScriptTest extends TestBase {

	private static final String JAVASCRIPT_CLASSIC_SUITE_FILENAME = "JS Classic Testsuite.suite";
	private ITestSuite fTestSuite;

	private ITestEntity getTestEntity(IPath path) {
		return getTestEntity(fTestSuite, path);
	}

	@BeforeEach
	public void runTestSuite() {
		fTestSuite = runSuite(TEST_PROJECT.getFile(JAVASCRIPT_CLASSIC_SUITE_FILENAME));
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
	public void passTestStatus() {
		final ITestEntity test = getTestEntity(new Path("/workspace/UnitTest/tests/JavaScript/Classic/Pass.js"));
		assertEquals(TestStatus.PASS, test.getStatus());
	}

	@Test
	public void ignoreTestStatus() {
		final ITestEntity test = getTestEntity(new Path("/workspace/UnitTest/tests/JavaScript/Classic/Ignore.js"));
		assertEquals(TestStatus.DISABLED, test.getStatus());
	}

	@Test
	public void failureTestStatus() {
		final ITestEntity test = getTestEntity(new Path("/workspace/UnitTest/tests/JavaScript/Classic/Failure.js"));
		assertEquals(TestStatus.FAILURE, test.getStatus());
	}

	@Test
	public void errorTestStatus() {
		ITestEntity test = getTestEntity(new Path("/workspace/UnitTest/tests/JavaScript/Classic/Java Error.js"));
		assertEquals(TestStatus.ERROR, test.getStatus());

		test = getTestEntity(new Path("/workspace/UnitTest/tests/JavaScript/Classic/JavaScript Error.js"));
		assertEquals(TestStatus.ERROR, test.getStatus());
	}

	@Test
	public void verifyOutput() {
		assertTrue(getTestOutput().contains("one"));
		assertTrue(getTestOutput().contains("two"));
		assertTrue(getTestOutput().contains("three"));
		assertTrue(getTestOutput().contains("four"));
		assertTrue(getTestOutput().contains("five"));
		assertTrue(getTestOutput().contains("six"));
		assertTrue(getTestOutput().contains("seven"));
		assertTrue(getTestOutput().contains("eight"));

		assertFalse(getTestOutput().contains("Never to be reached"));
	}
}
