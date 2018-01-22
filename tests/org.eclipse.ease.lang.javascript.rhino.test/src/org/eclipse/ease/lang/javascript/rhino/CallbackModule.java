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
package org.eclipse.ease.lang.javascript.rhino;

import java.lang.reflect.Method;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.modules.AbstractScriptModule;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.modules.IModuleCallbackProvider;
import org.eclipse.ease.modules.WrapToScript;

/**
 * Test module to verify module callbacks.
 */
public class CallbackModule extends AbstractScriptModule implements IModuleCallbackProvider {

	public int fPreExecutionCount = 0;
	public int fPostExecutionCount = 0;

	public Method fPreExecutionMethod;
	public Object[] fPreExecutionParameters;
	public Method fPostExecutionMethod;
	public Object fPostExecutionResult;

	@Override
	public void initialize(IScriptEngine engine, IEnvironment environment) {
		super.initialize(engine, environment);

		environment.addModuleCallback(this);
	}

	@Override
	public boolean hasPreExecutionCallback(Method method) {
		return "getModule".equals(method.getName());
	}

	@Override
	public boolean hasPostExecutionCallback(Method method) {
		return "getModule".equals(method.getName());
	}

	@Override
	public void preExecutionCallback(Method method, Object[] parameters) {
		fPreExecutionCount++;

		fPreExecutionMethod = method;
		fPreExecutionParameters = parameters;
	}

	@Override
	public void postExecutionCallback(Method method, Object result) {
		fPostExecutionCount++;

		fPostExecutionMethod = method;
		fPostExecutionResult = result;
	}

	/**
	 * Dummy method to avoid errors on missing documentation.
	 */
	@WrapToScript
	public void dummy() {
	}
}
