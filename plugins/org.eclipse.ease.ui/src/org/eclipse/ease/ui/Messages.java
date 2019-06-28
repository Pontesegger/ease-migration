/*******************************************************************************
 * Copyright (c) 2018 Bachmann electronic GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Bachmann electronic GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui;

import org.eclipse.osgi.util.NLS;

/**
 * @author schreibm
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ease.ui.messages"; //$NON-NLS-1$
	public static String AbstractLaunchDelegate_couldNotLaunch;
	public static String AbstractLaunchDelegate_launchError;
	public static String ModuleExplorerView_serachModules;
	public static String ModuleExplorerView_updateModulesExplorer;
	public static String ModulesPage_dragDrop;
	public static String ModulesPage_hiddenModules;
	public static String ModulesPage_visibleModules;
	public static String ModuleStackDropin_module;
	public static String ModuleStackDropin_moduleStack;
	public static String PasswordDialog_enterPwd;
	public static String PasswordDialog_savePwd;
	public static String PerformAdvancedSignPage_advancedSettings;
	public static String PerformAdvancedSignPage_enterKeystoreInfo;
	public static String PerformAdvancedSignPage_preffered;
	public static String PerformAdvancedSignPage_provideDigestAlgo;
	public static String PerformAdvancedSignPage_providePerformSignature;
	public static String PerformBasicSignPage_browse;
	public static String PerformBasicSignPage_chooseAlias;
	public static String PerformBasicSignPage_chooseKeystore;
	public static String PerformBasicSignPage_enterKeyStoreLoc;
	public static String PerformBasicSignPage_enterProperties;
	public static String PerformBasicSignPage_invalidPwd;
	public static String PerformBasicSignPage_keyStoreLoc;
	public static String PerformBasicSignPage_keystoreLoaded;
	public static String PerformBasicSignPage_keystorePwd;
	public static String PerformSignWizard_aliasPwd;
	public static String PerformSignWizard_invalidPrivateKey;
	public static String PerformSignWizard_performSignature;
	public static String PerformSignWizard_unableToAccessKeystore;
	public static String ScriptingPage_allowRemoteScripts;
	public static String ScriptingPage_allowUIThread;
	public static String ScriptingPage_security;
	public static String ShellPreferencePage_appearance;
	public static String ShellPreferencePage_autoFocus;
	public static String ShellPreferencePage_colorAndFonts;
	public static String ShellPreferencePage_entries;
	public static String ShellPreferencePage_flatModuleList;
	public static String ShellPreferencePage_historyLength;
	public static String ShellPreferencePage_keepLastCmd;
	public static String ShellPreferencePage_lookAndFeel;
	public static String ShellPreferencePage_preferredEngine;
	public static String ShellPreferencePage_shellStartupCmds;
	public static String VariablesDropin_name;
	public static String VariablesDropin_value;
	public static String VariablesDropin_variables;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
