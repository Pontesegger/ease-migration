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

package org.eclipse.ease.ui.help.hovers;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.internal.text.html.BrowserInformationControl;
import org.eclipse.jface.internal.text.html.BrowserInformationControlInput;

public interface IHoverContentProvider {

	/**
	 * @param origin
	 *            element that triggered the hover action
	 * @param detail
	 *            detail on the hover action. The content of this parameter depends on the type of the origin object. May be <code>null</code>.
	 * @return html content as string or a {@link BrowserInformationControlInput}
	 */
	Object getContent(Object origin, Object detail);

	/**
	 * Callback allowing to populate the popup toolbar
	 * @param control browser control
	 * @param toolBarManager toolbar being populated
	 */
	void populateToolbar(BrowserInformationControl control, ToolBarManager toolBarManager);
}
