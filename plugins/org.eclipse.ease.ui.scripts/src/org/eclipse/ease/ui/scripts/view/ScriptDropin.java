/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.scripts.view;

import org.eclipse.ease.IReplEngine;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.IScriptEngineProvider;
import org.eclipse.ease.ui.scripts.Messages;
import org.eclipse.ease.ui.scripts.ui.ScriptComposite;
import org.eclipse.ease.ui.views.shell.dropins.IShellDropin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPartSite;

public class ScriptDropin implements IShellDropin, IScriptEngineProvider {

	private IReplEngine fEngine;
	private ScriptComposite fComposite;

	@Override
	public void setScriptEngine(IReplEngine engine) {
		fEngine = engine;

		if (fComposite != null)
			fComposite.setEngine(fEngine.getDescription().getID());
	}

	@Override
	public Composite createPartControl(final IWorkbenchPartSite site, final Composite parent) {
		fComposite = new ScriptComposite(this, site, parent, SWT.NONE);

		if (fEngine != null)
			fComposite.setEngine(fEngine.getDescription().getID());

		return fComposite;
	}

	@Override
	public String getTitle() {
		return Messages.ScriptDropin_scripts;
	}

	@Override
	public IScriptEngine getScriptEngine() {
		return fEngine;
	}

	@Override
	public void setHidden(boolean hidden) {
		// nothing to do
	}
}
