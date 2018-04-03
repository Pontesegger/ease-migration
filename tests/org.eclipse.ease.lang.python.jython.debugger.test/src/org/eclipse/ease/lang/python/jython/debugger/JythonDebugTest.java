/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.python.jython.debugger;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.LineBreakpoint;
import org.eclipse.ease.testhelper.AbstractDebugTest;

public class JythonDebugTest extends AbstractDebugTest {

	@Override
	protected String getScriptSource() throws IOException {
		return readResource("org.eclipse.ease.testhelper", "/resources/DebugTest/main.py");
	}

	@Override
	protected LineBreakpoint setBreakpoint(IFile file, int lineNumber) throws CoreException {
		final IMarker marker = file.createMarker("org.python.pydev.debug.pyStopBreakpointMarker");
		marker.setAttribute("org.eclipse.debug.core.enabled", true);
		marker.setAttribute("org.eclipse.debug.core.id", "org.python.pydev.debug");
		marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);

		final LineBreakpoint breakpoint = new LineBreakpoint() {

			@Override
			public String getModelIdentifier() {
				return "org.python.pydev.debug";
			}
		};

		breakpoint.setMarker(marker);

		DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(breakpoint);

		return breakpoint;
	}

	@Override
	protected IBreakpoint[] getBreakpoints() throws CoreException {
		return DebugPlugin.getDefault().getBreakpointManager().getBreakpoints("org.python.pydev.debug");
	}

	@Override
	protected String getEngineId() {
		return JythonDebuggerEngine.ENGINE_ID;
	}
}
