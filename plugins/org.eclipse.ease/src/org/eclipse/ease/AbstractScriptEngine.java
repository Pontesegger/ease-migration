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
package org.eclipse.ease;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IProgressMonitorWithBlocking;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.ease.ISecurityCheck.ActionType;
import org.eclipse.ease.debugging.EaseDebugFrame;
import org.eclipse.ease.debugging.IScriptDebugFrame;
import org.eclipse.ease.debugging.ScriptStackTrace;
import org.eclipse.ease.security.ScriptUIAccess;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.tools.ListenerList;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.ui.internal.progress.ProgressManager.JobMonitor;
import org.osgi.framework.Version;

/**
 * Base implementation for a script engine. Handles Job implementation of script engine, adding script code for execution, module loading support and a basic
 * online help system.
 */
public abstract class AbstractScriptEngine extends Job implements IScriptEngine {

	/**
	 * Get the current script engine. Works only if executed from the script engine thread.
	 *
	 * @return script engine or <code>null</code>
	 */
	public static IScriptEngine getCurrentScriptEngine() {
		if (Job.getJobManager().currentJob() instanceof IScriptEngine)
			return (IScriptEngine) Job.getJobManager().currentJob();

		return null;
	}

	/**
	 * Get the beautified name of a file to be set as part of the job title.
	 *
	 * @param file
	 *            executed file
	 * @return beautified name or <code>null</code>
	 */
	private static String getFilename(Object file) {
		if (file instanceof IFile) {
			return ResourceTools.toAbsoluteLocation(file, null);
		} else if (file instanceof File) {
			return ResourceTools.toAbsoluteLocation(file, null);
		} else {
			return null;
		}
	}

	/** List of code parts to be executed. */
	private final List<Script> fScheduledScripts = new ArrayList<>();

	private final ListenerList<IExecutionListener> fExecutionListeners = new ListenerList<>();

	private PrintStream fOutputStream = null;

	private PrintStream fErrorStream = null;

	private InputStream fInputStream = null;

	private final ScriptStackTrace fStackTrace = new ScriptStackTrace();

	private EngineDescription fDescription;

	private boolean fSetupDone = false;

	/** Variables tried to set before engine was started. */
	private final Map<String, Object> fBufferedVariables = new HashMap<>();

	private boolean fCloseStreamsOnTerminate;

	/** Registered security checks for engine actions. */
	private final Map<ActionType, List<ISecurityCheck>> fSecurityChecks = new HashMap<>();

	private Object fExecutionRootFile;;

	/** Launch associated with this engine. */
	private ILaunch fLaunch = null;

	private IProgressMonitor fMonitor;

	/**
	 * Constructor. Sets the name for the underlying job.
	 *
	 * @param name
	 *            name of script engine job
	 */
	public AbstractScriptEngine(final String name) {
		super(String.format("[EASE %s Engine]", name));

		setSystem(false);
	}

	@Override
	public EngineDescription getDescription() {
		return fDescription;
	}

	@Override
	public final ScriptResult execute(final Object content) {
		final Script script = (content instanceof Script) ? (Script) content : new Script(content);

		synchronized (fScheduledScripts) {
			fScheduledScripts.add(script);
			fScheduledScripts.notifyAll();
		}

		return script.getResult();
	}

	@Override
	public Object inject(Object content, boolean uiThread) throws ExecutionException {
		final Script script = (content instanceof Script) ? (Script) content : new Script(content);

		// injected code shall not trigger a new event, therefore notifyListerners needs to be false
		return inject(script, false, uiThread).get();
	}

