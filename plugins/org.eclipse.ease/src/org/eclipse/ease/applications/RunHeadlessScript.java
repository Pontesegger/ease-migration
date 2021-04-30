/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
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

package org.eclipse.ease.applications;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
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

	/**
	 *
	 */
	private static final String HELP = "-help";

	/**
	 *
	 */
	private static final String ENGINE = "-engine";

	private static final String SCRIPT_ARGUMENTS = "scriptArguments";

	private static final String SCRIPT = "-script";

	private static final String REFRESH_WORKSPACE = "-refreshWorkspace";

	private static final String WORKSPACE = "-workspace";

	/** List of classes that should not be loaded on early startup. */
	private static final Collection<String> EARLY_STARTUP_BLACKLIST = Arrays.asList("org.eclipse.team.svn.ui.startup.SVNCoreStartup",
			"org.eclipse.egit.ui.internal.clone.GitCloneDropAdapter", "org.eclipse.equinox.internal.p2.ui.sdk.scheduler.AutomaticUpdateScheduler");

	private static Map<String, Object> extractInputParameters(final String[] arguments) {
		final Map<String, Object> parameters = new HashMap<>();
		parameters.put(SCRIPT_ARGUMENTS, new ArrayList<String>());

		for (int index = 0; index < arguments.length; index++) {
			if (parameters.containsKey(SCRIPT)) {
				// script arguments
				((List) parameters.get(SCRIPT_ARGUMENTS)).add(arguments[index]);

			} else if (SCRIPT.equals(arguments[index])) {
				if ((index + 1) < arguments.length) {
					parameters.put(SCRIPT, arguments[index + 1]);
					((List) parameters.get(SCRIPT_ARGUMENTS)).add(arguments[index + 1]);
					index++;

				} else
					throw new IllegalArgumentException("script name is missing");

			} else if (WORKSPACE.equals(arguments[index])) {
				if ((index + 1) < arguments.length) {
					parameters.put(WORKSPACE, arguments[index + 1]);
					index++;

				} else
					throw new IllegalArgumentException("workspace name is missing");

			} else if (REFRESH_WORKSPACE.equals(arguments[index])) {
				parameters.put(REFRESH_WORKSPACE, true);

			} else if (ENGINE.equals(arguments[index])) {
				if ((index + 1) < arguments.length) {
					parameters.put(ENGINE, arguments[index + 1]);
					index++;

				} else
					throw new IllegalArgumentException("engine ID is missing");

			} else if (HELP.equals(arguments[index])) {
				return null;

			} else
				throw new IllegalArgumentException(String.format("invalid argument: %s", arguments[index]));
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

	private static void printError(String message) {
		System.out.println("ERROR: " + message);
	}

	@Override
	public Object start(final IApplicationContext context) throws Exception {
		try {
			final Object object = context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
			if (object instanceof String[]) {
				final Map<String, Object> parameters = extractInputParameters((String[]) object);

				if (parameters != null) {

					// create workspace
					final Location location = loadWorkspace(parameters);

					loadEarlyStartupExtensions();

					try {
						// execute script
						if (parameters.containsKey(SCRIPT)) {

							final String scriptLocation = parameters.get(SCRIPT).toString();
							final String engineID = (parameters.containsKey(ENGINE)) ? parameters.get(ENGINE).toString() : null;
							final String[] arguments = ((List<String>) parameters.get(SCRIPT_ARGUMENTS)).toArray(new String[0]);

							try {
								final Object result = ScriptService.getInstance().executeScript(scriptLocation, engineID, arguments);
								return getScriptResult(result);

							} catch (final Throwable throwable) {
								return -1;
							}

						} else
							throw new IllegalArgumentException(String.format("Parameter '%s' is required", SCRIPT));

					} finally {
						if (location != null) {
							// persist workspace
							ResourcesPlugin.getWorkspace().save(true, null);
							// make sure we do not lock the workspace permanently
							location.release();
						}
					}

				} else
					// could not extract parameters
					printUsage();
			}

		} catch (final IOException e) {
			printError(String.format(e.getMessage()));
			printUsage();

		} catch (final IllegalArgumentException e) {
			printError(String.format("invalid command line argument%n\t%s%n", e.getMessage()));
			printUsage();
		}

		return -1;
	}

	private int getScriptResult(final Object result) {
		if (result != null) {
			if (ScriptResult.VOID.equals(result)) {
				return 0;
			}

			try {
				return Integer.parseInt(result.toString());
			} catch (final NumberFormatException e) {
				// no integer
			}

			try {
				return Double.valueOf(result.toString()).intValue();
			} catch (final NumberFormatException e) {
				// no double
			}

			// we do not know the return type, but typically parseBoolean() will deal with anything you throw at it
			return Boolean.parseBoolean(result.toString()) ? 0 : -1;

		} else
			return 0;
	}

	private Location loadWorkspace(final Map<String, Object> parameters) throws MalformedURLException, IOException, CoreException {
		Location location = null;
		if (parameters.containsKey(WORKSPACE)) {

			location = Platform.getInstanceLocation();
			// stick to the deprecated method as file.toURI().toURL() will not work on paths containing spaces
			final URL workspaceURL = new File(parameters.get(WORKSPACE).toString()).toURL();

			// check if workspace location has not been set yet (can be set only once!)
			if (!location.isSet()) {
				if (!location.set(workspaceURL, true))
					throw new IOException(String.format("Could not set the workspace to '%s'", location.getURL()));

			} else if (!location.getURL().toString().equals(workspaceURL.toString()))
				throw new IOException(String.format("Could not set the workspace as it is already set to '%s'", location.getURL()));

			if (parameters.containsKey(REFRESH_WORKSPACE))
				ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		}
		return location;
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
								printError(String.format("Failed to execute %s.earlyStartup(): %s", earlyStartupParticipant.getClass().getName(), e1));
							}
						}
					} catch (final CoreException e1) {
						printError("Could not create instance for startup code: " + e.getAttribute("class"));
					}
				}
			}
		}
	}

	@Override
	public void stop() {
	}
}
