/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
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
package org.eclipse.ease.ui.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ease.Activator;
import org.eclipse.ease.ui.Messages;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.prefs.Preferences;

public class ScriptingPage extends PreferencePage implements IWorkbenchPreferencePage {

	private Button btnAllowUIAccess;
	private Button btnAllowRemoteAccess;

	public ScriptingPage() {
	}

	@Override
	public void init(final IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected Control createContents(final Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));

		Group grpSecurisecurty = new Group(container, SWT.NONE);
		grpSecurisecurty.setLayout(new FillLayout(SWT.VERTICAL));
		grpSecurisecurty.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		grpSecurisecurty.setText(Messages.ScriptingPage_security);

		btnAllowUIAccess = new Button(grpSecurisecurty, SWT.CHECK);
		btnAllowUIAccess.setText(Messages.ScriptingPage_allowUIThread);

		btnAllowRemoteAccess = new Button(grpSecurisecurty, SWT.CHECK);
		btnAllowRemoteAccess.setText(Messages.ScriptingPage_allowRemoteScripts);

		performDefaults();

		return container;
	}

	@Override
	protected void performDefaults() {
		Preferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).node(Activator.PREFERENCES_NODE_SCRIPTS);

		boolean allowUIAccess = prefs.getBoolean(Activator.SCRIPTS_ALLOW_UI_ACCESS, Activator.DEFAULT_SCRIPTS_ALLOW_UI_ACCESS);
		btnAllowUIAccess.setSelection(allowUIAccess);

		boolean allowRemoteAccess = prefs.getBoolean(IPreferenceConstants.SCRIPTS_ALLOW_REMOTE_ACCESS,
				IPreferenceConstants.DEFAULT_SCRIPTS_ALLOW_REMOTE_ACCESS);
		btnAllowRemoteAccess.setSelection(allowRemoteAccess);

		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		Preferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID).node(Activator.PREFERENCES_NODE_SCRIPTS);

		prefs.putBoolean(Activator.SCRIPTS_ALLOW_UI_ACCESS, btnAllowUIAccess.getSelection());
		prefs.putBoolean(IPreferenceConstants.SCRIPTS_ALLOW_REMOTE_ACCESS, btnAllowRemoteAccess.getSelection());

		return super.performOk();
	}
}
