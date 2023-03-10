/*******************************************************************************
 * Copyright (c) 2013, 2016 Atos and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License_Identifier: EPL-2.0
 *
 * Contributors:
 *     Arthur Daussy - initial implementation
 *******************************************************************************/
package org.eclipse.ease;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.modules.ModuleHelper;
import org.eclipse.ease.modules.ScriptParameter;
import org.eclipse.ease.modules.WrapToScript;

public abstract class AbstractCodeFactory implements ICodeFactory {

	/** Default line break character. */
	public static final String LINE_DELIMITER = System.getProperty(Platform.PREF_LINE_SEPARATOR);

	protected String buildParameterList(List<Parameter> parameters) {
		return parameters.stream().map(p -> toSafeName(p.getName())).collect(Collectors.joining(", "));
	}

	@Override
	public String getDefaultValue(final Parameter parameter) {
		final String defaultStringValue = parameter.getDefaultValue().replaceAll("\\r", "\\\\r").replaceAll("\\n", "\\\\n");

		// null as default value
		if (ScriptParameter.NULL.equals(defaultStringValue))
			return getNullString();

		// base datatypes
		final Class<?> clazz = parameter.getClazz();
		if ((Integer.class.equals(clazz)) || (int.class.equals(clazz))) {
			try {
				return Integer.toString(Integer.parseInt(defaultStringValue));
			} catch (final NumberFormatException e1) {
				throw new IllegalArgumentException(String.format("Cannot convert default value '%s' of parameter '%s' to %s", parameter.getDefaultValue(),
						parameter.getName(), parameter.getClazz().getSimpleName()), e1);
			}
		}
		if ((Long.class.equals(clazz)) || (long.class.equals(clazz))) {
			try {
				return Long.toString(Long.parseLong(defaultStringValue));
			} catch (final NumberFormatException e1) {
				throw new IllegalArgumentException(String.format("Cannot convert default value '%s' of parameter '%s' to %s", parameter.getDefaultValue(),
						parameter.getName(), parameter.getClazz().getSimpleName()), e1);
			}
		}
		if ((Byte.class.equals(clazz)) || (byte.class.equals(clazz))) {
			try {
				return Integer.toString(Integer.parseInt(defaultStringValue));
			} catch (final NumberFormatException e1) {
				throw new IllegalArgumentException(String.format("Cannot convert default value '%s' of parameter '%s' to %s", parameter.getDefaultValue(),
						parameter.getName(), parameter.getClazz().getSimpleName()), e1);
			}
		}
		if ((Float.class.equals(clazz)) || (float.class.equals(clazz))) {
			try {
				return Float.toString(Float.parseFloat(defaultStringValue));
			} catch (final NumberFormatException e1) {
				throw new IllegalArgumentException(String.format("Cannot convert default value '%s' of parameter '%s' to %s", parameter.getDefaultValue(),
						parameter.getName(), parameter.getClazz().getSimpleName()), e1);
			}
		}
		if ((Double.class.equals(clazz)) || (double.class.equals(clazz))) {
			try {
				return Double.toString(Double.parseDouble(defaultStringValue));
			} catch (final NumberFormatException e1) {
				throw new IllegalArgumentException(String.format("Cannot convert default value '%s' of parameter '%s' to %s", parameter.getDefaultValue(),
						parameter.getName(), parameter.getClazz().getSimpleName()), e1);
			}
		}
		if ((Boolean.class.equals(clazz)) || (boolean.class.equals(clazz))) {
			return Boolean.parseBoolean(defaultStringValue) ? getTrueString() : getFalseString();
		}
		if ((Character.class.equals(clazz)) || (char.class.equals(clazz))) {
			if (defaultStringValue.isEmpty())
				throw new IllegalArgumentException(
						String.format("Cannot convert empty default value of parameter '%s' to %s", parameter.getName(), parameter.getClazz().getSimpleName()));

			return "'" + defaultStringValue.charAt(0) + "'";
		}
		if (String.class.equals(clazz))
			return "\"" + defaultStringValue + "\"";

		// undefined resolves to empty constructor
		if (ScriptParameter.UNDEFINED.equals(defaultStringValue)) {
			// look for empty constructor
			try {
				clazz.getConstructor();
				// empty constructor found, return class
				return classInstantiation(clazz, null);
			} catch (final SecurityException e) {
			} catch (final NoSuchMethodException e) {
			}
		}

		// look for string constructor
		try {
			clazz.getConstructor(String.class);
			// string constructor found, return class
			return classInstantiation(clazz, new String[] { "\"" + defaultStringValue + "\"" });
		} catch (final SecurityException e) {
		} catch (final NoSuchMethodException e) {
		}

		// special handling for string defaults passed to an Object.class
		if (clazz.isAssignableFrom(String.class))
			return classInstantiation(String.class, new String[] { "\"" + defaultStringValue + "\"" });

		return getNullString();
	}

	public static Collection<String> getMethodNames(final Method method) {
		final Set<String> methodNames = new HashSet<>();
		methodNames.add(method.getName());
		methodNames.addAll(getMethodAliases(method));

		return methodNames;
	}

	public static Collection<String> getMethodAliases(final Method method) {
		final Set<String> methodAliases = new HashSet<>();

		final WrapToScript wrapAnnotation = method.getAnnotation(WrapToScript.class);
		if (wrapAnnotation != null) {
			for (final String name : wrapAnnotation.alias().split(WrapToScript.DELIMITER))
				if (!name.isBlank())
					methodAliases.add(name.trim());
		}

		return methodAliases;
	}

	/**
	 * Get string representation for <code>null</code> in target language.
	 *
	 * @return <code>null</code> in target language.
	 */
	protected abstract String getNullString();

