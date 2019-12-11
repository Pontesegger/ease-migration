/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.helpgenerator;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.ease.helpgenerator.model.AbstractClassModel;

/**
 * Collects registered packages and converts classes & API links to http anchors.
 */
public class LinkProvider {

	public static enum JavaDocApi {
		JAVA_8, JAVA_10
	};

	/**
	 * Pattern to detect a link token: group(1) ... link or module identifier group(2) ... the link target
	 */
	private static final Pattern PATTERN_LINK = Pattern.compile("\\{@(link|module)\\s+(.*?)\\}", Pattern.DOTALL);

	/**
	 * Pattern to parse a link: group(1) ... FQN of class group(2) ... method name without method signature group(3) ... method parameters
	 */
	private static final Pattern PATTERN_INNER_LINK = Pattern.compile("(\\w+(?:\\.\\w+)*)?(?:#(\\w+)(?:(\\(.*?\\)))?)?");

	private static final Collection<String> CLASSES_IN_JAVA_LANG = Arrays.asList("Appendable", "AutoCloseable", "CharSequence", "Cloneable", "Comparable",
			"Iterable", "Readable", "Runnable", "Thread.UncaughtExceptionHandler", "Boolean", "Byte", "Character", "Character.Subset", "Character.UnicodeBlock",
			"Class", "ClassLoader", "ClassValue", "Compiler", "Double", "Enum", "Float", "InheritableThreadLocal", "Integer", "Long", "Math", "Number",
			"Object", "Package", "Process", "ProcessBuilder", "ProcessBuilder.Redirect", "Runtime", "RuntimePermission", "SecurityManager", "Short",
			"StackTraceElement", "StrictMath", "String", "StringBuffer", "StringBuilder", "System", "Thread", "ThreadGroup", "ThreadLocal", "Throwable", "Void",
			"Character.UnicodeScript", "ProcessBuilder.Redirect.Type", "Thread.State", "ArithmeticException", "ArrayIndexOutOfBoundsException",
			"ArrayStoreException", "ClassCastException", "ClassNotFoundException", "CloneNotSupportedException", "EnumConstantNotPresentException", "Exception",
			"IllegalAccessException", "IllegalArgumentException", "IllegalMonitorStateException", "IllegalStateException", "IllegalThreadStateException",
			"IndexOutOfBoundsException", "InstantiationException", "InterruptedException", "NegativeArraySizeException", "NoSuchFieldException",
			"NoSuchMethodException", "NullPointerException", "NumberFormatException", "ReflectiveOperationException", "RuntimeException", "SecurityException",
			"StringIndexOutOfBoundsException", "TypeNotPresentException", "UnsupportedOperationException", "AbstractMethodError", "AssertionError",
			"BootstrapMethodError", "ClassCircularityError", "ClassFormatError", "Error", "ExceptionInInitializerError", "IllegalAccessError",
			"IncompatibleClassChangeError", "InstantiationError", "InternalError", "LinkageError", "NoClassDefFoundError", "NoSuchFieldError",
			"NoSuchMethodError", "OutOfMemoryError", "StackOverflowError", "ThreadDeath", "UnknownError", "UnsatisfiedLinkError",
			"UnsupportedClassVersionError", "VerifyError", "VirtualMachineError", "Deprecated", "FunctionalInterface", "Override", "SafeVarargs",
			"SuppressWarnings");

	private static String removeGenerics(String text) {
		String result = text;
		while (result.contains("<")) {
			final int start = result.lastIndexOf('<');
			final int end = result.indexOf('>', start);

			if (end > start)
				result = result.substring(0, start) + result.substring(end + 1);
			else
				return result;
		}

		return result;
	}

	private static String getPackageName(String qualifiedName) {
		return (qualifiedName.contains(".")) ? qualifiedName.substring(0, qualifiedName.lastIndexOf('.')) : "";
	}

