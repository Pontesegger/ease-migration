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
package org.eclipse.ease.debugging.events.debugger;

import org.eclipse.ease.Script;
import org.eclipse.ease.debugging.events.AbstractEvent;

public class ScriptReadyEvent extends AbstractEvent implements IDebuggerEvent {

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
