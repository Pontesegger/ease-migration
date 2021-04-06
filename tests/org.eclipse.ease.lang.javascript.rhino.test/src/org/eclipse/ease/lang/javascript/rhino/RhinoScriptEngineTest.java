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

import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class RhinoScriptEngineTest extends AbstractRhinoScriptEngineTest {

	private RhinoScriptEngine fEngine;

	@BeforeEach
	public void beforeEach() {
		// we need to retrieve the service singleton as the workspace is not available in headless tests
		final IScriptService scriptService = ScriptService.getService();
		fEngine = (RhinoScriptEngine) scriptService.getEngineByID(RhinoScriptEngine.ENGINE_ID).createEngine();
		fEngine.setTerminateOnIdle(false);
	}

	@AfterEach
	public void afterEach() {
		fEngine.terminate();
	}

	@Override
	protected RhinoScriptEngine getScriptEngine() {
		return fEngine;
	}
}
