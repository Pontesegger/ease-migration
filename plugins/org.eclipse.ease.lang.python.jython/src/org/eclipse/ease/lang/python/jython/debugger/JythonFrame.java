/*******************************************************************************
 * Copyright (c) 2017 Kloesch and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Kloesch - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.python.jython.debugger;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ease.lang.python.debugger.IPyFrame;
import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyFunction;
import org.python.core.PyJavaPackage;
import org.python.core.PyJavaType;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyObjectDerived;

/**
 * Wrapper class for calling {@link IPyFrame} functionality on {@link PyObject}.
 */
public class JythonFrame implements IPyFrame {
	/**
	 * {@link IPyFrame} in Python form.
	 * <p>
	 * All calls simply wrap to this.
	 */
	private final PyObject fJythonFrame;

	/**
	 * Constructor only stores parameters to member.
	 *
	 * @param frame
	 *            {@link IPyFrame} in Python form.
	 */
	public JythonFrame(PyObject frame) {
		fJythonFrame = frame;
	}

	/**
	 * Utility method to check if a {@link PyObject} is a null object (either in Java or in Python).
	 *
	 * @param object
	 *            {@link PyObject} to check if it is null.
	 * @return <code>true</code> if object has null value.
	 */
	private static boolean isNull(PyObject object) {
		return (object != null) && !Py.None.equals(object);
	}

	@Override
	public String getFilename() {
		if (isNull(fJythonFrame)) {
			final PyObject filename = fJythonFrame.invoke("getFilename");
			return filename.asString();
		}
		return "<No Filename>";
	}

	@Override
	public int getLineNumber() {
		if (isNull(fJythonFrame)) {
			final PyObject lineNumber = fJythonFrame.invoke("getLineNumber");
			return Py.py2int(lineNumber);
		}
		return -1;
	}

	@Override
	public IPyFrame getParent() {
		if (isNull(fJythonFrame)) {
			return new JythonFrame(fJythonFrame.invoke("getParent"));
		}
		return null;
	}

	@Override
	public String toString() {
		return getFilename() + ": #" + getLineNumber();
	}

	@Override
	public Object getVariable(String name) {
		// FIXME implement
		return null;
	}

	@Override
	public Map<String, Object> getVariables() {
		final Map<String, Object> variables = new HashMap<>();

		final PyObject pyVariables = fJythonFrame.invoke("getVariables");

		if (pyVariables instanceof PyDictionary) {
			// TODO some overlap with JythonScriptEngine.internalGetVariables()
			final PyList keys = ((PyDictionary) pyVariables).keys();
			for (final Object key : keys) {
				Object value = ((PyDictionary) pyVariables).get(key);

				if (value instanceof PyObjectDerived)
					// unpack wrapped java objects
					value = ((PyObjectDerived) value).__tojava__(Object.class);

				if (value instanceof PyList)
					value = ((PyList) value).toArray();

				if ((!(value instanceof PyFunction)) && (!(value instanceof PyJavaPackage)) && (!(value instanceof PyJavaType)))
					variables.put(key.toString(), value);
			}
		}

		return variables;
	}

	@Override
	public void setVariable(String name, Object value) {
		fJythonFrame.invoke("setVariable", Py.java2py(name), Py.java2py(value));
	}
}
