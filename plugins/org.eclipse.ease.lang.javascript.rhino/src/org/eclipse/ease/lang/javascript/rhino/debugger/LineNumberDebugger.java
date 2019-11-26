/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.lang.javascript.rhino.debugger;

import org.eclipse.ease.Script;

public class LineNumberDebugger extends RhinoDebugger {

	public LineNumberDebugger(RhinoDebuggerEngine engine) {
		super(engine, false);
	}

	@Override
	protected void processLine(Script script, int lineNumber) {
		// we do not want to check for breakpoints, etc
	}
}
