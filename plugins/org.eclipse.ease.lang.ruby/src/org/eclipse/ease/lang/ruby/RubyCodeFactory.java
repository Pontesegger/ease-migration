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
package org.eclipse.ease.lang.ruby;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ease.AbstractCodeFactory;
import org.eclipse.ease.Logger;
import org.eclipse.ease.modules.EnvironmentModule;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.modules.ModuleHelper;
import org.eclipse.ease.tools.StringTools;

public class RubyCodeFactory extends AbstractCodeFactory {

	public static List<String> RESERVED_KEYWORDS = new ArrayList<>();

	static {
		// TODO set keywords
		// RESERVED_KEYWORDS.add("abstract");
	}

	@Override
	public String getSaveVariableName(final String variableName) {
		return RubyHelper.getSaveName(variableName);
	}

	@Override
	protected String createFieldWrapper(IEnvironment environment, String identifier, Field field) {
		final StringBuilder groovyCode = new StringBuilder();

		groovyCode.append(field.getName());
		groovyCode.append(" = ");
		groovyCode.append(field.getDeclaringClass().getName());
		groovyCode.append("::");
		groovyCode.append(field.getName());
		groovyCode.append(';');
		groovyCode.append(StringTools.LINE_DELIMITER);

		return groovyCode.toString();
	}

	@Override
	public String createFunctionWrapper(final IEnvironment environment, final String moduleVariable, final Method method) {

		final String methodId = environment.registerMethod(method);

		final StringBuilder rubyScriptCode = new StringBuilder();

		// parse parameters
		final List<Parameter> parameters = ModuleHelper.getParameters(method);

		// build parameter string
		final StringBuilder parameterList = new StringBuilder();
		for (final Parameter parameter : parameters)
			parameterList.append(", ").append(parameter.getName());

		if (parameterList.length() > 2)
			parameterList.delete(0, 2);

		final StringBuilder body = new StringBuilder();
		// insert parameter checks
		body.append(verifyParameters(parameters));

		// insert hooked pre execution code
		body.append("\t$").append(EnvironmentModule.getWrappedVariableName(environment)).append(".preMethodCallback('").append(methodId).append("'")
				.append(parameters.isEmpty() ? "" : ", ").append(parameterList).append(");").append(StringTools.LINE_DELIMITER);

		// insert method call
		if (Modifier.isStatic(method.getModifiers()))
			body.append("\t").append(RESULT_NAME).append(" = ").append(method.getDeclaringClass().getName()).append('.').append(method.getName()).append('(');
		else
			body.append("\t").append(RESULT_NAME).append(" = ").append('$').append(moduleVariable).append('.').append(method.getName()).append('(');

		body.append(parameterList);
		body.append(");\n");

		// insert hooked post execution code
		body.append("\t$").append(EnvironmentModule.getWrappedVariableName(environment)).append(".postMethodCallback('").append(methodId).append("', ")
				.append(RESULT_NAME).append(");").append(StringTools.LINE_DELIMITER);

		// insert return statement
		body.append("\treturn ").append(RESULT_NAME).append(";\n");

		// build function declarations
		for (final String name : getMethodNames(method)) {
			if (!isValidMethodName(name)) {
				Logger.error(PluginConstants.PLUGIN_ID,
						"The method name \"" + name + "\" from the module \"" + moduleVariable + "\" can not be wrapped because it's name is reserved");

			} else if (!name.isEmpty()) {
				rubyScriptCode.append("def ").append(name).append("(").append(parameterList).append(")\n");
				rubyScriptCode.append(body);
				rubyScriptCode.append("end\n");
			}
		}

		return rubyScriptCode.toString();
	}

	private StringBuilder verifyParameters(final List<Parameter> parameters) {
		final StringBuilder data = new StringBuilder();

		// FIXME currently not supported

		return data;
	}

	@Override
	public String classInstantiation(final Class<?> clazz, final String[] parameters) {
		final StringBuilder code = new StringBuilder();
		code.append(clazz.getName());
		code.append(".new(");

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

	private static boolean isValidMethodName(final String methodName) {
		return RubyHelper.isSaveName(methodName) && !RESERVED_KEYWORDS.contains(methodName);
	}

	@Override
	protected String getNullString() {
		return "nil";
	}

	@Override
	protected String getSingleLineCommentToken() {
		return "#";
	}

	@Override
	protected Object getLanguageIdentifier() {
		return "Ruby";
	}

	@Override
	protected String toSafeName(String name) {
		// TODO we do not have KEYWORDS yet, so we cannot filter
		return name;
	}
}
