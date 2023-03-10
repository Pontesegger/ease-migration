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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.ease.ScriptExecutionException;
import org.eclipse.ease.ScriptResult;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ShellModeEngineTest extends ModeTestBase {

	@Override
	protected ScriptResult executeCode(String code) throws Exception {
		return super.executeCode(code, true);
	}

	@Override
	protected void executeCode(String code, Object target) throws Exception {
		final ScriptResult result = super.executeCode(code, true);
		assertNotNull(result);
		assertNotNull(result.get());
		assertEquals(target, result.get());
	}

	@Test
	public void callModuleCode() throws Exception {
		final ScriptResult result = executeCode("exit(\"done\")");
		assertEquals("done", result.get());
	}

	@Test
	public void optionalModuleParameters() throws Exception {
		assertResultIsNone(executeCode("exit()"));
	}

	@Test
	public void getScriptEngine() throws Exception {
		final ScriptResult result = executeCode("getScriptEngine()");
		assertSame(fEngine, result.get());
	}

	@Test
	@Disabled("Disabled until Bug 493677 is resolved")
	public void print_() throws Exception {
		assertResultIsNone(executeCode("print_()"));
		assertEquals(String.format("%n"), fOutputStream.getAndClearOutput());
	}

	@Test
	@Disabled("Disabled until Bug 493677 is resolved")
	public void print_NoNewline() throws Exception {
		assertResultIsNone(executeCode("print_('', False)"));
		assertEquals("", fOutputStream.getAndClearOutput());
	}

	@Test
	@Disabled("Disabled until Bug 493677 is resolved")
	public void print_Text() throws Exception {
		assertResultIsNone(executeCode("print_('text')"));
		assertEquals(String.format("text%n"), fOutputStream.getAndClearOutput());
	}

	@Test
	@Disabled("Disabled until Bug 493677 is resolved")
	public void print_TextNoNewline() throws Exception {
		assertResultIsNone(executeCode("print_('text', False)"));
		assertEquals("text", fOutputStream.getAndClearOutput());
	}

	@Test
	public void incompleteStatement() throws Exception {
		push("def a():", true);
	}

	@Test
	public void multiLineStatement() throws Exception {
		push("def a():", true);
		push("    return 42", true);
		push("", false);
		// assertResultIsNone(push("", false));final
		final ScriptResult result = push("a()", false);
		assertEquals(42, result.get());
	}

	@Test
	public void invalidSyntax() throws Exception {
		final ScriptResult result = executeCode("1++");
		assertThrows(ScriptExecutionException.class, () -> result.get());
		assertTrue(fErrorStream.getAndClearOutput().contains("SyntaxError"));
		assertNull(result.get());
	}

	@Test
	public void runtimeError() throws Exception {
		final ScriptResult result = executeCode("x");
		assertThrows(ScriptExecutionException.class, () -> result.get());
		assertTrue(fErrorStream.getAndClearOutput().contains("NameError"));
		assertNull(result.get());
	}
}
