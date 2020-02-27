/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.applications;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ease.Activator;
import org.eclipse.ease.Logger;
import org.eclipse.ease.ScriptResult;
import org.eclipse.ease.service.ScriptService;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.ui.IStartup;

public class RunHeadlessScript implements IApplication {

	/** List of classes that should not be loaded on early startup. */
	private static final Collection<String> EARLY_STARTUP_BLACKLIST = Arrays.asList("org.eclipse.team.svn.ui.startup.SVNCoreStartup",
			"org.eclipse.egit.ui.internal.clone.GitCloneDropAdapter", "org.eclipse.equinox.internal.p2.ui.sdk.scheduler.AutomaticUpdateScheduler");

	private static Map<String, Object> extractInputParameters(final String[] arguments) {
		final Map<String, Object> parameters = new HashMap<>();
		parameters.put("args", new ArrayList<String>());

		for (int index = 0; index < arguments.length; index++) {
			if (parameters.containsKey("script")) {
				// script arguments
				((List) parameters.get("args")).add(arguments[index]);

			} else if ("-script".equals(arguments[index])) {
				if ((index + 1) < arguments.length) {
					parameters.put("script", arguments[index + 1]);
					((List) parameters.get("args")).add(arguments[index + 1]);
					index++;

				} else {
					System.out.println("ERROR: script name is missing");
					return null;
				}

			} else if ("-workspace".equals(arguments[index])) {
				if ((index + 1) < arguments.length) {
					parameters.put("workspace", arguments[index + 1]);
					index++;

				} else {
					System.out.println("ERROR: workspace location is missing");
					return null;
				}

			} else if ("-engine".equals(arguments[index])) {
				if ((index + 1) < arguments.length) {
					parameters.put("engine", arguments[index + 1]);
					index++;

				} else {
					System.out.println("ERROR: workspace location is missing");
					return null;
				}

			} else if ("-help".equals(arguments[index])) {
				return null;

			} else {
				System.out.println("ERROR: invalid args (" + arguments[index] + ")");
				return null;
			}
		}

		return parameters;
	}

	private static void printUsage() {
		System.out.println("SYNTAX: [-workspace <workspace location> [-refreshWorkspace]] [-engine <engineID>]-script <script name> <script parameters>");
		System.out.println("");
		System.out.println("\t\t<script name> is a path like 'file://C/myfolder/myscript.js'");
		System.out.println("\t\t<engineID> provides a dedicated script engine ID. Use org.eclipse.ease.listEngines application.");
		System.out.println("\t\t<workspace location> is a file system path like 'C:\\somefolder\\myworkspace'");
		System.out.println("\t\t\tif you provide a workspace you can use workspace:// identifiers in your scripts");
		System.out.println("\t\t\tif you provide a workspace you may ask to refresh it first prior to script execution");
		System.out.println("\t\t<script parameters> will be passed to the script as String[] in the variable 'argv'");
	}

	@Override
	public Object start(final IApplicationContext context) throws Exception {
		final Object object = context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		if (object instanceof String[]) {
			final Map<String, Object> parameters = extractInputParameters((String[]) object);

			if (parameters != null) {

				// create workspace
				Location location = null;
				if (parameters.containsKey("workspace")) {

					location = Platform.getInstanceLocation();
					// stick to the deprecated method as file.toURI().toURL() will not work on paths containing spaces
					final URL workspaceURL = new File(parameters.get("workspace").toString()).toURL();

					// check if workspace location has not been set yet (can be set only once!)
					if (!location.isSet()) {
						if (!location.set(workspaceURL, true)) {
							// could not lock the workspace.
							System.err.println("ERROR: Could not set the workspace to \"" + location.getURL() + "\"");
							return -1;
						}

					} else if (!location.getURL().toString().equals(workspaceURL.toString())) {
						System.err.println("ERROR: Could not set the workspace as it is already set to \"" + location.getURL() + "\"");
						return -1;
					}

					if (parameters.containsKey("refreshWorkspace"))
						ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
				}

				loadEarlyStartupExtensions();

				try {
					// execute script
					if (parameters.containsKey("script")) {

						final String scriptLocation = parameters.get("script").toString();
						final String engineID = (parameters.containsKey("engine")) ? parameters.get("engine").toString() : null;
						final String[] arguments = ((List<String>) parameters.get("args")).toArray(new String[0]);

						try {
							final Object result = ScriptService.getInstance().executeScript(scriptLocation, engineID, arguments);

							if (result != null) {
								if (ScriptResult.VOID.equals(result)) {
									return 0;
								}

								try {
									return Integer.parseInt(result.toString());
								} catch (final Exception e) {
									// no integer
								}

								try {
									return new Double(Double.parseDouble(result.toString())).intValue();
								} catch (final Exception e) {
									// no double
								}

								try {
									return Boolean.parseBoolean(result.toString()) ? 0 : -1;
								} catch (final Exception e) {
									// no boolean
								}

								// we do not know the return type, but typically parseBoolean() will deal with anything you throw at it
							} else
								return 0;

						} catch (final Throwable throwable) {
							return -1;
						}
					}

					System.err.println("ERROR: Could not access file \"" + parameters.get("script") + "\"");

				} finally {
					// persist workspace
					ResourcesPlugin.getWorkspace().save(true, null);

					// make sure we do not lock the workspace permanently
					if (location != null)
						location.release();
				}

			} else
				// could not extract parameters
				printUsage();
		}

		return -1;
	}

	/**
	 * Load eclipse extensions for extension point: org.eclipse.ui.startup.
	 */
	private void loadEarlyStartupExtensions() {
		final IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.ui.startup");
		for (final IConfigurationElement e : config) {
			if (e.getName().equals("startup")) {
				if (!EARLY_STARTUP_BLACKLIST.contains(e.getAttribute("class"))) {
					try {
						Logger.info(Activator.PLUGIN_ID, "Loading early startup extension: " + e.getAttribute("class"));
						final Object earlyStartupParticipant = e.createExecutableExtension("class");
						if (earlyStartupParticipant instanceof IStartup) {
							try {
								((IStartup) earlyStartupParticipant).earlyStartup();
							} catch (final Throwable e1) {
								System.err.println("ERROR: Failed to execute " + earlyStartupParticipant.getClass().getName() + ".earlyStartup(): " + e1);
							}
						}
					} catch (final CoreException e1) {
						System.err.println("ERROR: Could not create instance for startup code: " + e.getAttribute("class"));
					}
				}
			}
		}
	}

	@Override
	public void stop() {
	}
}
