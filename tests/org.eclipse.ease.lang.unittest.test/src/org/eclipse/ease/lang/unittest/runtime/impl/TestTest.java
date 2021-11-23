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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.eclipse.ease.debugging.ScriptStackTrace;
import org.eclipse.ease.lang.unittest.runtime.IRuntimeFactory;
import org.eclipse.ease.lang.unittest.runtime.ITest;
import org.eclipse.ease.lang.unittest.runtime.TestStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestTest extends TestEntityTest {

	@Override
	protected ITest getTestInstance() {
		return IRuntimeFactory.eINSTANCE.createTest();
	}

	@Test
	@DisplayName("setStackTrace() stores a clone of the trace")
	public void setStackTrace_stores_a_clone_of_the_trace() {
		final ScriptStackTrace stackTrace = mock(ScriptStackTrace.class);

		final ITest test = getTestInstance();
		test.setStackTrace(stackTrace);

		verify(stackTrace).clone();
		assertNotEquals(stackTrace, test.getStackTrace());
	}

	@Test
	@DisplayName("setEndTimestamp() verifies duration limit")
	public void setEndTimestamp_verifies_duration_limit() throws InterruptedException {
		final ITest test = getTestInstance();

		test.setDurationLimit(10);
		test.setEntityStatus(TestStatus.RUNNING);
		Thread.sleep(20);
		test.setEntityStatus(TestStatus.PASS);

		assertEquals(1, test.getResults().size());
		assertEquals(TestStatus.FAILURE, test.getStatus());

	}
}
