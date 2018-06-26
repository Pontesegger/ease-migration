/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.applications;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.ScriptResult;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.ui.IStartup;

public class RunHeadlessScript implements IApplication {

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
				}

				loadEarlyStartupExtensions();

				try {
					// execute script
					if (parameters.containsKey("script")) {
						// find script engine
						EngineDescription engineDescription = null;
						final IScriptService scriptService = ScriptService.getInstance();

						if (parameters.containsKey("engine"))
							// locate engine by ID
							engineDescription = scriptService.getEngineByID(parameters.get("engine").toString());

						else {
							// locate engine by file extension
							final ScriptType scriptType = scriptService.getScriptType(parameters.get("script").toString());
							if (scriptType != null)
								engineDescription = scriptService.getEngine(scriptType.getName());
						}

						if (engineDescription != null) {
							// create engine
							final IScriptEngine engine = engineDescription.createEngine();
							engine.setVariable("argv", ((List) parameters.get("args")).toArray(new String[0]));

							// TODO implement better URI handling - eg create URI and pass to script engine
							Object scriptObject = ResourceTools.resolve(parameters.get("script"));
							if (scriptObject == null)
								// no file available, try to include to resolve URIs
								scriptObject = "include(\"" + parameters.get("script") + "\")";

							final ScriptResult scriptResult = engine.executeSync(scriptObject);
							if (scriptResult.hasException()) {
								scriptResult.getException().printStackTrace(System.err);
								return -1;
							}

							final Object result = scriptResult.getResult();

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
						}
					}
				} finally {
					// make sure we do not lock the workspace permanently
					if (location != null)
						location.release();
				}

				System.err.println("ERROR: Could not access file \"" + parameters.get("script") + "\"");

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
				try {
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
		System.out.println("SYNTAX: [-workspace <workspace location>] [-engine <engineID>]-script <script name> <script parameters>");
		System.out.println("");
		System.out.println("\t\t<script name> is a path like 'file://C/myfolder/myscript.js'");
		System.out.println("\t\t<engineID> provides a dedicated script engine ID. Use org.eclipse.ease.listEngines application.");
		System.out.println("\t\t<workspace location> is a file system path like 'C:\\somefolder\\myworkspace'");
		System.out.println("\t\t\tif you provide a workspace you can use workspace:// identifiers in your scripts");
		System.out.println("\t\t<script parameters> will be passed to the script as String[] in the variable 'argv'");
	}

	@Override
	public void stop() {
	}
}
