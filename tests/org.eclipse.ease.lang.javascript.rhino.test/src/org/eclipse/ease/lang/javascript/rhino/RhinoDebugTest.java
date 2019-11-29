/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.javascript.rhino;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ease.lang.javascript.rhino.debugger.RhinoDebuggerEngine;
import org.eclipse.ease.testhelper.AbstractDebugTest;

public class RhinoDebugTest extends AbstractDebugTest {

	@Override
	protected Map<String, String> getScriptSources() throws IOException {
		final Map<String, String> sources = new HashMap<>();

		sources.put(MAIN_SCRIPT, readResource("org.eclipse.ease.testhelper", "/resources/DebugTest/main.js"));
		sources.put(INCLUDE_SCRIPT, readResource("org.eclipse.ease.testhelper", "/resources/DebugTest/include.js"));

		return sources;
	}

	@Override
	protected String getEngineId() {
		return RhinoDebuggerEngine.ENGINE_ID;
	}

	@Override
	protected String getDebugModelId() {
		return "org.eclipse.wst.jsdt.debug.model";
	}

	@Override
	protected String getBreakpointId() {
		return "org.eclipse.wst.jsdt.debug.core.line.breakpoint.marker";
	}
}
