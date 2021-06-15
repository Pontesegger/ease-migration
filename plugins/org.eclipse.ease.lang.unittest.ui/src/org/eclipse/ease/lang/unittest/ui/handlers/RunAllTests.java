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
import org.eclipse.ease.lang.unittest.TestSuiteScriptEngine;
import org.eclipse.ease.lang.unittest.runtime.ITestContainer;
import org.eclipse.ease.lang.unittest.runtime.ITestSuite;
import org.eclipse.ease.lang.unittest.ui.views.UnitTestView;
import org.eclipse.ease.ui.console.ScriptConsole;

public class RunAllTests extends AbstractViewToolbarHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final UnitTestView view = getView(event, UnitTestView.class);
		if (view != null) {

			final Object input = view.getFileTreeViewer().getInput();
			if (input instanceof ITestContainer) {
				final ITestSuite testRoot = (ITestSuite) ((ITestContainer) input).getCopyOfChildren().iterator().next();
				final TestSuiteScriptEngine engine = view.getCurrentEngine();

				view.notifyEngineCreation(engine);

				// prepare fresh console
				final ScriptConsole console = ScriptConsole.create(engine.getName() + ": " + testRoot.getResource(), engine);
				engine.setOutputStream(console.getOutputStream());
				engine.setErrorStream(console.getErrorStream());
				engine.setInputStream(console.getInputStream());

				engine.execute(getTestRoot(testRoot, event));
				engine.schedule();
			}
		}

		return null;
	}

	/**
	 * Get the root object to be executed by the engine.
	 *
	 * @param testSuite
	 *            test suite
	 * @param event
	 *            event that triggered this handler
	 * @return object to be executed
	 */
	protected Object getTestRoot(ITestSuite testSuite, ExecutionEvent event) {
		return testSuite;
	}
}
