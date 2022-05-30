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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.ease.Activator;

public class EaseJavaFieldVariable extends EaseDebugVariable {

	private static Object convert(String expression, Class<? extends Object> clazz) {
		if ((int.class.equals(clazz)) || (Integer.class.equals(clazz)))
			return Integer.parseInt(expression);

		if ((short.class.equals(clazz)) || (Short.class.equals(clazz)))
			return Short.parseShort(expression);

		if ((byte.class.equals(clazz)) || (Byte.class.equals(clazz)))
			return Byte.parseByte(expression);

		if ((char.class.equals(clazz)) || (Character.class.equals(clazz))) {
			if (expression.length() == 1)
				return expression.charAt(0);
		}

		if ((long.class.equals(clazz)) || (Long.class.equals(clazz)))
			return Long.parseLong(expression);

		if ((float.class.equals(clazz)) || (Float.class.equals(clazz)))
			return Float.parseFloat(expression);

		if ((double.class.equals(clazz)) || (Double.class.equals(clazz)))
			return Double.parseDouble(expression);

		if ((boolean.class.equals(clazz)) || (Boolean.class.equals(clazz)))
			return Boolean.parseBoolean(expression);

		if (String.class.equals(clazz))
			return expression;

		throw new IllegalArgumentException("Could not convert <" + expression + "> to " + clazz.getName());
	}

	private static boolean isSimpleType(Class<?> type) {
		if ((int.class.equals(type)) || (Integer.class.equals(type)))
			return true;

		if ((short.class.equals(type)) || (Short.class.equals(type)))
			return true;

		if ((byte.class.equals(type)) || (Byte.class.equals(type)))
			return true;

		if ((char.class.equals(type)) || (Character.class.equals(type)))
			return true;

		if ((long.class.equals(type)) || (Long.class.equals(type)))
			return true;

		if ((float.class.equals(type)) || (Float.class.equals(type)))
			return true;

		if ((double.class.equals(type)) || (Double.class.equals(type)))
			return true;

		if ((boolean.class.equals(type)) || (Boolean.class.equals(type)))
			return true;

		if (String.class.equals(type))
			return true;

		return false;
	}

	private final Object fBackingJavaObject;
	private final Field fField;

	public EaseJavaFieldVariable(final Field field, Object backingJavaObject, final IDebugElement parent) throws IllegalAccessException {
		super(field.getName(), field.get(backingJavaObject), parent, null);

		fField = field;
		fBackingJavaObject = backingJavaObject;

		getValue().setParent(parent);
	}

	@Override
	public String getReferenceTypeName() {
		return fField.getType().getSimpleName();
	}

	@Override
	public Type getType() {
		return Type.JAVA_OBJECT;
	}

	public boolean isPublic() {
		return Modifier.isPublic(fField.getModifiers());
	}

	public boolean isProtected() {
		return Modifier.isProtected(fField.getModifiers());
	}

	public boolean isPrivate() {
		return Modifier.isPrivate(fField.getModifiers());
	}

	public boolean isFinal() {
		return Modifier.isFinal(fField.getModifiers());
	}

	// ************************************************************
	// IValueModification
	// ************************************************************

	@Override
	public void setValue(final String expression) throws DebugException {
		try {
			final Object value = convert(expression, fField.getType());
			fField.set(fBackingJavaObject, value);
			getValue().update(value);

			if (getParent() instanceof EaseDebugStackFrame)
				((EaseDebugStackFrame) getParent()).fireChangeEvent(DebugEvent.CONTENT);

		} catch (final Exception e) {
			// cannot set, giving up
			throw new DebugException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Could not set variable \"" + getName() + "\"", e));
		}
	}

	@Override
	public boolean supportsValueModification() {
		if (isSimpleType(fField.getType()))
			return !Modifier.isFinal(fField.getModifiers());

		return false;
	}
}
