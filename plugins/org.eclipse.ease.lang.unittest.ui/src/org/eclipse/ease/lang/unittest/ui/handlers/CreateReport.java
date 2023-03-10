/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
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
package org.eclipse.ease.lang.unittest.ui.handlers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ease.Logger;
import org.eclipse.ease.lang.unittest.runtime.ITestContainer;
import org.eclipse.ease.lang.unittest.runtime.ITestEntity;
import org.eclipse.ease.lang.unittest.ui.Activator;
import org.eclipse.ease.lang.unittest.ui.dialogs.CreateReportDialog;
import org.eclipse.ease.lang.unittest.ui.views.UnitTestView;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Create test report command.
 */
public class CreateReport extends AbstractViewToolbarHandler {

	@Override
	public final Object execute(final ExecutionEvent event) throws ExecutionException {
		final UnitTestView view = getView(event, UnitTestView.class);
		if (view != null) {

			final ITestEntity testEntity = ((ITestContainer) view.getFileTreeViewer().getInput()).getCopyOfChildren().get(0);

			// we found a test suite to export
			final CreateReportDialog dialog = new CreateReportDialog(HandlerUtil.getActiveShell(event));
			if (dialog.open() == Window.OK) {

				try {
					String filename = dialog.getFileName();
					final String extension = "." + dialog.getReport().getDefaultExtension();
					if (!filename.toLowerCase().endsWith(extension))
						filename += extension;

					final FileWriter writer = new FileWriter(new File(filename));
					writer.write(dialog.getReport().createReport(dialog.getTitle(), dialog.getDescription(), testEntity));
					writer.close();

					try {
						if (dialog.isOpenReport()) {
							// open report after creation
							if (Platform.getOS().startsWith("win"))
								Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + new File(filename).toURI().toString());

							else if (Platform.getOS().startsWith("linux")) {
								String desktop = System.getenv("XDG_CURRENT_DESKTOP");
								if (desktop == null)
									desktop = System.getenv("sun.desktop");

								if ("KDE".equalsIgnoreCase(desktop))
									try {
										Runtime.getRuntime().exec("kfmclient exec " + new File(filename).toURI().toString());
									} catch (final IOException e) {
										Runtime.getRuntime().exec("xdg-open " + new File(filename).toURI().toString());
									}
								else if ("GNOME".equalsIgnoreCase(desktop))
									Runtime.getRuntime().exec("gnome-open " + new File(filename).toURI().toString());

							} else {
								// TODO add support for other platforms
								MessageDialog.openError(HandlerUtil.getActiveShell(event), "Could not open report",
										"Support for your platform (" + Platform.getOS() + ") not implemented. Please raise a bug if needed.");
							}
						}
					} catch (final Exception e) {
						// TODO add support for other platforms
						MessageDialog.openError(HandlerUtil.getActiveShell(event), "Could not open report",
								"Support for your platform (" + Platform.getOS() + ") not implemented. Please raise a bug if needed.");
					}

				} catch (final IOException e) {
					MessageDialog.openError(HandlerUtil.getActiveShell(event), "Create Report failed",
							"Could not open file for writing. Report could not be saved");
					Logger.error(Activator.PLUGIN_ID, "Could not create report file \"" + dialog.getFileName() + "\"", e);
				}
			}
		}

		return null;
	}
}
