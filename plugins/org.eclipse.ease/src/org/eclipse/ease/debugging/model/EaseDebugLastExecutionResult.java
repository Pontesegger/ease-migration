/*******************************************************************************
 * Copyright (c) 2018 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.debugging.model;

import org.eclipse.debug.core.model.IDebugElement;

public class EaseDebugLastExecutionResult extends EaseDebugVariable {

	private boolean fIsException = false;

	public EaseDebugLastExecutionResult(String name, Object value, IDebugElement parent, String referenceTypeName) {
		super(name, value, parent, referenceTypeName);
	}

	public EaseDebugLastExecutionResult(String name, Object value, String referenceTypeName) {
		super(name, value, referenceTypeName);
	}

	public EaseDebugLastExecutionResult(EaseDebugVariable variable) {
		this(variable.getName(), variable.getValue(), variable.getParent(), variable.getReferenceTypeName());

		update(variable.getValue());
	}

	public EaseDebugLastExecutionResult(String name, Throwable exception) {
		this(name, exception, "");

		fIsException = true;
	}

	public boolean isException() {
		return fIsException;
	}
}
