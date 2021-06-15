/*******************************************************************************
 * Copyright (c) 2016 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License_Identifier: EPL-2.0
 *
 * Contributors:
 *     Jonah Graham (Kichwa Coders) - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.python.py4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.ease.ScriptExecutionException;
import org.eclipse.ease.ScriptResult;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ScriptModeEngineTest extends ModeTestBase {

	@Override
	protected ScriptResult executeCode(String code) throws Exception {
		return super.executeCode(code, false);
	}

	@Override
	protected void executeCode(String code, Object target) throws Exception {
		final ScriptResult result = super.executeCode(code, false);
		assertNotNull(result);
		assertNotNull(result.get());
		assertEquals(target, result.get());
	}

	@Test
	@Disabled("Disabled until Bug 493677 is resolved")
	public void callExit() throws Exception {
		final ScriptResult result = executeCode("print_('this should be output', False)\nexit()\nprint_('this should not appear')");
		assertResultIsNone(result);
		assertEquals("this should be output", fOutputStream.getAndClearOutput());
	}

	@Test
	public void getScriptEngine() throws Exception {
		assertEquals(Integer.toString(System.identityHashCode(fEngine)), printExpression("java.lang.System.identityHashCode(getScriptEngine())"));
	}

	@Test
	public void incompleteStatement() throws Exception {
		final ScriptResult result = executeCode("def a():");
		assertThrows(ScriptExecutionException.class, () -> result.get());
		assertTrue(fErrorStream.getAndClearOutput().contains("SyntaxError"));
		assertNull(result.get());
	}

	@Test
	public void invalidSyntax() throws Exception {
		executeCode("1++");
		assertTrue(fErrorStream.getAndClearOutput().contains("SyntaxError"));
	}

	@Test
	public void runtimeError() throws Exception {
		executeCode("a");
		assertTrue(fErrorStream.getAndClearOutput().contains("NameError"));
	}

	@Test
	public void multiLineStatement() throws Exception {
		executeCode("def a():\n\treturn 42\na()", 42);
	}

	@Test
	@Disabled("Disabled until Bug 493677 is resolved")
	public void multiLinesOfCode() throws Exception {
		assertResultIsNone(executeCode("print_(1)\nprint_(2)"));
		assertEquals(String.format("1%n2%n"), fOutputStream.getAndClearOutput());
	}
}
