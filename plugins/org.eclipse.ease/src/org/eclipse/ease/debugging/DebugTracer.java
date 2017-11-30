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

package org.eclipse.ease.debugging;

import org.eclipse.core.runtime.Platform;

public class DebugTracer {

	private static final boolean DEBUG = org.eclipse.ease.Activator.getDefault().isDebugging()
			&& "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.ease/debug/debuggerEvents"));

	private static int fMinOriginLength = 10;

	public static void debug(String origin, String message) {
		if (DEBUG) {
			while (origin.length() < fMinOriginLength)
				origin = origin + " ";

			System.out.println("::: " + origin + ": " + message);
		}
	}
}
