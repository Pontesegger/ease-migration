/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
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
import org.eclipse.ease.debugging.events.model.ResumeRequest;

public class ResumedEvent extends AbstractEvent implements IDebuggerEvent {

	private final int fType;

	public ResumedEvent(final Object thread, final int type) {
		super(thread);
		fType = type;
	}

	public int getType() {
		return fType;
	}

	@Override
	public String toString() {
		return super.toString() + " (" + ResumeRequest.getTypeName(fType) + ")";
	}
}
