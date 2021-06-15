/*******************************************************************************
 * Copyright (c) 2018 christian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License_Identifier: EPL-2.0
 *
 * Contributors:
 *     christian - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.testhelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;

import org.eclipse.ease.ICodeFactory;
import org.eclipse.ease.IReplEngine;
import org.eclipse.ease.ScriptResult;
import org.eclipse.ease.modules.EnvironmentModule;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.modules.IModuleCallbackProvider;
import org.eclipse.ease.service.ScriptService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests basic environment loading and functionality.
 */
public abstract class AbstractEnvironmentTest {
	private IReplEngine fEngine;

	@BeforeEach
	public void setUp() throws Exception {
		// we need to retrieve the service singleton as the workspace is not available in headless tests
		fEngine = createScriptEngine();
		fEngine.setTerminateOnIdle(false);
	}

	protected abstract IReplEngine createScriptEngine();

	private String createComment(String text) {
		final ICodeFactory codeFactory = ScriptService.getCodeFactory(fEngine);
		return codeFactory.createCommentedString(text, false);
	}

	@AfterEach
	public void tearDown() {
		fEngine.terminate();
	}

	@Test
	public void bootstrappedEnvironment() throws ExecutionException {
		fEngine.schedule();

		// execute some code to synchronize startup sequence
		fEngine.execute(createComment("sync execution")).get();

		final Object environment = fEngine.getVariable(EnvironmentModule.getWrappedVariableName(new EnvironmentModule()));

		assertTrue(environment instanceof IEnvironment);
		assertEquals(environment, ((IEnvironment) environment).getModule(IEnvironment.class));
	}

	@Test
	public void reloadEnvironment() throws ExecutionException {
		fEngine.schedule();

		// execute some code to synchronize startup sequence
		fEngine.execute(createComment("sync execution")).get();

		final Object environment = fEngine.getVariable(EnvironmentModule.getWrappedVariableName(new EnvironmentModule()));

		final ScriptResult result = fEngine.execute("loadModule(\"" + EnvironmentModule.MODULE_NAME + "\")");

		assertEquals(environment, result.get());
	}

	@Test
	public void preMethodCallback() throws ExecutionException {
		fEngine.schedule();

		// execute some code to synchronize startup sequence
		fEngine.execute(createComment("sync execution")).get();

		final IEnvironment environment = (IEnvironment) fEngine.getVariable(EnvironmentModule.getWrappedVariableName(new EnvironmentModule()));

		final IModuleCallbackProvider callbackProvider = mock(IModuleCallbackProvider.class);

		when(callbackProvider.hasPreExecutionCallback(any(Method.class))).thenReturn(true);
		when(callbackProvider.hasPostExecutionCallback(any(Method.class))).thenReturn(false);
		environment.addModuleCallback(callbackProvider);

		fEngine.execute("getModule('" + EnvironmentModule.MODULE_NAME + "');").get();

		verify(callbackProvider, times(1)).preExecutionCallback(any(Method.class), any());
		verify(callbackProvider, times(0)).postExecutionCallback(any(Method.class), any());
	}

	@Test
	public void postMethodCallback() throws ExecutionException {
		fEngine.schedule();

		// execute some code to synchronize startup sequence
		fEngine.execute(createComment("sync execution")).get();

		final IEnvironment environment = (IEnvironment) fEngine.getVariable(EnvironmentModule.getWrappedVariableName(new EnvironmentModule()));

		final IModuleCallbackProvider callbackProvider = mock(IModuleCallbackProvider.class);

		when(callbackProvider.hasPreExecutionCallback(any(Method.class))).thenReturn(false);
		when(callbackProvider.hasPostExecutionCallback(any(Method.class))).thenReturn(true);
		environment.addModuleCallback(callbackProvider);

		fEngine.execute("getModule('" + EnvironmentModule.MODULE_NAME + "');").get();

		verify(callbackProvider, times(0)).preExecutionCallback(any(Method.class), any());
		verify(callbackProvider, times(1)).postExecutionCallback(any(Method.class), any());
	}

	@Test
	public void reloadFunctionDefinitions() throws ExecutionException {
		fEngine.schedule();

		// execute some code to synchronize startup sequence
		ScriptResult result = fEngine.execute("getModule('" + EnvironmentModule.MODULE_NAME + "');");
		assertNotNull(result.get());

		fEngine.execute("function getModule(name) { return null; }");
		result = fEngine.execute("getModule('" + EnvironmentModule.MODULE_NAME + "');");
		assertNull(result.get());

		fEngine.execute("loadModule('" + EnvironmentModule.MODULE_NAME + "');");
		result = fEngine.execute("getModule('" + EnvironmentModule.MODULE_NAME + "');");
		assertNotNull(result.get());
	}
}
