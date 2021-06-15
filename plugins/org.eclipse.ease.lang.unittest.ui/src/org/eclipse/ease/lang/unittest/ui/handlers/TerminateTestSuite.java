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

package org.eclipse.ease.lang.unittest.ui.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ease.lang.unittest.ui.views.UnitTestView;

public class TerminateTestSuite extends AbstractViewToolbarHandler {

	public static final String COMMAND_ID = "org.eclipse.ease.lang.unittest.ui.commands.terminateTestSuite";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final UnitTestView view = getView(event, UnitTestView.class);
		if (view != null)
			view.terminateSuite();

		return null;
	}
}