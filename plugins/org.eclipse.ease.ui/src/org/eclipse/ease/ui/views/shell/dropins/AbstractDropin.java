/*******************************************************************************
 * Copyright (c) 2021 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.views.shell.dropins;

import java.time.Duration;

import org.eclipse.ease.IExecutionListener;
import org.eclipse.ease.IReplEngine;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.IScriptEngineProvider;
import org.eclipse.ease.Script;
import org.eclipse.jface.util.Throttler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPartSite;

public abstract class AbstractDropin implements IShellDropin, IExecutionListener, IScriptEngineProvider {

	public static final ISelectionProvider EMPTY_SELECTION_PROVIDER = new ISelectionProvider() {
		@Override
		public void addSelectionChangedListener(ISelectionChangedListener listener) {
		}

		@Override
		public ISelection getSelection() {
			return StructuredSelection.EMPTY;
		}

		@Override
		public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		}

		@Override
		public void setSelection(ISelection selection) {
		}
	};

	private IReplEngine fEngine;

	private final Throttler fUiUpdater = new Throttler(Display.getDefault(), Duration.ofMillis(500), this::update);

	private boolean fIsActive = false;

	private boolean fGloballyHidden = false;

	private Composite fComposite = null;

	@Override
	public void setScriptEngine(IReplEngine engine) {
		if (fEngine != null)
			fEngine.removeExecutionListener(this);

		fEngine = engine;

		if (fEngine != null)
			fEngine.addExecutionListener(this);
	}

	@Override
	public IScriptEngine getScriptEngine() {
		return fEngine;
	}

	@Override
	public Composite getPartControl(final IWorkbenchPartSite site, final Composite parent) {
		if (fComposite == null) {

			fComposite = createComposite(site, parent);

			fIsActive = true;

			fComposite.addListener(SWT.Hide, event -> fIsActive = false);
			fComposite.addListener(SWT.Show, event -> {
				fIsActive = true;
				update();
			});
		}

		return fComposite;
	}

	@Override
	public void notify(IScriptEngine engine, Script script, int status) {
		switch (status) {
		case IExecutionListener.SCRIPT_END:
			fUiUpdater.throttledExec();
			break;

		case IExecutionListener.ENGINE_END:
			engine.removeExecutionListener(this);
			break;

		default:
			// nothing to do
			break;
		}
	}

	protected void update() {
		if ((!fGloballyHidden) && (fIsActive))
			updateUI();
	}

	@Override
	public void setHidden(boolean hidden) {
		fGloballyHidden = hidden;
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return EMPTY_SELECTION_PROVIDER;
	}

	protected abstract void updateUI();

	protected abstract Composite createComposite(IWorkbenchPartSite site, Composite parent);
}
