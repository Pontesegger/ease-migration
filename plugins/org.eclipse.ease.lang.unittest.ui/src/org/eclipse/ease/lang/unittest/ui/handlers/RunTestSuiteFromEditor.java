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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ease.lang.unittest.ui.editor.TestSuiteEditor;
import org.eclipse.ease.ui.launching.EaseLaunchDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class RunTestSuiteFromEditor extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
		if (activeEditor instanceof TestSuiteEditor) {
			final EaseLaunchDelegate launchDelegate = new EaseLaunchDelegate();
			launchDelegate.launch(HandlerUtil.getActiveEditor(event), getLaunchMode());
		}

		return null;
	}

	protected String getLaunchMode() {
		return ILaunchManager.RUN_MODE;
	}
}
