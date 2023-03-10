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
package org.eclipse.ease.debugging.events.model;

import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.ease.debugging.events.AbstractEvent;
import org.eclipse.ease.debugging.model.EaseDebugStackFrame;
import org.eclipse.ease.debugging.model.EaseDebugThread;
import org.eclipse.ease.debugging.model.EaseDebugVariable;

public class SetVariablesRequest extends AbstractEvent implements IModelRequest {

	private static Object getThread(IDebugElement element) {
		if (element instanceof EaseDebugThread)
			return ((EaseDebugThread) element).getThread();

		if (element instanceof EaseDebugStackFrame)
			return getThread(((EaseDebugStackFrame) element).getThread());

		return null;
	}

	private final IDebugElement fRequestor;
	private final EaseDebugVariable fVariable;
	private final String fExpression;

	public SetVariablesRequest(IDebugElement parent, EaseDebugVariable variable, String expression) {
		super(getThread(parent));

		fRequestor = parent;
		fVariable = variable;
		fExpression = expression;
	}

	public IDebugElement getRequestor() {
		return fRequestor;
	}

	public EaseDebugVariable getVariable() {
		return fVariable;
	}

	public String getExpression() {
		return fExpression;
	}
}
