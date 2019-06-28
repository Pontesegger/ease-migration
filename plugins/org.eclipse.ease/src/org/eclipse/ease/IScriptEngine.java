/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.net.URL;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.ease.service.EngineDescription;

/**
 * Interface for a script engine. A script engine is capable of interpreting script code at runtime. Script engines shall be derived from {@link Thread} and
 * therefore run separately from other code. An engine shall be started by calling {@link #schedule()}.
 */
public interface IScriptEngine {

	/** Trace enablement for script engines. */
	boolean TRACE_SCRIPT_ENGINE = org.eclipse.ease.Activator.getDefault().isDebugging()
			&& "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.ease/debug/scriptEngine"));

	/**
	 * Execute script code asynchronously. The code provided will be scheduled and executed as soon as all previously scheduled code is executed. If
	 * <i>content</i> is a {@link Reader} object, or a {@link File} special treatment is done, otherwise the toString() method is used to extract script code.
	 *
	 * @param content
	 *            content to be executed.
	 * @return execution result
	 */
	ScriptResult executeAsync(final Object content);

	/**
	 * Execute script code synchronously. The code provided will be scheduled and executed as soon as all previously scheduled code is executed.If
	 * <i>content</i> is a {@link Reader} object, or a {@link File} special treatment is done, otherwise the toString() method is used to extract script code.
	 * In case the engine was not started yet, it will be automatically started by this call. This is a blocking call.
	 *
	 * @param content
	 *            content to be executed.
	 * @return execution result
	 * @throws InterruptedException
	 *             when execution is interrupted
	 */
	ScriptResult executeSync(final Object content) throws InterruptedException;

	/**
	 * Inject script code and execute synchronously. Code passed to this method will be invoked immediately. It might interrupt a currently running execution
	 * requested asynchronously.
	 *
	 * @param content
	 *            content to be executed.
	 * @return execution result
	 */
	Object inject(final Object content);

	/**
	 * Inject script code and execute synchronously within the UI thread. Code passed to this method will be invoked immediately. It might interrupt a currently
	 * running execution requested asynchronously.
	 *
	 * @param content
	 *            content to be executed.
	 * @return execution result
	 */
	Object injectUI(final Object content);

	/**
	 * Get the currently executed file instance.
	 *
	 * @return currently executed file
	 */
	Object getExecutedFile();

	/**
	 * Set the default output stream for the interpreter.
	 *
	 * @param outputStream
	 *            default output stream
	 */
	void setOutputStream(final OutputStream outputStream);

	/**
	 * Set the default error stream for the interpreter.
	 *
	 * @param errorStream
	 *            default error stream
	 */
	void setErrorStream(final OutputStream errorStream);

	/**
	 * Set the default input stream for the interpreter.
	 *
	 * @param inputStream
	 *            default input stream
	 */
	void setInputStream(final InputStream inputStream);

	PrintStream getOutputStream();

	PrintStream getErrorStream();

	InputStream getInputStream();

	/**
	 * Set marker to automatically close I/O streams when engine is terminated.
	 *
	 * @param closeStreams
	 *            <code>true</code> to close streams
	 */
	void setCloseStreamsOnTerminate(final boolean closeStreams);

	/**
	 * Schedule script execution. This will start the script engine that either waits for input or immediate starts execution of previously scheduled input.
	 */
	void schedule();

	/**
	 * Terminate this interpreter. Addresses a request to terminate current script execution. When the request will be handled is implementation specific.
	 */
	void terminate();

	/**
	 * Stops the currently executed piece of code. Will continue to execute the next scheduled piece of code.
	 */
	void terminateCurrent();

	void addExecutionListener(IExecutionListener listener);

	void removeExecutionListener(IExecutionListener listener);

	/**
	 * Get the engine name.
	 *
	 * @return engine name
	 */
	String getName();

	/**
	 * Set a variable in the script engine. This variable will be stored in the global script scope
	 *
	 * @param name
	 *            variable name
	 * @param content
	 *            variable content
	 */
	void setVariable(final String name, final Object content);

	/**
	 * Get a script variable. Retrieve a variable from the global script scope.
	 *
	 * @param name
	 *            variable name
	 *
	 * @return variable content or <code>null</code>
	 */
	Object getVariable(final String name);

	/**
	 * Check if a variable exists within the scope of the engine. As a variable content may be <code>null</code>, {@link #getVariable(String)} might not be
	 * sufficient to query.
	 *
	 * @param name
	 *            variable name
	 * @return <code>true</code> when variable exists
	 */
	boolean hasVariable(String name);

	/**
	 * Get engine description for current engine.
	 *
	 * @return engine description
	 */
	EngineDescription getDescription();

	/**
	 * Get all variables from the scope.
	 *
	 * @return map of variables
	 */
	Map<String, Object> getVariables();

	/**
	 * Register a jar file and add it to the classpath. After registering, classes within the jar file shall be usable within the script.
	 *
	 * @param url
	 *            url to load jar file from
	 */
	void registerJar(final URL url);

	/**
	 * Join engine execution thread. Waits for engine execution up to <i>timeout</i>
	 *
	 * @param timeout
	 *            command timeout in milliseconds
	 * @throws InterruptedException
	 *             when join command got interrupted
	 */
	void join(long timeout) throws InterruptedException;

	/**
	 * Verify that engine was started and terminated.
	 *
	 * @return <code>true</code> when engine ran and terminated
	 */
	boolean isFinished();

	/**
	 * Add a dedicated security check for a certain script action. If the check was already registered for this action, no further check will be added.
	 *
	 * @param type
	 *            action type to add check for
	 * @param check
	 *            check to register
	 */
	void addSecurityCheck(ISecurityCheck.ActionType type, ISecurityCheck check);

	/**
	 * Remove registered security check for all actions. If the check was not registered at all nothing will happen.
	 *
	 * @param check
	 *            check to be removed
	 */
	void removeSecurityCheck(ISecurityCheck check);

	/**
	 * Get the launch that was used to create this engine.
	 *
	 * @return launch or <code>null</code> in case this engine was created without launch configuration
	 */
	ILaunch getLaunch();

	/**
	 * Get the monitor of the current running engine. The monitor is only valid while the script engine is running.
	 *
	 * @return current monitor or <code>null</code>
	 */
	IProgressMonitor getMonitor();
}
