/*******************************************************************************
 * Copyright (c) 2013, 2016 Atos and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Arthur Daussy - initial implementation
 *     Christian Pontesegger - simplified to use base class
 *******************************************************************************/
package org.eclipse.ease.lang.python;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.ease.AbstractCodeFactory;
import org.eclipse.ease.Logger;
import org.eclipse.ease.modules.EnvironmentModule;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.modules.ModuleHelper;
import org.eclipse.ease.tools.StringTools;

public class PythonCodeFactory extends AbstractCodeFactory {

	public static List<String> RESERVED_KEYWORDS = new ArrayList<>();

	/**
	 * List of Java primitive types as they need to be handled differently from normal classes when getting their py4j representation.
	 *
	 * bytes are handled differently as Python has native bytearray type.
	 */
	private static final List<Class<?>> PRIMITIVES = Arrays.asList(short.class, int.class, long.class, float.class, double.class, boolean.class, char.class);

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

	/**
	 * Returns the Python (py4j) identifier for the given class.
	 *
	 * @param cls
	 *            Class to get py4j identifier for.
	 * @return py4j class identifier.
	 */
	protected static String getPythonClassIdentifier(Class<?> cls) {
		if (PRIMITIVES.contains(cls)) {
			return String.format("gateway.jvm.%s", cls.getName());
		} else {
			return cls.getName();
		}
	}

	private static String toSafeNameStatic(String name) {
		while (RESERVED_KEYWORDS.contains(name))
			name = name + "_";

		return name;
	}

	private static String indent(String code, String indentation) {
		if (code.isEmpty())
			return code;

		return indentation + code.replaceAll("\n", "\n" + indentation).trim() + "\n";
	}

	/**
	 * Create wrapper code to convert all array parameters to actual Java arrays.
	 *
	 * @param parameters
	 *            List of parameters to create array conversion for.
	 * @return Wrapper code for performing array conversion.
	 */
	protected static String buildArrayConversions(List<Parameter> parameters) {
		return parameters.stream().map(PythonCodeFactory::buildArrayConversion).collect(Collectors.joining(StringTools.LINE_DELIMITER));
	}

	/**
	 * Create wrapper code to convert an array parameter to actual Java array.
	 *
	 * Generated code will have the following look: {@code
	 * 	try:
	 * 		tmp = gateway.new_array([array type], len([value to be converted]))
	 *      for index, value in enumerate([value to be converted]):
	 *          tmp[index] = value
	 *      [value to be converted] = tmp
	 *  except NameError:
	 *      pass
	 *  }
	 *
	 * @param parameter
	 *            Parameter to create conversion code for.
	 * @return Wrapper code for performing array conversion.
	 */
	protected static String buildArrayConversion(Parameter parameter) {
		final StringBuilder builder = new StringBuilder();
		if (parameter.getClazz().isArray()) {
			final String arrayType = getPythonClassIdentifier(parameter.getClazz().getComponentType());
			final String variableName = toSafeNameStatic(parameter.getName());
			builder.append(String.format("if %s is not None:", variableName)).append(StringTools.LINE_DELIMITER);
			builder.append("    try:").append(StringTools.LINE_DELIMITER);
			builder.append(String.format("        tmp = gateway.new_array(%s, len(%s))", arrayType, variableName)).append(StringTools.LINE_DELIMITER);
			builder.append(String.format("        for index, value in enumerate(%s):", variableName)).append(StringTools.LINE_DELIMITER);
			builder.append("            tmp[index] = value").append(StringTools.LINE_DELIMITER);
			builder.append(String.format("        %s =  tmp", variableName)).append(StringTools.LINE_DELIMITER);
			builder.append("    except NameError:").append(StringTools.LINE_DELIMITER);
			builder.append("        pass").append(StringTools.LINE_DELIMITER);
		}
		return builder.toString();
	}

	/**
	 * Convert integer parameters to double when the Java interface requires double/float. When integers are passed in python, Py4J does not automatically
	 * convert these numbers to double/float if required. Therefore we have to do this in the wrapper code.
	 *
	 * @param parameters
	 *            method parameters
	 * @return number conversion code
	 */
	private static String buildNumberConversions(List<Parameter> parameters) {
		final StringBuilder builder = new StringBuilder();
		for (final Parameter parameter : parameters) {
			if ((double.class.equals(parameter.getClazz())) || (Double.class.equals(parameter.getClazz())) || (float.class.equals(parameter.getClazz()))
					|| (Float.class.equals(parameter.getClazz()))) {
				builder.append(parameter.getName()).append(" = (").append(parameter.getName()).append(" * 1.0) if ").append(parameter.getName())
						.append(" else ").append(parameter.getName()).append(StringTools.LINE_DELIMITER);
			}
		}

		return builder.toString();
	}

	@Override
	protected String toSafeName(String name) {
		return toSafeNameStatic(name);
	}

	@Override
	protected String createFieldWrapper(IEnvironment environment, String identifier, Field field) {
		final StringBuilder pythonCode = new StringBuilder();

		pythonCode.append("import sys").append(StringTools.LINE_DELIMITER);

		pythonCode.append("if \"py4j\" in sys.modules:").append(StringTools.LINE_DELIMITER);
		pythonCode.append("    ");
		pythonCode.append(field.getName());
		pythonCode.append(" = py4j.java_gateway.get_field(");
		pythonCode.append(identifier);
		pythonCode.append(", \"");
		pythonCode.append(field.getName());
		pythonCode.append("\")");
		pythonCode.append(StringTools.LINE_DELIMITER);

		pythonCode.append("else:").append(StringTools.LINE_DELIMITER);
		pythonCode.append("    ");
		pythonCode.append(field.getName());
		pythonCode.append(" = ");
		pythonCode.append(identifier);
		pythonCode.append(".");
		pythonCode.append(field.getName());
		pythonCode.append(StringTools.LINE_DELIMITER);

		return pythonCode.toString();
	}

