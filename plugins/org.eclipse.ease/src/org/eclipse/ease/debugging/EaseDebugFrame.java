/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
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
package org.eclipse.ease.debugging;

import java.util.Collections;
import java.util.Map;

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
		return (fName != null) ? fName : "";
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

	public Map<String, Object> getVariables() {
		return Collections.emptyMap();
	}
}
