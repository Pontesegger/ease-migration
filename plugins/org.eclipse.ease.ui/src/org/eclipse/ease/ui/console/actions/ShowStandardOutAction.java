/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License_Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Christian Pontesegger - adaption to EScript project
 *******************************************************************************/
package org.eclipse.ease.ui.console.actions;

import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.preferences.IPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsole;

/**
 * Toggles preference show the console when output is written to standard output stream.
 * 
 * @since 3.3
 */
public class ShowStandardOutAction extends ShowWhenContentChangesAction {

	/**
	 * Constructs an action to toggle console auto activation preferences
	 * 
	 * @param console
	 */
	public ShowStandardOutAction(final IConsole console) {
		super(ConsoleMessages.ShowStandardOutAction_0, console);
		setId(Activator.PLUGIN_ID + ".ShowWhenStdoutChangesAction"); //$NON-NLS-1$
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.SHOW_WHEN_STDOUT_CHANGES_ACTION);
		setImageDescriptor(DebugUITools.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_STANDARD_OUT));
	}

	@Override
	protected String getKey() {
		return IPreferenceConstants.CONSOLE_OPEN_ON_OUT;
	}
}
