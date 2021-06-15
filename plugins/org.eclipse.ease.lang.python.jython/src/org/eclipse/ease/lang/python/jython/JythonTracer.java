/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License_Identifier: EPL-2.0
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.lang.python.jython;

import org.python.core.PyObject;

public class JythonTracer extends PyObject {

	@Override
	public PyObject __call__(final PyObject arg0, final PyObject arg1, final PyObject arg2) {
		if ("call".equals(arg1.toString())) {
			// call
			// return super.__call__(arg0, arg1, arg2);
		} else {
			// return super.__call__(arg0, arg1, arg2);
		}

		return this;
	}
}
