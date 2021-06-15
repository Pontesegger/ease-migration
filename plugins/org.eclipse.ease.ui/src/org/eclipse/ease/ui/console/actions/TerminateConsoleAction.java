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
package org.eclipse.ease.ui.console.actions;

import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.ease.IReplEngine;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.ui.console.ScriptConsole;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * ConsoleTerminateAction
 */
public class TerminateConsoleAction extends Action implements IUpdate {

	private ScriptConsole fConsole;

	/**
	 * Creates a terminate action for the console
	 *
	 * @param window
	 *            the window
	 * @param console
	 *            the console
	 */
	public TerminateConsoleAction(final IWorkbenchWindow window, final ScriptConsole console) {
		super(ConsoleMessages.ConsoleTerminateAction_0);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.CONSOLE_TERMINATE_ACTION);
		fConsole = console;
		setToolTipText(ConsoleMessages.ConsoleTerminateAction_1);
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_LCL_TERMINATE));
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_TERMINATE));
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_LCL_TERMINATE));
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.CONSOLE_TERMINATE_ACTION);
		update();
	}

	@Override
	public void update() {
		final IScriptEngine engine = fConsole.getScriptEngine();
		setEnabled(engine != null);
	}

	@Override
	public void run() {
		final IScriptEngine engine = fConsole.getScriptEngine();
		if (engine != null) {

			if ((engine instanceof IReplEngine) && (((IReplEngine) engine).getTerminateOnIdle()))
				// terminate completely
				engine.terminate();

			else
				// terminate current piece of code
				engine.terminateCurrent();
		}
	}

	public void dispose() {
		fConsole = null;
	}
}
