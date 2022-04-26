/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
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
package org.eclipse.ease.ui.views.shell.dropins;

import org.eclipse.ease.IReplEngine;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * Interface for shell drop-ins. A shell drop-in is a composite adding additional features to the script shell view. It resides in a sidebar of the shell and is
 * connected to the current script engine.
 */
public interface IShellDropin {

	/**
	 * Sets the script engine for this drop-in. If the engine is changed during runtime, this method gets called another time. For all other scripting events
	 * this drop-in should register a listener on the script engine.
	 *
	 * @param engine
	 *            script engine used in shell view
	 */
	void setScriptEngine(IReplEngine engine);

	/**
	 * Get the drop-in visual root component. Querying this method multiple times always returns the same object.
	 *
	 * @param site
	 *            workbench part site this drop-in is registered to
	 * @param parent
	 *            parent container to render in
	 * @return composite created within parent container (may not be <code>null</code>)
	 */
	Composite getPartControl(IWorkbenchPartSite site, Composite parent);

	/**
	 * Get this drop-in title. The title is used to populate a tabitem.
	 *
	 * @return drop-in title
	 */
	String getTitle();

	void setHidden(boolean hidden);

	/**
	 * Get the selection provider of this dropin or null.
	 *
	 * @return selection provider or <code>null</code>
	 */
	ISelectionProvider getSelectionProvider();
}
