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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A ScriptResult is a container for a script execution. As execution often occurs detached from the System thread, the result object contains an indicator for
 * pending and finished results. Results itself may contain an object or an exception.
 */
public class ScriptResult implements Future<Object> {

	/** Special void object for script methods not returning a result. */
	public static final Object VOID = new Object() {
		@Override
		public String toString() {
			return "<void>";
		}
	};

	private Object fResult = null;

	private ScriptExecutionException fException = null;

	private boolean fIsDone = false;

	/**
	 * Constructor of a pending execution.
	 */
	public ScriptResult() {
	}

	/**
	 * Verify that this ScriptResult is processed. If the result is ready, execution of the underlying script is done.
	 *
	 * @return true when processing is done
	 */
	public final synchronized boolean isReady() {
		return fIsDone;
	}

	/**
	 * Blocks execution until the execution result is ready.
	 *
	 * @throws InterruptedException
	 *             when waiting got interrupted externally
	 */
	public final synchronized void waitForResult() throws InterruptedException {
		if (!isReady())
			wait();
	}

	/**
	 * Blocks execution until the execution result is ready or the timeout is reached. Once this method returns you still need to query {@link #isReady()} as
	 * the timeout might have depleted.
	 *
	 * @param timeout
	 *            the maximum time to wait in milliseconds.
	 * @throws InterruptedException
	 *             when waiting got interrupted externally
	 * @throws TimeoutException
	 *             when result is not ready after timeout
	 */
	public final synchronized void waitForResult(long timeout) throws InterruptedException, TimeoutException {
		if (!isReady())
			wait(Math.max(0, timeout));

		if (!isReady())
			throw new TimeoutException(String.format("Result not ready after %d milliseconds", timeout));
	}

	/**
	 * Get the result value stored.
	 *
	 * @return result value
	 */
	public final synchronized Object getResult() {
		return fResult;
	}

	/**
	 * Set the result to be stored.
	 *
	 * @param result
	 *            object to be stored
	 */
	public final synchronized void setResult(final Object result) {
		fResult = result;
		fIsDone = true;

		notifyAll();
	}

	/**
	 * Set an exception to be stored for this result.
	 *
	 * @param e
	 *            exception to be stored
	 */
	public final synchronized void setException(final ScriptExecutionException e) {
		fException = e;
		fIsDone = true;

		notifyAll();
	}

	/**
	 * Get the exception stored within this result.
	 *
	 * @return stored exception or null
	 */
	public final synchronized ScriptExecutionException getException() {
		return fException;
	}

	@Override
	public final String toString() {
		if (fException != null)
			return "Exception: " + fException.getLocalizedMessage();

		return ((fResult != null) ? fResult.toString() : "[null]");
	}

	/**
	 * Checks whether this result contains an exception.
	 *
	 * @return true when this result contains an exception
	 */
	public final synchronized boolean hasException() {
		return (fException != null);
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		return isReady();
	}

	@Override
	public Object get() throws InterruptedException, ExecutionException {
		waitForResult();

		if (hasException())
			throw getException();

		return getResult();
	}

	@Override
	public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		waitForResult(unit.toMillis(timeout));

		if (hasException())
			throw getException();

		return getResult();
	}
}
