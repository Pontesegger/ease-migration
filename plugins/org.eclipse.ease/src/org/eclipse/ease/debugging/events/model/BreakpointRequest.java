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
package org.eclipse.ease.debugging.events.model;

import org.eclipse.core.resources.IMarker;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.ease.Script;
import org.eclipse.ease.debugging.events.AbstractEvent;

public class BreakpointRequest extends AbstractEvent implements IModelRequest {

	public enum Mode {
		ADD, REMOVE
	}

	private final Script fScript;

	private final IBreakpoint fBreakpoint;

	private final Mode fMode;

	public BreakpointRequest(final Script script, final IBreakpoint breakpoint, final Mode mode) {
		fScript = script;
		fBreakpoint = breakpoint;
		fMode = mode;
	}

	/**
	 * Remove all breakpoints request.
	 *
	 * @param mode
	 *            has to be set to Mode.REMOVE
	 */
	public BreakpointRequest(final Mode mode) {
		this(null, null, mode);
	}

	public Script getScript() {
		return fScript;
	}

	public IBreakpoint getBreakpoint() {
		return fBreakpoint;
	}

	public Mode getMode() {
		return fMode;
	}

	public boolean isRemoveAllBreakpointsRequest() {
		return (fScript == null) && (fBreakpoint == null) && (Mode.REMOVE == fMode);
	}

	@Override
	public String toString() {
		if (isRemoveAllBreakpointsRequest())
			return super.toString() + " (remove all)";

		final int lineNumber = getBreakpoint().getMarker().getAttribute(IMarker.LINE_NUMBER, -1);
		if (fMode == Mode.ADD)
			return super.toString() + " (+ line " + lineNumber + ")";
		else if (fMode == Mode.REMOVE)
			return super.toString() + " (- line " + lineNumber + ")";

		return super.toString();
	}
}
