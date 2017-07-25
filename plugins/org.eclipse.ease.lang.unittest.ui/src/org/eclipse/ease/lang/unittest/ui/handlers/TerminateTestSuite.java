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