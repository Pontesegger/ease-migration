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

package org.eclipse.ease.lang.python.jython.debugger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ease.testhelper.AbstractDebugTest;

public class JythonDebugTest extends AbstractDebugTest {

	@Override
	protected Map<String, String> getScriptSources() throws IOException {
		final Map<String, String> sources = new HashMap<>();

		sources.put(MAIN_SCRIPT, readResource("org.eclipse.ease.testhelper", "/resources/DebugTest/main.py"));
		sources.put(INCLUDE_SCRIPT, readResource("org.eclipse.ease.testhelper", "/resources/DebugTest/include.py"));

		return sources;
	}

	@Override
	protected String getEngineId() {
		return JythonDebuggerEngine.ENGINE_ID;
	}

	@Override
	protected String getDebugModelId() {
		return "org.python.pydev.debug";
	}

	@Override
	protected String getBreakpointId() {
		return "org.python.pydev.debug.pyStopBreakpointMarker";
	}
}
