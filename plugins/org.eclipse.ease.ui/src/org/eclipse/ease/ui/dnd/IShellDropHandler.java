/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.dnd;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.swt.dnd.DND;

/**
 * Handler interface for custom drop events for the script shell view.
 */
public interface IShellDropHandler {

	/**
	 * Verify that object can be handled.
	 *
	 * @param scriptEngine
	 *            script engine to execute drop action
	 * @param element
	 *            element to be dropped
	 * @return <code>true</code> when element can be handled
	 */
	boolean accepts(IScriptEngine scriptEngine, Object element);

	/**
	 * Perform the drop action.
	 *
	 * @param scriptEngine
	 *            script engine to execute drop action
	 * @param element
	 *            element to be dropped
	 */
	void performDrop(IScriptEngine scriptEngine, Object element);

	/**
	 * Perform the drop action.
	 *
	 * @param scriptEngine
	 *            script engine to execute drop action
	 * @param element
	 *            element to be dropped
	 * @param detail
	 *            the operation being performed, see {@link DND#DROP_NONE}, {@link DND#DROP_MOVE},
	 *            {@link DND#DROP_COPY},{@link DND#DROP_LINK},{@link DND#DROP_DEFAULT}
	 */
	default void performDrop(IScriptEngine scriptEngine, Object element, int detail) {
		performDrop(scriptEngine, element);
	}
}
