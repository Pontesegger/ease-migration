/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.debugging.events.model;

import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.ease.debugging.events.AbstractEvent;
import org.eclipse.ease.debugging.model.EaseDebugStackFrame;

public class EvaluateExpressionRequest extends AbstractEvent implements IModelRequest {

	private final String fExpression;
	private final EaseDebugStackFrame fContext;
	private final IWatchExpressionListener fListener;

	public EvaluateExpressionRequest(String expression, EaseDebugStackFrame context, Object thread, IWatchExpressionListener listener) {
		super(thread);
		fExpression = expression;
		fContext = context;
		fListener = listener;
	}

	public String getExpression() {
		return fExpression;
	}

	public EaseDebugStackFrame getContext() {
		return fContext;
	}

	public IWatchExpressionListener getListener() {
		return fListener;
	}
}
