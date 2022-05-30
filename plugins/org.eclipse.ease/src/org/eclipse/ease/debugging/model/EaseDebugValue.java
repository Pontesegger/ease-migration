/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IValue;

public class EaseDebugValue implements IValue {

	public static boolean isPrimitiveType(final Object value) {
		return (value instanceof Integer) || (value instanceof Byte) || (value instanceof Short) || (value instanceof Boolean) || (value instanceof Character)
				|| (value instanceof Long) || (value instanceof Double) || (value instanceof Float);
	}

	private static Collection<Field> getFields(Class<? extends Object> clazz) {
		final Collection<Field> fields = new HashSet<>();
		fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
		fields.addAll(Arrays.asList(clazz.getFields()));

		final Class<?> superclass = clazz.getSuperclass();
		if (superclass != null)
			fields.addAll(getFields(superclass));

		return fields;
	}

	private IDebugElement fParent;

	private Object fValue;

	private List<EaseDebugVariable> fVariables = null;

	private String fValueString = null;

	public EaseDebugValue(final IDebugElement parent, final Object value) {
		fParent = parent;
		fValue = value;
	}

	public void setParent(IDebugElement parent) {
		fParent = parent;

		// do not use getVariables() here as we do not want to deeply populate everything right now.
		if (fVariables != null) {
			for (final EaseDebugVariable variable : fVariables)
				variable.setParent(fParent);
		}
	}

	@Override
	public String getReferenceTypeName() {
		if (getValue() != null) {
			if (isPrimitiveType(getValue())) {
				if (getValue() instanceof Integer)
					return "int";
				if (getValue() instanceof Boolean)
					return "bool";
				if (getValue() instanceof Character)
					return "char";

				return fValue.getClass().getSimpleName().toLowerCase();
			}

			return getValue().getClass().getSimpleName();
		}

		return "";
	}

	@Override
	public String getValueString() {
		if (fValueString != null)
			return fValueString;

		if (isPrimitiveType(fValue))
			return fValue.toString();

		if (fValue instanceof String)
			return "\"" + fValue + "\" (id=" + getUniqueID(getValue()) + ")";

		if (getValue() != null) {
			if (getValue().getClass().isArray())
				return fValue.getClass().getComponentType().getSimpleName() + "[" + Array.getLength(getValue()) + "] (id=" + getUniqueID(getValue()) + ")";

			return fValue.getClass().getSimpleName() + " (id=" + getUniqueID(getValue()) + ")";

		} else
			return "null";
	}

	private String getUniqueID(Object value) {
		if (getDebugTarget() != null)
			return Integer.toString(getDebugTarget().getUniqueVariableId(value));

		return "<none>";
	}

	@Override
	public boolean isAllocated() {
		return fValue != null;
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (String.class.equals(adapter))
			adapter.cast((fValue != null) ? fValue.toString() : "");

		return null;
	}

	@Override
	public EaseDebugTarget getDebugTarget() {
		// FIXME can we change fParent to EaseDebugElement?
		if (fParent != null)
			return (EaseDebugTarget) fParent.getDebugTarget();

		return null;
	}

	@Override
	public String getModelIdentifier() {
		return getDebugTarget().getModelIdentifier();
	}

	@Override
	public ILaunch getLaunch() {
		return getDebugTarget().getLaunch();
	}

	public Object getValue() {
		return fValue;
	}

	@Override
	public EaseDebugVariable[] getVariables() {
		if (fVariables == null) {
			fVariables = new ArrayList<>();

			if (getValue() == null)
				return new EaseDebugVariable[0];

			if (isPrimitiveType(getValue()))
				return new EaseDebugVariable[0];

			if (getValue().getClass().isArray()) {
				final int length = Array.getLength(getValue());
				for (int index = 0; index < length; index++) {
					final EaseDebugVariable variable = new EaseJavaArrayElementVariable(index, getValue(), fParent);
					fVariables.add(variable);
				}

			} else if (getValue() instanceof Collection<?>) {
				final Collection<?> asCollection = (Collection<?>) getValue();
				int index = 0;
				for (final Object value : asCollection) {
					final EaseDebugVariable variable = new EaseDebugVariable(String.format("[%d]", index++), value, fParent, null);
					fVariables.add(variable);
				}
			} else {

				// populate generic Java fields
				Collection<Field> fields = getFields(getValue().getClass());
				fields = fields.stream().filter(field -> !Modifier.isStatic(field.getModifiers())).collect(Collectors.toSet());

				for (final Field field : fields) {
					if (field.trySetAccessible()) {
						try {
							final EaseDebugVariable variable = new EaseJavaFieldVariable(field, getValue(), fParent);
							fVariables.add(variable);
						} catch (final IllegalAccessException e) {
							// not expected to be thrown as field is accessible
						}
					}
				}
			}

			sortVariables();
		}

		return fVariables.toArray(new EaseDebugVariable[0]);
	}

	@Override
	public boolean hasVariables() {
		if (getValue() == null)
			return false;

		if (isPrimitiveType(getValue()))
			return false;

		// in case we did not fetch variables yet we delay evaluation and pretend we have children
		return (fVariables != null) ? (getVariables().length > 0) : true;
	}

	public void setVariables(Collection<EaseDebugVariable> variables) {
		if (variables != null) {
			fVariables = new ArrayList<>(variables);
			sortVariables();
		}
	}

	public void update(Object value) {
		fValue = value;
		fVariables = null;
	}

	public void setValueString(String valueString) {
		fValueString = valueString;
	}

	private void sortVariables() {

		Collections.sort(fVariables, (o1, o2) -> {
			final String name1 = o1.getName();
			final String name2 = o2.getName();

			if ((name1.startsWith("[")) && (name1.endsWith("]")) && (name2.startsWith("[")) && (name2.endsWith("]"))) {
				// array operator
				try {
					final int index1 = Integer.parseInt(name1.substring(1, name1.length() - 1));
					final int index2 = Integer.parseInt(name2.substring(1, name2.length() - 1));

					return index1 - index2;
				} catch (final NumberFormatException e) {
					// not a number, ignore and use string compare
				}
			}

			return name1.compareTo(name2);
		});
	}
}
