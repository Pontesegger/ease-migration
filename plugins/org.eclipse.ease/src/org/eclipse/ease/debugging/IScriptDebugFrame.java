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
package org.eclipse.ease.debugging;

import org.eclipse.ease.Script;

public interface IScriptDebugFrame {

	int TYPE_FILE = 1;

	int TYPE_FUNCTION = 2;

	int getLineNumber();

	Script getScript();

	int getType();

	String getName();

	void setLineNumber(int lineNumber);

	void setVariable(String name, Object content);

	Object inject(String expression) throws Throwable;
}
