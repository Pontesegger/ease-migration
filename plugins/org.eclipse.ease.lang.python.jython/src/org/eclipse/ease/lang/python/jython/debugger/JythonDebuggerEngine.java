/*******************************************************************************
 * Copyright (c) 2014 Martin Kloesch and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License_Identifier: EPL-2.0
 *
 * Contributors:
 *     Martin Kloesch - initial implementation of Debugger extensions
 *******************************************************************************/
package org.eclipse.ease.lang.python.jython.debugger;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.ease.Script;
import org.eclipse.ease.ScriptEngineException;
import org.eclipse.ease.ScriptObjectType;
import org.eclipse.ease.debugging.EaseDebugFrame;
import org.eclipse.ease.debugging.ScriptStackTrace;
import org.eclipse.ease.debugging.model.EaseDebugVariable;
import org.eclipse.ease.debugging.model.EaseDebugVariable.Type;
import org.eclipse.ease.lang.python.debugger.IPythonDebugEngine;
import org.eclipse.ease.lang.python.debugger.PythonDebugger;
import org.eclipse.ease.lang.python.debugger.PythonEventDispatchJob;
import org.eclipse.ease.lang.python.debugger.ResourceHelper;
import org.eclipse.ease.lang.python.debugger.model.PythonDebugTarget;
import org.eclipse.ease.lang.python.jython.JythonScriptEngine;
import org.python.core.Py;
import org.python.core.PyBoolean;
import org.python.core.PyDictionary;
import org.python.core.PyFloat;
import org.python.core.PyInstance;
import org.python.core.PyInteger;
import org.python.core.PyLong;
import org.python.core.PyNone;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyStringMap;

/**
 * A script engine to execute/debug Python code on a Jython interpreter.
 *
 * Uses most of JythonScriptEngine's functionality and only extends it when file is to be debugged.
 */
public class JythonDebuggerEngine extends JythonScriptEngine implements IPythonDebugEngine {

	public static final String ENGINE_ID = "org.eclipse.ease.python.jythonDebugger";

	private PythonDebugger fDebugger = null;

	public JythonDebuggerEngine() {
		super("Jython Debugger");

		setDebugger(new JythonDebugger(this, false));
	}

	@Override
	public void setDebugger(final PythonDebugger debugger) {
		fDebugger = debugger;
	}

	@Override
	protected void setupEngine() throws ScriptEngineException {
		super.setupEngine();

		// in case we were called using "Run as"
		if (fDebugger != null) {
			try {
				// load python part of debugger
				try (final InputStream stream = ResourceHelper.getResourceStream("org.eclipse.ease.lang.python", "pysrc/edb.py")) {
					internalSetVariable(PythonDebugger.PYTHON_DEBUGGER_VARIABLE, Py.java2py(fDebugger));
					super.internalExecute(new Script("Load Python debugger", stream), "Load Python Debugger");
				}

			} catch (final Throwable e) {
				throw new ScriptEngineException("Failed to load Python Debugger", e);
			}
		}
	}

	@Override
	protected Object internalExecute(final Script script, final String fileName) throws Exception {
		if (fDebugger != null) {
			return fDebugger.execute(script);
		}
		return super.internalExecute(script, fileName);
	}

	@Override
	public void setupDebugger(final ILaunch launch, final boolean suspendOnStartup, final boolean suspendOnScriptLoad, final boolean showDynamicCode) {
		final PythonDebugTarget target = new PythonDebugTarget(launch, suspendOnStartup, suspendOnScriptLoad, showDynamicCode);
		launch.addDebugTarget(target);

		final PythonDebugger debugger = new JythonDebugger(this, showDynamicCode);

		setDebugger(debugger);

		new PythonEventDispatchJob(target, debugger);
	}

	@Override
	public ScriptStackTrace getExceptionStackTrace() {
		// FIXME to be implemented
		return null;
	}

	@Override
	public ScriptStackTrace getExceptionStackTrace(Object thread) {
		// FIXME to be implemented
		return null;
	}

