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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.ui.completion.BasicContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class AbstractCompletionProviderTest {

	@Test
	@DisplayName("isActive() = true for valid context")
	public void isValid_equals_true_for_valid_context() {
		final ICompletionContext context = mock(ICompletionContext.class);
		when(context.isValid()).thenReturn(true);

		assertTrue(new TestCompletionProvider().isActive(context));
	}

	@Test
	@DisplayName("isActive() = false for invalid context")
	public void isValid_equals_false_for_invalid_context() {
		final ICompletionContext context = mock(ICompletionContext.class);
		when(context.isValid()).thenReturn(false);

		assertFalse(new TestCompletionProvider().isActive(context));
	}

	@Test
	@DisplayName("isMethodParameter(getName, 0) = true")
	public void isMethodParameter_equals_true() {
		assertTrue(new MethodParameterCompletionProvider("getName", 0).isActive(createContext("java.io.File().getName(")));
	}

	@Test
	@DisplayName("isMethodParameter(setReadable, 1) = true")
	public void isMethodParameter_equals_true_for_2nd_parameter() {
		assertTrue(new MethodParameterCompletionProvider("setReadable", 1).isActive(createContext("java.io.File().setReadable(true,")));
	}

	@Test
	@DisplayName("isMethodParameter(getName, 0) = false for wrong method name")
	public void isMethodParameter_equals_false_for_wrong_method_name() {
		assertFalse(new MethodParameterCompletionProvider("getName", 0).isActive(createContext("java.io.File().getParent(")));
	}

	@Test
	@DisplayName("isMethodParameter(getName, 0) = false for wrong parameter index")
	public void isMethodParameter_equals_false_for_wrong_parameter_index() {
		assertFalse(new MethodParameterCompletionProvider("getName", 0).isActive(createContext("java.io.File().getName(2,")));
	}

	@Test
	@DisplayName("isMethodParameter(getName, 0) = false when no method is detected")
	public void isMethodParameter_equals_false_when_no_method_is_detected() {
		assertFalse(new MethodParameterCompletionProvider("getName", 0).isActive(createContext("java.io.File(")));
	}

	@ParameterizedTest(name = "for ''{0}''")
	@DisplayName("isStringParameter() = true")
	@ValueSource(strings = { "java.io.File().getName('", "java.io.File().getName(\"", "java.io.File().getName('This is", "java.io.File().getName(2, '" })
	public void isStringParameter_equals_true(String input) {
		assertTrue(new StringParameterCompletionProvider().isActive(createContext(input)));
	}

	@ParameterizedTest(name = "for ''{0}''")
	@DisplayName("isStringParameter() = false")
	@ValueSource(strings = { "java.io.File().getName(", "java.io.File().getName(12", "java.io.File().getName(bar", "java.io.File().getName(''",
			"java.io.File().getName(2,3" })
	public void isStringParameter_equals_false(String input) {
		assertFalse(new StringParameterCompletionProvider().isActive(createContext(input)));
	}

	private static ICompletionContext createContext(String input) {
		return new BasicContext(null, null, input, input.length());
	}

	private static class TestCompletionProvider extends AbstractCompletionProvider {
		@Override
		protected void prepareProposals(ICompletionContext context) {
			// nothing to do
		}
	}

	private static final class MethodParameterCompletionProvider extends TestCompletionProvider {

		private final String fMethodName;
		private final int fParameterIndex;

		private MethodParameterCompletionProvider(String methodName, int parameterIndex) {
			fMethodName = methodName;
			fParameterIndex = parameterIndex;
		}

		@Override
		public boolean isActive(ICompletionContext context) {
			return isMethodParameter(context, fMethodName, fParameterIndex);
		}
	}

	private static final class StringParameterCompletionProvider extends TestCompletionProvider {

		@Override
		public boolean isActive(ICompletionContext context) {
			return isStringParameter(context);
		}
	}
}
