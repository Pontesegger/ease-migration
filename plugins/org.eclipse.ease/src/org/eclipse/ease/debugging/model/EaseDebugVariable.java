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
package org.eclipse.ease.debugging.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.ease.debugging.events.model.SetVariablesRequest;

public class EaseDebugVariable implements IVariable, Comparable<EaseDebugVariable> {

	public enum Type {
		PRIMITIVE, NATIVE, NATIVE_OBJECT, NATIVE_ARRAY, JAVA_OBJECT
	}

	private IDebugElement fParent;

	private final String fName;

	private EaseDebugValue fValue;

	private final String fReferenceTypeName;

	private boolean fChanged = false;

	private Type fType=Type.NATIVE;

	public EaseDebugVariable(final String name, final Object value, final IDebugElement parent, String referenceTypeName) {
		fName = name;
		fValue = new EaseDebugValue(parent, value);
		fParent = parent;
		fReferenceTypeName = referenceTypeName;
		
		if (EaseDebugValue.isPrimitiveType(getValue().getValue()))
			fType = Type.PRIMITIVE;
	}

	public EaseDebugVariable(final String name, final Object value, String referenceTypeName) {
		this(name, value, null, referenceTypeName);
	}

	public void setParent(IDebugElement parent) {
		fParent = parent;

		getValue().setParent(fParent);
	}

	protected IDebugElement getParent() {
		return fParent;
	}

	@Override
	public EaseDebugValue getValue() {
		return fValue;
	}

	@Override
	public String getName() {
		return fName;
	}

	@Override
	public String getReferenceTypeName() {
		return (fReferenceTypeName != null) ? fReferenceTypeName : fValue.getReferenceTypeName();
	}

	@Override
	public boolean hasValueChanged() {
		return fChanged;
	}

	@Override
	public String toString() {
		return getName() + ":" + getValue().getValueString();
	}

	@Override
	public String getModelIdentifier() {
		return getDebugTarget().getModelIdentifier();
	}

	@Override
	public EaseDebugTarget getDebugTarget() {
		return (EaseDebugTarget) fParent.getDebugTarget();
	}

	@Override
	public ILaunch getLaunch() {
		return getDebugTarget().getLaunch();
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	// ************************************************************
	// IValueModification
	// ************************************************************

	@Override
	public boolean supportsValueModification() {
		return true;
	}

	@Override
	public boolean verifyValue(final String expression) {
		return true;
	}

	@Override
	public void setValue(final String expression) throws DebugException {
		getDebugTarget().fireDispatchEvent(new SetVariablesRequest(fParent, this, expression));
	}

	@Override
	public void setValue(final IValue value) throws DebugException {
		setValue(value.getValueString());
	}

	@Override
	public boolean verifyValue(final IValue value) {
		try {
			return verifyValue(value.getValueString());

		} catch (final DebugException e) {
			return false;
		}
	}

	public void update(EaseDebugValue value) {
		value.setParent(getParent());

		// simple change detection: changed when string representation of variable is changed. No check is done on child elements
		fChanged = !getValue().getValueString().equals(value.getValueString());

		fValue = value;
	}

	@Override
	public int compareTo(EaseDebugVariable o) {
		return getName().compareTo(o.getName());
	}

	public Type getType() {
		return fType;
	}
	public void setType(Type type) {
		fType = type;
	}
}
