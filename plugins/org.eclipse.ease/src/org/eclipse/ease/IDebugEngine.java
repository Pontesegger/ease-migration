/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease;

import java.util.Collection;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.ease.debugging.ScriptStackTrace;
import org.eclipse.ease.debugging.model.EaseDebugVariable;

/**
 * Interface to be implemented by a script debug engine.
 */
public interface IDebugEngine extends IScriptEngine {

	/**
	 * Get the current stack trace. A trace is a stack starting with the root file executed by the engine. Function calls and files (called via include command)
	 * will be put on top of that stack. Each entry may contain a pointer to the current line number executed. Traces might be created dynamically on demand or
	 * accumulated during execution depending on the underlying engine.
	 *
	 * @return current stack trace
	 */
	ScriptStackTrace getStackTrace();

	/**
	 * Get the stack trace of the last thrown exception of the current thread. Will be populated when an exception is thrown by the script engine. Can be
	 * evaluated in a try/catch statement within the script. Only available during script runtime.
	 *
	 * @return last exception stack trace or <code>null</code>
	 */
	ScriptStackTrace getExceptionStackTrace();

	/**
	 * Get the stack trace of the last thrown exception of the given thread. Will be populated when an exception is thrown by the script engine. Can be
	 * evaluated in a try/catch statement within the script. Only available during script runtime.
	 *
	 * @param thread
	 *            thread to get last exception stacktrace from
	 * @return last exception stack trace or <code>null</code>
	 */
	ScriptStackTrace getExceptionStackTrace(Object thread);

	void setupDebugger(ILaunch launch, boolean suspendOnStartup, boolean suspendOnScriptLoad, boolean showDynamicCode);

	/**
	 * Remove a variable from the scope.
	 *
	 * @param name
	 *            variable to be removed.
	 * @return
	 */
	Object removeVariable(final String name);

	/**
	 * Get variables within a specific scope or child elements for a given object
	 *
	 * @param scope
	 *            scope or parent object
	 * @return variables assigned to scope
	 */
	Collection<EaseDebugVariable> getVariables(Object scope);
}
