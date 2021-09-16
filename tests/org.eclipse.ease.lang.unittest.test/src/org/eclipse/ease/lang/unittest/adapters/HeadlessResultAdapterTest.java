/*******************************************************************************
 * Copyright (c) 2021 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.unittest.adapters;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.ease.lang.unittest.runtime.ITestSuite;
import org.eclipse.ease.lang.unittest.runtime.TestStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class HeadlessResultAdapterTest {

	@Test
	@DisplayName("getAdapterList() returns only ITestSuite")
	public void getAdapterList_returns_only_ITestSuite() {
		assertArrayEquals(new Class<?>[] { ITestSuite.class }, new HeadlessResultAdapter().getAdapterList());
	}

	@Test
	@DisplayName("getAdapter() declines to adapt generic object")
	public void getAdapter_declines_to_adapt_generic_object() {
		assertNull(new HeadlessResultAdapter().getAdapter(new Object(), Integer.class));
	}

	@Test
	@DisplayName("getAdapter() declines to adapt to Boolean")
	public void getAdapter_declines_to_adapt_to_Boolean() {
		final ITestSuite suite = mock(ITestSuite.class);
		assertNull(new HeadlessResultAdapter().getAdapter(suite, Boolean.class));
	}

	@Test
	@DisplayName("getAdapter(PASS) = 0")
	public void getAdapter_adapts_testsuite_with_no_errors_to_0() {
		final ITestSuite suite = mock(ITestSuite.class);
		when(suite.getStatus()).thenReturn(TestStatus.PASS);

		assertEquals(0, new HeadlessResultAdapter().getAdapter(suite, Integer.class));
	}

	@Test
	@DisplayName("getAdapter(FAILURE) = 100")
	public void getAdapter_adapts_testsuite_with_failures_to_100() {
		final ITestSuite suite = mock(ITestSuite.class);
		when(suite.getStatus()).thenReturn(TestStatus.FAILURE);

		assertEquals(100, new HeadlessResultAdapter().getAdapter(suite, Integer.class));
	}

	@Test
	@DisplayName("getAdapter(ERROR) = 200")
	public void getAdapter_adapts_testsuite_with_errors_to_200() {
		final ITestSuite suite = mock(ITestSuite.class);
		when(suite.getStatus()).thenReturn(TestStatus.ERROR);

		assertEquals(200, new HeadlessResultAdapter().getAdapter(suite, Integer.class));
	}

	@Test
	@DisplayName("adapter can be called via adapter framework")
	public void adapter_can_be_called_via_adapter_framework() {
		final ITestSuite suite = mock(ITestSuite.class);
		when(suite.getStatus()).thenReturn(TestStatus.PASS);

		assertEquals(0, Adapters.adapt(suite, Integer.class));
	}
}
