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
	 * Set the result to be stored.
	 *
	 * @param result
	 *            object to be stored
	 */
	public final void setResult(final Object result) {
		synchronized (this) {
			if (isDone())
				throw new IllegalArgumentException("ScriptResult already completed");

			fResult = result;
			fIsDone = true;

			notifyAll();
		}
	}

	/**
	 * Set an exception to be stored for this result.
	 *
	 * @param e
	 *            exception to be stored
	 */
	public final void setException(final ScriptExecutionException e) {
		synchronized (this) {
			if (isDone())
				throw new IllegalArgumentException("ScriptResult already completed");

			fException = e;
			fIsDone = true;

			notifyAll();
		}
	}

	@Override
	public final String toString() {
		try {
			final Object result = get();
			return ((result == null) ? "[null]" : fResult.toString());

		} catch (final ExecutionException e) {
			return "Exception: " + e.getLocalizedMessage();
		}
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
		synchronized (this) {
			return fIsDone;
		}
	}

	@Override
	public Object get() throws ExecutionException {
		waitForResult();

		return getResultOrThrow();
	}

	@Override
	public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		waitForResult(unit.toMillis(timeout));

		return getResultOrThrow();
	}

	public Object get(long milliSeconds) throws InterruptedException, ExecutionException, TimeoutException {
		return get(milliSeconds, TimeUnit.MILLISECONDS);
	}

	private Object getResultOrThrow() throws ScriptExecutionException {
		synchronized (this) {
			if (fException != null)
				throw fException;

			return fResult;
		}
	}

	/**
	 * Blocks execution until the execution result is ready.
	 *
	 * @deprecated use {@link #get()}
	 */
	@Deprecated
	public void waitForResult() throws ExecutionException {
		// instead of removing this method should be marked 'private'
		synchronized (this) {
			while (!isDone()) {
				try {
					wait();
				} catch (final InterruptedException e) {
					throw new ScriptEngineInterruptedException(e);
				}
			}
		}
	}

	/**
	 * Blocks execution until the execution result is ready or the timeout is reached. Once this method returns you still need to query {@link #isReady()} as
	 * the timeout might have depleted.
	 *
	 * @param milliseconds
	 *            the maximum time to wait in milliseconds.
	 * @deprecated use {@link #get(long, TimeUnit)}
	 */
	@Deprecated
	public void waitForResult(long milliseconds) throws InterruptedException, TimeoutException {
		// instead of removing this method should be marked 'private'
		final long waitUntil = System.currentTimeMillis() + milliseconds;
		synchronized (this) {
			while (!isDone() && (System.currentTimeMillis() < waitUntil))
				wait(waitUntil - System.currentTimeMillis());

			if (!isDone())
				throw new TimeoutException(String.format("Result not ready after %d milliseconds", milliseconds));
		}
	}

	/**
	 * Verify that this ScriptResult is processed. If the result is ready, execution of the underlying script is done.
	 *
	 * @return true when processing is done
	 * @deprecated use {@link #isDone()}
	 */
	@Deprecated
	public final boolean isReady() {
		return isDone();
	}

	/**
	 * Get the result value stored.
	 *
	 * @return result value
	 * @deprecated use {@link #get()}
	 */
	@Deprecated
	public final Object getResult() {
		try {
			return get();
		} catch (final ExecutionException e) {
			throw new RuntimeException("Execution failed", e);
		}
	}

	/**
	 * Get the exception stored within this result.
	 *
	 * @return stored exception or null
	 * @deprecated use {@link #get()}
	 */
	@Deprecated
	public final Throwable getException() {
		try {
			get();
			throw new RuntimeException("Execution did not throw an exception");

		} catch (final ExecutionException e) {
			return e;
		}
	}

	/**
	 * Checks whether this result contains an exception.
	 *
	 * @return true when this result contains an exception
	 * @deprecated use {@link #get()}
	 */
	@Deprecated
	public final boolean hasException() {
		try {
			get();
			return false;

		} catch (final ExecutionException e) {
			return true;
		}
	}
}
