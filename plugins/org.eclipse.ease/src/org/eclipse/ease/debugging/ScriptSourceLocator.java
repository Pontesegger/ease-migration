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
package org.eclipse.ease.debugging;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.ease.debugging.model.EaseDebugStackFrame;

public class ScriptSourceLocator implements IPersistableSourceLocator {

	@Override
	public Object getSourceElement(final IStackFrame stackFrame) {
		if (stackFrame instanceof EaseDebugStackFrame)
			return ((EaseDebugStackFrame) stackFrame).getScript();

		return null;
	}

	@Override
	public String getMemento() throws CoreException {
		return null;
	}

	@Override
	public void initializeFromMemento(final String memento) throws CoreException {
		// nothing to do
	}

	@Override
	public void initializeDefaults(final ILaunchConfiguration configuration) throws CoreException {
		// nothing to do
	}
}