	private static String getSimpleClassName(String qualifiedName) {
		return (qualifiedName.contains(".")) ? qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1) : qualifiedName;
	}

	private static String getPluginIdFromModuleId(String moduleId) {
		return moduleId.substring(0, moduleId.lastIndexOf('.'));
	}

	private static String getModuleNameFromModuleId(String moduleId) {
		return moduleId.substring(moduleId.lastIndexOf('.') + 1);
	}

	/** Maps (URL to use) -> Collection of package names. */
	private final Map<String, Collection<String>> fExternalDocs = new HashMap<>();

	private AbstractClassModel fClassModel;

	private JavaDocApi fApiIdentifier = JavaDocApi.JAVA_8;

	public void registerAddress(final String location, final Collection<String> packages) {
		fExternalDocs.put(location, packages);
	}

	public void setClassModel(AbstractClassModel classModel) {
		fClassModel = classModel;
	}

	public String insertLinks(final String text) {

		if (text == null)
			return null;

		final StringBuilder output = new StringBuilder();
		int startPos = 0;
		final Matcher matcher = PATTERN_LINK.matcher(text);

		while (matcher.find()) {
			output.append(text.substring(startPos, matcher.start()));
			startPos = matcher.end();

			// remove generics
			String linkTarget = matcher.group(2).replace('\r', ' ').replace('\n', ' ');
			linkTarget = removeGenerics(linkTarget);

			final Matcher linkMatcher = PATTERN_INNER_LINK.matcher(linkTarget);
			if (linkMatcher.matches()) {
				// group 1 = class
				// group 2 = method name
				// group 3 = params (with parenthesis)

				final String fullClassName = resolveClassName(linkMatcher.group(1));
				final String methodName = linkMatcher.group(2);
				String methodParameters = linkMatcher.group(3);
				if (methodParameters == null)
					methodParameters = "()";

				if ("link".equals(matcher.group(1))) {
					// link to java API

					final String link = createMethodLink(methodName, methodParameters);

					if (fullClassName == null) {
						// link to same document
						output.append("<a href='" + link + "'>" + methodName + methodParameters + "</a>");

					} else {
						// external document
						final String classURL = findClassURL(fullClassName);
						if (classURL != null) {
							output.append("<a href='" + classURL + link + "'>");
							output.append(getSimpleClassName(fullClassName));

							if (methodName != null) {
								output.append('.');
								output.append(methodName);
								if (methodParameters != null)
									output.append(methodParameters);
							}

							output.append("</a>");

						} else
							output.append(fullClassName);
					}

				} else if ("module".equals(matcher.group(1))) {
					// link to a scripting module
					if (fullClassName == null) {
						// link to same document
						output.append("<a href='#" + methodName + "'>" + methodName + methodParameters + "</a>");
					} else {
						// external document
						final String moduleId = linkMatcher.group(1);
						final String pluginId = getPluginIdFromModuleId(moduleId);
						if (linkMatcher.group(2) != null) {
							final List<String> parameters = extractParameters(methodParameters);
							final String parameterString = parameters.stream().map(LinkProvider::getSimpleClassName).collect(Collectors.joining(", "));

							output.append("<a href='../../" + pluginId + "/help/" + AbstractModuleDoclet.createHTMLFileName(linkMatcher.group(1)) + "#"
									+ methodName + "'>" + getModuleNameFromModuleId(moduleId) + "." + methodName + "(" + parameterString + ")</a>");
						} else
							output.append("<a href='../../" + pluginId + "/help/" + AbstractModuleDoclet.createHTMLFileName(moduleId) + "'>"
									+ getModuleNameFromModuleId(moduleId) + " module</a>");
					}
				}
			}
		}

		if (startPos == 0)
			return text;

		output.append(text.substring(startPos));

		return output.toString();
	}

	/**
	 * Create the link for a javaDoc method documentation.
	 *
	 * @param methodName
	 *            name of method
	 * @param methodParameters
	 *            method parameters
	 * @return link
	 */
	private String createMethodLink(String methodName, String methodParameters) {
		final StringBuilder result = new StringBuilder();

		if (methodName != null) {
			result.append("#");
			result.append(methodName);

			final List<String> parameters = extractParameters(methodParameters);

			switch (getApiIdentifier()) {
			case JAVA_8:
				result.append('-');
				result.append(parameters.stream().map(p -> p.replace("[]", ":A")).collect(Collectors.joining("-")));
				result.append('-');
				break;
			// link += methodParameters.replaceAll(" ", "%20");
			}
		}

		return result.toString();
	}

	/**
	 * Extracts FQ class names for all parameters.
	 *
	 * @param parameterString
	 *            parameter signature including brackets, eg "(int, byte[], Collection&lt;String&gt; data)"
	 * @return list of FQ class names
	 */
	private List<String> extractParameters(String parameterString) {
		final String[] tokens = parameterString.replaceAll("\\s", "").replaceAll("[()]", "").split(",");

		return Arrays.asList(tokens).stream().map(p -> resolveClassName(p)).collect(Collectors.toList());
	}

	private String resolveClassName(final String candidate) {
		if (candidate == null)
			return null;

		if (candidate.contains("."))
			return candidate;

		// check for classes in java.lang package
		if (CLASSES_IN_JAVA_LANG.contains(candidate))
			return "java.lang." + candidate;

		// check for an import
		for (final String importStatement : fClassModel.getImportedClasses()) {
			if (importStatement.endsWith("." + candidate))
				return importStatement;
		}

		return candidate;
	}

	private String findClassURL(String qualifiedName) {

		final String packageName = getPackageName(qualifiedName);
		if (!packageName.isEmpty()) {
			// first run, look for exact package match
			for (final Entry<String, Collection<String>> entry : fExternalDocs.entrySet()) {
				if (entry.getValue().contains(packageName))
					return entry.getKey() + "/" + qualifiedName.replace('.', '/') + ".html";
			}

			// not found; try to locate matching parent package and hope for the
			// best
			for (final Entry<String, Collection<String>> entry : fExternalDocs.entrySet()) {
				for (final String entryPackage : entry.getValue()) {
					if (packageName.startsWith(entryPackage))
						return entry.getKey() + "/" + qualifiedName.replace('.', '/') + ".html";
				}
			}
		}

		return null;
	}

	public void setApi(JavaDocApi apiIdentifier) {
		fApiIdentifier = apiIdentifier;
	}

	public JavaDocApi getApiIdentifier() {
		return fApiIdentifier;
	}
}
