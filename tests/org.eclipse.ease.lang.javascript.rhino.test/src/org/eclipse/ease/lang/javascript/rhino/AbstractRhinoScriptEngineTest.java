/*******************************************************************************
 * Copyright (c) 2021 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.javascript.rhino;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.ExecutionException;

import org.eclipse.core.runtime.Path;
import org.eclipse.ease.IDebugEngine;
import org.eclipse.ease.ScriptResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public abstract class AbstractRhinoScriptEngineTest {

	private static final String SCRIPT_ENVIRONMENT_MODULE_WITH_OPTIONAL_PARAMS = "exit();";
	private static final String SCRIPT_ENVIRONMENT_MODULE = "exit('done');";
	private static final String SCRIPT_SNIPPET_1 = "foo = 'one' + 'two';";
	private static final String SCRIPT_SNIPPET_2 = String.format("new java.lang.String(\"%s\");", SCRIPT_SNIPPET_1);
	private static final String SCRIPT_SNIPPET_4 = "new org.eclipse.core.runtime.Path('/');";

	private static final String THROW_JAVA_EXCEPTION = "throw new java.lang.Exception();";
	private static final String THROW_JAVA_CLASS = "throw new java.lang.String('test');";
	private static final String THROW_JAVASCRIPT_EXCEPTION = "throw 'JavaScript';";
	private static final String THROW_SYNTAX_ERROR = "'asdf";
	private static final String THROW_RUNTIME_ERROR = "var x;\nx.foobar();\n";
	private static final String THROW_WRAPPED_EXCEPTION = "java.lang.Class.forName(\"NotThere\")";

	protected abstract RhinoScriptEngine getScriptEngine();

	protected ScriptResult executeCode(Object code) {
		final ScriptResult result = getScriptEngine().execute(code);
		getScriptEngine().schedule();

		return result;
	}

	@Test
	@DisplayName("execute() javascript assignment")
	public void execute_javascript_assignment() throws ExecutionException {
		final ScriptResult result = executeCode(SCRIPT_SNIPPET_1);

		assertEquals("onetwo", result.get());
		assertEquals("onetwo", getScriptEngine().getVariable("foo"));
	}

	@Test
	@DisplayName("execute() Java class instantiation")
	public void execute_java_class_instantiation() throws ExecutionException {
		final ScriptResult result = executeCode(SCRIPT_SNIPPET_2);

		assertEquals(SCRIPT_SNIPPET_1, result.get());
	}

	@Test
	@DisplayName("execute() Eclipse class instantiation")
	public void execute_eclipse_class_instantiation() throws ExecutionException {
		final ScriptResult result = executeCode(SCRIPT_SNIPPET_4);

		assertEquals(new Path("/"), result.get());
	}

	@Test
	@DisplayName("execute() Environment module code")
	public void execute_environment_module_code() throws ExecutionException {
		final ScriptResult result = executeCode(SCRIPT_ENVIRONMENT_MODULE);

		assertEquals("done", result.get());
	}

	@Test
	@DisplayName("execute() Environment module code with optional parameters")
	public void execute_environment_module_code_with_optional_parameters() throws ExecutionException {
		final ScriptResult result = executeCode(SCRIPT_ENVIRONMENT_MODULE_WITH_OPTIONAL_PARAMS);

		assertNull(result.get());
	}

	@Test
	@DisplayName("execute() throws java exception")
	public void execute_throws_java_exception() {
		final ScriptResult result = executeCode(THROW_JAVA_EXCEPTION);

		try {
			result.get();
			fail("Expected ExecutionException");

		} catch (final ExecutionException e) {
			assertEquals(Exception.class, e.getCause().getClass());

			assertNotNull(getScriptEngine().getExceptionStackTrace());
			assertEquals(1, getScriptEngine().getExceptionStackTrace().size());

			if (getScriptEngine() instanceof IDebugEngine)
				assertEquals(1, getScriptEngine().getExceptionStackTrace().get(0).getLineNumber());
		}
	}

	@Test
	@DisplayName("execute() throws java class")
	public void execute_throws_java_class() {
		final ScriptResult result = executeCode(THROW_JAVA_CLASS);

		try {
			result.get();
			fail("Expected ExecutionException");

		} catch (final ExecutionException e) {
			assertEquals("ScriptException: test", e.getMessage());

			assertNotNull(getScriptEngine().getExceptionStackTrace());
			assertEquals(1, getScriptEngine().getExceptionStackTrace().size());

			if (getScriptEngine() instanceof IDebugEngine)
				assertEquals(1, getScriptEngine().getExceptionStackTrace().get(0).getLineNumber());
		}
	}

	@Test
	@DisplayName("execute() throws javascript object")
	public void execute_throws_javascript_object() {
		final ScriptResult result = executeCode(THROW_JAVASCRIPT_EXCEPTION);

		try {
			result.get();
			fail("Expected ExecutionException");

		} catch (final ExecutionException e) {
			assertEquals("ScriptException: JavaScript", e.getMessage());

			assertNotNull(getScriptEngine().getExceptionStackTrace());
			assertEquals(1, getScriptEngine().getExceptionStackTrace().size());

			if (getScriptEngine() instanceof IDebugEngine)
				assertEquals(1, getScriptEngine().getExceptionStackTrace().get(0).getLineNumber());
		}
	}

	@Test
	@DisplayName("execute() throws syntax error")
	public void execute_throws_syntax_error() {
		final ScriptResult result = executeCode(THROW_SYNTAX_ERROR);

		try {
			result.get();
			fail("Expected ExecutionException");

		} catch (final ExecutionException e) {
			assertTrue(e.getMessage().contains("unterminated string literal"));

			assertNotNull(getScriptEngine().getExceptionStackTrace());
			assertEquals(1, getScriptEngine().getExceptionStackTrace().size());

			if (getScriptEngine() instanceof IDebugEngine)
				assertEquals(1, getScriptEngine().getExceptionStackTrace().get(0).getLineNumber());
		}
	}

	@Test
	@DisplayName("execute() throws javascript runtime error")
	public void execute_throws_javascript_runtime_error() {
		final ScriptResult result = executeCode(THROW_RUNTIME_ERROR);

		try {
			result.get();
			fail("Expected ExecutionException");

		} catch (final ExecutionException e) {
			assertTrue(e.getMessage().contains("foobar"));
			assertTrue(e.getMessage().contains("undefined"));

			assertNotNull(getScriptEngine().getExceptionStackTrace());
			assertEquals(1, getScriptEngine().getExceptionStackTrace().size());

			if (getScriptEngine() instanceof IDebugEngine)
				assertEquals(2, getScriptEngine().getExceptionStackTrace().get(0).getLineNumber());
		}
	}

	@Test
	@DisplayName("execute() throws wrapped java exception")
	public void execute_throws_wrapped_java_exception() {
		final ScriptResult result = executeCode(THROW_WRAPPED_EXCEPTION);

		try {
			result.get();
			fail("Expected ExecutionException");

		} catch (final ExecutionException e) {
			assertTrue(e.getMessage().contains("NotThere"));
			assertEquals(ClassNotFoundException.class, e.getCause().getClass());

			assertNotNull(getScriptEngine().getExceptionStackTrace());
			assertEquals(1, getScriptEngine().getExceptionStackTrace().size());

			if (getScriptEngine() instanceof IDebugEngine)
				assertEquals(1, getScriptEngine().getExceptionStackTrace().get(0).getLineNumber());
		}
	}
}
