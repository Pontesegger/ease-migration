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
 *     Arthur Daussy - Allow optional parameter
 *******************************************************************************/
package org.eclipse.ease.lang.javascript;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.ease.AbstractCodeFactory;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Logger;
import org.eclipse.ease.modules.EnvironmentModule;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.modules.ModuleHelper;
import org.eclipse.ease.tools.StringTools;

public class JavaScriptCodeFactory extends AbstractCodeFactory {

	private static final List<String> RESERVED_KEYWORDS = Arrays.asList("abstract", "arguments", "boolean", "break", "byte", "case", "catch", "char", "class",
			"const", "continue", "debugger", "default", "delete", "do", "double", "else", "enum", "eval", "export", "extends", "false", "final", "finally",
			"float", "for", "function", "goto", "if", "implements", "import", "in", "instanceof", "int", "interface", "let", "long", "native", "new", "null",
			"package", "private", "protected", "public", "return", "short", "static", "super", "switch", "synchronized", "this", "throw", "throws", "transient",
			"true", "try", "typeof", "var", "void", "volatile", "while", "with", "yield");

	public static boolean isSaveName(final String identifier) {
		return Pattern.matches("[a-zA-Z_$][a-zA-Z0-9_$]*", identifier) && !RESERVED_KEYWORDS.contains(identifier);
	}

	@Override
	public String classInstantiation(final Class<?> clazz, final String[] parameters) {
		final StringBuilder code = new StringBuilder();
		code.append("new Packages.").append(clazz.getName()).append('(');

		if (parameters != null)
			code.append(Arrays.asList(parameters).stream().collect(Collectors.joining(", ")));

		code.append(')');

		return code.toString();
	}

	@Override
	protected String getNullString() {
		return "null";
	}

	@Override
	public String getSaveVariableName(final String variableName) {
		// check if name is already valid
		if (isSaveName(variableName))
			return variableName;

		// not valid, convert string to valid format
		final StringBuilder buffer = new StringBuilder(variableName.replaceAll("[^a-zA-Z0-9_$]", "_"));

		// check for valid first character
		if (buffer.length() > 0) {
			final char start = buffer.charAt(0);
			if (((start < 65) || ((start > 90) && (start < 97)) || (start > 122)) && (start != '_'))
				buffer.insert(0, '_');
		} else {
			// buffer is empty, create a random string of lowercase letters
			buffer.append('_');
			final Random random = new Random();
			for (int index = 0; index < random.nextInt(20); index++)
				buffer.append('a' + random.nextInt(26));
		}

		if (RESERVED_KEYWORDS.contains(buffer.toString()))
			return "_" + buffer.toString();

		return buffer.toString();
	}

	private StringBuilder verifyParameters(Method method, final List<Parameter> parameters, String indent) {
		final StringBuilder data = new StringBuilder();

		if (!parameters.isEmpty()) {
			final Parameter parameter = parameters.get(parameters.size() - 1);
			data.append(indent).append("if (typeof " + parameter.getName() + " === \"undefined\") {").append(StringTools.LINE_DELIMITER);
			if (parameter.isOptional()) {
				data.append(indent).append("\t" + parameter.getName() + " = " + getDefaultValue(parameter) + ';').append(StringTools.LINE_DELIMITER);
			} else {
				data.append(indent).append("\tthrow 'Parameter <" + parameter.getName() + "> from " + method.getName() + "() is not optional';")
						.append(StringTools.LINE_DELIMITER);

			}
			data.append(verifyParameters(method, parameters.subList(0, parameters.size() - 1), indent + "\t"));
			data.append(indent).append('}').append(StringTools.LINE_DELIMITER);
		}

		return data;
	}

