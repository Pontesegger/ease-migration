/*******************************************************************************
 * Copyright (c) 2013, 2016 Atos and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Arthur Daussy - initial implementation
 *     Christian Pontesegger - simplified to use base class
 *******************************************************************************/
package org.eclipse.ease.lang.python;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ease.AbstractCodeFactory;
import org.eclipse.ease.Logger;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.modules.IScriptFunctionModifier;
import org.eclipse.ease.modules.ModuleHelper;

public class PythonCodeFactory extends AbstractCodeFactory {

	public static List<String> RESERVED_KEYWORDS = new ArrayList<>();

	static {
		RESERVED_KEYWORDS.add("and");
		RESERVED_KEYWORDS.add("as");
		RESERVED_KEYWORDS.add("assert");
		RESERVED_KEYWORDS.add("break");
		RESERVED_KEYWORDS.add("class");
		RESERVED_KEYWORDS.add("continue");
		RESERVED_KEYWORDS.add("def");
		RESERVED_KEYWORDS.add("del");
		RESERVED_KEYWORDS.add("elif");
		RESERVED_KEYWORDS.add("else");
		RESERVED_KEYWORDS.add("except");
		RESERVED_KEYWORDS.add("exec");
		RESERVED_KEYWORDS.add("finally");
		RESERVED_KEYWORDS.add("for");
		RESERVED_KEYWORDS.add("from");
		RESERVED_KEYWORDS.add("global");
		RESERVED_KEYWORDS.add("if");
		RESERVED_KEYWORDS.add("import");
		RESERVED_KEYWORDS.add("in");
		RESERVED_KEYWORDS.add("is");
		RESERVED_KEYWORDS.add("lambda");
		RESERVED_KEYWORDS.add("not");
		RESERVED_KEYWORDS.add("or");
		RESERVED_KEYWORDS.add("pass");
		RESERVED_KEYWORDS.add("print");
		RESERVED_KEYWORDS.add("raise");
		RESERVED_KEYWORDS.add("return");
		RESERVED_KEYWORDS.add("try");
		RESERVED_KEYWORDS.add("while");
		RESERVED_KEYWORDS.add("with");
		RESERVED_KEYWORDS.add("yield");

		// built in functions
		RESERVED_KEYWORDS.add("__import__");
		RESERVED_KEYWORDS.add("abs");
		RESERVED_KEYWORDS.add("all");
		RESERVED_KEYWORDS.add("any");
		RESERVED_KEYWORDS.add("ascii");
		RESERVED_KEYWORDS.add("bin");
		RESERVED_KEYWORDS.add("bool");
		RESERVED_KEYWORDS.add("bytearray");
		RESERVED_KEYWORDS.add("bytes");
		RESERVED_KEYWORDS.add("callable");
		RESERVED_KEYWORDS.add("chr");
		RESERVED_KEYWORDS.add("classmethod");
		RESERVED_KEYWORDS.add("compile");
		RESERVED_KEYWORDS.add("complex");
		RESERVED_KEYWORDS.add("delattr");
		RESERVED_KEYWORDS.add("dict");
		RESERVED_KEYWORDS.add("dir");
		RESERVED_KEYWORDS.add("divmod");
		RESERVED_KEYWORDS.add("enumerate");
		RESERVED_KEYWORDS.add("eval");
		RESERVED_KEYWORDS.add("exec");
		RESERVED_KEYWORDS.add("filter");
		RESERVED_KEYWORDS.add("float");
		RESERVED_KEYWORDS.add("format");
		RESERVED_KEYWORDS.add("frozenset");
		RESERVED_KEYWORDS.add("getattr");
		RESERVED_KEYWORDS.add("globals");
		RESERVED_KEYWORDS.add("hasattr");
		RESERVED_KEYWORDS.add("hash");
		RESERVED_KEYWORDS.add("help");
		RESERVED_KEYWORDS.add("hex");
		RESERVED_KEYWORDS.add("id");
		RESERVED_KEYWORDS.add("input");
		RESERVED_KEYWORDS.add("int");
		RESERVED_KEYWORDS.add("isinstance");
		RESERVED_KEYWORDS.add("issubclass");
		RESERVED_KEYWORDS.add("iter");
		RESERVED_KEYWORDS.add("len");
		RESERVED_KEYWORDS.add("list");
		RESERVED_KEYWORDS.add("locals");
		RESERVED_KEYWORDS.add("map");
		RESERVED_KEYWORDS.add("max");
		RESERVED_KEYWORDS.add("memoryview");
		RESERVED_KEYWORDS.add("min");
		RESERVED_KEYWORDS.add("next");
		RESERVED_KEYWORDS.add("object");
		RESERVED_KEYWORDS.add("oct");
		RESERVED_KEYWORDS.add("open");
		RESERVED_KEYWORDS.add("ord");
		RESERVED_KEYWORDS.add("pow");
		RESERVED_KEYWORDS.add("print");
		RESERVED_KEYWORDS.add("property");
		RESERVED_KEYWORDS.add("range");
		RESERVED_KEYWORDS.add("repr");
		RESERVED_KEYWORDS.add("reversed");
		RESERVED_KEYWORDS.add("round");
		RESERVED_KEYWORDS.add("set");
		RESERVED_KEYWORDS.add("setattr");
		RESERVED_KEYWORDS.add("slice");
		RESERVED_KEYWORDS.add("sorted");
		RESERVED_KEYWORDS.add("staticmethod");
		RESERVED_KEYWORDS.add("str");
		RESERVED_KEYWORDS.add("sum");
		RESERVED_KEYWORDS.add("super");
		RESERVED_KEYWORDS.add("tuple");
		RESERVED_KEYWORDS.add("type");
		RESERVED_KEYWORDS.add("vars");
		RESERVED_KEYWORDS.add("zip");
	}

