/*******************************************************************************
 * Copyright (c) 2018 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.debugging.events.debugger;

import org.eclipse.ease.debugging.events.AbstractEvent;

public class AbstractThreadEvent extends AbstractEvent implements IDebuggerEvent {

	private final Object fThread;

	protected AbstractThreadEvent(Object thread) {
		fThread = thread;
	}

	protected AbstractThreadEvent() {
		this(Thread.currentThread());
	}

	public Object getThread() {
		return fThread;
	}

	@Override
	public String toString() {
		return super.toString() + "(" + getThread() + ")";
	}
}
