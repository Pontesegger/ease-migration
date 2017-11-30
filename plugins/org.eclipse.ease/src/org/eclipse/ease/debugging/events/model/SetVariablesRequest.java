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

import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.ease.debugging.events.AbstractEvent;
import org.eclipse.ease.debugging.model.EaseDebugVariable;

public class SetVariablesRequest extends AbstractEvent implements IModelRequest {

	private final IDebugElement fRequestor;
	private final EaseDebugVariable fVariable;
	private final String fExpression;

	public SetVariablesRequest(IDebugElement parent, EaseDebugVariable variable, String expression) {
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
