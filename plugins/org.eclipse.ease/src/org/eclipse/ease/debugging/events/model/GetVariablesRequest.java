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
package org.eclipse.ease.debugging.events.model;

import org.eclipse.ease.debugging.events.AbstractEvent;
import org.eclipse.ease.debugging.model.EaseDebugStackFrame;

public class GetVariablesRequest extends AbstractEvent implements IModelRequest {

	private final EaseDebugStackFrame fRequestor;

	public GetVariablesRequest(EaseDebugStackFrame requestor) {
		super(requestor.getThread().getThread());

		fRequestor = requestor;
	}

	public EaseDebugStackFrame getRequestor() {
		return fRequestor;
	}
}