	/**
	 * Inject script code to the script engine. Injected code is processed synchronous by the current thread unless <i>uiThread</i> is set to <code>true</code>.
	 * Nevertheless this is a blocking call.
	 *
	 * @param script
	 *            script to be executed
	 * @param notifyListeners
	 *            <code>true</code> when listeners should be informed of code fragment
	 * @param uiThread
	 *            when set to <code>true</code> run injected code in UI thread
	 * @return script execution result
	 */
	private ScriptResult inject(final Script script, final boolean notifyListeners, final boolean uiThread) {

		try {
			Logger.trace(Activator.PLUGIN_ID, TRACE_SCRIPT_ENGINE, "Executing script (" + script.getTitle() + "):", script.getCode());
			final String filename = getFilename(script.getFile());
			fStackTrace.add(0, new EaseDebugFrame(script, 0, IScriptDebugFrame.TYPE_FILE, filename));
			updateJobName(filename);

			// apply security checks
			final List<ISecurityCheck> securityChecks = fSecurityChecks.get(ActionType.INJECT_CODE);
			if (securityChecks != null) {
				for (final ISecurityCheck check : securityChecks) {
					if (!check.doIt(ActionType.INJECT_CODE, script, uiThread))
						throw new ScriptEngineException("Security check failed: " + check.toString());
				}
			}

			// execution
			if (notifyListeners)
				notifyExecutionListeners(script, IExecutionListener.SCRIPT_START);
			else
				notifyExecutionListeners(script, IExecutionListener.SCRIPT_INJECTION_START);

			script.setResult(execute(script, fStackTrace.get(0).getName(), uiThread));

		} catch (final BreakException e) {
			script.setResult(e.getCondition());

		} catch (final Throwable e) {
			// only do the printing if this is the last script on the stack
			// otherwise we will print multiple times for each rethrow
			if (fStackTrace.size() <= 1)
				e.printStackTrace(getErrorStream());

			if (e instanceof ScriptExecutionException)
				script.setException((ScriptExecutionException) e);
			else
				script.setException(new ScriptExecutionException(e));

		} finally {
			if (notifyListeners)
				notifyExecutionListeners(script, IExecutionListener.SCRIPT_END);
			else
				notifyExecutionListeners(script, IExecutionListener.SCRIPT_INJECTION_END);

			if (!fStackTrace.isEmpty())
				fStackTrace.remove(0);
		}

		return script.getResult();
	}

	private void updateJobName(String filename) {
		if (filename != null) {
			String baseName = getName();
			if (baseName.contains("]"))
				baseName = baseName.substring(0, baseName.indexOf(']') + 1);

			setName(baseName + " " + filename);
		}
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		fMonitor = monitor;

		addStopButtonMonitor();

		IStatus returnStatus = setupRun();
		if (Status.OK_STATUS.equals(returnStatus)) {
			// main loop
			while (!shallTerminate()) {

				try {
					synchronized (fScheduledScripts) {
						waitForNextScript();

						if (!fScheduledScripts.isEmpty()) {
							final Script piece = fScheduledScripts.remove(0);
							inject(piece, true, false);
						}
					}
				} catch (final InterruptedException e) {
					// waiting got interrupted, quite likely a shutdown
				}
			}
		}

		if (getMonitor().isCanceled())
			returnStatus = Status.CANCEL_STATUS;

		return cleanupRun(returnStatus);
	}

	private void waitForNextScript() throws InterruptedException {
		synchronized (fScheduledScripts) {
			while ((fScheduledScripts.isEmpty()) && (!shallTerminate())) {
				Logger.trace(Activator.PLUGIN_ID, TRACE_SCRIPT_ENGINE, "Engine idle: " + getName());
				fScheduledScripts.wait();
			}
		}
	}

	private IStatus setupRun() {
		Logger.trace(Activator.PLUGIN_ID, TRACE_SCRIPT_ENGINE, "Engine started: " + getName());

		addSecurityCheck(ActionType.INJECT_CODE, ScriptUIAccess.getInstance());

		try {
			setupEngine();
			fSetupDone = true;

			// engine is initialized, set buffered variables
			for (final Entry<String, Object> entry : fBufferedVariables.entrySet()) {
				setVariable(entry.getKey(), entry.getValue());
			}

			fBufferedVariables.clear();

			// setup new trace
			fStackTrace.clear();

			notifyExecutionListeners(null, IExecutionListener.ENGINE_START);

		} catch (final ScriptEngineException e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Could not setup script engine", e);
		}

		return Status.OK_STATUS;
	}

	private IStatus cleanupRun(IStatus returnStatus) {

		// discard pending code pieces
		synchronized (fScheduledScripts) {
			for (final Script script : fScheduledScripts)
				script.setException(new ScriptExecutionException("Engine got terminated"));

			fScheduledScripts.clear();
		}

		notifyExecutionListeners(null, IExecutionListener.ENGINE_END);

		try {
			teardownEngine();
		} catch (final ScriptEngineException e) {
			if (returnStatus.getSeverity() < IStatus.ERROR) {
				// We were almost all OK (or just warnings/infos) but then we failed at shutdown
				// Note we don't override a CANCEL
				returnStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Could not teardown script engine", e);
			}
		} finally {
			synchronized (fScheduledScripts) {
				fScheduledScripts.notifyAll();
			}

			closeStreams();

			Logger.trace(Activator.PLUGIN_ID, TRACE_SCRIPT_ENGINE, "Engine terminated: " + getName());

			fMonitor.done();
			fMonitor = null;
		}

		return returnStatus;
	}

