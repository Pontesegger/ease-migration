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

	/**
	 * Get the exception stored within this result.
	 *
	 * @return stored exception or null
	 */
	public final ScriptExecutionException getException() {
		synchronized (this) {
			return fException;
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

	/**
	 * Checks whether this result contains an exception.
	 *
	 * @return true when this result contains an exception
	 */
	public final boolean hasException() {
		synchronized (this) {
			return (fException != null);
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

		return getResult();
	}

	@Override
	public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		waitForResult(unit.toMillis(timeout));

		return getResult();
	}

	private Object getResult() throws ScriptExecutionException {
		if (hasException())
			throw getException();

		synchronized (this) {
			return fResult;
		}
	}

	public Object get(long milliSeconds) throws InterruptedException, ExecutionException, TimeoutException {
		return get(milliSeconds, TimeUnit.MILLISECONDS);
	}

	private void waitForResult() throws ExecutionException {
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

	private void waitForResult(long milliseconds) throws InterruptedException, TimeoutException {
		final long waitUntil = System.currentTimeMillis() + milliseconds;
		synchronized (this) {
			while (!isDone() && (System.currentTimeMillis() < waitUntil))
				wait(waitUntil - System.currentTimeMillis());

			if (!isDone())
				throw new TimeoutException(String.format("Result not ready after %d milliseconds", milliseconds));
		}
	}
}
