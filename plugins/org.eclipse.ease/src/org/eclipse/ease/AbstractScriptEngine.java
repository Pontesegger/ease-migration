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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IProgressMonitorWithBlocking;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.ease.ISecurityCheck.ActionType;
import org.eclipse.ease.debugging.EaseDebugFrame;
import org.eclipse.ease.debugging.IScriptDebugFrame;
import org.eclipse.ease.debugging.ScriptStackTrace;
import org.eclipse.ease.security.ScriptUIAccess;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.ui.internal.progress.ProgressManager.JobMonitor;

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

	/** List of code junks to be executed. */
	private final List<Script> fScheduledScripts = Collections.synchronizedList(new ArrayList<Script>());

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
	private final HashMap<ActionType, List<ISecurityCheck>> fSecurityChecks = new HashMap<>();

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
		super("[EASE " + name + " Engine]");

		// by default an engine shall be visible to the user. If the engine
		setSystem(false);
	}

	@Override
	public EngineDescription getDescription() {
		return fDescription;
	}

	@Override
	public final ScriptResult executeAsync(final Object content) {
		final Script script = (content instanceof Script) ? (Script) content : new Script(content);
		fScheduledScripts.add(script);

		synchronized (this) {
			notifyAll();
		}

		return script.getResult();
	}

	@Override
	public final ScriptResult executeSync(final Object content) throws InterruptedException {

		// we need to schedule the script first or the engine might finish before we can schedule the script
		final ScriptResult result = executeAsync(content);

		if (getState() == NONE)
			// automatically schedule engine as it is not started yet
			schedule();

		synchronized (result) {
			while (!result.isReady())
				result.wait();
		}

		return result;
	}

	@Override
	public final Object inject(final Object content) {
		return internalInject(content, false);
	}

	@Override
	public final Object injectUI(final Object content) {
		return internalInject(content, true);
	}

	private final Object internalInject(final Object content, final boolean uiThread) {
		// injected code shall not trigger a new event, therefore notifyListerners needs to be false
		ScriptResult result;
		if (content instanceof Script)
			result = inject((Script) content, false, uiThread);
		else
			result = inject(new Script(content), false, uiThread);

		if (result.hasException()) {
			// re-throw previous exception

			if (result.getException() instanceof RuntimeException)
				throw (RuntimeException) result.getException();

			throw new RuntimeException(result.getException().getMessage(), result.getException());
		}

		return result.getResult();
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

		synchronized (script.getResult()) {
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

				script.setResult(execute(script, script.getFile(), fStackTrace.get(0).getName(), uiThread));

			} catch (final BreakException e) {
				script.setResult(e.getCondition());

			} catch (final Throwable e) {
				script.setException(e);

				// only do the printing if this is the last script on the stack
				// otherwise we will print multiple times for each rethrow
				if (fStackTrace.size() <= 1)
					e.printStackTrace(getErrorStream());

			} finally {
				if (notifyListeners)
					notifyExecutionListeners(script, IExecutionListener.SCRIPT_END);
				else
					notifyExecutionListeners(script, IExecutionListener.SCRIPT_INJECTION_END);

				if (!fStackTrace.isEmpty())
					fStackTrace.remove(0);
			}
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

				// execute code
				if (!fScheduledScripts.isEmpty()) {
					final Script piece = fScheduledScripts.remove(0);
					inject(piece, true, false);

				} else {
					synchronized (this) {
						try {
							Logger.trace(Activator.PLUGIN_ID, TRACE_SCRIPT_ENGINE, "Engine idle: " + getName());
							wait();
						} catch (final InterruptedException e) {
						}
					}
				}
			}
		}

		if (getMonitor().isCanceled())
			returnStatus = Status.CANCEL_STATUS;

		return cleanupRun(returnStatus);
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
		}

		fScheduledScripts.clear();

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
			synchronized (this) {
				notifyAll();
			}

			closeStreams();

			Logger.trace(Activator.PLUGIN_ID, TRACE_SCRIPT_ENGINE, "Engine terminated: " + getName());

			fMonitor.done();
			fMonitor = null;
		}

		return returnStatus;
	}

	/**
	 * Add monitor to detect clicks on the stop button in the Progress view.
	 */
	private void addStopButtonMonitor() {
		if (fMonitor instanceof JobMonitor)
			((JobMonitor) fMonitor).addProgressListener(new ScriptEngineMonitor());
	}

	/**
	 * Evaluate if the engine shall terminate.
	 *
	 * @return <code>true</code> when termination is requested or there is no more work to be done
	 */
	protected boolean shallTerminate() {
		return getMonitor().isCanceled() || fScheduledScripts.isEmpty();
	}

	@Override
	public void terminate() {

		final IProgressMonitor monitor = getMonitor();
		if ((monitor != null) && (!monitor.isCanceled()))
			monitor.setCanceled(true);

		terminateCurrent();

		synchronized (this) {
			notify();
		}
	}

	/**
	 * Check engine for cancellation request and terminate if indicated by the monitor.
	 */
	public void checkForCancellation() {
		final IProgressMonitor monitor = getMonitor();
		if ((monitor != null) && (monitor.isCanceled())) {
			if (Thread.currentThread().equals(getThread()))
				throw new ScriptExecutionException("Engine got terminated");
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

			synchronized (this) {
				while (!isFinished())
					wait(1000);
			}
		}
	}

	@Override
	public void joinEngine(final long timeout) throws InterruptedException {
		if (!Thread.currentThread().equals(getThread())) {
			// we cannot join our own thread

			synchronized (this) {
				if (!isFinished())
					wait(timeout);
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
		for (final Object listener : fExecutionListeners.getListeners())
			((IExecutionListener) listener).notify(this, script, status);
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

	/**
	 * Split a string with comma separated arguments.
	 *
	 * @param arguments
	 *            comma separated arguments
	 * @return trimmed list of arguments
	 */
	public static final String[] extractArguments(final String arguments) {
		final ArrayList<String> args = new ArrayList<>();
		if (arguments != null) {

			final String[] tokens = arguments.split(",");
			for (final String token : tokens) {
				if (!token.trim().isEmpty())
					args.add(token.trim());
			}
		}

		return args.toArray(new String[args.size()]);
	}

	@Override
	public void addSecurityCheck(ActionType type, ISecurityCheck check) {
		if (!fSecurityChecks.containsKey(type))
			fSecurityChecks.put(type, new ArrayList<ISecurityCheck>());

		if (!fSecurityChecks.get(type).contains(check))
			fSecurityChecks.get(type).add(check);
	}

	@Override
	public void removeSecurityCheck(ISecurityCheck check) {
		for (final List<ISecurityCheck> entry : fSecurityChecks.values()) {
			entry.remove(check);
		}
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
	 */
	protected abstract Object internalGetVariable(String name);

	/**
	 * Internal version of {@link #getVariables()}. Only called after script engine was initialized successfully.
	 */
	protected abstract Map<String, Object> internalGetVariables();

	/**
	 * Internal version of {@link #hasVariable(String)}. Only called after script engine was initialized successfully.
	 */
	protected abstract boolean internalHasVariable(String name);

	/**
	 * Internal version of {@link #setVariable(String, Object)}. Only called after script engine was initialized successfully.
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
	 * @param fileName
	 *            name of file executed
	 * @param uiThread
	 * @param reader
	 *            reader for script data to be executed
	 * @param uiThread
	 *            when set to <code>true</code> run code in UI thread
	 * @return execution result
	 * @throws Throwable
	 *             any exception thrown during script execution
	 */
	protected abstract Object execute(Script script, Object reference, String fileName, boolean uiThread) throws Throwable;

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
