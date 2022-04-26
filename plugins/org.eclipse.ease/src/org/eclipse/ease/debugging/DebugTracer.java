/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
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

package org.eclipse.ease.debugging;

import org.eclipse.core.runtime.Platform;

public final class DebugTracer {

	private static final boolean USE_DEBUG = org.eclipse.ease.Activator.getDefault().isDebugging()
			&& "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.ease/debug/debuggerEvents"));

	private static int fMinOriginLength = 10;

	public static void debug(String origin, String message) {
		if (USE_DEBUG) {
			while (origin.length() < fMinOriginLength)
				origin = origin + " ";

			System.out.println("::: " + origin + ": " + message);
		}
	}

	@Deprecated
	private DebugTracer() {
		throw new UnsupportedOperationException("Constructor of utility class shall not be called.");
	}
}
