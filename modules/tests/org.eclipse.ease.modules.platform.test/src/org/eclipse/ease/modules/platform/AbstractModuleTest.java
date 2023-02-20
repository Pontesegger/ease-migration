/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
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

package org.eclipse.ease.modules.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;

import org.eclipse.ease.ICodeFactory;
import org.eclipse.ease.ScriptResult;
import org.eclipse.ease.lang.javascript.rhino.RhinoScriptEngine;
import org.eclipse.ease.modules.EnvironmentModule;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class AbstractModuleTest {

	private RhinoScriptEngine fEngine;

	@BeforeEach
	public void setUp() throws Exception {
		// we need to retrieve the service singleton as the workspace is not available in headless tests
		final IScriptService scriptService = ScriptService.getService();
		fEngine = (RhinoScriptEngine) scriptService.getEngineByID(RhinoScriptEngine.ENGINE_ID).createEngine();
	}

	@Test
	public void loadModule() throws NoSuchMethodException, ExecutionException {

		final ICodeFactory codeFactory = ScriptService.getCodeFactory(fEngine);
		final Method loadModuleMethod = EnvironmentModule.class.getMethod("loadModule", String.class, boolean.class);
		final String call = codeFactory.createFunctionCall(loadModuleMethod, getModuleID(), false);

		final ScriptResult result = executeCode(call);
		assertEquals(getModuleClass(), result.get().getClass());
	}

	/**
	 * Get the class of the module under test.
	 *
	 * @return class of module
	 */
	protected abstract Object getModuleClass();

	/**
	 * Get the full name of the module under test. This equals the path to be used for a loadModule() command
	 *
	 * @return full module name
	 */
	protected abstract Object getModuleID();

	private ScriptResult executeCode(final Object code) {
		final ScriptResult result = fEngine.execute(code);
		fEngine.schedule();

		return result;
	}
}