	@Override
	public String createCommentedString(String comment, boolean addBlockComment) {
		if (addBlockComment) {
			// beautify block comments by having '*' characters in each line
			final StringBuilder builder = new StringBuilder();
			builder.append("/**").append(StringTools.LINE_DELIMITER);
			for (final String line : comment.split("\\r?\\n"))
				builder.append(" * ").append(line).append(StringTools.LINE_DELIMITER);

			builder.append(" */").append(StringTools.LINE_DELIMITER);

			return builder.toString();

		} else
			return super.createCommentedString(comment, addBlockComment);
	}

	@Override
	public String createWrapper(IEnvironment environment, Object instance, String identifier, boolean customNamespace, IScriptEngine engine) {

		if (customNamespace)
			// create object wrapper
			return createObjectWrapper(environment, instance, identifier);

		else
			return super.createWrapper(environment, instance, identifier, customNamespace, engine);
	}

	private String createObjectWrapper(IEnvironment environment, Object instance, String identifier) {
		final StringBuilder scriptCode = new StringBuilder();

		scriptCode.append(String.format("%stemporary_wrapper_object = {%n", IEnvironment.EASE_CODE_PREFIX));
		scriptCode.append(String.format("\tjavaInstance: %s,%n", identifier));

		scriptCode.append(String.format("%n\t// field definitions%n"));
		for (final Field field : ModuleHelper.getFields(instance.getClass())) {
			if (isSupportedByLanguage(field))
				scriptCode.append(String.format("\t%s: %s.%s,%n", field.getName(), identifier, field.getName()));
		}

		scriptCode.append(StringTools.LINE_DELIMITER).append("\t// method definitions").append(StringTools.LINE_DELIMITER);
		for (final Method method : ModuleHelper.getMethods(instance.getClass())) {
			if (isSupportedByLanguage(method)) {

				// parse parameters
				final List<Parameter> parameters = ModuleHelper.getParameters(method);

				final String body = "\t\t" + buildMethodBody(environment, parameters, method, identifier).replaceAll("\n", "\n\t\t");

				// method header
				scriptCode.append('\t').append(method.getName()).append(": function(");
				// method parameters
				scriptCode.append(buildParameterList(parameters)).append(") {").append(StringTools.LINE_DELIMITER);
				// method body
				scriptCode.append(body).append(StringTools.LINE_DELIMITER);
				// method footer
				scriptCode.append("\t},").append(StringTools.LINE_DELIMITER).append(StringTools.LINE_DELIMITER);

				// append method aliases
				for (final String alias : getMethodAliases(method)) {
					if (!isSaveName(alias)) {
						Logger.error(PluginConstants.PLUGIN_ID,
								"The method name \"" + alias + "\" from the module \"" + identifier + "\" can not be wrapped because it's name is reserved");

					} else if (!alias.isEmpty()) {
						scriptCode.append('\t').append(alias).append(": function(");
						scriptCode.append(buildParameterList(parameters)).append(") {").append(StringTools.LINE_DELIMITER);
						scriptCode.append("\t\t// method alias").append(StringTools.LINE_DELIMITER);
						scriptCode.append("\t\treturn this.").append(method.getName()).append('(').append(buildParameterList(parameters)).append(");")
								.append(StringTools.LINE_DELIMITER);
						scriptCode.append("\t},").append(StringTools.LINE_DELIMITER).append(StringTools.LINE_DELIMITER);
					}
				}
			}
		}

		scriptCode.append("};").append(StringTools.LINE_DELIMITER);

		return scriptCode.toString();
	}

