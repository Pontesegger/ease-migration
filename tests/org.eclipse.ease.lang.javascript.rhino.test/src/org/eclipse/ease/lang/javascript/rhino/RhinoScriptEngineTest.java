/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.lang.javascript.rhino;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.core.runtime.Path;
import org.eclipse.ease.ScriptExecutionException;
import org.eclipse.ease.ScriptResult;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RhinoScriptEngineTest {
	private static final String SCRIPT_SNIPPET_1 = "foo = 40 + 2;";
	private static final String SCRIPT_SNIPPET_2 = "new java.lang.String(\"" + SCRIPT_SNIPPET_1 + "\");";
	private static final String SCRIPT_SNIPPET_4 = "new org.eclipse.core.runtime.Path(\"/\");";

	private static final String THROW_JAVA_EXCEPTION = "throw new java.lang.Exception();";
	private static final String THROW_JAVA_CLASS = "throw new java.lang.String('test');";
	private static final String THROW_JAVASCRIPT_EXCEPTION = "throw 'JavaScript';";
	private static final String THROW_SYNTAX_ERROR = "'asdf";
	private static final String THROW_RUNTIME_ERROR = "var x;\nx.foobar();\n";
	private static final String THROW_WRAPPED_EXCEPTION = "java.lang.Class.forName('NotThere')";

	private RhinoScriptEngine fEngine;

	@BeforeEach
	public void beforeEach() throws Exception {
		// we need to retrieve the service singleton as the workspace is not available in headless tests
		final IScriptService scriptService = ScriptService.getService();
		fEngine = (RhinoScriptEngine) scriptService.getEngineByID(RhinoScriptEngine.ENGINE_ID).createEngine();
		fEngine.setTerminateOnIdle(false);
	}

	@AfterEach
	public void afterEach() {
		fEngine.terminate();
	}

	@Test
	public void simpleScriptCode() throws InterruptedException {
		final ScriptResult result = fEngine.executeSync(SCRIPT_SNIPPET_1);

		assertEquals(42, result.getResult());
		assertNull(result.getException());

		assertEquals(42, fEngine.getVariable("foo"));
	}

	@Test
	public void accessJavaClasses() throws InterruptedException {
		final ScriptResult result = fEngine.executeSync(SCRIPT_SNIPPET_2);

		assertEquals(SCRIPT_SNIPPET_1, result.getResult());
		assertNull(result.getException());
	}

	@Test
	public void accessEclipseClasses() throws InterruptedException {
		final ScriptResult result = fEngine.executeSync(SCRIPT_SNIPPET_4);

		assertEquals(new Path("/"), result.getResult());
		assertNull(result.getException());
	}

	@Test
	public void callModuleCode() throws InterruptedException {
		final ScriptResult result = fEngine.executeSync("exit(\"done\");");

		assertEquals("done", result.getResult());
		assertNull(result.getException());
	}

	@Test
	public void optionalModuleParameters() throws InterruptedException {
		final ScriptResult result = fEngine.executeSync("exit();");

		assertNull(result.getResult());
		assertNull(result.getException());
	}

	@Test
	public void throwJavaException() throws InterruptedException {
		final ScriptResult result = fEngine.executeSync(THROW_JAVA_EXCEPTION);

		assertNull(result.getResult());
		assertTrue(result.getException() instanceof ScriptExecutionException);
		assertTrue(result.getException().getCause() instanceof Exception);

		assertNotNull(fEngine.getExceptionStackTrace());
		assertEquals(1, fEngine.getExceptionStackTrace().size());
	}

	@Test
	public void throwJavaClass() throws InterruptedException {
		final ScriptResult result = fEngine.executeSync(THROW_JAVA_CLASS);

		assertNull(result.getResult());
		assertTrue(result.getException() instanceof ScriptExecutionException);
		assertEquals("ScriptException: test", result.getException().getMessage());

		assertNotNull(fEngine.getExceptionStackTrace());
		assertEquals(1, fEngine.getExceptionStackTrace().size());
	}

	@Test
	public void throwJavaScriptException() throws InterruptedException {
		final ScriptResult result = fEngine.executeSync(THROW_JAVASCRIPT_EXCEPTION);

		assertNull(result.getResult());
		assertTrue(result.getException() instanceof ScriptExecutionException);

		assertNotNull(fEngine.getExceptionStackTrace());
		assertEquals(1, fEngine.getExceptionStackTrace().size());
	}

	@Test
	public void causeSyntaxErrorException() throws InterruptedException {
		final ScriptResult result = fEngine.executeSync(THROW_SYNTAX_ERROR);

		assertNull(result.getResult());
		assertTrue(result.getException() instanceof ScriptExecutionException);

		assertNotNull(fEngine.getExceptionStackTrace());
		assertEquals(1, fEngine.getExceptionStackTrace().size());
	}

	@Test
	public void causeRuntimeErrorException() throws InterruptedException {
		final ScriptResult result = fEngine.executeSync(THROW_RUNTIME_ERROR);

		assertNull(result.getResult());
		assertTrue(result.getException() instanceof ScriptExecutionException);

		assertNotNull(fEngine.getExceptionStackTrace());
		assertEquals(1, fEngine.getExceptionStackTrace().size());
	}

	@Test
	public void causeWrappedException() throws InterruptedException {
		final ScriptResult result = fEngine.executeSync(THROW_WRAPPED_EXCEPTION);

		assertNull(result.getResult());
		assertTrue(result.getException() instanceof ScriptExecutionException);
		assertEquals(ClassNotFoundException.class, result.getException().getCause().getClass());

		assertNotNull(fEngine.getExceptionStackTrace());
		assertEquals(1, fEngine.getExceptionStackTrace().size());
	}
}
