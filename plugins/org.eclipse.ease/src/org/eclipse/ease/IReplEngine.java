/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
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

import org.eclipse.ease.debugging.model.EaseDebugVariable;

public interface IReplEngine extends IScriptEngine {

	/**
	 * Set a marker that the interpreter should terminate instead entering IDLE mode. If set, the interpreter will execute all pending requests and terminate
	 * afterwards.
	 *
	 * @param terminate
	 *            <code>true</code> to request termination
	 */
	void setTerminateOnIdle(boolean terminate);

	/**
	 * Get termination condition when engine is idle.
	 *
	 * @return <code>true</code> when engine is terminated when idle
	 */
	boolean getTerminateOnIdle();

	/**
	 * Get variables defined on the top level scope of the script engine.
	 *
	 * @return defined variables
	 */
	Collection<EaseDebugVariable> getDefinedVariables();

	/**
	 * Get type information on a given script object.
	 *
	 * @param object
	 *            object to inspect
	 * @return object type
	 */
	ScriptObjectType getType(Object object);

	/**
	 * Get the String representation of a script object. A script object can be an execution result or a variable content.
	 *
	 * @param object
	 *            script object
	 * @return String representation
	 */
	String toString(Object object);

	/**
	 * Get the result of the last script execution.
	 *
	 * @return script result of last execution
	 */
	EaseDebugVariable getLastExecutionResult();
}
