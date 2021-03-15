/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *     Arthur Daussy - Allow optional parameter
 *******************************************************************************/
package org.eclipse.ease.lang.javascript;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ease.AbstractCodeFactory;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Logger;
import org.eclipse.ease.modules.EnvironmentModule;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.modules.ModuleHelper;
import org.eclipse.ease.tools.StringTools;

public class JavaScriptCodeFactory extends AbstractCodeFactory {

	public static List<String> RESERVED_KEYWORDS = new ArrayList<>();

	static {
		RESERVED_KEYWORDS.add("abstract");
		RESERVED_KEYWORDS.add("arguments");
		RESERVED_KEYWORDS.add("boolean");
		RESERVED_KEYWORDS.add("break");
		RESERVED_KEYWORDS.add("byte");
		RESERVED_KEYWORDS.add("case");
		RESERVED_KEYWORDS.add("catch");
		RESERVED_KEYWORDS.add("char");
		RESERVED_KEYWORDS.add("class");
		RESERVED_KEYWORDS.add("const");
		RESERVED_KEYWORDS.add("continue");
		RESERVED_KEYWORDS.add("debugger");
		RESERVED_KEYWORDS.add("default");
		RESERVED_KEYWORDS.add("delete");
		RESERVED_KEYWORDS.add("do");
		RESERVED_KEYWORDS.add("double");
		RESERVED_KEYWORDS.add("else");
		RESERVED_KEYWORDS.add("enum");
		RESERVED_KEYWORDS.add("eval");
		RESERVED_KEYWORDS.add("export");
		RESERVED_KEYWORDS.add("extends");
		RESERVED_KEYWORDS.add("false");
		RESERVED_KEYWORDS.add("final");
		RESERVED_KEYWORDS.add("finally");
		RESERVED_KEYWORDS.add("float");
		RESERVED_KEYWORDS.add("for");
		RESERVED_KEYWORDS.add("function");
		RESERVED_KEYWORDS.add("goto");
		RESERVED_KEYWORDS.add("if");
		RESERVED_KEYWORDS.add("implements");
		RESERVED_KEYWORDS.add("import");
		RESERVED_KEYWORDS.add("in");
		RESERVED_KEYWORDS.add("instanceof");
		RESERVED_KEYWORDS.add("int");
		RESERVED_KEYWORDS.add("interface");
		RESERVED_KEYWORDS.add("let");
		RESERVED_KEYWORDS.add("long");
		RESERVED_KEYWORDS.add("native");
		RESERVED_KEYWORDS.add("new");
		RESERVED_KEYWORDS.add("null");
		RESERVED_KEYWORDS.add("package");
		RESERVED_KEYWORDS.add("private");
		RESERVED_KEYWORDS.add("protected");
		RESERVED_KEYWORDS.add("public");
		RESERVED_KEYWORDS.add("return");
		RESERVED_KEYWORDS.add("short");
		RESERVED_KEYWORDS.add("static");
		RESERVED_KEYWORDS.add("super");
		RESERVED_KEYWORDS.add("switch");
		RESERVED_KEYWORDS.add("synchronized");
		RESERVED_KEYWORDS.add("this");
		RESERVED_KEYWORDS.add("throw");
		RESERVED_KEYWORDS.add("throws");
		RESERVED_KEYWORDS.add("transient");
		RESERVED_KEYWORDS.add("true");
		RESERVED_KEYWORDS.add("try");
		RESERVED_KEYWORDS.add("typeof");
		RESERVED_KEYWORDS.add("var");
		RESERVED_KEYWORDS.add("void");
		RESERVED_KEYWORDS.add("volatile");
		RESERVED_KEYWORDS.add("while");
		RESERVED_KEYWORDS.add("with");
		RESERVED_KEYWORDS.add("yield");
	}

	private static boolean isValidMethodName(final String methodName) {
		return JavaScriptHelper.isSaveName(methodName) && !RESERVED_KEYWORDS.contains(methodName);
	}

	@Override
	public String classInstantiation(final Class<?> clazz, final String[] parameters) {
		final StringBuilder code = new StringBuilder();
		code.append("new Packages.").append(clazz.getName()).append('(');

		if (parameters != null) {
			for (final String parameter : parameters)
				code.append(parameter).append(", ");

			if (parameters.length > 0)
				code.delete(code.length() - 2, code.length());
		}

		code.append(')');

		return code.toString();
	}

	@Override
	protected String getNullString() {
		return "null";
	}

	@Override
	public String getSaveVariableName(final String variableName) {
		return JavaScriptHelper.getSaveName(variableName);
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

		scriptCode.append(EnvironmentModule.EASE_CODE_PREFIX + "temporary_wrapper_object = {").append(StringTools.LINE_DELIMITER);
		scriptCode.append("\tjavaInstance: ").append(identifier).append(',').append(StringTools.LINE_DELIMITER);

		scriptCode.append(StringTools.LINE_DELIMITER).append("\t// field definitions").append(StringTools.LINE_DELIMITER);
		for (final Field field : ModuleHelper.getFields(instance.getClass())) {
			if (isSupportedByLanguage(field)) {
				scriptCode.append('\t').append(field.getName()).append(": ").append(identifier).append('.').append(field.getName()).append(',')
						.append(StringTools.LINE_DELIMITER);
			}
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
					if (!isValidMethodName(alias)) {
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
			if (!isValidMethodName(name)) {
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

		final String methodId = ((EnvironmentModule) environment).registerMethod(method);

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
