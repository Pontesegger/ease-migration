/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
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
 * Adds generic REPL support to the AbstractScriptEngine.
 */
public abstract class AbstractReplScriptEngine extends AbstractScriptEngine implements IReplEngine {

	/** Indicator to terminate once this Job gets IDLE. */
	private volatile boolean fTerminateOnIdle = true;

	/**
	 * Constructor. Sets the name for the underlying job.
	 *
	 * @param name
	 *            name of script engine job
	 */
	public AbstractReplScriptEngine(final String name) {
		super(name);
	}

	@Override
	public final void setTerminateOnIdle(final boolean terminate) {
		fTerminateOnIdle = terminate;
		synchronized (this) {
			notifyAll();
		}
	}

	@Override
	public boolean getTerminateOnIdle() {
		return fTerminateOnIdle;
	}

	/**
	 * Get termination status of the interpreter. A terminated interpreter cannot be restarted.
	 *
	 * @return true if interpreter is terminated.
	 */
	@Override
	protected boolean isTerminated() {
		return fTerminateOnIdle && isIdle();
	}

	/**
	 * Get idle status of the interpreter. The interpreter is IDLE if there are no pending execution requests and the interpreter is not terminated.
	 *
	 * @return true if interpreter is IDLE
	 */
	@Override
	public boolean isIdle() {
		return super.isTerminated();
	}

	@Override
	public void terminate() {
		setTerminateOnIdle(true);

		super.terminate();
	}
}
