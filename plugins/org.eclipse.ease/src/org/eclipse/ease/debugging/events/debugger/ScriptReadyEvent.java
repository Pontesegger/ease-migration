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
package org.eclipse.ease.debugging.events.debugger;

import org.eclipse.ease.Script;

public class ScriptReadyEvent extends AbstractThreadEvent implements IDebuggerEvent {

	private final Script fScript;

	private final boolean fRoot;

	public ScriptReadyEvent(final Script script, final Object thread, final boolean root) {
		super(thread);

		fScript = script;
		fRoot = root;
	}

	public Script getScript() {
		return fScript;
	}

	public boolean isRoot() {
		return fRoot;
	}
}
