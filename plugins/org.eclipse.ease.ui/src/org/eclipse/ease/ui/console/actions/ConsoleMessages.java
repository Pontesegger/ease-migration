/*******************************************************************************
 *  Copyright (c) 2000, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License_Identifier: EPL-2.0
 * 
 *  Contributors:
 *  IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.console.actions;

import org.eclipse.osgi.util.NLS;

public class ConsoleMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.debug.internal.ui.views.console.ConsoleMessages";//$NON-NLS-1$

	public static String ConsoleRemoveAllTerminatedAction_0;
	public static String ConsoleRemoveAllTerminatedAction_1;

	public static String ConsoleTerminateAction_0;
	public static String ConsoleTerminateAction_1;

	public static String ProcessConsole_0;

	public static String ProcessConsole_1;
	public static String ProcessConsole_2;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, ConsoleMessages.class);
	}

	public static String ConsoleRemoveTerminatedAction_0;

	public static String ConsoleRemoveTerminatedAction_1;

	public static String ShowStandardErrorAction_0;

	public static String ShowStandardOutAction_0;
}