	@Override
	public Object removeVariable(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<EaseDebugVariable> getVariables(Object scope) {
		final Collection<EaseDebugVariable> variables = new ArrayList<>();

		if (scope instanceof EaseDebugFrame) {
			// local scope
			for (final Entry<String, Object> entry : ((EaseDebugFrame) scope).getVariables().entrySet())
				variables.add(createVariable(entry.getKey(), entry.getValue()));

		} else {
			// global scope
			for (final Entry<String, Object> entry : getVariables().entrySet())
				variables.add(createVariable(entry.getKey(), entry.getValue()));
		}

		return variables;
	}

	@Override
	public ScriptObjectType getType(Object object) {
		if (object instanceof PyDictionary)
			return ScriptObjectType.NATIVE_OBJECT;

		if (object instanceof PyInstance)
			return ScriptObjectType.NATIVE_OBJECT;

		if ((object != null) && (object.getClass().isArray()))
			return ScriptObjectType.NATIVE_ARRAY;

		return super.getType(object);
	}

	@Override
	protected EaseDebugVariable createVariable(String name, Object value) {
		if (value instanceof PyNone)
			return super.createVariable(name, null);

		if (value instanceof PyString)
			return super.createVariable(name, Py.tojava((PyObject) value, String.class));

		if (value instanceof PyInteger)
			return super.createVariable(name, Py.tojava((PyObject) value, Integer.class));

		if (value instanceof PyLong)
			return super.createVariable(name, Py.tojava((PyObject) value, Long.class));

		if (value instanceof PyFloat)
			return super.createVariable(name, Py.tojava((PyObject) value, Float.class));

		if (value instanceof PyBoolean)
			return super.createVariable(name, Py.tojava((PyObject) value, Boolean.class));

		final EaseDebugVariable variable = super.createVariable(name, value);

		if (value instanceof PyDictionary) {
			variable.getValue().setValueString("object{" + getDefinedVariables(value).size() + "}");
			variable.setType(Type.NATIVE_OBJECT);
		}

		if (value instanceof PyInstance) {
			variable.getValue().setValueString("object{" + getDefinedVariables(value).size() + "}");
			variable.setType(Type.NATIVE_OBJECT);
		}

		if ((value != null) && (value.getClass().isArray())) {
			variable.getValue().setValueString("array[" + getDefinedVariables(value).size() + "]");
			variable.setType(Type.NATIVE_ARRAY);
		}

		return variable;
	}

	@Override
	protected Collection<EaseDebugVariable> getDefinedVariables(Object scope) {
		if (scope instanceof PyDictionary) {
			final Collection<EaseDebugVariable> childObjects = new ArrayList<>();

			for (final Object id : ((PyDictionary) scope).keySet()) {
				final Object object = ((PyDictionary) scope).get(id);
				if (acceptVariable(object))
					childObjects.add(createVariable(id.toString(), object));
			}

			return childObjects;

		} else if (scope instanceof PyInstance) {
			final Collection<EaseDebugVariable> childObjects = new ArrayList<>();

			final PyObject fields = ((PyInstance) scope).__dict__;
			if (fields instanceof PyStringMap) {
				for (final Object id : ((PyStringMap) fields).keys().toArray()) {
					final PyObject value = fields.__finditem__(id.toString());

					if (acceptVariable(value))
						childObjects.add(createVariable(id.toString(), value));
				}
			}

			return childObjects;
		}

		if ((scope != null) && (scope.getClass().isArray())) {
			final Collection<EaseDebugVariable> childObjects = new ArrayList<>();

			final Object[] array = (Object[]) scope;
			for (int index = 0; index < array.length; index++) {
				childObjects.add(createVariable("[" + Integer.toString(index) + "]", array[index]));
			}

			return childObjects;
		}

		return super.getDefinedVariables(scope);
	}

	@Override
	protected boolean acceptVariable(Object value) {
		return true;
	}
}
