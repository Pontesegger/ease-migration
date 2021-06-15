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

package org.eclipse.ease.modules;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ScriptParameterTest {

	private static Method getTestMethod() throws NoSuchMethodException, SecurityException {
		return ScriptParameterTest.class.getMethod("dummyMethodForHelperTests", String.class, String.class, String.class);
	}

	@Test
	@DisplayName("Helper.isOptional() == false when annotation is missing")
	public void isOptional_false_when_annotation_is_missing() throws NoSuchMethodException, SecurityException {
		assertFalse(ScriptParameter.Helper.isOptional(getTestMethod().getParameters()[0]));
	}

	@Test
	@DisplayName("Helper.isOptional() == false when annotation has no default value")
	public void isOptional_false_when_annotation_has_no_default_value() throws NoSuchMethodException, SecurityException {
		assertFalse(ScriptParameter.Helper.isOptional(getTestMethod().getParameters()[1]));
	}

	@Test
	@DisplayName("Helper.isOptional() == true when annotation with default value is present")
	public void isOptional_true_when_annotation_with_default_value_exists() throws NoSuchMethodException, SecurityException {
		assertTrue(ScriptParameter.Helper.isOptional(getTestMethod().getParameters()[2]));
	}

	@Test
	@DisplayName("Helper.getDefaultValue() == null when annotation is missing")
	public void getDefaultValue_null_when_annotation_is_missing() throws NoSuchMethodException, SecurityException {
		assertNull(ScriptParameter.Helper.getDefaultValue(getTestMethod().getParameters()[0]));
	}

	@Test
	@DisplayName("Helper.getDefaultValue() == null when annotation has no default value")
	public void getDefaultValue_null_when_annotation_has_no_default_value() throws NoSuchMethodException, SecurityException {
		assertNull(ScriptParameter.Helper.getDefaultValue(getTestMethod().getParameters()[1]));
	}

	@Test
	@DisplayName("Helper.getDefaultValue() == '...' when annotation with default value is present")
	public void getDefaultValue_has_value_when_annotation_with_default_value_exists() throws NoSuchMethodException, SecurityException {
		assertNotNull(ScriptParameter.Helper.getDefaultValue(getTestMethod().getParameters()[2]));
	}

	public void dummyMethodForHelperTests(String notAnnotated, @ScriptParameter String annotated,
			@ScriptParameter(defaultValue = "") String annotatedWithDefault) {
	}
}
