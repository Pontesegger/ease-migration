/*******************************************************************************
 * Copyright (c) 2013, 2016 Christian Pontesegger and others.
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
package org.eclipse.ease;

import java.lang.reflect.Method;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.modules.ScriptParameter;

/**
 * An ICodeFactory is capable of generating code fragments for a dedicated target language.
 */
public interface ICodeFactory {

	/** intermediate name of original method call return value. */
	String RESULT_NAME = "__result";

	/** Trace enablement for module wrappers. */
	boolean TRACE_MODULE_WRAPPER = Activator.getDefault().isDebugging()
			&& "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.ease/debug/moduleWrapper"));

	/**
	 * Parameter definition class. Holds data to describe a script parameter used in a function call.
	 */
	class Parameter {

		private Class<?> fClazz;
		private String fName = "";
		private boolean fOptional = false;
		private String fDefaultValue = ScriptParameter.NULL;

		public void setClass(final Class<?> clazz) {
			fClazz = clazz;
		}

		public void setName(final String name) {
			fName = name;
		}

		public void setOptional(final boolean optional) {
			fOptional = optional;
		}

		public void setDefault(final String defaultValue) {
			fDefaultValue = defaultValue;
		}

		public String getName() {
			return fName;
		}

		public Class<?> getClazz() {
			return fClazz;
		}

		public String getDefaultValue() {
			return fDefaultValue;
		}

		public boolean isOptional() {
			return fOptional;
		}
	}

	/**
	 * Converts a given string to a save variable name for the target language. Typically filters invalid characters and verifies that the returned string does
	 * not match any reserved keyword. Does not verify if the returned name is already in use.
	 *
	 * @param variableName
	 *            variable name candidate
	 * @return converted variable name
	 */
	String getSaveVariableName(String variableName);

	/**
	 * Create code to instantiate a java class.
	 *
	 * @param clazz
	 *            class to instantiate
	 * @param parameters
	 *            parameters used for class instantiation
	 * @return wrapped script code
	 */
	String classInstantiation(Class<?> clazz, String[] parameters);

	/**
	 * Create code to call a wrapped function.
	 *
	 * @param method
	 *            method to be called
	 * @param parameters
	 *            call parameters
	 * @return script code to call function
	 */
	String createFunctionCall(Method method, Object... parameters);

	/**
	 * Get the default value for a given parameter.
	 *
	 * @param parameter
	 *            parameter to get default value for
	 * @return String representation of default value
	 */
	String getDefaultValue(Parameter parameter);

	/**
	 * Create code for the provided comment. Typically line or block comment tokens will be added around the comment. Start block comment token will be added
	 * immediately before comment and end block comment token will be added immediately after comment. Format comment properly to get proper result.
	 *
	 * @param comment
	 *            the comment
	 * @param blockComment
	 *            <code>true</code> for adding block comment or <code>false</code> for adding (multiple) line comments
	 * @return the comment string with comment tokens.
	 */
	String createCommentedString(String comment, boolean blockComment);

	/**
	 * Create script wrapper code for a given java instance.
	 *
	 * @param environment
	 *            environment module instance
	 * @param instance
	 *            object instance to wrap
	 * @param identifier
	 *            script variable name for wrapped Java object
	 * @param customNamespace
	 *            whether to store methods to the global namespace or to create a custom object
	 * @param engine
	 *            script engine
	 * @return create wrapped script code
	 */
	String createWrapper(IEnvironment environment, Object instance, String identifier, boolean customNamespace, IScriptEngine engine);
}
