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
 *     Arthur Daussy - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.lang.python.jython;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ease.AbstractReplScriptEngine;
import org.eclipse.ease.Script;
import org.eclipse.ease.ScriptEngineException;
import org.eclipse.ease.ScriptResult;
import org.eclipse.ease.lang.python.PythonHelper;
import org.eclipse.ease.lang.python.preferences.IPreferenceConstants;
import org.eclipse.ease.tools.RunnableWithResult;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.python.core.CompileMode;
import org.python.core.CompilerFlags;
import org.python.core.Py;
import org.python.core.PyBoolean;
import org.python.core.PyFloat;
import org.python.core.PyFunction;
import org.python.core.PyIgnoreMethodTag;
import org.python.core.PyInteger;
import org.python.core.PyJavaPackage;
import org.python.core.PyJavaType;
import org.python.core.PyList;
import org.python.core.PyLong;
import org.python.core.PyNone;
import org.python.core.PyObject;
import org.python.core.PyObjectDerived;
import org.python.core.PyString;
import org.python.core.PyStringMap;
import org.python.util.InteractiveInterpreter;

public class JythonScriptEngine extends AbstractReplScriptEngine {

	public static final String ENGINE_ID = "org.eclipse.ease.python.jython";

	protected InteractiveInterpreter fEngine;

	private PyObject fResult;

	private class DisplayHook extends PyObject {

		private static final long serialVersionUID = -6793040471701923706L;

		@Override
		public PyObject __call__(final PyObject arg0) {
			fResult = arg0;
			return Py.None;
		}
	}

	public JythonScriptEngine() {
		super("Jython");
	}

	protected JythonScriptEngine(String name) {
		super(name);
	}

	@Override
	public void terminateCurrent() {
		final Job terminationJob = new Job("Termination Jython script") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					getEngine().getSystemState().callExitFunc();
				} catch (final PyIgnoreMethodTag e) {
					// TODO handle this exception (but for now, at least know it happened)
					throw new RuntimeException(e);
				}

