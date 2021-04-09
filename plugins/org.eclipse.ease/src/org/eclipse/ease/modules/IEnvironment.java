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
package org.eclipse.ease.modules;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ease.IScriptEngine;

public interface IEnvironment extends IScriptModule {

	String EASE_CODE_PREFIX = "__EASE_";
	String MODULE_PREFIX = EASE_CODE_PREFIX + "MOD_";

	static IEnvironment getEnvironment(final IScriptEngine engine) {
		for (final Object variable : engine.getVariables().values()) {
			if (variable instanceof IEnvironment)
				return (IEnvironment) variable;
		}

		return null;
	}

	static IEnvironment getEnvironment() {
		final Job currentJob = Job.getJobManager().currentJob();
		if (currentJob instanceof IScriptEngine)
			return getEnvironment((IScriptEngine) currentJob);

		return null;
	}

	IScriptEngine getScriptEngine();

	Object getModule(String name);

	<T extends Object, U extends Class<T>> T getModule(U clazz);

	/**
	 * Retrieve a list of loaded modules. The returned list is read only.
	 *
	 * @return list of modules (might be empty)
	 */
	List<Object> getModules();

	/**
	 * Print to standard output.
	 *
	 * @param text
	 *            message to write
	 * @param lineFeed
	 *            <code>true</code> to add a line feed after the text
	 */
	void print(Object text, boolean lineFeed);

	void addModuleListener(IModuleListener listener);

	void removeModuleListener(IModuleListener listener);

	/**
	 * Load a module. Loading a module generally enhances the script environment with new functions and variables. If a module was already loaded before, it
	 * gets refreshed and moved to the top of the module stack. When a module is loaded, all its dependencies are loaded too. So loading one module might change
	 * the whole module stack.
	 * <p>
	 * When not using a custom namespace all variables and functions are loaded to the global namespace, possibly overriding existing functions. In such cases
	 * the Java module instance is returned. When <i>useCustomNamespace</i> is used a dynamic script object is created and returned.In such cases the global
	 * namespace is not changed. The namespace behavior is also used for loading dependencies.
	 * </p>
	 *
	 * @param moduleIdentifier
	 *            name of module to load
	 * @param useCustomNamespace
	 *            set to <code>true</code> if functions and constants should not be stored to the global namespace but to a custom object
	 * @return loaded module instance
	 * @throws ExecutionException
	 *             when execution of wrapped module code fails
	 */
	Object loadModule(String moduleIdentifier, @ScriptParameter(defaultValue = "false") boolean useCustomNamespace) throws ExecutionException;

	/**
	 * Wrap a java instance. Will create accessors in the target language for methods and constants defined by the java instance <i>toBeWrapped</i>. If the
	 * instance contains annotations of type {@link WrapToScript} only these will be wrapped. If no annotation can be found, all public methods/constants will
	 * be wrapped. As some target languages might not support method overloading this might result in some methods not wrapped correctly.
	 *
	 * @param toBeWrapped
	 *            instance to be wrapped
	 * @param useCustomNamespace
	 *            set to <code>true</code> if functions and constants should not be stored to the global namespace but to the return value only
	 * @return wrapped object instance or java class when put to global namespace
	 * @throws ExecutionException
	 *             when execution of wrapped code fails
	 */
	Object wrap(Object toBeWrapped, boolean useCustomNamespace) throws ExecutionException;

	/**
	 * Register a callback provider for module functions.
	 *
	 * @param callbackProvider
	 *            callback provider instance
	 */
	void addModuleCallback(IModuleCallbackProvider callbackProvider);

	/**
	 * Register a method in the environment to allow for callbacks.
	 *
	 * @param method
	 *            method to be registered
	 * @return unique ID identifying the method
	 */
	String registerMethod(Method method);
}
