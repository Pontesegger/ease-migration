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
package org.eclipse.ease.debugging.events.debugger;

import java.util.Collection;

import org.eclipse.ease.debugging.events.AbstractEvent;
import org.eclipse.ease.debugging.model.EaseDebugStackFrame;
import org.eclipse.ease.debugging.model.EaseDebugVariable;

public class VariablesEvent extends AbstractEvent implements IDebuggerEvent {

	private final EaseDebugStackFrame fRequestor;
	private final Collection<EaseDebugVariable> fVariables;

	public VariablesEvent(final EaseDebugStackFrame requestor, final Collection<EaseDebugVariable> variables) {
		fRequestor = requestor;
		fVariables = variables;
	}

	public EaseDebugStackFrame getRequestor() {
		return fRequestor;
	}

	public Collection<EaseDebugVariable> getVariables() {
		return fVariables;
	}
}
