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
package org.eclipse.ease.lang.javascript.rhino;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.ease.ScriptEngineCancellationException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

public class ObservingContextFactory extends ContextFactory {

	private final Set<Context> fTerminationRequests = Collections.synchronizedSet(new HashSet<>());

	@Override
	protected void observeInstructionCount(final Context cx, final int instructionCount) {
		if (fTerminationRequests.remove(cx))
			throw new ScriptEngineCancellationException();

		super.observeInstructionCount(cx, instructionCount);
	}

	public void terminate(final Context context) {
		fTerminationRequests.add(context);
	}

	public void cancelTerminate(final Context context) {
		fTerminationRequests.remove(context);
	}
}
