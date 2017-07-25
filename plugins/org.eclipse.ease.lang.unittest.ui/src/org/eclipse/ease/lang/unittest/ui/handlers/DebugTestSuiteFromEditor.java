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

package org.eclipse.ease.lang.unittest.ui.handlers;

import org.eclipse.debug.core.ILaunchManager;

public class DebugTestSuiteFromEditor extends RunTestSuiteFromEditor {

	@Override
	protected String getLaunchMode() {
		return ILaunchManager.DEBUG_MODE;
	}
}
