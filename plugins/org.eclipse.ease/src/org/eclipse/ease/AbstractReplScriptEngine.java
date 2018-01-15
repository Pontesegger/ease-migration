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
		final String referenceType = getTypeName(value);
		final EaseDebugVariable variable = new EaseDebugVariable(name, value, referenceType);

		// TODO find nicer approach to set type
		if ("Java Object".equals(referenceType))
			variable.setType(Type.JAVA_OBJECT);

		variable.getValue().setVariables(getDefinedVariables(value));

		return variable;
	}

	protected String getTypeName(Object object) {
		switch (getType(object)) {
		case JAVA_OBJECT:
			return "Java Object";

		case JAVA_PRIMITIVE:
			if (object instanceof Integer)
				return "int";

			if (object instanceof Byte)
				return "byte";

			if (object instanceof Short)
				return "short";

			if (object instanceof Boolean)
				return "boolean";

			if (object instanceof Character)
				return "char";

			if (object instanceof Long)
				return "long";

			if (object instanceof Double)
				return "double";

			if (object instanceof Float)
				return "float";

			// fall through
		default:
			return "";
		}
	}

	@Override
	public ScriptObjectType getType(Object object) {
		if (object != null) {
			if ((object instanceof Integer) || (object instanceof Byte) || (object instanceof Short) || (object instanceof Boolean)
					|| (object instanceof Character) || (object instanceof Long) || (object instanceof Double) || (object instanceof Float))
				return ScriptObjectType.JAVA_PRIMITIVE;

			return ScriptObjectType.JAVA_OBJECT;

		} else
			return ScriptObjectType.UNKNOWN;
	}
}
