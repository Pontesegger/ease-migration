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
package org.eclipse.ease.debugging.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.ease.Script;
import org.eclipse.ease.debugging.IScriptDebugFrame;
import org.eclipse.ease.debugging.events.model.GetVariablesRequest;

public class EaseDebugStackFrame extends EaseDebugElement implements IStackFrame {

	private static EaseDebugVariable findExistingVariable(String name, Collection<EaseDebugVariable> variables) {
		for (final EaseDebugVariable variable : variables) {
			if (variable.getName().equals(name))
				return variable;
		}

		return null;
	}

	private final EaseDebugThread fThread;

	private final IScriptDebugFrame fDebugFrame;

	private final List<EaseDebugVariable> fVariables = new ArrayList<>();

	private boolean fDirty = true;

	public EaseDebugStackFrame(final EaseDebugThread thread, final IScriptDebugFrame debugFrame) {
		super(thread.getDebugTarget());
		fThread = thread;

		fDebugFrame = debugFrame;
	}

	@Override
	public IThread getThread() {
		return fThread;
	}

	@Override
	public EaseDebugVariable[] getVariables() {
		if (fDirty)
			getDebugTarget().fireDispatchEvent(new GetVariablesRequest(this));

		return fVariables.toArray(new EaseDebugVariable[fVariables.size()]);
	}

	@Override
	public boolean hasVariables() {
		return getVariables().length > 0;
	}

	@Override
	public int getLineNumber() {
		return getDebugFrame().getLineNumber();
	}

	@Override
	public int getCharStart() {
		return -1;
	}

	@Override
	public int getCharEnd() {
		return -1;
	}

	@Override
	public String getName() {
		return getScript().toString();
	}

	@Override
	public IRegisterGroup[] getRegisterGroups() {
		return new IRegisterGroup[0];
	}

	@Override
	public boolean hasRegisterGroups() {
		return false;
	}

	public Script getScript() {
		return getDebugFrame().getScript();
	}

	public IScriptDebugFrame getDebugFrame() {
		return fDebugFrame;
	}

	public void setDirty() {
		fDirty = true;

		fireChangeEvent(DebugEvent.CONTENT);
	}

	public void setVariables(Collection<EaseDebugVariable> variables) {
		final Collection<EaseDebugVariable> oldVariables = new ArrayList<>(fVariables);
		fVariables.clear();

		for (final EaseDebugVariable variable : variables) {
			final EaseDebugVariable existingVariable = findExistingVariable(variable.getName(), oldVariables);
			if (existingVariable != null) {
				// update existing variable
				existingVariable.update(variable.getValue());
				fVariables.add(existingVariable);

			} else {
				// new variable
				variable.setParent(this);
				fVariables.add(variable);
			}
		}

		Collections.sort(fVariables);

		fireChangeEvent(DebugEvent.CONTENT);

		fDirty = false;
	}
}