	@Override
	public String createFunctionWrapper(IEnvironment environment, final String moduleVariable, final Method method) {

		final StringBuilder pythonCode = new StringBuilder();

		// parse parameters
		final List<Parameter> parameters = ModuleHelper.getParameters(method);

		// build parameter string
		final StringBuilder methodSignature = new StringBuilder();
		final StringBuilder methodCall = new StringBuilder();

		for (final Parameter parameter : parameters) {
			methodSignature.append(", ").append(toSafeName(parameter.getName()));
			methodCall.append(", ").append(toSafeName(parameter.getName()));
			if (parameter.isOptional())
				methodSignature.append(" = ").append(getDefaultValue(parameter));
		}

		if (methodSignature.length() > 2) {
			methodSignature.delete(0, 2);
			methodCall.delete(0, 2);
		}

		final String body = buildMethodBody(parameters, method, moduleVariable, environment);

		// build function declarations
		for (final String name : getMethodNames(method)) {
			if (!isValidMethodName(name)) {
				Logger.error(Activator.PLUGIN_ID,
						"The method name \"" + name + "\" from the module \"" + moduleVariable + "\" can not be wrapped because it's name is reserved");

			} else if (!name.isEmpty()) {
				pythonCode.append("def ").append(name).append('(').append(methodSignature).append("):\n");
				pythonCode.append(indent(body, "    "));
				pythonCode.append('\n');
				pythonCode.append(name).append(".__ease__ = True\n");
				pythonCode.append('\n');
			}
		}

		return pythonCode.toString();
	}

	private String buildMethodBody(List<Parameter> parameters, Method method, String classIdentifier, IEnvironment environment) {
		final StringBuilder body = new StringBuilder();

		final String methodId = ((EnvironmentModule) environment).registerMethod(method);

		// insert deprecation warnings
		if (ModuleHelper.isDeprecated(method))
			body.append("printError(\"" + method.getName() + "() is deprecated. Consider updating your code.\", True)").append(StringTools.LINE_DELIMITER);

		// Convert Lists to Java arrays
		body.append(buildArrayConversions(parameters)).append(StringTools.LINE_DELIMITER);

		// convert numbers to double/float where needed
		body.append(buildNumberConversions(parameters)).append(StringTools.LINE_DELIMITER);

		// check for callbacks
		body.append("if not ").append(EnvironmentModule.getWrappedVariableName(environment)).append(".hasMethodCallback(\"").append(methodId).append("\"):")
				.append(StringTools.LINE_DELIMITER);

		// simple execution
		body.append("    return ").append(classIdentifier).append(".").append(method.getName()).append("(").append(buildParameterList(parameters)).append(")")
				.append(StringTools.LINE_DELIMITER);
		body.append(StringTools.LINE_DELIMITER);

		// execution with callbacks
		body.append("else:").append(StringTools.LINE_DELIMITER);
		if (environment.getScriptEngine().getDescription().getID().startsWith("org.eclipse.ease.lang.python.py4j")) {
			// special handling for Py4J as it cannot use java varargs parameters directly
			body.append("    ").append("parameters_array = gateway.new_array(gateway.jvm.Object, ").append(parameters.size()).append(")")
					.append(StringTools.LINE_DELIMITER);
			for (int index = 0; index < parameters.size(); index++) {
				// Py4J cannot execute
				// gateway.new_array(gateway.jvm.Object, 1)[0] = None
				// as the default value for array elements is null anyway, we simply do not set those
				body.append("    if ").append(toSafeName(parameters.get(index).getName())).append(" != None:").append(StringTools.LINE_DELIMITER);
				body.append("        ").append("parameters_array[").append(index).append("] = ").append(toSafeName(parameters.get(index).getName()))
						.append(StringTools.LINE_DELIMITER);
			}

			body.append("    ").append(EnvironmentModule.getWrappedVariableName(environment)).append(".preMethodCallback(\"").append(methodId)
					.append("\", parameters_array)").append(StringTools.LINE_DELIMITER);

		} else {
			body.append("    ").append(EnvironmentModule.getWrappedVariableName(environment)).append(".preMethodCallback(\"").append(methodId).append("\"")
					.append(parameters.isEmpty() ? "" : ", ").append(buildParameterList(parameters)).append(")").append(StringTools.LINE_DELIMITER);
		}

		body.append("    ").append(RESULT_NAME).append(" = ").append(classIdentifier).append(".").append(method.getName()).append("(")
				.append(buildParameterList(parameters)).append(")").append(StringTools.LINE_DELIMITER);
		body.append("    ").append(EnvironmentModule.getWrappedVariableName(environment)).append(".postMethodCallback(\"").append(methodId).append("\"")
				.append(", ").append(RESULT_NAME).append(")").append(StringTools.LINE_DELIMITER);
		body.append("    return ").append(RESULT_NAME).append(StringTools.LINE_DELIMITER);

		return body.toString();
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

	/**
	 * Create wrapper code for Pep302 import statements.
	 *
	 * @param environment
	 *            script environment instance
	 * @param instance
	 *            instance to wrap
	 * @param identifier
	 *            instance identifier to be used
	 * @return wrapper code to be loaded by python
	 */
	public String createPep302WrapperCode(EnvironmentModule environment, Object instance, String identifier) {
		return createWrapper(environment, instance, identifier, false, environment.getScriptEngine());
	}
}
