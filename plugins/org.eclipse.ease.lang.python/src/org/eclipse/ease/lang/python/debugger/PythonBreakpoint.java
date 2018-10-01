/*******************************************************************************
 * Copyright (c) 2018 Martin Kloesch and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Martin Kloesch - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.python.debugger;

/**
 * Helper class to have simpler data exchange format between EASE breakpoint information and Python counterpart.
 *
 * FIXME: Could reuse functionality from org.python.pydev.debug.model.PyBreakpoint.
 */
public class PythonBreakpoint {
	private final String fFilename;
	private final int fLineno;

	/**
	 * Constructor only stores parameters to members.
	 *
	 * @param filename
	 *            Filename for the breakpoint.
	 * @param lineno
	 *            Linenumber for the breakpoint.
	 */
	public PythonBreakpoint(final String filename, final int lineno) {
		fFilename = filename;
		fLineno = lineno;
	}

	/**
	 * Returns the filename for the breakpoint.
	 *
	 * @return breakpoint's filename.
	 */
	public String getFilename() {
		return fFilename;
	}

	/**
	 * Returns the line number for the breakpoint.
	 *
	 * @return breakpoint's line number.
	 */
	public int getLineno() {
		return fLineno;
	}

}