				return Status.OK_STATUS;
			}
		};

		terminationJob.setSystem(true);
		terminationJob.schedule();
	}

	@Override
	protected void setupEngine() throws ScriptEngineException {
		fEngine = new InteractiveInterpreter();

		// register display callback method to extract execution result
		final DisplayHook displayHook = new DisplayHook();
		getEngine().getSystemState().__displayhook__ = displayHook;
		getEngine().getSystemState().__dict__.__setitem__("displayhook", displayHook);

		getEngine().getSystemState().__setattr__("_jy_interpreter", Py.java2py(getEngine()));
		// imp.load("site");
		getEngine().getSystemState().path.insert(0, Py.EmptyString);

		setOutputStream(getOutputStream());
		setInputStream(getInputStream());
		setErrorStream(getErrorStream());

		/*
		 * Not optimized for now. This should done at a Python System level
		 */
		for (final String libraryPath : getPythonLibraries()) {
			if ((libraryPath != null) && !libraryPath.isEmpty()) {
				final PyString element = new PyString(libraryPath);
				final PyList systemPath = getEngine().getSystemState().path;
				if (!systemPath.contains(element)) {
					systemPath.add(0, element);
				}
			}
		}

		getEngine().getSystemState().settrace(new JythonTracer());

		// FIXME ev we need to set the system path to make jython aware of the changes
	}

	@Override
	protected Object execute(final Script script, final String fileName, final boolean uiThread) throws Throwable {
		if (uiThread) {
			// run in UI thread
			final RunnableWithResult<Object> runnable = new RunnableWithResult<Object>() {

				@Override
				public Object runWithTry() throws Throwable {

					// call execute again, now from correct thread
					return internalExecute(script, fileName);
				}
			};

			Display.getDefault().syncExec(runnable);

			return runnable.getResultOrThrow();

		} else
			// run in engine thread
			return internalExecute(script, fileName);
	}

	protected Object internalExecute(final Script script, final String fileName) throws Exception {
		fResult = Py.None;

		final PyObject code = Py.compile_command_flags(script.getCode(), "(none)", CompileMode.exec, new CompilerFlags(), true);
		if (code == Py.None)
			throw new IllegalArgumentException("Could not compile code");

		final Object file = script.getFile();
		File f = null;
		if (file instanceof IFile) {
			f = ((IFile) file).getLocation().toFile();
		} else if (file instanceof File) {
			f = ((File) file);

		}
		PyString newString = null;
		if (f != null) {
			final String absolutePath = f.getAbsolutePath();
			setVariable("__file__", absolutePath);
			final String containerPart = f.getParent();
			newString = Py.newString(containerPart);
			Py.getSystemState().path.insert(0, newString);
		}
		Py.exec(code, getEngine().getLocals(), null);
		if (newString != null) {
			Py.getSystemState().path.remove(newString);
		}
		return toJava(fResult);
	}

	private static Object toJava(final PyObject result) {
		if (result instanceof PyNone)
			return null;

		if (result instanceof PyObjectDerived)
			return result.__tojava__(Object.class);

		if (result instanceof PyBoolean)
			return ((PyBoolean) result).getBooleanValue();

		if (result instanceof PyInteger)
			return ((PyInteger) result).getValue();

		if (result instanceof PyFloat)
			return ((PyFloat) result).getValue();

		if (result instanceof PyLong)
			return ((PyLong) result).getValue();

		if (result instanceof PyString)
			return ((PyString) result).getString();

		if (result instanceof PyInteger)
			return ((PyInteger) result).getValue();

		return result;
	}

	@Override
	public void setOutputStream(final OutputStream outputStream) {
		super.setOutputStream(outputStream);

		if (getEngine() != null)
			getEngine().setOut(getOutputStream());
	}

	@Override
	public void setInputStream(final InputStream inputStream) {
		super.setInputStream(inputStream);

		if (getEngine() != null)
			getEngine().setIn(getInputStream());
	}

	@Override
	public void setErrorStream(final OutputStream errorStream) {
		super.setErrorStream(errorStream);

		if (getEngine() != null)
			getEngine().setErr(getErrorStream());
	}

	protected Collection<String> getPythonLibraries() {
		final IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		final String libraries = preferences.getString(IPreferenceConstants.PYTHON_LIBRARIES);
		return Arrays.asList(libraries.split(";"));
	}

	@Override
	public void registerJar(final URL url) {
		// FIXME implement jar classloader
		throw new UnsupportedOperationException("Registering JARs is not supported for python");
	}

	protected InteractiveInterpreter getEngine() {
		return fEngine;
	}

	@Override
	protected Object internalGetVariable(final String name) {
		Object value = getEngine().get(name);
		if (value instanceof PyObjectDerived)
			// unpack wrapped java objects
			value = ((PyObjectDerived) value).__tojava__(Object.class);

		return value;
	}

	@Override
	protected Map<String, Object> internalGetVariables() {
		final HashMap<String, Object> variables = new HashMap<>();

		final PyObject locals = getEngine().getLocals();
		final PyList keys = ((PyStringMap) locals).keys();
		for (final Object key : keys) {
			final Object value = internalGetVariable(key.toString());
			if ((!(value instanceof PyFunction)) && (!(value instanceof PyJavaPackage)) && (!(value instanceof PyJavaType)))
				variables.put(key.toString(), internalGetVariable(key.toString()));
		}

		return variables;
	}

	@Override
	protected boolean internalHasVariable(final String name) {
		return getEngine().get(name) != null;
	}

	@Override
	protected void internalSetVariable(final String name, final Object content) {
		if (!PythonHelper.isSaveName(name))
			throw new RuntimeException("\"" + name + "\" is not a valid Python variable name");

		getEngine().set(name, content);
		getEngine().getSystemState().builtins.__setitem__(name, Py.java2py(content));
	}

	@Override
	public String toString(Object object) {
		if ((object == null) || (ScriptResult.VOID.equals(object)))
			return null;

		return super.toString(object);
	}
}