	/**
	 * Get string representation for <code>true</code> in target language.
	 *
	 * @return <code>true</code> in target language.
	 */
	protected String getTrueString() {
		return Boolean.TRUE.toString();
	}

	/**
	 * Get string representation for <code>false</code> in target language.
	 *
	 * @return <code>false</code> in target language.
	 */
	protected String getFalseString() {
		return Boolean.FALSE.toString();
	}

	@Override
	public String createFunctionCall(final Method method, final Object... parameters) {
		final String parameterString = Arrays.asList(parameters).stream().map(p -> parameterToString(p)).collect(Collectors.joining(", "));
		return String.format("%s(%s);", method.getName(), parameterString);
	}

	private String parameterToString(Object parameter) {
		if (parameter instanceof String)
			return String.format("\"%s\"", ((String) parameter).replace("\"", "\\\"").replace("\\", "\\\\"));

		if (parameter == null)
			return getNullString();

		if (parameter instanceof Boolean)
			return ((Boolean) parameter) ? getTrueString() : getFalseString();

		return parameter.toString();
	}

	/**
	 * As many languages use //, provide a default of // style comments.
	 *
	 * @return "// "
	 */
	protected String getSingleLineCommentToken() {
		return "// ";
	}

	/**
	 * As many languages use /*, provide a default of /* style comments.
	 *
	 * @return "/*"
	 */
	protected String getMultiLineCommentStartToken() {
		return "/*";
	}

	/**
	 * Default block comment end token.
	 *
	 * @return asterisk, dash
	 */
	protected String getMultiLineCommentEndToken() {
		return "*/";
	}

	@Override
	public String createCommentedString(String comment, boolean addBlockComment) {
		if (addBlockComment) {
			return getMultiLineCommentStartToken() + comment + getMultiLineCommentEndToken();

		} else {
			final String token = getSingleLineCommentToken();
			final Stream<String> split = Pattern.compile("\r?\n").splitAsStream(comment);
			return split.map(s -> token + s).collect(Collectors.joining(System.lineSeparator()));
		}
	}

	@Override
	public String createWrapper(IEnvironment environment, Object instance, String identifier, boolean customNamespace, IScriptEngine engine) {

		if (!customNamespace) {
			// put functions/fields to global namespace
			final StringBuilder scriptCode = new StringBuilder();

			// create wrappers for methods
			for (final Method method : ModuleHelper.getMethods(instance.getClass())) {
				if (isSupportedByLanguage(method)) {

					final String code = createFunctionWrapper(environment, identifier, method);

					if ((code != null) && !code.isEmpty()) {
						scriptCode.append(code);
						scriptCode.append('\n');
					}
				}
			}

			// create wrappers for final fields
			for (final Field field : ModuleHelper.getFields(instance.getClass())) {
				if (isSupportedByLanguage(field)) {
					final String code = createFieldWrapper(environment, identifier, field);

					if ((code != null) && !code.isEmpty()) {
						scriptCode.append(code);
						scriptCode.append('\n');
					}
				}
			}

			return scriptCode.toString();

		} else
			throw new RuntimeException("Object wrappers not supported by default wrapper");
	}

	/**
	 * Verify that a method can be wrapped for the current target language.
	 *
	 * @param method
	 *            method to be queried
	 * @return <code>true</code> when method can be wrapped
	 */
	protected boolean isSupportedByLanguage(Method method) {
		return isSupportedByLanguage(method.getAnnotation(WrapToScript.class));
	}

	/**
	 * Verify that a field can be wrapped for the current target language.
	 *
	 * @param field
	 *            method to be queried
	 * @return <code>true</code> when field can be wrapped
	 */
	protected boolean isSupportedByLanguage(Field field) {
		return isSupportedByLanguage(field.getAnnotation(WrapToScript.class));
	}

	/**
	 * Verify that an annotation indicates wrapping support for the current target language.
	 *
	 * @param annotation
	 *            annotation to be queried
	 * @return <code>true</code> when annotation indicated wrapping support
	 */
	private boolean isSupportedByLanguage(WrapToScript annotation) {
		if (annotation != null) {
			final String languages = annotation.supportedLanguages().trim();
			if (!languages.isEmpty()) {
				final String[] supportedLanguages = languages.split(",");
				if (supportedLanguages[0].startsWith("!")) {
					// exclude pattern
					for (final String language : supportedLanguages) {
						if (language.substring(1).equals(getLanguageIdentifier()))
							return false;
					}

				} else {
					// include pattern
					for (final String language : supportedLanguages) {
						if (language.equals(getLanguageIdentifier()))
							return true;
					}

					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Get the language identifier for this code factory.
	 *
	 * @return language identifier as defined in extension point scriptType.name
	 */
	protected abstract Object getLanguageIdentifier();

	/**
	 * Create code for a method wrapper function in the global namespace of the script engine.
	 *
	 * @param environment
	 *            environment instance
	 * @param identifier
	 *            function name to be used
	 * @param method
	 *            method to refer to
	 * @return script code to be injected into the script engine
	 */
	protected abstract String createFunctionWrapper(IEnvironment environment, String identifier, Method method);

	/**
	 * Create code for a field wrapper function in the global namespace of the script engine.
	 *
	 * @param environment
	 *            environment instance
	 * @param identifier
	 *            function name to be used
	 * @param field
	 *            field to refer to
	 * @return script code to be injected into the script engine
	 */
	protected abstract String createFieldWrapper(IEnvironment environment, String identifier, Field field);

	/**
	 * Convert a given name to a script language safe name. To make sure it is safe we check against a list of given keywords. We then append '_' until the
	 * value is save.
	 *
	 * @param name
	 *            name to check
	 * @return safe name
	 */
	protected abstract String toSafeName(String name);
}
