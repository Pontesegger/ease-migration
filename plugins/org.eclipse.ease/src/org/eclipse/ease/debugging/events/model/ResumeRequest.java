/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.debugging.events.model;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.ease.debugging.events.AbstractEvent;

public class ResumeRequest extends AbstractEvent implements IModelRequest {

	public static String getTypeName(int type) {
		switch (type) {
		case DebugEvent.CLIENT_REQUEST:
			return "CLIENT_REQUEST";

		case DebugEvent.STEP_INTO:
			return "STEP_INTO";

		case DebugEvent.STEP_OVER:
			return "STEP_OVER";

		case DebugEvent.STEP_RETURN:
			return "STEP_RETURN";

		case DebugEvent.STEP_END:
			return "STEP_END";

		default:
			return "<unknown>";
		}
	}

	/** See {@link DebugEvent}. */
	private final int fType;

	/**
	 * Constructor.
	 *
	 * @param type
	 *            one of {@link DebugEvent#CLIENT_REQUEST}, {@link DebugEvent#STEP_INTO}, {@link DebugEvent#STEP_OVER}, {@link DebugEvent#STEP_RETURN}
	 * @param thread
	 *            thread to resume
	 */
	public ResumeRequest(final int type, final Thread thread) {
		fType = type;
	}

	public int getType() {
		return fType;
	}

	@Override
	public String toString() {
		return super.toString() + " (" + getTypeName(fType) + ")";
	}
}
