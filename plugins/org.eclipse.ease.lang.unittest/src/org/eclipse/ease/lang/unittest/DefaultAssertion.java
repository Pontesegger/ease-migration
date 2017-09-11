/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.lang.unittest;

import java.util.List;

import org.eclipse.ease.debugging.IScriptDebugFrame;

/**
 * A default implementation for {@link IAssertion} that adds support for error messages.
 */
public class DefaultAssertion implements IAssertion {

	/** Optional error message. */
	private final String fErrorMessage;
	private final boolean fValid;
	private final List<IScriptDebugFrame> fStackTrace;

	public DefaultAssertion(final List<IScriptDebugFrame> stackTrace, final boolean valid, final String errorDescription) {
		fStackTrace = stackTrace;
		fValid = valid;
		fErrorMessage = errorDescription;
	}

	public DefaultAssertion(final boolean valid, final String errorDescription) {
		this(null, valid, errorDescription);
	}

	/**
	 * Default constructor for invalid assertions.
	 *
	 * @param errorDescription
	 *            cause of error
	 */
	public DefaultAssertion(final String errorDescription) {
		this(null, false, errorDescription);
	}

	@Override
	public boolean isValid() {
		return fValid;
	}

	/**
	 * Get a stacktrace of the location that raised the assertion.
	 *
	 * @return stacktrace or <code>null</code>
	 */
	public List<IScriptDebugFrame> getStackTrace() {
		return fStackTrace;
	}

	@Override
	public String toString() {
		if (isValid())
			return IAssertion.VALID.toString();

		return (fErrorMessage != null) ? fErrorMessage : IAssertion.INVALID.toString();
	}
}
