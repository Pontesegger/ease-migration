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
package org.eclipse.ease.debugging;

import java.util.Map;

import org.eclipse.ease.Script;

public interface IScriptDebugFrame {

	int TYPE_FILE = 1;

	int TYPE_FUNCTION = 2;

	int getLineNumber();

	Script getScript();

	int getType();

	String getName();

	Map<String, Object> getVariables();

	void setLineNumber(int lineNumber);
}
