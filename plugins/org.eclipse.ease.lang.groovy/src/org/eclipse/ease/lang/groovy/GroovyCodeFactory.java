/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
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
package org.eclipse.ease.lang.groovy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ease.Logger;
import org.eclipse.ease.modules.EnvironmentModule;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.modules.ModuleHelper;
import org.eclipse.ease.tools.StringTools;

public class GroovyCodeFactory extends org.eclipse.ease.AbstractCodeFactory {

	public static List<String> RESERVED_KEYWORDS = new ArrayList<>();

	static {
		RESERVED_KEYWORDS.add("abstract");
		RESERVED_KEYWORDS.add("as");
		RESERVED_KEYWORDS.add("assert");
		RESERVED_KEYWORDS.add("boolean");
		RESERVED_KEYWORDS.add("break");
		RESERVED_KEYWORDS.add("byte");
		RESERVED_KEYWORDS.add("case");
		RESERVED_KEYWORDS.add("catch");
		RESERVED_KEYWORDS.add("char");
		RESERVED_KEYWORDS.add("class");
		RESERVED_KEYWORDS.add("const");
		RESERVED_KEYWORDS.add("continue");
		RESERVED_KEYWORDS.add("def");
		RESERVED_KEYWORDS.add("default");
		RESERVED_KEYWORDS.add("do");
		RESERVED_KEYWORDS.add("double");
		RESERVED_KEYWORDS.add("else");
		RESERVED_KEYWORDS.add("enum");
		RESERVED_KEYWORDS.add("extends");
		RESERVED_KEYWORDS.add("false");
		RESERVED_KEYWORDS.add("final");
		RESERVED_KEYWORDS.add("finally");
		RESERVED_KEYWORDS.add("float");
		RESERVED_KEYWORDS.add("for");
		RESERVED_KEYWORDS.add("goto");
		RESERVED_KEYWORDS.add("if");
		RESERVED_KEYWORDS.add("implements");
		RESERVED_KEYWORDS.add("import");
		RESERVED_KEYWORDS.add("in");
		RESERVED_KEYWORDS.add("instanceof");
		RESERVED_KEYWORDS.add("int");
		RESERVED_KEYWORDS.add("interface");
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
		RESERVED_KEYWORDS.add("strictfp");
		RESERVED_KEYWORDS.add("super");
		RESERVED_KEYWORDS.add("switch");
		RESERVED_KEYWORDS.add("synchronized");
		RESERVED_KEYWORDS.add("this");
		RESERVED_KEYWORDS.add("threadsafe");
		RESERVED_KEYWORDS.add("throw");
		RESERVED_KEYWORDS.add("throws");
		RESERVED_KEYWORDS.add("transient");
		RESERVED_KEYWORDS.add("true");
		RESERVED_KEYWORDS.add("try");
		RESERVED_KEYWORDS.add("void");
		RESERVED_KEYWORDS.add("volatile");
		RESERVED_KEYWORDS.add("while");
	}

	private static boolean isValidMethodName(final String methodName) {
		return GroovyHelper.isSaveName(methodName) && !RESERVED_KEYWORDS.contains(methodName);
	}

	@Override
	public String classInstantiation(final Class<?> clazz, final String[] parameters) {
		final StringBuilder code = new StringBuilder();
		code.append("import ");
		code.append(clazz.getName());
		code.append(";\n");

		code.append("new ");
		code.append(clazz.getName());
		code.append("(");

		if (parameters != null) {
			for (final String parameter : parameters) {
				code.append('"');
				code.append(parameter);
				code.append('"');
				code.append(", ");
			}
			if (parameters.length > 0)
				code.replace(code.length() - 2, code.length(), "");
		}

		code.append(")");

		return code.toString();
	}

	@Override
	protected String getNullString() {
		return "null";
	}

	@Override
	public String getSaveVariableName(final String variableName) {
		return GroovyHelper.getSaveName(variableName);
	}

	private StringBuilder verifyParameters(final List<Parameter> parameters) {
		final StringBuilder data = new StringBuilder();

		// FIXME currently not supported
		// if (!parameters.isEmpty()) {
		// Parameter parameter = parameters.get(parameters.size() - 1);
		// data.append("\tif (typeof " + parameter.getName() + " === \"undefined\") {\n");
		// if (parameter.isOptional()) {
		// data.append("\t\t" + parameter.getName() + " = " + getDefaultValue(parameter) + ";\n");
		// } else {
		// data.append("\t\tthrow new java.lang.RuntimeException('Parameter " + parameter.getName() + " is not optional');\n");
		//
		// }
		// data.append(verifyParameters(parameters.subList(0, parameters.size() - 1)));
		// data.append("\t}\n");
		// }

		return data;
	}

	@Override
	protected String createFieldWrapper(IEnvironment environment, String identifier, Field field) {
		final StringBuilder groovyCode = new StringBuilder();

		groovyCode.append(field.getName());
		groovyCode.append(" = ");
		groovyCode.append(identifier);
		groovyCode.append('.');
		groovyCode.append(field.getName());
		groovyCode.append(';');
		groovyCode.append(StringTools.LINE_DELIMITER);

		return groovyCode.toString();
	}

	@Override
	public String createFunctionWrapper(final IEnvironment environment, final String moduleVariable, final Method method) {

		final String methodId = environment.registerMethod(method);

		final StringBuilder groovyCode = new StringBuilder();

		// parse parameters
		final List<Parameter> parameters = ModuleHelper.getParameters(method);

		// build parameter string
		final StringBuilder parameterList = new StringBuilder();
		for (final Parameter parameter : parameters)
			parameterList.append(", ").append(toSafeName(parameter.getName()));

		if (parameterList.length() > 2)
			parameterList.delete(0, 2);

		final StringBuilder body = new StringBuilder();
		// insert parameter checks
		body.append(verifyParameters(parameters));

		// insert hooked pre execution code
		body.append("\t").append(EnvironmentModule.getWrappedVariableName(environment)).append(".preMethodCallback('").append(methodId).append("'")
				.append(parameters.isEmpty() ? "" : ", ").append(parameterList).append(");").append(StringTools.LINE_DELIMITER);

		// insert method call
		body.append("\t").append(RESULT_NAME).append(" = ").append(moduleVariable).append(".").append(method.getName()).append("(");
		body.append(parameterList);
		body.append(");").append(StringTools.LINE_DELIMITER);

		// insert hooked post execution code
		body.append("\t").append(EnvironmentModule.getWrappedVariableName(environment)).append(".postMethodCallback('").append(methodId).append("', ")
				.append(RESULT_NAME).append(");").append(StringTools.LINE_DELIMITER);

		// insert return statement
		body.append("\treturn ").append(RESULT_NAME).append(";").append(StringTools.LINE_DELIMITER);

		// build function declarations
		for (final String name : getMethodNames(method)) {
			if (!isValidMethodName(name)) {
				Logger.error(PluginConstants.PLUGIN_ID,
						"The method name \"" + name + "\" from the module \"" + moduleVariable + "\" can not be wrapped because it's name is reserved");

			} else if (!name.isEmpty()) {
				groovyCode.append(name).append(" = { ").append(parameterList).append(" ->").append(StringTools.LINE_DELIMITER);
				groovyCode.append(body);
				groovyCode.append("}").append(StringTools.LINE_DELIMITER);
			}
		}

		return groovyCode.toString();
	}

	@Override
	protected Object getLanguageIdentifier() {
		return "Groovy";
	}

	@Override
	protected String toSafeName(String name) {
		while (RESERVED_KEYWORDS.contains(name))
			name = name + "_";

		return name;
	}
}
