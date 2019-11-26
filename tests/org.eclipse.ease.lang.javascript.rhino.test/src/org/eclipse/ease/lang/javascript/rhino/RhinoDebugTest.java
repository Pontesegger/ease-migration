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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.LineBreakpoint;
import org.eclipse.ease.lang.javascript.rhino.debugger.RhinoDebuggerEngine;
import org.eclipse.ease.testhelper.AbstractDebugTest;

public class RhinoDebugTest extends AbstractDebugTest {

	@Override
	protected String getScriptSource() throws IOException {
		return readResource("org.eclipse.ease.testhelper", "/resources/DebugTest/main.js");
	}

	@Override
	protected LineBreakpoint setBreakpoint(IFile file, int lineNumber) throws CoreException {
		final IMarker marker = file.createMarker("org.eclipse.wst.jsdt.debug.core.line.breakpoint.marker");
		marker.setAttribute("org.eclipse.debug.core.enabled", true);
		marker.setAttribute("org.eclipse.debug.core.id", "org.eclipse.wst.jsdt.debug.model");
		marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);

		final LineBreakpoint breakpoint = new LineBreakpoint() {

			@Override
			public String getModelIdentifier() {
				return "org.eclipse.wst.jsdt.debug.model";
			}
		};

		breakpoint.setMarker(marker);

		DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(breakpoint);

		return breakpoint;
	}

	@Override
	protected IBreakpoint[] getBreakpoints() throws CoreException {
		return DebugPlugin.getDefault().getBreakpointManager().getBreakpoints("org.eclipse.wst.jsdt.debug.model");
	}

	@Override
	protected String getEngineId() {
		return RhinoDebuggerEngine.ENGINE_ID;
	}
}
