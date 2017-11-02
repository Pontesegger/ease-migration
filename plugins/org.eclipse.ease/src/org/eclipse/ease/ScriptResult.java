/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease;

/**
 * A ScriptResult is a container for a script execution. As execution often occurs detached from the System thread, the result object contains an indicator for
 * pending and finished results. Results itself may contain an object or an exception.
 */
public class ScriptResult {

	/** script execution result. */
	private Object fResult = null;

	/** script execution exception. */
	private Throwable fException = null;

	private boolean fIsDone = false;

	/**
	 * Constructor of a pending execution.
	 */
	public ScriptResult() {
	}

	/**
	 * Constructor for a finished execution.
	 *
	 * @param result
	 *            result of execution
	 */
	public ScriptResult(final Object result) {
		setResult(result);
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
	 */
	public final synchronized void waitForResult() {
		while (!isReady()) {
			try {
				this.wait();
			} catch (final InterruptedException e) {
			}
		}
	}

	/**
	 * Blocks execution until the execution result is ready or the timeout is reached. Once this method returns you still need to query {@link #isReady()} as
	 * the timeout might have depleted.
	 *
	 * @param timeout
	 *            the maximum time to wait in milliseconds.
	 */
	public final synchronized void waitForResult(long timeout) {
		final long stopTimestamp = System.currentTimeMillis() + timeout;

		while ((!isReady()) && (System.currentTimeMillis() < stopTimestamp)) {
			try {
				this.wait(Math.max(0, System.currentTimeMillis() - stopTimestamp));
			} catch (final InterruptedException e) {
			}
		}
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
	final synchronized void setResult(final Object result) {
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
	final synchronized void setException(final Throwable e) {
		fException = e;
		fIsDone = true;
		notifyAll();
	}

	/**
	 * Get the exception stored within this result.
	 *
	 * @return stored exception or null
	 */
	public final synchronized Throwable getException() {
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
	public final boolean hasException() {
		return (fException != null);
	}
}
