/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.debugging;

import java.util.ArrayList;
import java.util.LinkedList;

public class ScriptStackTrace extends LinkedList<IScriptDebugFrame> {

	private static final long serialVersionUID = -2646312686843773431L;

	@Override
	public ScriptStackTrace clone() {

		final ScriptStackTrace result = new ScriptStackTrace();

		for (final IScriptDebugFrame frame : new ArrayList<>(this))
			result.add(new EaseDebugFrame(frame));

		return result;
	}
}
