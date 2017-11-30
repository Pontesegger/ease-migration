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
package org.eclipse.ease.debugging.model;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IWatchExpressionResult;
import org.eclipse.ease.Activator;

public class EaseWatchExpressionResult implements IWatchExpressionResult {

	private final String fExpression;
	private final Object fResult;
	private final Throwable fException;
	private final EaseDebugElement fDebugElement;

	public EaseWatchExpressionResult(String expression, Object result, Throwable exception, EaseDebugElement debugElement) {
		fExpression = expression;
		fResult = result;
		fException = exception;
		fDebugElement = debugElement;
	}

	@Override
	public IValue getValue() {
		return new EaseDebugValue(fDebugElement, fResult);
	}

	@Override
	public boolean hasErrors() {
		return fException != null;
	}

	@Override
	public String[] getErrorMessages() {
		if (hasErrors())
			return new String[] { (fException.getMessage() != null) ? fException.getMessage() : fException.getClass().getName() };

		return new String[0];
	}

	@Override
	public String getExpressionText() {
		return fExpression;
	}

	@Override
	public DebugException getException() {
		return new DebugException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error during wtch expression evaluation", fException));
	}
}