	/**
	 * Add monitor to detect clicks on the stop button in the Progress view. This functionality improves usability, but is not essential to scripting.
	 */
	private void addStopButtonMonitor() {
		if (fMonitor instanceof JobMonitor) {
			final Version workbenchBundleVersion = Platform.getBundle("org.eclipse.ui.workbench").getVersion();
			if (workbenchBundleVersion.compareTo(Version.valueOf("3.120.0")) >= 0) {
				addStopButtonMonitorForEclipse2020v09();

			} else if (workbenchBundleVersion.compareTo(Version.valueOf("3.110.1")) >= 0) {
				addStopButtonMonitorForEclipseOxygenTo2020v06();
			} else {
				// JobMonitor is a private class up to 3.110.1 (Eclipse Oxygen)
			}
		}
	}

	private void addStopButtonMonitorForEclipseOxygenTo2020v06() {
		// JobMonitor changed its API from
		// addProgressListener(IProgressMonitor)
		// to
		// addProgressListener(IProgressMonitorWithBlocking)
		// in 2020.09
		try {
			final Method addProgressListener = JobMonitor.class.getDeclaredMethod("addProgressListener", new Class[] { IProgressMonitorWithBlocking.class });
			addProgressListener.invoke(fMonitor, new ScriptEngineMonitor());
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// silently ignore
		}
	}

	private void addStopButtonMonitorForEclipse2020v09() {
		((JobMonitor) fMonitor).addProgressListener(new ScriptEngineMonitor());
	}

	/**
	 * Evaluate if the engine shall terminate.
	 *
	 * @return <code>true</code> when termination is requested or there is no more work to be done
	 */
	protected boolean shallTerminate() {
		synchronized (fScheduledScripts) {
			return getMonitor().isCanceled() || fScheduledScripts.isEmpty();
		}
	}

	@Override
	public void terminate() {
		final IProgressMonitor monitor = getMonitor();
		if ((monitor != null) && (!monitor.isCanceled()))
			monitor.setCanceled(true);

		terminateCurrent();

		synchronized (fScheduledScripts) {
			fScheduledScripts.notifyAll();
		}
	}

	@Override
	public boolean isFinished() {
		// setup was done, hence we were started
		return (Job.NONE == getState()) && fSetupDone;
	}

	@Override
	public void joinEngine() throws InterruptedException {
		if (!Thread.currentThread().equals(getThread())) {
			// we cannot join our own thread

			synchronized (fScheduledScripts) {
				while (!isFinished())
					fScheduledScripts.wait(1000);
			}
		}
	}

	@Override
	public void joinEngine(final long timeout) throws InterruptedException {
		if (!Thread.currentThread().equals(getThread())) {
			// we cannot join our own thread

			synchronized (fScheduledScripts) {
				if (!isFinished())
					fScheduledScripts.wait(timeout);
			}
		}
	}

	@Override
	public IProgressMonitor getMonitor() {
		return fMonitor;
	}

	private void closeStreams() {
		if (fCloseStreamsOnTerminate) {
			// gracefully close I/O streams
			try {
				if ((getInputStream() != null) && (!System.in.equals(getInputStream())))
					getInputStream().close();
			} catch (final IOException e) {
			}
			try {
				if ((getOutputStream() != null) && (!System.out.equals(getOutputStream())))
					getOutputStream().close();
			} catch (final Exception e) {
			}
			try {
				if ((getErrorStream() != null) && (!System.err.equals(getErrorStream())))
					getErrorStream().close();
			} catch (final Exception e) {
			}
		}

		fOutputStream = null;
		fErrorStream = null;
		fInputStream = null;
	}

	@Override
	public void setCloseStreamsOnTerminate(final boolean closeStreams) {
		fCloseStreamsOnTerminate = closeStreams;
	}

	@Override
	public PrintStream getOutputStream() {
		return (fOutputStream != null) ? fOutputStream : System.out;
	}

	@Override
	public void setOutputStream(final OutputStream outputStream) {
		if (outputStream instanceof PrintStream)
			fOutputStream = (PrintStream) outputStream;

		else if (outputStream != null)
			fOutputStream = new PrintStream(outputStream);
		else
			fOutputStream = null;
	}

	@Override
	public InputStream getInputStream() {
		return (fInputStream != null) ? fInputStream : System.in;
	}

	@Override
	public void setInputStream(final InputStream inputStream) {
		fInputStream = inputStream;
	}

	@Override
	public PrintStream getErrorStream() {
		return (fErrorStream != null) ? fErrorStream : System.err;
	}

	@Override
	public void setErrorStream(final OutputStream errorStream) {
		if (errorStream instanceof PrintStream)
			fErrorStream = (PrintStream) errorStream;

		else if (errorStream != null)
			fErrorStream = new PrintStream(errorStream);
		else
			fErrorStream = null;
	}

	@Override
	public void addExecutionListener(final IExecutionListener listener) {
		fExecutionListeners.add(listener);
	}

	@Override
	public void removeExecutionListener(final IExecutionListener listener) {
		fExecutionListeners.remove(listener);
	}

