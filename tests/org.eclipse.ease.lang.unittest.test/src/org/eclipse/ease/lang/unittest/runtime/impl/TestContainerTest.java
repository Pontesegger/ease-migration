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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.eclipse.ease.lang.unittest.execution.ITestExecutionStrategy;
import org.eclipse.ease.lang.unittest.runtime.IRuntimeFactory;
import org.eclipse.ease.lang.unittest.runtime.ITest;
import org.eclipse.ease.lang.unittest.runtime.ITestContainer;
import org.eclipse.ease.lang.unittest.runtime.ITestFile;
import org.eclipse.ease.lang.unittest.runtime.ITestFolder;
import org.eclipse.ease.lang.unittest.runtime.TestStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestContainerTest extends TestEntityTest {

	@Override
	protected ITestContainer getTestInstance() {
		return new Container();
	}

	@Override
	@Test
	@Disabled
	public void getResource_returns_parent_resource() {
		// behavior is different for TestContainer
	}

	@Test
	@DisplayName("getTest() = null when child test does not exist")
	public void getTest_is_null_when_child_test_does_not_exist() {
		final ITestContainer container = getTestInstance();
		final ITest test = container.getTest("pleaseCreate");

		assertEquals("pleaseCreate", test.getName());
	}

	@Test
	@DisplayName("getTest() returns child with matching name")
	public void getTest_returns_child_with_matching_name() {
		final ITestContainer container = getTestInstance();

		final ITest test1 = IRuntimeFactory.eINSTANCE.createTest();
		test1.setName("first");
		container.getChildren().add(test1);
		final ITest test2 = IRuntimeFactory.eINSTANCE.createTest();
		test2.setName("second");
		container.getChildren().add(test2);

		assertEquals(test2, container.getTest("second"));
	}

	@Test
	@DisplayName("run() executes each child")
	public void run_executes_each_child() {
		final ITestContainer container = getTestInstance();

		final ITestFile test1 = IRuntimeFactory.eINSTANCE.createTestFile();
		container.getChildren().add(test1);
		final ITestFile test2 = IRuntimeFactory.eINSTANCE.createTestFile();
		container.getChildren().add(test2);

		final ITestExecutionStrategy strategy = mock(ITestExecutionStrategy.class);

		container.run(strategy);

		verify(strategy, times(2)).execute(any());
	}

	@Test
	@DisplayName("getChildContainers() returns only containers")
	public void getChildContainers_returns_only_containers() {
		final ITestContainer container = getTestInstance();

		final ITest test = IRuntimeFactory.eINSTANCE.createTest();
		container.getChildren().add(test);
		final ITestFolder testFolder = IRuntimeFactory.eINSTANCE.createTestFolder();
		container.getChildren().add(testFolder);

		assertEquals(1, container.getChildContainers().size());
	}

	@Test
	@DisplayName("getStatus() returns worst child status")
	public void getStatus_returns_worst_child_status() {
		final ITestContainer container = getTestInstance();

		final ITestFile test1 = IRuntimeFactory.eINSTANCE.createTestFile();
		test1.setEntityStatus(TestStatus.FAILURE);
		container.getChildren().add(test1);
		final ITestFile test2 = IRuntimeFactory.eINSTANCE.createTestFile();
		test2.setEntityStatus(TestStatus.PASS);
		container.getChildren().add(test2);

		assertEquals(TestStatus.FAILURE, container.getStatus());
	}

	@Test
	@DisplayName("getStatus() returns entity status")
	public void getStatus_returns_entity_status() {
		final ITestContainer container = getTestInstance();

		final ITestFile test1 = IRuntimeFactory.eINSTANCE.createTestFile();
		test1.setEntityStatus(TestStatus.FAILURE);
		container.getChildren().add(test1);
		final ITestFile test2 = IRuntimeFactory.eINSTANCE.createTestFile();
		test2.setEntityStatus(TestStatus.PASS);
		container.getChildren().add(test2);

		container.setEntityStatus(TestStatus.ERROR);

		assertEquals(TestStatus.ERROR, container.getStatus());
	}

	@Test
	@DisplayName("hasError() queries children for error status")
	public void hasError_queries_children_for_error_status() {
		final ITestContainer container = getTestInstance();

		final ITestFile test1 = IRuntimeFactory.eINSTANCE.createTestFile();
		test1.addError("error", null);
		container.getChildren().add(test1);

		assertTrue(container.hasError());
	}

	@Test
	@DisplayName("reset() deletes children of type ITest")
	public void reset_deletes_children_of_type_ITest() {
		final ITestContainer container = getTestInstance();

		final ITestFile testFile = IRuntimeFactory.eINSTANCE.createTestFile();
		container.getChildren().add(testFile);

		final ITest test = IRuntimeFactory.eINSTANCE.createTest();
		container.getChildren().add(test);

		container.reset();

		assertEquals(1, container.getChildren().size());
		assertEquals(testFile, container.getChildren().get(0));
	}

	@Test
	@DisplayName("getEstimatedDuration() sums child estimations")
	public void getEstimatedDuration_sums_child_estimations() {
		final ITestContainer container = getTestInstance();

		for (final int estimation : new int[] { 10, 20, 30 }) {
			final ITestFile testFile = IRuntimeFactory.eINSTANCE.createTestFile();
			testFile.setEstimatedDuration(estimation);
			container.getChildren().add(testFile);
		}

		assertEquals(60, container.getEstimatedDuration());
	}

	@Test
	@DisplayName("setTerminated(true) terminates children too")
	public void setTerminated_terminates_children_too() {
		final ITestContainer container = getTestInstance();

		final ITestFile testFile = IRuntimeFactory.eINSTANCE.createTestFile();
		container.getChildren().add(testFile);

		container.setTerminated(true);

		assertTrue(container.isTerminated());
		assertTrue(testFile.isTerminated());
	}

	private static class Container extends TestContainer {

	}
}
