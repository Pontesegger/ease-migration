/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ease.debugging.model.EaseDebugLastExecutionResult;
import org.eclipse.ease.debugging.model.EaseDebugVariable;
import org.eclipse.ease.debugging.model.EaseDebugVariable.Type;

/**
 * Adds generic REPL support to the AbstractScriptEngine.
 */
public abstract class AbstractReplScriptEngine extends AbstractScriptEngine implements IReplEngine {

	/** Indicator to terminate once this Job gets IDLE. */
	private volatile boolean fTerminateOnIdle = true;

	/** Result of last script execution. */
	private ScriptResult fLastExecutionResult = null;

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

		// if the engine remains active when IDLE this is likely a shell, therefore hide its job from users
		if (getState() == Job.NONE)
			// we can only set this before the engine got started
			setSystem(!terminate);

		synchronized (this) {
			notifyAll();
		}
	}

	@Override
	public boolean getTerminateOnIdle() {
		return fTerminateOnIdle;
	}

	@Override
	protected boolean shallTerminate() {
		if (getTerminateOnIdle())
			return super.shallTerminate();

		return getMonitor().isCanceled();
	}

	@Override
	public void terminate() {
		setTerminateOnIdle(true);

		super.terminate();
	}

	@Override
	public Collection<EaseDebugVariable> getDefinedVariables() {
		return getVariables().entrySet().stream().filter(entry -> acceptVariable(entry.getValue()))
				.map(entry -> createVariable(entry.getKey(), entry.getValue())).collect(Collectors.toSet());
	}

	/**
	 * Check if variable should be filtered.
	 *
	 * @param value
	 *            value to verify
	 * @return <code>true</code> if variable should be shown
	 */
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

		else
			variable.getValue().setVariables(getDefinedVariables(value));

		return variable;
	}

	protected String getTypeName(Object object) {
		switch (getType(object)) {
		case NATIVE_ARRAY:
			String languageName = getDescription().getSupportedScriptTypes().get(0).getName();
			return languageName + " Array";

		case NATIVE_OBJECT:
			languageName = getDescription().getSupportedScriptTypes().get(0).getName();
			return languageName + " Object";

		case NATIVE:
			languageName = getDescription().getSupportedScriptTypes().get(0).getName();
			return "Generic " + languageName;

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

			if (ScriptResult.VOID.equals(object))
				return ScriptObjectType.VOID;

			return ScriptObjectType.JAVA_OBJECT;

		} else
			return ScriptObjectType.NULL;
	}

	@Override
	public String toString(Object object) {
		if (ScriptResult.VOID.equals(object))
			return "<undefined>";

		if (object != null)
			return object.toString();

		return "null";
	}

	protected final String buildArrayString(List<Object> elements) {
		final StringBuilder output = new StringBuilder("[");

		for (final Object element : elements)
			output.append(", ").append(toString(element));

		output.append(']');

		if (output.length() > 4)
			output.delete(1, 3);

		return output.toString();
	}

	protected final String buildObjectString(Map<String, Object> elements) {
		final StringBuilder output = new StringBuilder("{");

		for (final Entry<String, Object> entry : elements.entrySet())
			output.append(", ").append(entry.getKey()).append(": ").append(toString(entry.getValue()));

		output.append('}');

		if (output.length() > 4)
			output.delete(1, 3);

		return output.toString();
	}

	@Override
	protected void notifyExecutionListeners(Script script, int status) {
		if (IExecutionListener.SCRIPT_END == status)
			fLastExecutionResult = script.getResult();

		super.notifyExecutionListeners(script, status);
	}

	@Override
	public EaseDebugVariable getLastExecutionResult() {
		if (fLastExecutionResult != null) {
			try {
				final Object result = fLastExecutionResult.get();

				if (Objects.equals(ScriptResult.VOID, result)) {
					return new EaseDebugLastExecutionResult("no method return value", ScriptResult.VOID, "");

				} else {
					final EaseDebugVariable variable = createVariable("script returned", fLastExecutionResult.getResult());
					return new EaseDebugLastExecutionResult(variable);
				}

			} catch (final ExecutionException e) {
				return new EaseDebugLastExecutionResult("script exception", e);
			}

		} else
			return new EaseDebugLastExecutionResult("no method return value", null, "");
	}

	@Override
	protected void teardownEngine() {
		fLastExecutionResult = null;
	}
}
