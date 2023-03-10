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

import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.preferences.IPreferenceConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.console.IConsole;

/**
 * Abstract action for toggling preference to automatically show the console when a streams content changes.
 * 
 * @since 3.3
 */
public abstract class ShowWhenContentChangesAction extends Action implements IPropertyChangeListener {

	private final String mConsoleName;

	/**
	 * Constructs an action to toggle console auto activation preferences
	 * 
	 * @param console
	 */
	public ShowWhenContentChangesAction(final String name, final IConsole console) {
		super(name, IAction.AS_CHECK_BOX);
		mConsoleName = console.getName();
		setToolTipText(name);
		getPreferenceStore().addPropertyChangeListener(this);
		update();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		String property = event.getProperty();
		if (property.equals(getPreferenceKey())) {
			update();
		}
	}

	private String getPreferenceKey() {
		return IPreferenceConstants.CONSOLE_BASE + "." + mConsoleName + "." + getKey();
	}

	protected abstract String getKey();

	private void update() {
		IPreferenceStore store = getPreferenceStore();
		if (store.getBoolean(getPreferenceKey())) {
			// on
			setChecked(true);
		} else {
			// off
			setChecked(false);
		}
	}

	/**
	 * @return
	 */
	private IPreferenceStore getPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		IPreferenceStore store = getPreferenceStore();
		boolean show = isChecked();
		store.removePropertyChangeListener(this);
		store.setValue(getPreferenceKey(), show);
		store.addPropertyChangeListener(this);
	}

	/**
	 * Must be called to dispose this action.
	 */
	public void dispose() {
		getPreferenceStore().removePropertyChangeListener(this);
	}
}
