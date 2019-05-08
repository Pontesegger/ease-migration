/*******************************************************************************
 * Copyright (c) 2013 Atos
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Arthur Daussy - initial implementation
 *******************************************************************************/
package org.eclipse.ease.tools;

public abstract class RunnableWithResult<T extends Object> implements Runnable {

	private T fResult = null;
	private Throwable fThrowable = null;

	@Override
	public final void run() {
		try {
			fResult = runWithTry();
		} catch (final Throwable e) {
			fThrowable = e;
		}
	}

	/**
	 * Get the result of the run execution. In case an exception was thrown it gets rethrown encapsulated in a {@link RuntimeException}.
	 *
	 * @return runnable result
	 * @throws RuntimeException
	 *             encapsulated exception encountered during run
	 */
	public T getResult() throws RuntimeException {
		if (fThrowable != null)
			throw new RuntimeException("", fThrowable);

		return fResult;
	}

	/**
	 * Get the result of the run execution. Does rethrow exceptions that occurred during the run.
	 *
	 * @return runnable result
	 * @throws RuntimeException
	 *             encapsulated exception encountered during run
	 */
	public T getResultOrThrow() throws Throwable {
		if (fThrowable != null)
			throw fThrowable;

		return fResult;
	}

	/**
	 * Run method to be implemented by the derived class. Exceptions thrown will automatically get caught an rethrown on a {@link #getResult()}.
	 */
	public abstract T runWithTry() throws Throwable;
}
