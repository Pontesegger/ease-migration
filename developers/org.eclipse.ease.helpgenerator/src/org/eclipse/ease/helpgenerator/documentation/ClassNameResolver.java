/*******************************************************************************
 * Copyright (c) 2020 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.helpgenerator.documentation;

import java.util.Arrays;
import java.util.Collection;

public class ClassNameResolver implements IClassNameResolver {

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

	private final Collection<String> fImportedClasses;

	public ClassNameResolver(Collection<String> importedClasses) {
		fImportedClasses = importedClasses;
	}

	@Override
	public String resolveClassName(String className) {
		if (className.contains("."))
			return className;

		if (CLASSES_IN_JAVA_LANG.contains(className))
			return "java.lang." + className;

		for (final String importedClassName : fImportedClasses) {
			if (importedClassName.endsWith("." + className))
				return importedClassName;
		}

		return className;
	}
}
