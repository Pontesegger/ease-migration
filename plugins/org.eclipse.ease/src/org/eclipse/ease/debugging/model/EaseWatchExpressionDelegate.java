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
package org.eclipse.ease.debugging.model;

import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IWatchExpressionDelegate;
import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.ease.debugging.events.model.EvaluateExpressionRequest;

public class EaseWatchExpressionDelegate implements IWatchExpressionDelegate {

	@Override
	public void evaluateExpression(String expression, IDebugElement context, IWatchExpressionListener listener) {
		if (context instanceof EaseDebugStackFrame) {
			final Object thread = ((EaseDebugStackFrame) context).getThread().getThread();
			((EaseDebugStackFrame) context).getDebugTarget()
					.fireDispatchEvent(new EvaluateExpressionRequest(expression, (EaseDebugStackFrame) context, thread, listener));
		}
	}
}