	@Override
	public String createFunctionWrapper(final IEnvironment environment, final String moduleVariable, final Method method) {

		final StringBuilder pythonCode = new StringBuilder();

		// parse parameters
		final List<Parameter> parameters = ModuleHelper.getParameters(method);

		// build parameter string
		final StringBuilder methodSignature = new StringBuilder();
		final StringBuilder methodCall = new StringBuilder();

		for (final Parameter parameter : parameters) {
			methodSignature.append(", ").append(parameter.getName());
			methodCall.append(", ").append(parameter.getName());
			if (parameter.isOptional())
				methodSignature.append(" = ").append(getDefaultValue(parameter));
		}

		if (methodSignature.length() > 2) {
			methodSignature.delete(0, 2);
			methodCall.delete(0, 2);
		}

		final StringBuilder body = new StringBuilder();

		// insert hooked pre execution code
		body.append(getPreExecutionCode(environment, method));

		// insert method call
		body.append('\t').append(IScriptFunctionModifier.RESULT_NAME).append(" = ").append(moduleVariable).append('.').append(method.getName()).append('(');
		body.append(methodCall);
		body.append(")\n");

		// insert hooked post execution code
		body.append(indent(getPostExecutionCode(environment, method), "\t"));

		// insert return statement
		body.append("\treturn ").append(IScriptFunctionModifier.RESULT_NAME).append('\n');

		// build function declarations
		for (final String name : getMethodNames(method)) {
			if (!isValidMethodName(name)) {
				Logger.error(Activator.PLUGIN_ID,
						"The method name \"" + name + "\" from the module \"" + moduleVariable + "\" can not be wrapped because it's name is reserved");

			} else if (!name.isEmpty()) {
				pythonCode.append("def ").append(name).append('(').append(methodSignature).append("):\n");
				pythonCode.append(body);
				pythonCode.append('\n');
			}
		}

		return pythonCode.toString();
	}

	/**
	 * @param postExecutionCode
	 * @param string
	 * @return
	 */
	private static String indent(String code, String indentation) {
		if (code.isEmpty())
			return code;

		return indentation + code.replaceAll("\n", "\n" + indentation).trim() + "\n";
	}

	@Override
	public String getSaveVariableName(final String variableName) {
		return PythonHelper.getSaveName(variableName);
	}

	@Override
	public String classInstantiation(final Class<?> clazz, final String[] parameters) {
		final StringBuilder code = new StringBuilder();
		code.append(clazz.getCanonicalName());
		code.append('(');

		if (parameters != null) {
			for (final String parameter : parameters) {
				code.append(parameter);
				code.append(", ");
			}
			if (parameters.length > 0)
				code.delete(code.length() - 2, code.length());
		}

		code.append(')');

		return code.toString();
	}

	public boolean isValidMethodName(final String methodName) {
		return PythonHelper.isSaveName(methodName) && !RESERVED_KEYWORDS.contains(methodName);
	}

	@Override
	protected String getNullString() {
		return "None";
	}

	@Override
	protected String getTrueString() {
		return "True";
	}

	@Override
	protected String getFalseString() {
		return "False";
	}

	@Override
	protected String getSingleLineCommentToken() {
		return "# ";
	}

	@Override
	protected String getMultiLineCommentStartToken() {
		return "\"\"\"";
	}

	@Override
	protected String getMultiLineCommentEndToken() {
		return getMultiLineCommentStartToken();
	}

	@Override
	protected Object getLanguageIdentifier() {
		return "Python";
	}
}
