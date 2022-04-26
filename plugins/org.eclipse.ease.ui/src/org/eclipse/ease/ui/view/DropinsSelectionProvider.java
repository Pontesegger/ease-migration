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

package org.eclipse.ease.ui.view;

import java.util.Collection;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ease.ui.views.shell.dropins.AbstractDropin;
import org.eclipse.ease.ui.views.shell.dropins.IShellDropin;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.TabFolder;

public class DropinsSelectionProvider implements ISelectionProvider, SelectionListener, ISelectionChangedListener {

	private final ListenerList<ISelectionChangedListener> fListeners = new ListenerList<>();
	private final Collection<IShellDropin> fDropins;

	public DropinsSelectionProvider(TabFolder tabFolder, Collection<IShellDropin> dropins) {
		fDropins = dropins;

		for (final IShellDropin dropin : dropins)
			dropin.getSelectionProvider().addSelectionChangedListener(this);

		tabFolder.addSelectionListener(this);
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		fListeners.add(listener);
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		fListeners.remove(listener);
	}

	@Override
	public ISelection getSelection() {
		return getSelectionProvider().getSelection();
	}

	@Override
	public void setSelection(ISelection selection) {
		getSelectionProvider().setSelection(selection);
	}

	private ISelectionProvider getSelectionProvider() {
		for (final IShellDropin dropin : fDropins) {
			if (dropin.getPartControl(null, null).isVisible())
				return dropin.getSelectionProvider();
		}

		return AbstractDropin.EMPTY_SELECTION_PROVIDER;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		selectionChanged(new SelectionChangedEvent(this, getSelection()));
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		for (final ISelectionChangedListener listener : fListeners)
			listener.selectionChanged(event);
	}
}
