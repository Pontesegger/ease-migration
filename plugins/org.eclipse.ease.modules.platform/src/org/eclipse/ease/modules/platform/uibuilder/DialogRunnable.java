/*******************************************************************************
 * Copyright (c) 2019 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.modules.platform.uibuilder;

import org.eclipse.swt.widgets.Composite;

public abstract class DialogRunnable implements Runnable {

	private Composite fComposite;

	private ScriptableDialog fDialog;

	public void setDialog(ScriptableDialog dialog) {
		fDialog = dialog;
	}

	public ScriptableDialog getDialog() {
		return fDialog;
	}

	public void setComposite(Composite composite) {
		fComposite = composite;
	}

	public Composite getComposite() {
		return fComposite;
	}
}