	@Override
	protected String createFunctionWrapper(IEnvironment environment, final String moduleVariable, final Method method) {

		final StringBuilder javaScriptCode = new StringBuilder();

		// parse parameters
		final List<Parameter> parameters = ModuleHelper.getParameters(method);

		// build parameter string
		final String parameterList = buildParameterList(parameters);

		final String body = "\t" + buildMethodBody(environment, parameters, method, moduleVariable).replaceAll("\n", "\n\t");

		// build function declarations
		for (final String name : getMethodNames(method)) {
			if (!isSaveName(name)) {
				Logger.error(PluginConstants.PLUGIN_ID,
						"The method name \"" + name + "\" from the module \"" + moduleVariable + "\" can not be wrapped because it's name is reserved");

			} else if (!name.isEmpty()) {
				javaScriptCode.append("function ").append(name).append('(').append(parameterList).append(") {").append(StringTools.LINE_DELIMITER);
				javaScriptCode.append(body).append(StringTools.LINE_DELIMITER);
				javaScriptCode.append('}').append(StringTools.LINE_DELIMITER);
			}
		}

		return javaScriptCode.toString();
	}

	@Override
	protected String createFieldWrapper(IEnvironment environment, String identifier, Field field) {
		final StringBuilder javaScriptCode = new StringBuilder();

		javaScriptCode.append(String.format("var %s = %s.%s;", field.getName(), identifier, field.getName()));
		javaScriptCode.append(StringTools.LINE_DELIMITER);

		return javaScriptCode.toString();
	}

	private String buildMethodBody(IEnvironment environment, List<Parameter> parameters, Method method, String classIdentifier) {
		final StringBuilder body = new StringBuilder();
		// insert parameter checks
		body.append("// verify mandatory and optional parameters").append(StringTools.LINE_DELIMITER);
		body.append(verifyParameters(method, parameters, "")).append(StringTools.LINE_DELIMITER);

		// insert deprecation warnings
		if (ModuleHelper.isDeprecated(method))
			body.append("printError('" + method.getName() + "() is deprecated. Consider updating your code.', true);").append(StringTools.LINE_DELIMITER);

		final String methodId = environment.registerMethod(method);

		// body.append("var hasCallback = false;").append(StringTools.LINE_DELIMITER);
		body.append("if (!").append(EnvironmentModule.getWrappedVariableName(environment)).append(".hasMethodCallback(\"").append(methodId).append("\")) {")
				.append(StringTools.LINE_DELIMITER);

		// plain method call
		body.append("\t// delegate call to java layer").append(StringTools.LINE_DELIMITER);
		body.append("\treturn ").append(classIdentifier).append('.').append(method.getName()).append('(');
		body.append(buildParameterList(parameters));
		body.append(");").append(StringTools.LINE_DELIMITER).append(StringTools.LINE_DELIMITER);

		body.append("} else {").append(StringTools.LINE_DELIMITER);

		// method call with callbacks
		body.append("\t").append(EnvironmentModule.getWrappedVariableName(environment)).append(".preMethodCallback('").append(methodId).append("'")
				.append(parameters.isEmpty() ? "" : ", ").append(buildParameterList(parameters)).append(");").append(StringTools.LINE_DELIMITER);
		body.append(StringTools.LINE_DELIMITER);
		body.append("\t// delegate call to java layer").append(StringTools.LINE_DELIMITER);
		body.append("\tvar ").append(RESULT_NAME).append(" = ").append(classIdentifier).append('.').append(method.getName()).append('(');
		body.append(buildParameterList(parameters));
		body.append(");").append(StringTools.LINE_DELIMITER);
		body.append(StringTools.LINE_DELIMITER);
		body.append("\t").append(EnvironmentModule.getWrappedVariableName(environment)).append(".postMethodCallback('").append(methodId).append("', ")
				.append(RESULT_NAME).append(");").append(StringTools.LINE_DELIMITER);
		body.append(StringTools.LINE_DELIMITER);
		body.append("\treturn __result;").append(StringTools.LINE_DELIMITER);
		body.append(StringTools.LINE_DELIMITER);
		body.append("}").append(StringTools.LINE_DELIMITER);

		return body.toString();
	}

	@Override
	protected Object getLanguageIdentifier() {
		return "JavaScript";
	}

	@Override
	protected String toSafeName(String name) {
		while (RESERVED_KEYWORDS.contains(name))
			name = name + "_";

		return name;
	}
}
