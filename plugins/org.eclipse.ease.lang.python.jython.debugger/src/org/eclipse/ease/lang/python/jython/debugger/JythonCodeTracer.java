/*******************************************************************************
 * Copyright (c) 2017 Martin Kloesch and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Martin Kloesch - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.python.jython.debugger;

import org.eclipse.ease.Script;
import org.eclipse.ease.lang.python.debugger.ICodeTracer;
import org.python.core.Py;
import org.python.core.PyObject;

/**
 * Wrapper class for calling {@link ICodeTracer} functionality on {@link PyObject}.
 */
public class JythonCodeTracer implements ICodeTracer {
	/**
	 * {@link ICodeTracer} in Python form.
	 * <p>
	 * All calls simply wrap to this.
	 */
	private final PyObject fPyTracer;

	/**
	 * Constructor only stores parameters to member.
	 *
	 * @param tracer
	 *            {@link ICodeTracer} in Python form.
	 */
	public JythonCodeTracer(PyObject tracer) {
		fPyTracer = tracer;
	}

	@Override
	public Object run(Script script, String filename) {
		final PyObject result = fPyTracer.invoke("run", Py.java2py(script), Py.java2py(filename));

		return Py.tojava(result, Object.class);
	}
}
