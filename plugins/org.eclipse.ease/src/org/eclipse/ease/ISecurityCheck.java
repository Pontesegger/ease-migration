/*******************************************************************************
 * Copyright (c) 2016 Christian Pontesegger and others.
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
 * Interface used for security callbacks. Callbacks can be registered to append additional checks for certain script engine actions.
 */
public interface ISecurityCheck {

	public enum ActionType {
		INJECT_CODE // called before code fragments get executed
	}

	/**
	 * Verification to be performed before a certain engine action is executed.
	 * <p>
	 * For {@link ActionType#INJECT_CODE} following parameters are provided: data[0] ... {@link Script} instance data[1] ... boolean indicator if script should
	 * be run in the UI thread
	 * </p>
	 * When <code>false</code> is returned the action will be silently skipped if possible. On {@link SecurityException}s the exception message will be provided
	 * as user feedback.
	 *
	 * @param action
	 *            type of action to be performed
	 * @param data
	 *            optional data assigned to that action
	 * @return <code>true</code> when action can be performed
	 * @throws SecurityException
	 *             to provide a dedicated error message to the user
	 */
	boolean doIt(ActionType action, Object... data) throws SecurityException;
}
