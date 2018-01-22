/*******************************************************************************
 * Copyright (c) 2018 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.modules;

import java.lang.reflect.Method;

/**
 * Interface for script function callbacks. Such callbacks can be registered for any method and allow to execute arbitrary code before and/or after a method
 * invocation from a module.
 */
public interface IModuleCallbackProvider {
	/**
	 * Check if the provider has a pre execution callback for the given method.
	 *
	 * @param method
	 *            method to look up
	 * @return <code>true</code> if a pre execution callback is provided
	 */
	boolean hasPreExecutionCallback(Method method);

	/**
	 * Check if the provider has a post execution callback for the given method.
	 *
	 * @param method
	 *            method to look up
	 * @return <code>true</code> if a post execution callback is provided
	 */
	boolean hasPostExecutionCallback(Method method);

	/**
	 * Actual callback before the method gets executed.
	 *
	 * @param method
	 *            method that triggers callback
	 * @param parameters
	 *            method call parameters
	 */
	void preExecutionCallback(Method method, Object[] parameters);

	/**
	 * Actual callback after the method got executed.
	 *
	 * @param method
	 *            method that triggers callback
	 */
	void postExecutionCallback(Method method, Object result);
}
