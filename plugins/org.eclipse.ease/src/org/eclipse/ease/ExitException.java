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

public class ExitException extends BreakException {

	private static final long serialVersionUID = 9134574495641360069L;

	public ExitException() {
	}

	public ExitException(final Object condition) {
		super(condition);
	}
}
