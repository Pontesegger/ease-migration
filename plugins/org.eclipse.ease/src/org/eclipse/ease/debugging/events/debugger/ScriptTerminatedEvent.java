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

public class ScriptTerminatedEvent extends AbstractThreadEvent implements IDebuggerEvent {

	private final Script fScript;

	public ScriptTerminatedEvent(final Script script, final Object thread) {
		super(thread);

		fScript = script;
	}

	public Script getScript() {
		return fScript;
	}
}
