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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.ease.ScriptResult;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;
import org.junit.Before;
import org.junit.Test;

public class RhinoModuleLoadingTest {

	private RhinoScriptEngine fEngine;

	@Before
	public void setUp() throws Exception {
		// we need to retrieve the service singleton as the workspace is not available in headless tests
		final IScriptService scriptService = ScriptService.getService();
		fEngine = (RhinoScriptEngine) scriptService.getEngineByID(RhinoScriptEngine.ENGINE_ID).createEngine();
	}

	@Test
	public void moduleCallbacks() throws InterruptedException {
		final String code = "module = loadModule('/TestCallback');\n" + "getModule('/TestCallback');";
		final ScriptResult result = fEngine.executeSync(code);

		final Object module = result.getResult();

		assertTrue(module instanceof CallbackModule);
		assertEquals(1, ((CallbackModule) module).fPreExecutionCount);
		assertEquals("getModule", ((CallbackModule) module).fPreExecutionMethod.getName());
		assertArrayEquals(new Object[] { "/TestCallback" }, ((CallbackModule) module).fPreExecutionParameters);

		assertEquals(1, ((CallbackModule) module).fPostExecutionCount);
		assertEquals("getModule", ((CallbackModule) module).fPostExecutionMethod.getName());
		assertEquals(module, ((CallbackModule) module).fPostExecutionResult);
	}
}
