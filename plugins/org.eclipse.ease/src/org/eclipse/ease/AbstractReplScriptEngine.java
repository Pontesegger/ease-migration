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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;

import org.eclipse.ease.debugging.model.EaseDebugVariable;
import org.eclipse.ease.debugging.model.EaseDebugVariable.Type;

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

	@Override
	public Collection<EaseDebugVariable> getDefinedVariables() {
		final Collection<EaseDebugVariable> result = new HashSet<>();

		for (final Entry<String, Object> entry : getVariables().entrySet()) {
			if (acceptVariable(entry.getValue())) {
				final EaseDebugVariable variable = createVariable(entry.getKey(), entry.getValue());
				result.add(variable);
			}
		}

		return result;
	}

	protected boolean acceptVariable(Object value) {
		return true;
	}

	protected Collection<EaseDebugVariable> getDefinedVariables(Object scope) {
		return null;
	}

	protected EaseDebugVariable createVariable(String name, Object value) {
		final String referenceType = getReferenceType(value);
		final EaseDebugVariable variable = new EaseDebugVariable(name, value, referenceType);

		// TODO find nicer approach to set type
		if ("Java Object".equals(referenceType))
			variable.setType(Type.JAVA_OBJECT);

		variable.getValue().setVariables(getDefinedVariables(value));

		return variable;
	}

	protected String getReferenceType(Object value) {
		if (value != null) {
			if (value instanceof Integer)
				return "int";

			if (value instanceof Byte)
				return "byte";

			if (value instanceof Short)
				return "short";

			if (value instanceof Boolean)
				return "boolean";

			if (value instanceof Character)
				return "char";

			if (value instanceof Long)
				return "long";

			if (value instanceof Double)
				return "double";

			if (value instanceof Float)
				return "float";

			return "Java Object";

		} else
			return "";
	}
}
