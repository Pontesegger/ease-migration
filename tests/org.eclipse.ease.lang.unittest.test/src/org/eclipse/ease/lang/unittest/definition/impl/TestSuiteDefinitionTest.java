/*******************************************************************************
 * Copyright (c) 2022 Christian Pontesegger and others.
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

package org.eclipse.ease.lang.unittest.definition.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.core.runtime.Path;
import org.eclipse.ease.lang.unittest.definition.Flag;
import org.eclipse.ease.lang.unittest.definition.IDefinitionFactory;
import org.eclipse.ease.lang.unittest.definition.ITestSuiteDefinition;
import org.eclipse.ease.lang.unittest.definition.IVariable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestSuiteDefinitionTest {
	@Test
	@DisplayName("setVariable() sets new variable")
	public void setVariable_sets_new_variable() {
		final ITestSuiteDefinition definition = IDefinitionFactory.eINSTANCE.createTestSuiteDefinition();

		assertTrue(definition.getVariables().isEmpty());

		definition.setVariable("test", "content");

		assertEquals(1, definition.getVariables().size());
		assertNotNull(definition.getVariable("test"));
		assertEquals("content", definition.getVariable("test").getContent());
	}

	@Test
	@DisplayName("setVariable() updates existing variable")
	public void setVariable_updates_existing_variable() {
		final ITestSuiteDefinition definition = IDefinitionFactory.eINSTANCE.createTestSuiteDefinition();

		final IVariable variable = IDefinitionFactory.eINSTANCE.createVariable();
		variable.setFullName(new Path("test"));
		variable.setContent("old");
		definition.getVariables().add(variable);

		assertEquals(1, definition.getVariables().size());

		definition.setVariable("test", "content");

		assertEquals(1, definition.getVariables().size());
		assertEquals("content", definition.getVariable("test").getContent());
	}

	@Test
	@DisplayName("setFlag(<known flag>) sets flag")
	public void setFlag_sets_flag() {
		final ITestSuiteDefinition definition = IDefinitionFactory.eINSTANCE.createTestSuiteDefinition();

		definition.setFlag("STOP_SUITE_ON_ERROR", "true");

		assertTrue(definition.getFlag(Flag.STOP_SUITE_ON_ERROR, false));
	}

	@Test
	@DisplayName("setFlag(<unknown flag>) throws")
	public void setFlag_throws_on_unknown_flag() {
		final ITestSuiteDefinition definition = IDefinitionFactory.eINSTANCE.createTestSuiteDefinition();

		assertThrows(IllegalArgumentException.class, () -> definition.setFlag("unknown", "true"));
	}
}
