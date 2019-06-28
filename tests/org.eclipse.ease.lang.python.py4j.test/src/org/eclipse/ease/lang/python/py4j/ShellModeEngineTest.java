/*******************************************************************************
 * Copyright (c) 2016 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Jonah Graham (Kichwa Coders) - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.python.py4j;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import org.eclipse.ease.ScriptExecutionException;
import org.eclipse.ease.ScriptResult;
import org.junit.Ignore;
import org.junit.Test;

public class ShellModeEngineTest extends ModeTestBase {

	@Override
	protected ScriptResult executeCode(String code) throws Exception {
		return super.executeCode(code, true);
	}

	@Override
	protected void executeCode(String code, Object target) throws Exception {
		final ScriptResult result = super.executeCode(code, true);
		assertNotNull(result);
		assertNull(result.getException());
		assertNotNull(result.getResult());
		assertEquals(target, result.getResult());
	}

	@Test
	public void callModuleCode() throws Exception {
		final ScriptResult result = executeCode("exit(\"done\")");
		assertNull(result.getException());
		assertEquals("done", result.getResult());
	}

	@Test
	public void optionalModuleParameters() throws Exception {
		assertResultIsNone(executeCode("exit()"));
	}

	@Test
	public void getScriptEngine() throws Exception {
		final ScriptResult result = executeCode("getScriptEngine()");
		assertNull(result.getException());
		assertSame(fEngine, result.getResult());
	}

	@Test
	@Ignore("Disabled until Bug 493677 is resolved")
	public void print_() throws Exception {
		assertResultIsNone(executeCode("print_()"));
		assertEquals(String.format("%n"), fOutputStream.getAndClearOutput());
	}

	@Test
	@Ignore("Disabled until Bug 493677 is resolved")
	public void print_NoNewline() throws Exception {
		assertResultIsNone(executeCode("print_('', False)"));
		assertEquals("", fOutputStream.getAndClearOutput());
	}

	@Test
	@Ignore("Disabled until Bug 493677 is resolved")
	public void print_Text() throws Exception {
		assertResultIsNone(executeCode("print_('text')"));
		assertEquals(String.format("text%n"), fOutputStream.getAndClearOutput());
	}

	@Test
	@Ignore("Disabled until Bug 493677 is resolved")
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
		assertNull(result.getException());
		assertEquals(42, result.getResult());
	}

	@Test
	public void invalidSyntax() throws Exception {
		final ScriptResult result = executeCode("1++");
		assertThat(result.getException(), instanceOf(ScriptExecutionException.class));
		assertThat(fErrorStream.getAndClearOutput(), containsString("SyntaxError"));
		assertNull(result.getResult());
	}

	@Test
	public void runtimeError() throws Exception {
		final ScriptResult result = executeCode("x");
		assertThat(result.getException(), instanceOf(ScriptExecutionException.class));
		assertThat(fErrorStream.getAndClearOutput(), containsString("NameError"));
		assertNull(result.getResult());
	}

}
