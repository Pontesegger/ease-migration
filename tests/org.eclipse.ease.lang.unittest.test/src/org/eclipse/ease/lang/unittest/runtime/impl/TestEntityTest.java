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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.Path;
import org.eclipse.ease.IDebugEngine;
import org.eclipse.ease.debugging.ScriptStackTrace;
import org.eclipse.ease.lang.unittest.runtime.IMetadata;
import org.eclipse.ease.lang.unittest.runtime.IRuntimeFactory;
import org.eclipse.ease.lang.unittest.runtime.ITestEntity;
import org.eclipse.ease.lang.unittest.runtime.ITestFolder;
import org.eclipse.ease.lang.unittest.runtime.ITestResult;
import org.eclipse.ease.lang.unittest.runtime.ITestSuite;
import org.eclipse.ease.lang.unittest.runtime.TestStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestEntityTest {

	protected ITestEntity getTestInstance() {
		return new Entity();
	}

	@Test
	@DisplayName("setEntityStatus(RUNNING) sets start timestamp")
	public void setEntityStatus_RUNNING_sets_start_timestamp() {
		final ITestEntity entity = getTestInstance();

		assertEquals(0, entity.getStartTimestamp());

		entity.setEntityStatus(TestStatus.RUNNING);

		assertNotEquals(0, entity.getStartTimestamp());
		assertTrue(entity.getStartTimestamp() <= System.currentTimeMillis());
	}

	@Test
	@DisplayName("setEntityStatus(FINISHED) sets end timestamp")
	public void setEntityStatus_FINISHED_sets_end_timestamp() {
		final ITestEntity entity = getTestInstance();

		assertEquals(0, entity.getEndTimestamp());

		entity.setEntityStatus(TestStatus.FINISHED);

		assertNotEquals(0, entity.getEndTimestamp());
		assertTrue(entity.getEndTimestamp() <= System.currentTimeMillis());
	}

	@Test
	@DisplayName("getDuration() = 0 for not executed test")
	public void getDuration_is_0_for_not_executed_test() {
		assertEquals(0, new Entity().getDuration());
	}

	@Test
	@DisplayName("getDuration() returns current execution time for running test")
	public void getDuration_returns_current_execution_time_for_running_test() throws InterruptedException {
		final ITestEntity entity = getTestInstance();
		entity.setEntityStatus(TestStatus.RUNNING);

		Thread.sleep(100);
		assertTrue(entity.getDuration() >= 100);

		Thread.sleep(100);
		assertTrue(entity.getDuration() >= 200);
	}

	@Test
	@DisplayName("getDuration() returns execution time for completed test")
	public void getDuration_returns_execution_time_for_completed_test() throws InterruptedException {
		final ITestEntity entity = getTestInstance();
		entity.setEntityStatus(TestStatus.RUNNING);
		Thread.sleep(100);
		entity.setEntityStatus(TestStatus.FINISHED);

		assertTrue(entity.getDuration() >= 100);
	}

	@Test
	@DisplayName("addError() adds error result")
	public void addError_adds_error_result() {
		final IDebugEngine engine = mock(IDebugEngine.class);
		when(engine.getExceptionStackTrace()).thenReturn(new ScriptStackTrace());

		final ITestEntity entity = getTestInstance();
		entity.addError("error message", engine);

		assertEquals(1, entity.getResults().size());

		assertEquals(TestStatus.ERROR, entity.getResults().get(0).getStatus());
		assertNotNull(entity.getResults().get(0).getStackTrace());
	}

	@Test
	@DisplayName("getResource() returns parent resource")
	public void getResource_returns_parent_resource() {
		final ITestFolder parent = IRuntimeFactory.eINSTANCE.createTestFolder();
		parent.setResource("resource");

		final ITestEntity entity = getTestInstance();
		entity.setParent(parent);

		assertEquals("resource", entity.getResource());
	}

	@Test
	@DisplayName("hasError() = true when error exists")
	public void hasError_is_true_when_error_exists() {
		final ITestEntity entity = getTestInstance();
		entity.addError("error", null);

		assertTrue(entity.hasError());
	}

	@Test
	@DisplayName("hasError() = false when no error exists")
	public void hasError_is_false_when_no_error_exists() {
		final ITestEntity entity = getTestInstance();

		assertFalse(entity.hasError());
	}

	@Test
	@DisplayName("getStatus() = status of worst result")
	public void getStatus_is_status_of_worst_result() {
		final ITestEntity entity = getTestInstance();

		for (final TestStatus status : new TestStatus[] { TestStatus.ERROR, TestStatus.PASS }) {
			final ITestResult result = IRuntimeFactory.eINSTANCE.createTestResult();
			result.setStatus(status);
			entity.getResults().add(result);
		}

		assertEquals(TestStatus.ERROR, entity.getStatus());
	}

	@Test
	@DisplayName("getStatus() = entity status if it is the worst status")
	public void getStatus_is_entity_status_if_it_is_the_worst_status() {
		final ITestEntity entity = getTestInstance();
		entity.setEntityStatus(TestStatus.ERROR);

		for (final TestStatus status : new TestStatus[] { TestStatus.FAILURE, TestStatus.PASS }) {
			final ITestResult result = IRuntimeFactory.eINSTANCE.createTestResult();
			result.setStatus(status);
			entity.getResults().add(result);
		}

		assertEquals(TestStatus.ERROR, entity.getStatus());
	}

	@Test
	@DisplayName("getStatus() = DISABLED when 1 result is disabled")
	public void getStatus_is_DISABLED_when_1_result_is_disabled() {
		final ITestEntity entity = getTestInstance();

		for (final TestStatus status : new TestStatus[] { TestStatus.ERROR, TestStatus.PASS, TestStatus.DISABLED }) {
			final ITestResult result = IRuntimeFactory.eINSTANCE.createTestResult();
			result.setStatus(status);
			entity.getResults().add(result);
		}

		assertEquals(TestStatus.DISABLED, entity.getStatus());
	}

	@Test
	@DisplayName("getStatus() = DISABLED when entity status is disabled")
	public void getStatus_is_DISABLED_when_entity_status_is_disabled() {
		final ITestEntity entity = getTestInstance();
		entity.setEntityStatus(TestStatus.DISABLED);

		for (final TestStatus status : new TestStatus[] { TestStatus.ERROR, TestStatus.PASS }) {
			final ITestResult result = IRuntimeFactory.eINSTANCE.createTestResult();
			result.setStatus(status);
			entity.getResults().add(result);
		}

		assertEquals(TestStatus.DISABLED, entity.getStatus());
	}

	@Test
	@DisplayName("getRoot() returns root element")
	public void getRoot_returns_root_element() {
		final ITestFolder parent = IRuntimeFactory.eINSTANCE.createTestFolder();

		final ITestEntity entity = getTestInstance();
		entity.setParent(parent);

		assertEquals(parent, entity.getRoot());
	}

	@Test
	@DisplayName("getTestSuite() returns master suite")
	public void getTestSuite_returns_master_suite() {
		final ITestSuite suite = IRuntimeFactory.eINSTANCE.createTestSuite();

		final ITestEntity entity = getTestInstance();
		entity.setParent(suite);

		assertEquals(suite, entity.getTestSuite());
	}

	@Test
	@DisplayName("reset() clears timestamps")
	public void reset_clears_timestamps() {
		final ITestEntity entity = getTestInstance();
		entity.setEntityStatus(TestStatus.RUNNING);
		entity.setEntityStatus(TestStatus.FINISHED);

		entity.reset();

		assertEquals(0, entity.getStartTimestamp());
		assertEquals(0, entity.getEndTimestamp());
	}

	@Test
	@DisplayName("reset() clears results")
	public void reset_clears_results() {
		final ITestEntity entity = getTestInstance();
		entity.addError("error", null);

		entity.reset();

		assertTrue(entity.getResults().isEmpty());
	}

	@Test
	@DisplayName("reset() clears metadata")
	public void reset_clears_metadata() {
		final IMetadata metadata = IRuntimeFactory.eINSTANCE.createMetadata();
		metadata.setKey("key");

		final ITestEntity entity = getTestInstance();
		entity.getMetadata().add(metadata);

		entity.reset();

		assertTrue(entity.getMetadata().isEmpty());
	}

	@Test
	@DisplayName("reset() clears status")
	public void reset_clears_status() {
		final ITestEntity entity = getTestInstance();
		entity.setEntityStatus(TestStatus.ERROR);

		entity.reset();

		assertEquals(TestStatus.NOT_RUN, entity.getStatus());
	}

	@Test
	@DisplayName("getWorstResult() returns worst execution result")
	public void getWorstResult_returns_worst_execution_result() {
		final ITestEntity entity = getTestInstance();

		for (final TestStatus state : TestStatus.VALUES) {
			final ITestResult result = IRuntimeFactory.eINSTANCE.createTestResult();
			result.setStatus(state);
			entity.getResults().add(result);
		}

		assertEquals(TestStatus.RUNNING, entity.getWorstResult().getStatus());
	}

	@Test
	@DisplayName("getResults(FAILURE) returns only matching results")
	public void getResults_FAILURE_returns_only_matching_results() {
		final ITestEntity entity = getTestInstance();

		final TestStatus[] states = new TestStatus[] { TestStatus.FAILURE, TestStatus.PASS, TestStatus.ERROR, TestStatus.FAILURE, TestStatus.FAILURE,
				TestStatus.PASS };

		for (final TestStatus state : states) {
			final ITestResult result = IRuntimeFactory.eINSTANCE.createTestResult();
			result.setStatus(state);
			entity.getResults().add(result);
		}

		assertEquals(3, entity.getResults(TestStatus.FAILURE).size());
	}

	@Test
	@DisplayName("getFullPath() = test name")
	public void getFullPath_returns_test_name() {
		final ITestEntity entity = getTestInstance();
		entity.setName("TestName");

		assertEquals(new Path("/TestName"), entity.getFullPath());
	}

	@Test
	@DisplayName("getFullPath() = /parent/test name")
	public void getFullPath_returns_parent_path_and_test_name() {
		final ITestFolder parent = IRuntimeFactory.eINSTANCE.createTestFolder();
		parent.setName("parent");

		final ITestEntity entity = getTestInstance();
		entity.setName("TestName");

		entity.setParent(parent);

		assertEquals(new Path("/parent/TestName"), entity.getFullPath());
	}

	@Test
	@DisplayName("setDisabled() marks test as disabled")
	public void setDisabled_marks_test_as_disabled() {
		final ITestEntity entity = getTestInstance();
		assertFalse(entity.isDisabled());

		entity.setDisabled("disabled");
		assertTrue(entity.isDisabled());
	}

	@Test
	@DisplayName("isDisabled() = false for finished test")
	public void isDisabled_is_false_for_finished_test() {
		final ITestEntity entity = getTestInstance();
		entity.setEntityStatus(TestStatus.RUNNING);
		entity.setEntityStatus(TestStatus.FINISHED);

		assertFalse(entity.isDisabled());
	}

	@Test
	@DisplayName("isDisabled() = true when 1 result is DISABLED")
	public void isDisabled_is_true_when_1_result_is_DISABLED() {
		final ITestEntity entity = getTestInstance();
		entity.setEntityStatus(TestStatus.RUNNING);

		final ITestResult result = IRuntimeFactory.eINSTANCE.createTestResult();
		result.setStatus(TestStatus.DISABLED);
		entity.getResults().add(result);

		entity.setEntityStatus(TestStatus.FINISHED);

		assertTrue(entity.isDisabled());
	}

	@Test
	@DisplayName("isDisabled() = true when status = DISABLED")
	public void isDisabled_is_true_when_status_is_DISABLED() {
		final ITestEntity entity = getTestInstance();
		entity.setEntityStatus(TestStatus.DISABLED);

		assertTrue(entity.isDisabled());
	}

	@Test
	@DisplayName("getEstimatedDuration() = -1 by default")
	public void getEstimatedDuration_is_minus_1_by_default() {
		assertEquals(-1, getTestInstance().getEstimatedDuration());
	}

	private static class Entity extends TestEntity {

	}
}
