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

/**
 * Interface to be implemented by any class interested in getting notified of script execution event.
 */
public interface IExecutionListener {

	/** Event execution ended. */
	int ENGINE_START = 1;

	/** Event execution ended. */
	int ENGINE_END = 2;

	/** Event script execution started. */
	int SCRIPT_START = 3;

	/** Event script execution ended. */
	int SCRIPT_END = 4;

	/** Event injected script (subscript) started. */
	int SCRIPT_INJECTION_START = 5;

	/** Event injected script (subscript) ended. */
	int SCRIPT_INJECTION_END = 6;

	/**
	 * Notifies the listeners when script execution started/ended or when the engine itself is started/ended.
	 *
	 * @param engine
	 *            Script Engine
	 * @param script
	 *            Script or <code>null</code>
	 * @param status
	 *            Event Status
	 */
	void notify(IScriptEngine engine, Script script, int status);
}
