/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease;

import java.util.Collection;

import org.eclipse.ease.debugging.model.EaseDebugVariable;

public interface IReplEngine extends IScriptEngine {
	/**
	 * Returns the execution state of the engine. If the engine is processing code or is terminated this will return <code>false</code>. If the engine is
	 * waiting for further scripts to execute this will return <code>true</code>.
	 *
	 * @return execution state.
	 */
	boolean isIdle();

	/**
	 * Set a marker that the interpreter should terminate instead entering IDLE mode. If set, the interpreter will execute all pending requests and terminate
	 * afterwards.
	 *
	 * @param terminate
	 *            <code>true</code> to request termination
	 */
	void setTerminateOnIdle(final boolean terminate);

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

}
