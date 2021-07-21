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

package org.eclipse.ease.ui.completion.provider;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.ui.completion.BasicContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class AbstractCompletionProviderTest {

	@ParameterizedTest(name = "for ''{0}''")
	@DisplayName("isMethodParameter(foo, 0) = true")
	@ValueSource(strings = { "foo(", "com.test.foo(" })
	public void isMethodParameter_equals_true(String input) {
		assertTrue(new TestCompletionProvider("foo", 0).isActive(new BasicContext(null, null, input, input.length())));
	}

	@ParameterizedTest(name = "for ''{0}''")
	@DisplayName("isMethodParameter(foo, 0) = false")
	@ValueSource(strings = { "bar(", "foo", "foo(," })
	public void isMethodParameter_equals_false(String input) {
		assertFalse(new TestCompletionProvider("foo", 0).isActive(new BasicContext(null, null, input, input.length())));
	}

	@ParameterizedTest(name = "for ''{0}''")
	@DisplayName("isMethodParameter(foo, 2) = true")
	@ValueSource(strings = { "foo(a, b, ", "foo(a, b, cpart", "com.test.foo(1,2,", "com.test.foo(1,bar(1,2,3)," })
	public void isMethodParameter_equals_true_for_parameter_index(String input) {
		assertTrue(new TestCompletionProvider("foo", 2).isActive(new BasicContext(null, null, input, input.length())));
	}

	@ParameterizedTest(name = "for ''{0}''")
	@DisplayName("isMethodParameter(foo, 2) = false")
	@ValueSource(strings = { "bar(1,2,", "foo", "foo(1,", "foo(1,2,3," })
	public void isMethodParameter_equals_false_for_parameter_index(String input) {
		assertFalse(new TestCompletionProvider("foo", 2).isActive(new BasicContext(null, null, input, input.length())));
	}

	private static final class TestCompletionProvider extends AbstractCompletionProvider {

		private final String fMethodName;
		private final int fParameterIndex;

		private TestCompletionProvider(String methodName, int parameterIndex) {
			fMethodName = methodName;
			fParameterIndex = parameterIndex;
		}

		@Override
		public boolean isActive(ICompletionContext context) {
			return super.isActive(context) && isMethodParameter(context, fMethodName, fParameterIndex);
		}

		@Override
		protected void prepareProposals(ICompletionContext context) {
			// nothing to do
		}
	}
}
