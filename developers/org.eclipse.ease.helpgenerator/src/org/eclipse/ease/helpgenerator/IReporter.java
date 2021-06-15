/*******************************************************************************
 * Copyright (c) 2019 Christian Pontesegger and others.
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

package org.eclipse.ease.helpgenerator;

public interface IReporter {

	static int INFO = 0;
	static int WARNING = 1;
	static int ERROR = 2;

	/**
	 * Report a message.
	 *
	 * @param status
	 *            one of {@link #INFO}, {@link #WARNING}, {@link #ERROR}
	 * @param message
	 *            message to be reported
	 */
	void report(int status, String message);

	void reportMissingDocs(String message);

	void reportInvalidHtml(String message);

	/**
	 * @return
	 */
	boolean hasErrors();
}
