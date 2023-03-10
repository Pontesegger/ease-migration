/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
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
package org.eclipse.ease.lang.javascript.rhino.debugger.model;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.ease.Script;
import org.eclipse.ease.debugging.model.EaseDebugTarget;

public class RhinoDebugTarget extends EaseDebugTarget {

	public RhinoDebugTarget(final ILaunch launch, final boolean suspendOnStartup, final boolean suspendOnScriptLoad, final boolean showDynamicCode) {
		super(launch, suspendOnStartup, suspendOnScriptLoad, showDynamicCode);
	}

	@Override
	public String getName() {
		return "EASE Rhino Debugger";
	}

	@Override
	protected IBreakpoint[] getBreakpoints(final Script script) {
		return DebugPlugin.getDefault().getBreakpointManager().getBreakpoints("org.eclipse.wst.jsdt.debug.model");
	}

	@Override
	public boolean supportsBreakpoint(final IBreakpoint breakpoint) {
		return true;
	}

	@Override
	public boolean canSuspend() {
		return false;
	}

	@Override
	public String getModelIdentifier() {
		return "org.eclipse.ease.debugModelPresentation.rhino";
	}
}
