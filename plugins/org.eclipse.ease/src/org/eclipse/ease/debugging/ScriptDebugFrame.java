/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.debugging;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ease.Script;

/**
 * Frame containing debug location information for a dedicated script source.
 */
public class ScriptDebugFrame implements IScriptDebugFrame {

	private final Script fScript;
	private int fLineNumber;
	private final int fType;
	private final String fName;

	public ScriptDebugFrame(final Script script, final int lineNumber, final int type, final String name) {
		fScript = script;
		fLineNumber = lineNumber;
		fType = type;
		fName = name;
	}

	public ScriptDebugFrame(final Script script, final int lineNumber, final int type) {
		this(script, lineNumber, type, null);
	}

	public ScriptDebugFrame(final IScriptDebugFrame frame) {
		this(frame.getScript(), frame.getLineNumber(), frame.getType(), frame.getName());
	}

	@Override
	public int getLineNumber() {
		return fLineNumber;
	}

	@Override
	public Script getScript() {
		return fScript;
	}

	@Override
	public int getType() {
		return fType;
	}

	@Override
	public String getName() {
		return (fName != null) ? fName : getScript().getTitle();
	}

	@Override
	public Map<String, Object> getVariables() {
		return Collections.emptyMap();
	}

	@Override
	public Map<String, Object> getVariables(final Object parent) {
		final Map<String, Object> variables = new LinkedHashMap<>();

		if ((parent != null) && (!ScriptDebugValue.isSimpleType(parent))) {
			if (parent.getClass().isArray()) {
				// handle arrays

				if (parent instanceof Object[]) {
					for (int index = 0; index < ((Object[]) parent).length; index++)
						variables.put("[" + index + "]", ((Object[]) parent)[index]);

				} else if (parent instanceof char[]) {
					for (int index = 0; index < ((char[]) parent).length; index++)
						variables.put("[" + index + "]", ((char[]) parent)[index]);

				} else if (parent instanceof byte[]) {
					for (int index = 0; index < ((byte[]) parent).length; index++)
						variables.put("[" + index + "]", ((byte[]) parent)[index]);

				} else if (parent instanceof boolean[]) {
					for (int index = 0; index < ((boolean[]) parent).length; index++)
						variables.put("[" + index + "]", ((boolean[]) parent)[index]);

				} else if (parent instanceof short[]) {
					for (int index = 0; index < ((short[]) parent).length; index++)
						variables.put("[" + index + "]", ((short[]) parent)[index]);

				} else if (parent instanceof int[]) {
					for (int index = 0; index < ((int[]) parent).length; index++)
						variables.put("[" + index + "]", ((int[]) parent)[index]);

				} else if (parent instanceof long[]) {
					for (int index = 0; index < ((long[]) parent).length; index++)
						variables.put("[" + index + "]", ((long[]) parent)[index]);

				} else if (parent instanceof double[]) {
					for (int index = 0; index < ((double[]) parent).length; index++)
						variables.put("[" + index + "]", ((double[]) parent)[index]);

				} else if (parent instanceof float[]) {
					for (int index = 0; index < ((float[]) parent).length; index++)
						variables.put("[" + index + "]", ((float[]) parent)[index]);
				}

			} else {
				// handle java objects
				for (final Field field : getFields(parent.getClass())) {
					try {
						if (!Modifier.isStatic(field.getModifiers())) {
							if (!field.isAccessible())
								field.setAccessible(true);

							variables.put(field.getName(), field.get(parent));
						}
					} catch (final Exception e) {
					}
				}
			}
		}

		return variables;
	}

	/**
	 * Get all the variables in a class from its entire class hierarchy
	 *
	 * @param currentClass
	 *            Class object for which variable list is required
	 * @return array of variables
	 */
	private Field[] getFields(Class<?> currentClass) {
		final List<Field> fields = new ArrayList<>();
		while (currentClass != null) {
			fields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
			currentClass = currentClass.getSuperclass();
		}

		return fields.toArray(new Field[0]);
	}

	@Override
	public void setLineNumber(final int lineNumber) {
		fLineNumber = lineNumber;
	}
}
