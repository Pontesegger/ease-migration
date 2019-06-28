/*******************************************************************************
 * Copyright (c) 2018 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.debugging.events.debugger;

import org.eclipse.ease.debugging.events.AbstractEvent;

public class ThreadCreatedEvent extends AbstractEvent implements IDebuggerEvent {

	public ThreadCreatedEvent() {
		super();
	}

	public ThreadCreatedEvent(Object thread) {
		super(thread);
	}
}
