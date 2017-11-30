/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.debugging;

import org.eclipse.ease.Script;

/**
 * Frame containing debug location information for a dedicated script source.
 */
public class EaseDebugFrame implements IScriptDebugFrame {

	private final Script fScript;
	private int fLineNumber;
	private final int fType;
	private final String fName;

	public EaseDebugFrame(final Script script, final int lineNumber, final int type, final String name) {
		fScript = script;
		fLineNumber = lineNumber;
		fType = type;
		fName = name;
	}

	public EaseDebugFrame(final Script script, final int lineNumber, final int type) {
		this(script, lineNumber, type, null);
	}

	public EaseDebugFrame(final IScriptDebugFrame frame) {
		this(frame.getScript(), frame.getLineNumber(), frame.getType(), frame.getName());
	}

	@Override
	public int getLineNumber() {
		return fLineNumber;
	}

	@Override
	public Script getScript() {
		return fScript;
	}

	@Override
	public int getType() {
		return fType;
	}

	@Override
	public String getName() {
		return (fName != null) ? fName : getScript().getTitle();
	}

	@Override
	public void setLineNumber(final int lineNumber) {
		fLineNumber = lineNumber;
	}

	@Override
	public void setVariable(String name, Object content) {
		throw new UnsupportedOperationException("Setting variables is not supported");
	}

	@Override
	public Object inject(String expression) throws Throwable {
		throw new UnsupportedOperationException("Executing code on a stackframe is not supported");
	}
}
