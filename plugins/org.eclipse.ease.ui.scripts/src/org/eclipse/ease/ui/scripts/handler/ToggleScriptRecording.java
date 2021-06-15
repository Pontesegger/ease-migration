/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
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
package org.eclipse.ease.ui.scripts.handler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.ease.IExecutionListener;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.IScriptEngineProvider;
import org.eclipse.ease.Logger;
import org.eclipse.ease.Script;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.tools.StringTools;
import org.eclipse.ease.ui.scripts.Activator;
import org.eclipse.ease.ui.scripts.Messages;
import org.eclipse.ease.ui.scripts.ScriptEditorInput;
import org.eclipse.ease.ui.scripts.ScriptStorage;
import org.eclipse.ease.ui.scripts.dialogs.SelectScriptStorageDialog;
import org.eclipse.ease.ui.scripts.preferences.PreferencesHelper;
import org.eclipse.ease.ui.scripts.repository.IRepositoryService;
import org.eclipse.ease.ui.tools.ToggleHandler;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.menus.UIElement;

/**
 * Start/stop script recording.
 */
public class ToggleScriptRecording extends ToggleHandler implements IHandler, IElementUpdater, IExecutionListener {

	private static final Map<IScriptEngine, StringBuffer> fRecordings = new HashMap<>();

	private static StringBuffer addHeaderData(StringBuffer buffer, String scriptName, ScriptType scriptType) {

		final Map<String, String> keywords = new HashMap<>();
		keywords.put("name", new Path(scriptName).makeRelative().toString());
		keywords.put("description", "Script recorded by user.");
		keywords.put("script-type", scriptType.getName());
		keywords.put("author", System.getProperty("user.name"));
		keywords.put("date-recorded", new SimpleDateFormat("yyyy-MM-dd, HH:mm").format(new Date()));
		final String keywordBlock = scriptType.getCodeFactory().createKeywordHeader(keywords, null);

		buffer.insert(0, StringTools.LINE_DELIMITER);
		buffer.insert(0, scriptType.getCodeFactory().createCommentedString(keywordBlock, true));

		return buffer;
	}

	private static String askForScriptName(ExecutionEvent event, ScriptStorage storage) {
		// ask for script name
		final InputDialog dialog = new InputDialog(HandlerUtil.getActiveShell(event), Messages.ToggleScriptRecording_saveScript,
				Messages.ToggleScriptRecording_enterUniqueName, "", name -> {
					if ((storage != null) && (storage.exists(new Path(name).makeAbsolute().toString())))
						return NLS.bind(Messages.ToggleScriptRecording_nameAlreadyInUse, name);

					return null;
				});

		if (dialog.open() == Window.OK)
			return dialog.getValue();

		return null;
	}

	private static ScriptStorage createOrGetStorage() {
		// if no default storage is selected, ask the user for the correct location
		if (PreferencesHelper.getUserScriptStorageLocation() == null) {

			// user did not select a storage yet, ask for location
			final SelectScriptStorageDialog dialog = new SelectScriptStorageDialog(Display.getDefault().getActiveShell());
			if (dialog.open() == Window.OK) {
				final IRepositoryService repositoryService = PlatformUI.getWorkbench().getService(IRepositoryService.class);
				repositoryService.addLocation(dialog.getLocation(), true, true);
			}

			else
				return null;
		}

		return ScriptStorage.createStorage();
	}

	private boolean fChecked = false;

	@Override
	protected final void executeToggle(final ExecutionEvent event, final boolean checked) {
		final IWorkbenchPart part = HandlerUtil.getActivePart(event);

		if (part instanceof IScriptEngineProvider) {
			final IScriptEngine engine = ((IScriptEngineProvider) part).getScriptEngine();

			if (engine != null) {
				if (checked) {
					// start recording, eventually overrides a running recording
					// of the same provider
					fRecordings.put(engine, new StringBuffer());
					engine.addExecutionListener(this);

				} else {
					// stop recording
					StringBuffer buffer = fRecordings.remove(engine);

					if (buffer.length() > 0) {
						// script data is available

						final ScriptStorage storage = createOrGetStorage();
						final String scriptName = askForScriptName(event, storage);
						final EngineDescription description = engine.getDescription();
						final ScriptType scriptType = description.getSupportedScriptTypes().iterator().next();

						buffer = addHeaderData(buffer, scriptName, scriptType);

						if (storage != null) {
							// store script
							final String fileName = scriptName + "." + scriptType.getDefaultExtension();
							if (!storage.store(fileName, buffer.toString()))
								// could not store script
								MessageDialog.openError(HandlerUtil.getActiveShell(event), Messages.ToggleScriptRecording_saveError,
										Messages.ToggleScriptRecording_couldNotStore);

						} else {
							// we do not have a storage, open script in editor
							// and let user decide what to do
							final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
							try {
								final IEditorDescriptor editor = IDE.getDefaultEditor(
										ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("/sample/foo." + scriptType.getDefaultExtension())));
								final IEditorPart openEditor = IDE.openEditor(page, new ScriptEditorInput(scriptName, buffer.toString()), editor.getId());
								// the editor starts indicating it is not dirty,
								// so ask the user to perform a save as action
								openEditor.doSaveAs();

							} catch (final PartInitException e) {
								Logger.error(Activator.PLUGIN_ID, "Could not open editor for recorded script.", e);
							}
						}
					}
				}
			}
		}

		fChecked = checked;
	}

	@Override
	public final void updateElement(final UIElement element, @SuppressWarnings("rawtypes") final Map parameters) {
		super.updateElement(element, parameters);

		if (fChecked)
			element.setIcon(org.eclipse.ease.ui.Activator.getImageDescriptor(Activator.PLUGIN_ID, "icons/elcl16/stop_script_recording.png"));

		else
			element.setIcon(org.eclipse.ease.ui.Activator.getImageDescriptor(Activator.PLUGIN_ID, "icons/elcl16/start_script_recording.png"));
	}

	@Override
	public void notify(final IScriptEngine engine, final Script script, final int status) {
		if (IExecutionListener.SCRIPT_END == status) {
			try {
				final StringBuffer buffer = fRecordings.get(engine);
				if (buffer != null) {
					buffer.append(script.getCode());

					if (!buffer.toString().endsWith(StringTools.LINE_DELIMITER))
						buffer.append(StringTools.LINE_DELIMITER);
				} else
					engine.removeExecutionListener(this);
			} catch (final Exception e) {
				// could not fetch code from script, gracefully fail
			}
		}
	}
}
