/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License_Identifier: EPL-2.0
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

		case DebugEvent.BREAKPOINT:
			return "BREAKPOINT";

		default:
			return "<unknown>:" + type;
		}
	}

	/** See {@link DebugEvent}. */
	private final int fType;
	private final Object fThread;

	/**
	 * Constructor.
	 *
	 * @param type
	 *            one of {@link DebugEvent#CLIENT_REQUEST}, {@link DebugEvent#STEP_INTO}, {@link DebugEvent#STEP_OVER}, {@link DebugEvent#STEP_RETURN}
	 * @param thread
	 *            thread to resume
	 */
	public ResumeRequest(final int type, final Object thread) {
		fType = type;
		fThread = thread;
	}

	public int getType() {
		return fType;
	}

	@Override
	public Object getThread() {
		return fThread;
	}

	@Override
	public String toString() {
		return super.toString() + " (" + getTypeName(fType) + ")";
	}
}
