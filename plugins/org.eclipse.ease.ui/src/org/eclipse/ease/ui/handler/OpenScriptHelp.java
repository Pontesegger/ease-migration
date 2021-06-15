/*******************************************************************************
 * Copyright (c) 2015 Vidura Mudalige and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License_Identifier: EPL-2.0
 *
 * Contributors:
 *     Vidura Mudalige - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.PlatformUI;

public class OpenScriptHelp extends AbstractHandler implements IHandler {

	public static final String SCRIPT_GUIDE_URI = "/org.eclipse.ease.help/help/html/scripting_guide.html";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(SCRIPT_GUIDE_URI);
		return null;
	}

}
