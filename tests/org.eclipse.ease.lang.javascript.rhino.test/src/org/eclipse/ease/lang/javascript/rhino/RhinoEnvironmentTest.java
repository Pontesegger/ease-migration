/*******************************************************************************
 * Copyright (c) 2018 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.javascript.rhino;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.eclipse.ease.ScriptResult;
import org.eclipse.ease.modules.EnvironmentModule;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.modules.IModuleCallbackProvider;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RhinoEnvironmentTest {

	private RhinoScriptEngine fEngine;

	@Before
	public void setUp() throws Exception {
		// we need to retrieve the service singleton as the workspace is not available in headless tests
		final IScriptService scriptService = ScriptService.getService();
		fEngine = (RhinoScriptEngine) scriptService.getEngineByID(RhinoScriptEngine.ENGINE_ID).createEngine();
		fEngine.setTerminateOnIdle(false);
	}

	@After
	public void tearDown() {
		fEngine.terminate();
	}

	@Test
	public void bootstrappedEnvironment() throws InterruptedException {
		fEngine.schedule();

		// execute some code to synchronize startup sequence
		fEngine.executeSync("// sync execution");

		final Object environment = fEngine.getVariable(EnvironmentModule.getWrappedVariableName(new EnvironmentModule()));

		assertTrue(environment instanceof IEnvironment);
		assertEquals(environment, ((IEnvironment) environment).getModule(IEnvironment.class));
	}

	@Test
	public void reloadEnvironment() throws InterruptedException {
		fEngine.schedule();

		// execute some code to synchronize startup sequence
		fEngine.executeSync("// sync execution");

		final Object environment = fEngine.getVariable(EnvironmentModule.getWrappedVariableName(new EnvironmentModule()));

		final ScriptResult result = fEngine.executeSync("loadModule('" + EnvironmentModule.MODULE_NAME + "');");

		assertEquals(environment, result.getResult());
	}

	@Test
	public void preMethodCallback() throws InterruptedException {
		fEngine.schedule();

		// execute some code to synchronize startup sequence
		fEngine.executeSync("// sync execution");

		final EnvironmentModule environment = (EnvironmentModule) fEngine.getVariable(EnvironmentModule.getWrappedVariableName(new EnvironmentModule()));

		final IModuleCallbackProvider callbackProvider = mock(IModuleCallbackProvider.class);

		when(callbackProvider.hasPreExecutionCallback(any(Method.class))).thenReturn(true);
		when(callbackProvider.hasPostExecutionCallback(any(Method.class))).thenReturn(false);
		environment.addModuleCallback(callbackProvider);

		fEngine.executeSync("getModule('" + EnvironmentModule.MODULE_NAME + "');");

		verify(callbackProvider, times(1)).preExecutionCallback(any(Method.class), any());
		verify(callbackProvider, times(0)).postExecutionCallback(any(Method.class), any());
	}

	@Test
	public void postMethodCallback() throws InterruptedException {
		fEngine.schedule();

		// execute some code to synchronize startup sequence
		fEngine.executeSync("// sync execution");

		final EnvironmentModule environment = (EnvironmentModule) fEngine.getVariable(EnvironmentModule.getWrappedVariableName(new EnvironmentModule()));

		final IModuleCallbackProvider callbackProvider = mock(IModuleCallbackProvider.class);

		when(callbackProvider.hasPreExecutionCallback(any(Method.class))).thenReturn(false);
		when(callbackProvider.hasPostExecutionCallback(any(Method.class))).thenReturn(true);
		environment.addModuleCallback(callbackProvider);

		fEngine.executeSync("getModule('" + EnvironmentModule.MODULE_NAME + "');");

		verify(callbackProvider, times(0)).preExecutionCallback(any(Method.class), any());
		verify(callbackProvider, times(1)).postExecutionCallback(any(Method.class), any());
	}

	@Test
	public void reloadFunctionDefinitions() throws InterruptedException {
		fEngine.schedule();

		// execute some code to synchronize startup sequence
		ScriptResult result = fEngine.executeSync("getModule('" + EnvironmentModule.MODULE_NAME + "');");
		assertNotNull(result.getResult());

		fEngine.executeSync("function getModule(name) { return null; }");
		result = fEngine.executeSync("getModule('" + EnvironmentModule.MODULE_NAME + "');");
		assertNull(result.getResult());

		fEngine.executeSync("loadModule('" + EnvironmentModule.MODULE_NAME + "');");
		result = fEngine.executeSync("getModule('" + EnvironmentModule.MODULE_NAME + "');");
		assertNotNull(result.getResult());
	}
}
