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
package org.eclipse.ease.debugging.events.model;

import org.eclipse.ease.debugging.events.AbstractEvent;

public class GetStackFramesRequest extends AbstractEvent implements IModelRequest {

	private final Object fThread;

	public GetStackFramesRequest(final Object thread) {
		fThread = thread;
	}

	public Object getThread() {
		return fThread;
	}
}
