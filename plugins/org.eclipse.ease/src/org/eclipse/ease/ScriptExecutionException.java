/*******************************************************************************
 * Copyright (c) 2015 Andreas Wallner and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andreas Wallner - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease;

import java.io.File;
import java.io.PrintStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.ease.debugging.IScriptDebugFrame;
import org.eclipse.ease.debugging.ScriptStackTrace;

/**
 * A common class to be thrown if an error happens during script execution.
 *
 * The individual script engines should convert their internal exceptions into this one, so that we can display nicely formatted and useful error messages if
 * errors happen during script parsing/running.
 */
public class ScriptExecutionException extends RuntimeException {
	private static final long serialVersionUID = 1887518058581732543L;

	final private String fLineSource;
	final private int fColumnNumber;
	final private ScriptStackTrace fScriptStackTrace;
	final private String fErrorName;

	/**
	 * Internal constructor used for special script exceptions. Does not provide meaningful output for {@link #getMessage()} and {@link #printStackTrace()}.
	 */
	protected ScriptExecutionException() {
		super();

		fLineSource = null;
		fColumnNumber = 0;
		fScriptStackTrace = null;
		fErrorName = null;
	}

	/**
	 * Instantiate wrapper exception.
	 *
	 * @param message
	 *            error message
	 * @param lineNumber
	 *            number of the line where the error happened
	 * @param columnNumber
	 *            number of the column where the error happened
	 * @param lineSource
	 *            source code of the line where the error happened
	 * @param errorName
	 *            name/type of the error (exception, syntax error, etc)
	 * @param scriptStackTrace
	 *            script stack trace
	 * @param cause
	 *            root exception
	 */
	public ScriptExecutionException(final String message, final int columnNumber, final String lineSource, final String errorName,
			final ScriptStackTrace scriptStackTrace, Throwable cause) {
		super(message, cause);

		fLineSource = lineSource;
		fColumnNumber = columnNumber;
		fScriptStackTrace = scriptStackTrace;
		fErrorName = errorName;
	}

	@Override
	public final String getMessage() {
		final StringBuilder buffer = new StringBuilder();

		// TODO add In function '...' if ST is available

		if (fErrorName != null) {
			buffer.append(fErrorName);
			buffer.append(": ");
		}

		if (super.getMessage() != null)
			buffer.append(super.getMessage());
		else if (getCause() != null)
			buffer.append(getCause().getClass().getName());

		if (fLineSource != null) {
			// add source causing the error
			buffer.append('\n').append(fLineSource);

			// add marker for error location
			if (fColumnNumber != 0) {
				buffer.append('\n');
				for (int i = 1; i < fColumnNumber; i++)
					buffer.append(" ");

				buffer.append('^');
			}
		}

		return buffer.toString();
	}

	@Override
	public void printStackTrace(final PrintStream s) {
		s.println(this);

		if (fScriptStackTrace != null) {
			for (final IScriptDebugFrame traceElement : fScriptStackTrace) {
				if (traceElement.getScript() != null) {
					String fileName = "<unknown source>";
					final Object file = traceElement.getScript().getFile();
					if (file instanceof IFile)
						fileName = ((IFile) file).getName();
					else if (file instanceof File)
						fileName = ((File) file).getName();

					final String name = ((traceElement.getName() != null) && (!traceElement.getName().isEmpty())) ? ("," + traceElement.getName()) : "";
					final String lineInfo = (traceElement.getLineNumber() > 0) ? ":" + traceElement.getLineNumber() : "";
					s.println("\tat " + fileName + name + lineInfo);
				}
			}
		}

		final Throwable cause = getCause();
		if (cause != null) {
			s.println("\nJava Stacktrace:");
			cause.printStackTrace(s);
		}
	}
}
