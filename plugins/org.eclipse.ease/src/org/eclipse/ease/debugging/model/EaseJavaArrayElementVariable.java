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

import java.lang.reflect.Array;

import org.eclipse.debug.core.model.IDebugElement;

public class EaseJavaArrayElementVariable extends EaseDebugVariable {

	private static Object convert(String expression, Class<? extends Object> clazz) {
		if ((int.class.equals(clazz)) || (Integer.class.equals(clazz)))
			return Integer.parseInt(expression);

		if ((char.class.equals(clazz)) || (Character.class.equals(clazz))) {
			if (expression.length() == 1)
				return expression.charAt(0);
		}

		// FIXME add further conversions

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
	private final int fIndex;

	public EaseJavaArrayElementVariable(final int index, Object backingJavaObject, final IDebugElement parent) {
		super("[" + index + "]", Array.get(backingJavaObject, index), parent, null);

		fIndex = index;
		fBackingJavaObject = backingJavaObject;

		getValue().setParent(parent);
	}

	@Override
	public String getReferenceTypeName() {
		return fBackingJavaObject.getClass().getComponentType().getSimpleName();
	}

	@Override
	public Type getType() {
		return Type.JAVA_OBJECT;
	}

	// ************************************************************
	// IValueModification
	// ************************************************************

	@Override
	public void setValue(final String expression) {
		Array.set(fBackingJavaObject, fIndex, convert(expression, fBackingJavaObject.getClass().getComponentType()));
	}

	@Override
	public boolean supportsValueModification() {
		if (isSimpleType(fBackingJavaObject.getClass().getComponentType()))
			return true;

		return false;
	}
}
