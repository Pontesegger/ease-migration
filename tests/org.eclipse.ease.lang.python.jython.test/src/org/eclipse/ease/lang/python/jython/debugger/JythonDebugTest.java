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

package org.eclipse.ease.lang.python.jython.debugger;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.LineBreakpoint;
import org.eclipse.ease.testhelper.AbstractDebugTest;
import org.junit.Ignore;
import org.junit.Test;

public class JythonDebugTest extends AbstractDebugTest {

	@Override
	protected String getScriptSource() throws IOException {
		return readResource("org.eclipse.ease.testhelper", "/resources/DebugTest/main.py");
	}

	@Override
	protected LineBreakpoint setBreakpoint(IFile file, int lineNumber) throws CoreException {
		final IMarker marker = file.createMarker("org.python.pydev.debug.pyStopBreakpointMarker");
		marker.setAttribute("org.eclipse.debug.core.enabled", true);
		marker.setAttribute("org.eclipse.debug.core.id", "org.python.pydev.debug");
		marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);

		final LineBreakpoint breakpoint = new LineBreakpoint() {

			@Override
			public String getModelIdentifier() {
				return "org.python.pydev.debug";
			}
		};

		breakpoint.setMarker(marker);

		DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(breakpoint);

		return breakpoint;
	}

	@Override
	protected IBreakpoint[] getBreakpoints() throws CoreException {
		return DebugPlugin.getDefault().getBreakpointManager().getBreakpoints("org.python.pydev.debug");
	}

	@Override
	protected String getEngineId() {
		return JythonDebuggerEngine.ENGINE_ID;
	}

	// FIXME tests temporarily disabled as they fail when loading the python debug target

	@Override
	@Test
	@Ignore
	public void hasDebugTarget() throws CoreException {
	}

	@Override
	@Test
	@Ignore
	public void breakpointLocation() throws CoreException {
	}

	@Override
	@Test
	@Ignore
	public void stepOverOnDebugTarget() throws CoreException {
	}

	@Override
	@Test
	@Ignore
	public void stepOverOnProcess() throws CoreException {
	}

	@Override
	@Test
	@Ignore
	public void stepOverOnThread() throws CoreException {
	}

	@Override
	@Test
	@Ignore
	public void stepOverOnStackFrame() throws CoreException {
	}

	@Override
	@Test
	@Ignore
	public void stepIntoOnDebugTarget() throws CoreException {
	}

	@Override
	@Test
	@Ignore
	public void stepIntoOnProcess() throws CoreException {
	}

	@Override
	@Test
	@Ignore
	public void stepIntoOnThread() throws CoreException {
	}

	@Override
	@Test
	@Ignore
	public void stepIntoOnStackFrame() throws CoreException {
	}

	@Override
	@Test
	@Ignore
	public void stepReturnOnDebugTarget() throws CoreException {
	}

	@Override
	@Test
	@Ignore
	public void stepReturnOnProcess() throws CoreException {
	}

	@Override
	@Test
	@Ignore
	public void stepReturnOnThread() throws CoreException {
	}

	@Override
	@Test
	@Ignore
	public void stepReturnOnStackFrame() throws CoreException {
	}

	@Override
	@Test
	@Ignore
	public void resumeOnDebugTarget() throws CoreException, IOException {
	}

	@Override
	@Test
	@Ignore
	public void resumeOnProcess() throws CoreException, IOException {
	}

	@Override
	@Test
	@Ignore
	public void resumeOnThread() throws CoreException, IOException {
	}

	@Override
	@Test
	@Ignore
	public void resumeOnStackFrame() throws CoreException, IOException {
	}

	@Override
	@Test
	@Ignore
	public void terminateDebugTargetInSuspendedState() throws CoreException {
	}

	@Override
	@Test
	@Ignore
	public void terminateProcessInSuspendedState() throws CoreException {
	}

	@Override
	@Test
	@Ignore
	public void terminateThreadInSuspendedState() throws CoreException {
	}

	@Override
	@Test
	@Ignore
	public void terminateStackFrameInSuspendedState() throws CoreException {
	}

	@Override
	@Test
	@Ignore
	public void disconnectDebugTargetInSuspendedState() throws CoreException {
	}

	@Override
	@Test
	@Ignore
	public void disconnectProcessInSuspendedState() throws CoreException {
	}

	@Override
	@Test
	@Ignore
	public void disconnectThreadInSuspendedState() throws CoreException {
	}

	@Override
	@Test
	@Ignore
	public void disconnectStackFrameInSuspendedState() throws CoreException {
	}

	@Override
	@Test
	@Ignore
	public void evaluateWatchExpression() throws CoreException, IOException {
	}

	@Override
	@Test
	@Ignore
	public void suspendedState() throws CoreException, IOException {
	}

	@Override
	@Test
	@Ignore
	public void terminatedState() throws CoreException {
	}

	@Override
	@Test
	@Ignore
	public void primitiveDoubleVariable() throws CoreException, IOException {
	}

	@Override
	@Test
	@Ignore
	public void primitiveStringVariable() throws CoreException, IOException {
	}

	@Override
	@Test
	@Ignore
	public void nullVariable() throws CoreException, IOException {
	}

	@Override
	@Test
	@Ignore
	public void nativeArrayVariable() throws CoreException, IOException {
	}

	@Override
	@Test
	@Ignore
	public void arrayVariableSorting() throws CoreException, IOException {
	}

	@Override
	@Test
	@Ignore
	public void nativeObjectVariable() throws CoreException, IOException {
	}

	@Override
	@Test
	@Ignore
	public void javaClassVariable() throws CoreException, IOException {
	}

	@Override
	@Test
	@Ignore
	public void innerScopeVariableBeforeOuterScopeVariable() throws CoreException {
	}

	@Override
	@Test
	@Ignore
	public void modifyVariableKeepingType() throws CoreException, IOException {
	}
}
