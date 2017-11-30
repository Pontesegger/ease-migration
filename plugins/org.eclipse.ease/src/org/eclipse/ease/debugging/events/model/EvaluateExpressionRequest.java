/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
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
import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.ease.debugging.events.AbstractEvent;

public class EvaluateExpressionRequest extends AbstractEvent implements IModelRequest {

	private final String fExpression;
	private final IDebugElement fContext;
	private final IWatchExpressionListener fListener;

	public EvaluateExpressionRequest(String expression, IDebugElement context, IWatchExpressionListener listener) {
		fExpression = expression;
		fContext = context;
		fListener = listener;
	}

	public String getExpression() {
		return fExpression;
	}

	public IDebugElement getContext() {
		return fContext;
	}

	public IWatchExpressionListener getListener() {
		return fListener;
	}
}
