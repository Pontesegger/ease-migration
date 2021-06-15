/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
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

import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.debug.core.model.IWatchExpressionResult;
import org.eclipse.ease.debugging.events.AbstractEvent;
import org.eclipse.ease.debugging.model.EaseDebugElement;
import org.eclipse.ease.debugging.model.EaseWatchExpressionResult;

public class EvaluateExpressionEvent extends AbstractEvent implements IDebuggerEvent {

	private final String fExpression;
	private final Object fResult;
	private final Throwable fException;
	private final IWatchExpressionListener fListener;

	public EvaluateExpressionEvent(String expression, Object result, Throwable exception, IWatchExpressionListener listener) {
		fExpression = expression;
		fResult = result;
		fException = exception;
		fListener = listener;
	}

	public Object getResult() {
		return fResult;
	}

	public Throwable getException() {
		return fException;
	}

	public IWatchExpressionListener getListener() {
		return fListener;
	}

	public IWatchExpressionResult getWatchExpressionResult(EaseDebugElement debugElement) {
		return new EaseWatchExpressionResult(fExpression, fResult, fException, debugElement);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ease.debugging.events.AbstractEvent#toString()
	 */
	@Override
	public String toString() {
		if (getException() != null)
			return super.toString() + " (throws " + getException().getClass().getName() + ")";

		return super.toString() + " (" + getResult() + ")";
	}
}
