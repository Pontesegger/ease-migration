/*******************************************************************************
 * Copyright (c) 2018 Martin Kloesch and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Martin Kloesch - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.python.py4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ease.lang.python.py4j.internal.Py4jDebuggerEngine;
import org.eclipse.ease.testhelper.AbstractDebugTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class Py4jDebugTest extends AbstractDebugTest {

	@Override
	protected Map<String, String> getScriptSources() throws IOException {
		final Map<String, String> sources = new HashMap<>();

		sources.put(MAIN_SCRIPT, readResource("org.eclipse.ease.testhelper", "/resources/DebugTest/main.py"));
		sources.put(INCLUDE_SCRIPT, readResource("org.eclipse.ease.testhelper", "/resources/DebugTest/include.py"));

		return sources;
	}

	@Override
	protected String getEngineId() {
		return Py4jDebuggerEngine.ENGINE_ID;
	}

	@Override
	protected String getDebugModelId() {
		return "org.python.pydev.debug";
	}

	@Override
	protected String getBreakpointId() {
		return "org.python.pydev.debug.pyStopBreakpointMarker";
	}

	@Override
	@Test
	@Disabled
	public void nativeObjectVariable() throws CoreException, IOException {
		// TODO: Currently not possible to access native Python variables
	}

	@Override
	@Test
	@Disabled
	public void nativeArrayVariable() throws CoreException, IOException {
		// TODO: Currently not possible to access native Python variables
	}

	@Override
	@Test
	@Disabled
	public void stepIntoIncludeCommand() throws CoreException {
		// TODO see https://bugs.eclipse.org/bugs/show_bug.cgi?id=553619
	}

	@Override
	@Test
	@Disabled
	public void resumeOnLastIncludeLine() throws CoreException {
		// TODO see https://bugs.eclipse.org/bugs/show_bug.cgi?id=553652
	}
}
