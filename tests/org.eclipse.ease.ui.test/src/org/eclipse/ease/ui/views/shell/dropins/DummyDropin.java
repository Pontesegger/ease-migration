/*******************************************************************************
 * Copyright (c) 2022 Christian Pontesegger and others.
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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPartSite;

public class DummyDropin extends AbstractDropin {

	@Override
	public String getTitle() {
		return "Dummy";
	}

	@Override
	protected void updateUI() {
		// nothing to do
	}

	@Override
	protected Composite createComposite(IWorkbenchPartSite site, Composite parent) {
		return null;
	}
}
