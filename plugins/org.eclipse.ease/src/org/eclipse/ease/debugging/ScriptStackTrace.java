/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.debugging;

import java.util.LinkedList;

public class ScriptStackTrace extends LinkedList<IScriptDebugFrame> {

	private static final long serialVersionUID = -2646312686843773431L;

	@Override
	public ScriptStackTrace clone() {

		final ScriptStackTrace result = new ScriptStackTrace();

		for (final IScriptDebugFrame frame : this)
			result.add(new ScriptDebugFrame(frame));

		return result;
	}
}
