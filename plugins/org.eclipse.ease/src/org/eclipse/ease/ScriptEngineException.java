/*******************************************************************************
 * Copyright (c) 2016 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Jonah Graham (Kichwa Coders) - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease;

/**
 * A checked exception to indicate a script engine exception.
 */
public class ScriptEngineException extends Exception {

	private static final long serialVersionUID = -4763996831240579435L;

	/**
	 * @see Exception#Exception()
	 */
	public ScriptEngineException() {
	}

	/**
	 * @see Exception#Exception(String)
	 */
	public ScriptEngineException(String message) {
		super(message);
	}

	/**
	 * @see Exception#Exception(Throwable)
	 */
	public ScriptEngineException(Throwable cause) {
		super(cause);
	}

	/**
	 * @see Exception#Exception(String, Throwable)
	 */
	public ScriptEngineException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @see Exception#Exception(String, Throwable, boolean, boolean)
	 */
	protected ScriptEngineException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