	protected void notifyExecutionListeners(final Script script, final int status) {
		for (final Object listener : fExecutionListeners.getListeners()) {
			try {
				((IExecutionListener) listener).notify(this, script, status);
			} catch (final Throwable e) {
				Logger.warning(Activator.PLUGIN_ID, "Execution listener did throw an exception", e);
			}
		}
	}

	public ScriptStackTrace getStackTrace() {
		return fStackTrace;
	}

	@Override
	public Object getExecutedFile() {
		for (final IScriptDebugFrame trace : getStackTrace()) {
			if (trace.getType() == IScriptDebugFrame.TYPE_FILE) {
				if (trace.getScript() != null) {
					final Object file = trace.getScript().getFile();
					if (file != null)
						return file;
				}
			}
		}

		return fExecutionRootFile;
	}

	public void setExecutionRootFile(Object executionRootFile) {
		fExecutionRootFile = executionRootFile;
	}

	public void setEngineDescription(final EngineDescription description) {
		fDescription = description;
	}

	@Override
	public void setVariable(final String name, final Object content) {
		if (fSetupDone)
			internalSetVariable(name, content);

		else
			fBufferedVariables.put(name, content);
	}

	@Override
	public Object getVariable(final String name) {
		if (fSetupDone)
			return internalGetVariable(name);

		return fBufferedVariables.get(name);
	}

	@Override
	public boolean hasVariable(final String name) {
		if (fSetupDone)
			return internalHasVariable(name);

		return fBufferedVariables.containsKey(name);
	}

	@Override
	public Map<String, Object> getVariables() {
		if (fSetupDone)
			return internalGetVariables();

		return Collections.unmodifiableMap(fBufferedVariables);
	}

	@Override
	public void addSecurityCheck(ActionType type, ISecurityCheck check) {
		if (!fSecurityChecks.containsKey(type))
			fSecurityChecks.put(type, new ArrayList<ISecurityCheck>());

		if (!fSecurityChecks.get(type).contains(check))
			fSecurityChecks.get(type).add(check);
	}

	protected List<Script> getScheduledScripts() {
		return fScheduledScripts;
	}

	public void setLaunch(ILaunch launch) {
		fLaunch = launch;
	}

	@Override
	public ILaunch getLaunch() {
		return fLaunch;
	}

	/**
	 * Internal version of {@link #getVariable(String)}. Only called after script engine was initialized successfully.
	 *
	 * @param name
	 *            variable to retrieve
	 * @return variable content
	 */
	protected abstract Object internalGetVariable(String name);

	/**
	 * Internal version of {@link #getVariables()}. Only called after script engine was initialized successfully.
	 *
	 * @return map of engine variables: name -> value
	 */
	protected abstract Map<String, Object> internalGetVariables();

	/**
	 * Internal version of {@link #hasVariable(String)}. Only called after script engine was initialized successfully.
	 *
	 * @param name
	 *            variable to verify
	 * @return <code>true</code> when variable exists
	 */
	protected abstract boolean internalHasVariable(String name);

	/**
	 * Internal version of {@link #setVariable(String, Object)}. Only called after script engine was initialized successfully.
	 *
	 * @param name
	 *            variable to set
	 * @param content
	 *            value to set variable to
	 */
	protected abstract void internalSetVariable(String name, Object content);

	/**
	 * Setup method for script engine. Run directly after the engine is activated.
	 *
	 * Unresolvable errors should be indicated by throwing a ScriptEngineException with details as to what went wrong.
	 */
	protected abstract void setupEngine() throws ScriptEngineException;

	/**
	 * Teardown engine. Called immediately before the engine terminates. This method is called even when {@link #setupEngine()} fails.
	 */
	protected abstract void teardownEngine() throws ScriptEngineException;

	/**
	 * Execute script code.
	 *
	 * @param script
	 *            script to be executed
	 * @param fileName
	 *            name of file executed
	 * @param uiThread
	 *            when set to <code>true</code> run code in UI thread
	 * @return execution result
	 * @throws Throwable
	 *             any exception thrown during script execution
	 */
	protected abstract Object execute(Script script, String fileName, boolean uiThread) throws Throwable;

	/**
	 * Simple monitor to forward cancellation requests to the script engine.
	 */
	private class ScriptEngineMonitor extends NullProgressMonitor implements IProgressMonitorWithBlocking {

		@Override
		public void setCanceled(boolean cancelled) {
			super.setCanceled(cancelled);

			if (isCanceled())
				terminate();
		}

		@Override
		public void setBlocked(IStatus reason) {
			// nothing to do
		}

		@Override
		public void clearBlocked() {
			// nothing to do
		}
	}
}
