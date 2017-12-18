/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.lang.javascript.rhino.debugger;

import org.eclipse.ease.IDebugEngine;
import org.eclipse.ease.Script;

public class LineNumberDebugger extends RhinoDebugger {

	public LineNumberDebugger(IDebugEngine engine) {
		super(engine, false);
	}

	@Override
	protected void processLine(Script script, int lineNumber) {
		// we do not want to check for breakpoints, etc
	}
}
