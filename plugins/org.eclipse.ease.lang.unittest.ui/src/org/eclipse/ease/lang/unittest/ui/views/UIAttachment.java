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
package org.eclipse.ease.lang.unittest.ui.views;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.IScriptEngineLaunchExtension;
import org.eclipse.ease.Logger;
import org.eclipse.ease.lang.unittest.TestSuiteScriptEngine;
import org.eclipse.ease.lang.unittest.ui.Activator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class UIAttachment implements IScriptEngineLaunchExtension {

	@Override
	public void createEngine(IScriptEngine engine) {

		if (engine instanceof TestSuiteScriptEngine) {

			try {
				Display.getDefault().syncExec(() -> {
					// try to open script unittest view
					try {
						final IViewPart unittestView = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(UnitTestView.VIEW_ID, null,
								IWorkbenchPage.VIEW_ACTIVATE);

						if (unittestView instanceof UnitTestView)
							((UnitTestView) unittestView).notifyEngineCreation((TestSuiteScriptEngine) engine);

					} catch (final PartInitException e) {
						// giving up
						Logger.error(Activator.PLUGIN_ID, "could not locate <Script Unittest> view");
					}
				});
			} catch (final IllegalStateException e) {
				// running in headless mode
			}
		}
	}
}
