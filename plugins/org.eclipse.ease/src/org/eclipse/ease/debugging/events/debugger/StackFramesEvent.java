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

import java.util.List;

import org.eclipse.ease.debugging.IScriptDebugFrame;
import org.eclipse.ease.debugging.events.AbstractEvent;

public class StackFramesEvent extends AbstractEvent implements IDebuggerEvent {

	private final List<IScriptDebugFrame> fDebugFrames;

	public StackFramesEvent(final List<IScriptDebugFrame> debugFrames, final Object thread) {
		super(thread);

		fDebugFrames = debugFrames;
	}

	public List<IScriptDebugFrame> getDebugFrames() {
		return fDebugFrames;
	}
}
