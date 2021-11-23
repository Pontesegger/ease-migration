/*******************************************************************************
 * Copyright (c) 2021 Christian Pontesegger and others.
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

package org.eclipse.ease.lang.unittest.runtime.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ease.lang.unittest.definition.ITestSuiteDefinition;
import org.eclipse.ease.lang.unittest.execution.ITestExecutionStrategy;
import org.eclipse.ease.lang.unittest.runtime.IRuntimeFactory;
import org.eclipse.ease.lang.unittest.runtime.ITestFile;
import org.eclipse.ease.lang.unittest.runtime.ITestSuite;
import org.eclipse.ease.lang.unittest.runtime.TestStatus;
import org.eclipse.emf.common.util.BasicEList;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestFileTest extends TestContainerTest {

	@Override
	protected ITestFile getTestInstance() {
		return IRuntimeFactory.eINSTANCE.createTestFile();
	}

	@Override
	@Test
	@Disabled
	public void run_executes_each_child() {
		// run executes the current file, nothing else
	}

	@Test
	@DisplayName("getEstimatedDuration() = 0 for DISABLED status")
	public void getEstimatedDuration_is_0_for_disabled_status() {
		final ITestFile testFile = getTestInstance();
		testFile.setEntityStatus(TestStatus.DISABLED);

		assertEquals(0, testFile.getEstimatedDuration());
	}

	@Test
	@DisplayName("reset() removes error markers from files")
	public void reset_removes_error_markers_from_files() throws CoreException {
		final IFile file = mock(IFile.class);

		final ITestFile testFile = getTestInstance();
		testFile.setResource(file);

		testFile.reset();

		verify(file).deleteMarkers(any(), anyBoolean(), anyInt());
	}

	@Test
	@DisplayName("reset() disables TestFile when set in suite description")
	public void reset_disables_TestFile_when_set_in_suite_description() {
		final ITestSuiteDefinition definition = mock(ITestSuiteDefinition.class);
		final BasicEList<IPath> disabledPaths = new BasicEList<>();
		disabledPaths.add(new Path("/file"));
		when(definition.getDisabledResources()).thenReturn(disabledPaths);

		final ITestSuite suite = IRuntimeFactory.eINSTANCE.createTestSuite();
		suite.setDefinition(definition);
		suite.setName("suite");

		final ITestFile testFile = getTestInstance();
		testFile.setName("file");
		testFile.setParent(suite);

		testFile.reset();

		assertTrue(testFile.isDisabled());
	}

	@Test
	@DisplayName("run() does not execute disabled tests")
	public void run_does_not_execute_disabled_tests() {
		final ITestExecutionStrategy strategy = mock(ITestExecutionStrategy.class);

		final ITestFile testFile = getTestInstance();
		testFile.setDisabled("disabled");
		testFile.run(strategy);

		verify(strategy, never()).createScriptEngine(any(), any());
	}

	@Test
	@DisplayName("run() adds error when no engine is found")
	public void run_adds_error_when_no_engine_is_found() {
		final ITestExecutionStrategy strategy = mock(ITestExecutionStrategy.class);

		final ITestFile testFile = getTestInstance();
		testFile.run(strategy);

		assertEquals(1, testFile.getChildren().size());
		assertEquals(TestStatus.ERROR, testFile.getStatus());
	}
}
