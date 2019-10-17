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
package org.eclipse.ease.lang.unittest.ui.views;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.ease.tools.ListenerList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;

public class MultiSelectionProvider implements ISelectionProvider, ISelectionChangedListener, FocusListener {

	private final ListenerList<ISelectionChangedListener> fListeners = new ListenerList<>();

	private final Collection<ISelectionProvider> fBaseProviders = new HashSet<>();

	private ISelectionProvider fCurrentProvider;

	public void addSelectionProvider(ISelectionProvider provider) {
		fBaseProviders.add(provider);
		provider.addSelectionChangedListener(this);

		if (provider instanceof Viewer)
			((Viewer) provider).getControl().addFocusListener(this);

		if (fCurrentProvider == null)
			fCurrentProvider = provider;
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		fListeners.add(listener);
	}

	@Override
	public ISelection getSelection() {
		return fCurrentProvider.getSelection();
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		fListeners.remove(listener);
	}

	@Override
	public void setSelection(ISelection selection) {
		fCurrentProvider.setSelection(selection);
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		fCurrentProvider = event.getSelectionProvider();

		// promote event
		for (final ISelectionChangedListener listener : fListeners)
			listener.selectionChanged(event);
	}

	@Override
	public void focusGained(FocusEvent e) {
		final Object source = e.getSource();

		// find matching base provider
		for (final ISelectionProvider provider : fBaseProviders) {
			if (provider instanceof Viewer) {
				if (((Viewer) provider).getControl().equals(source)) {
					selectionChanged(new SelectionChangedEvent(provider, provider.getSelection()));
					// only one selection is possible
					break;
				}
			}
		}
	}

	@Override
	public void focusLost(FocusEvent e) {
		// nothing to do
	}
}
