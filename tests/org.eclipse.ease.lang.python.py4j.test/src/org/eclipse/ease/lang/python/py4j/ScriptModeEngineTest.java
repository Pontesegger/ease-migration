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
import static org.junit.Assert.assertThat;

import org.eclipse.ease.ScriptExecutionException;
import org.eclipse.ease.ScriptResult;
import org.junit.Ignore;
import org.junit.Test;

public class ScriptModeEngineTest extends ModeTestBase {

	@Override
	protected ScriptResult executeCode(String code) throws Exception {
		return super.executeCode(code, false);
	}

	@Override
	protected void executeCode(String code, Object target) throws Exception {
		final ScriptResult result = super.executeCode(code, false);
		assertNotNull(result);
		assertNull(result.getException());
		assertNotNull(result.getResult());
		assertEquals(target, result.getResult());
	}

	@Test
	@Ignore("Disabled until Bug 493677 is resolved")
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
		assertThat(result.getException(), instanceOf(ScriptExecutionException.class));
		assertThat(fErrorStream.getAndClearOutput(), containsString("SyntaxError"));
		assertNull(result.getResult());
	}

	@Test
	public void invalidSyntax() throws Exception {
		executeCode("1++");
		assertThat(fErrorStream.getAndClearOutput(), containsString("SyntaxError"));
	}

	@Test
	public void runtimeError() throws Exception {
		executeCode("a");
		assertThat(fErrorStream.getAndClearOutput(), containsString("NameError"));
	}

	@Test
	public void multiLineStatement() throws Exception {
		executeCode("def a():\n\treturn 42\na()", 42);
	}

	@Test
	@Ignore("Disabled until Bug 493677 is resolved")
	public void multiLinesOfCode() throws Exception {
		assertResultIsNone(executeCode("print_(1)\nprint_(2)"));
		assertEquals(String.format("1%n2%n"), fOutputStream.getAndClearOutput());
	}
}
