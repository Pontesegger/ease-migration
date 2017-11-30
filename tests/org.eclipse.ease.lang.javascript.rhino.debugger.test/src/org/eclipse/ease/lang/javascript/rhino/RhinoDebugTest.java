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

package org.eclipse.ease.lang.javascript.rhino;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.debug.core.model.IWatchExpressionResult;
import org.eclipse.debug.core.model.LineBreakpoint;
import org.eclipse.ease.debugging.model.EaseDebugElement;
import org.eclipse.ease.debugging.model.EaseDebugProcess;
import org.eclipse.ease.debugging.model.EaseDebugStackFrame;
import org.eclipse.ease.debugging.model.EaseDebugTarget;
import org.eclipse.ease.debugging.model.EaseDebugThread;
import org.eclipse.ease.debugging.model.EaseDebugVariable;
import org.eclipse.ease.debugging.model.EaseWatchExpressionDelegate;
import org.eclipse.ease.lang.javascript.rhino.debugger.RhinoDebuggerEngine;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ui.PlatformUI;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class RhinoDebugTest extends WorkspaceTest {

	private interface IDebugElementProvider {
		EaseDebugElement getDebugElement();
	}

	/** Amount of lines in the test source code. Can be used as a breakpoint location. */
	private static final int LAST_LINE_NUMBER = getLineNumber("print(\"End of script\");");

	private static int getLineNumber(String text) {
		try {
			final List<String> lines = Arrays
					.asList(readResource("org.eclipse.ease.lang.javascript.rhino.debugger", "/resources/RhinoDebugTest/main.js").split("\r?\n"));

			return lines.indexOf(text) + 1;
		} catch (final IOException e) {
			throw new RuntimeException("Line not found in source file", e);
		}
	}

	private static void sleep(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (final InterruptedException e) {
		}
	}

	private void clearBreakpoints() throws CoreException {
		for (final IBreakpoint breakpoint : getBreakpoints())
			DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(breakpoint, true);
	}

	private static LineBreakpoint setBreakpoint(IFile file, int lineNumber) throws CoreException {
		final IMarker marker = file.createMarker("org.eclipse.wst.jsdt.debug.core.line.breakpoint.marker");
		marker.setAttribute("org.eclipse.debug.core.enabled", true);
		marker.setAttribute("org.eclipse.debug.core.id", "org.eclipse.wst.jsdt.debug.model");
		marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);

		final LineBreakpoint breakpoint = new LineBreakpoint() {

			@Override
			public String getModelIdentifier() {
				return "org.eclipse.wst.jsdt.debug.model";
			}
		};

		breakpoint.setMarker(marker);

		DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(breakpoint);

		return breakpoint;
	}

	private static IBreakpoint[] getBreakpoints() throws CoreException {
		return DebugPlugin.getDefault().getBreakpointManager().getBreakpoints("org.eclipse.wst.jsdt.debug.model");
	}

	private IFile fFile;
	private ILaunch fLaunchMock;
	private IDebugTarget fDebugTarget = null;

	@Before
	public void setup() throws UnsupportedEncodingException, CoreException, IOException {
		final IProject project = createProject("Rhino Debug Test");
		fFile = createFile("RhinoDebugTest.js", readResource("org.eclipse.ease.lang.javascript.rhino.debugger", "/resources/RhinoDebugTest/main.js"), project);
		clearBreakpoints();

		fLaunchMock = mock(ILaunch.class);
		fDebugTarget = null;
	}

	@Test
	public void hasDebugTarget() throws CoreException {
		createEngine();
		assertNotNull(getDebugTarget());
	}

	@Test(timeout = 5000)
	public void breakpointLocation() throws CoreException {
		setBreakpoint(fFile, 2);
		assertEquals(1, getBreakpoints().length);

		final RhinoDebuggerEngine engine = createEngine();

		engine.executeAsync(fFile);
		final int suspendedEvents = runUntilTerminated(engine, () -> {
			final EaseDebugStackFrame stackFrame = getTopmostStackFrame();
			if (stackFrame != null) {
				assertEquals(2, stackFrame.getLineNumber());
				getDebugTarget().resume();
			}
		});

		assertEquals(1, suspendedEvents);
	}

	// ---------- step over tests ----------------------------------------------------------------------------------------------------

	public void stepOverTestTemplate(IDebugElementProvider elementProvider) throws CoreException {
		setBreakpoint(fFile, getLineNumber("var result = testMethod(2, 3);"));
		assertEquals(1, getBreakpoints().length);

		final RhinoDebuggerEngine engine = createEngine();

		engine.executeAsync(fFile);
		final int suspendedEvents = runUntilTerminated(engine, new Runnable() {

			private boolean fFirstSuspend = true;

			@Override
			public void run() {
				if (fFirstSuspend) {
					final IStackFrame[] stackFrames = getStackFrames();
					assertEquals(1, stackFrames.length);
					assertEquals(getLineNumber("var result = testMethod(2, 3);"), getTopmostStackFrame().getLineNumber());

					elementProvider.getDebugElement().stepOver();
					fFirstSuspend = false;

				} else {
					final IStackFrame[] stackFrames = getStackFrames();
					assertEquals(1, stackFrames.length);
					assertEquals(getLineNumber("var result = testMethod(2, 3);") + 1, getTopmostStackFrame().getLineNumber());

					elementProvider.getDebugElement().resume();
				}
			}
		});

		assertEquals(2, suspendedEvents);
	}

	@Test(timeout = 5000)
	public void stepOverOnDebugTarget() throws CoreException {
		stepOverTestTemplate(() -> getDebugTarget());
	}

	@Test(timeout = 5000)
	public void stepOverOnProcess() throws CoreException {
		stepOverTestTemplate(() -> getProcess());
	}

	@Test(timeout = 5000)
	public void stepOverOnThread() throws CoreException {
		stepOverTestTemplate(() -> getThread());
	}

	@Test(timeout = 5000)
	public void stepOverOnStackFrame() throws CoreException {
		stepOverTestTemplate(() -> getTopmostStackFrame());
	}

	// ---------- step into tests ----------------------------------------------------------------------------------------------------

	public void stepIntoTestTemplate(IDebugElementProvider elementProvider) throws CoreException {
		setBreakpoint(fFile, getLineNumber("var result = testMethod(2, 3);"));
		assertEquals(1, getBreakpoints().length);

		final RhinoDebuggerEngine engine = createEngine();

		engine.executeAsync(fFile);
		final int suspendedEvents = runUntilTerminated(engine, new Runnable() {

			private boolean fFirstSuspend = true;

			@Override
			public void run() {
				if (fFirstSuspend) {
					final IStackFrame[] stackFrames = getStackFrames();
					assertEquals(1, stackFrames.length);
					assertEquals(getLineNumber("var result = testMethod(2, 3);"), getTopmostStackFrame().getLineNumber());

					elementProvider.getDebugElement().stepInto();
					fFirstSuspend = false;

				} else {
					final IStackFrame[] stackFrames = getStackFrames();
					assertEquals(2, stackFrames.length);
					assertEquals(getLineNumber("function testMethod(a, b) {"), getTopmostStackFrame().getLineNumber());

					elementProvider.getDebugElement().resume();
				}
			}
		});

		assertEquals(2, suspendedEvents);
	}

	@Test(timeout = 5000)
	public void stepIntoOnDebugTarget() throws CoreException {
		stepIntoTestTemplate(() -> getDebugTarget());
	}

	@Test(timeout = 5000)
	public void stepIntoOnProcess() throws CoreException {
		stepIntoTestTemplate(() -> getProcess());
	}

	@Test(timeout = 5000)
	public void stepIntoOnThread() throws CoreException {
		stepIntoTestTemplate(() -> getThread());
	}

	@Test(timeout = 5000)
	public void stepIntoOnStackFrame() throws CoreException {
		stepIntoTestTemplate(() -> getTopmostStackFrame());
	}

	// ---------- step return tests ----------------------------------------------------------------------------------------------------

	public void stepReturnTestTemplate(IDebugElementProvider elementProvider) throws CoreException {
		setBreakpoint(fFile, getLineNumber("	return a + b;"));
		assertEquals(1, getBreakpoints().length);

		final RhinoDebuggerEngine engine = createEngine();

		engine.executeAsync(fFile);
		final int suspendedEvents = runUntilTerminated(engine, new Runnable() {

			private boolean fFirstSuspend = true;

			@Override
			public void run() {
				if (fFirstSuspend) {
					final IStackFrame[] stackFrames = getStackFrames();
					assertEquals(2, stackFrames.length);
					assertEquals(getLineNumber("	return a + b;"), getTopmostStackFrame().getLineNumber());

					elementProvider.getDebugElement().stepReturn();
					fFirstSuspend = false;

				} else {
					final IStackFrame[] stackFrames = getStackFrames();
					assertEquals(1, stackFrames.length);
					assertEquals(getLineNumber("var result = testMethod(2, 3);") + 1, getTopmostStackFrame().getLineNumber());

					elementProvider.getDebugElement().resume();
				}
			}
		});

		assertEquals(2, suspendedEvents);
	}

	@Test(timeout = 5000)
	public void stepReturnOnDebugTarget() throws CoreException {
		stepReturnTestTemplate(() -> getDebugTarget());
	}

	@Test(timeout = 5000)
	public void stepReturnOnProcess() throws CoreException {
		stepReturnTestTemplate(() -> getProcess());
	}

	@Test(timeout = 5000)
	public void stepReturnOnThread() throws CoreException {
		stepReturnTestTemplate(() -> getThread());
	}

	@Test(timeout = 5000)
	public void stepReturnOnStackFrame() throws CoreException {
		stepReturnTestTemplate(() -> getTopmostStackFrame());
	}

	// ---------- resume tests ----------------------------------------------------------------------------------------------------

	public void resumeTestTemplate(IDebugElementProvider elementProvider) throws CoreException {
		setBreakpoint(fFile, LAST_LINE_NUMBER);
		assertEquals(1, getBreakpoints().length);

		final RhinoDebuggerEngine engine = createEngine();

		engine.executeAsync(fFile);
		final int suspendedEvents = runUntilTerminated(engine, () -> {
			elementProvider.getDebugElement().resume();
		});

		assertEquals(1, suspendedEvents);
	}

	@Test(timeout = 5000)
	public void resumeOnDebugTarget() throws CoreException {
		resumeTestTemplate(() -> getDebugTarget());
	}

	@Test(timeout = 5000)
	public void resumeOnProcess() throws CoreException {
		resumeTestTemplate(() -> getProcess());
	}

	@Test(timeout = 5000)
	public void resumeOnThread() throws CoreException {
		resumeTestTemplate(() -> getThread());
	}

	@Test(timeout = 5000)
	public void resumeOnStackFrame() throws CoreException {
		resumeTestTemplate(() -> getTopmostStackFrame());
	}

	// ---------- terminate tests ----------------------------------------------------------------------------------------------------

	public void terminateTestTemplate(IDebugElementProvider elementProvider) throws CoreException {
		setBreakpoint(fFile, 1);
		setBreakpoint(fFile, 2);
		assertEquals(2, getBreakpoints().length);

		final RhinoDebuggerEngine engine = createEngine();

		engine.executeAsync(fFile);
		final int suspendedEvents = runUntilTerminated(engine, () -> {
			elementProvider.getDebugElement().terminate();
		});

		assertTrue(getDebugTarget().isTerminated());
		assertTrue(getProcess().isTerminated());
		assertTrue(getThread().isTerminated());
		assertTrue(getTopmostStackFrame().isTerminated());

		assertEquals(1, suspendedEvents);
	}

	@Test(timeout = 5000)
	public void terminateDebugTargetInSuspendedState() throws CoreException {
		terminateTestTemplate(() -> getDebugTarget());
	}

	@Test(timeout = 5000)
	public void terminateProcessInSuspendedState() throws CoreException {
		terminateTestTemplate(() -> getProcess());
	}

	@Test(timeout = 5000)
	public void terminateThreadInSuspendedState() throws CoreException {
		terminateTestTemplate(() -> getThread());
	}

	@Test(timeout = 5000)
	public void terminateStackFrameInSuspendedState() throws CoreException {
		terminateTestTemplate(() -> getTopmostStackFrame());
	}

	// ---------- disconnect tests ----------------------------------------------------------------------------------------------------

	public void disconnectTestTemplate(IDebugElementProvider elementProvider) throws CoreException {
		setBreakpoint(fFile, 1);
		setBreakpoint(fFile, 2);
		assertEquals(2, getBreakpoints().length);

		final RhinoDebuggerEngine engine = createEngine();

		engine.executeAsync(fFile);
		final int suspendedEvents = runUntilTerminated(engine, () -> {
			getDebugTarget().disconnect();

			assertTrue(getDebugTarget().isDisconnected());
			assertTrue(getProcess().isDisconnected());
			assertTrue(getThread().isDisconnected());
			assertTrue(getTopmostStackFrame().isDisconnected());
		});

		assertEquals(1, suspendedEvents);
	}

	@Test(timeout = 5000)
	public void disconnectDebugTargetInSuspendedState() throws CoreException {
		disconnectTestTemplate(() -> getDebugTarget());
	}

	@Test(timeout = 5000)
	public void disconnectProcessInSuspendedState() throws CoreException {
		disconnectTestTemplate(() -> getProcess());
	}

	@Test(timeout = 5000)
	public void disconnectThreadInSuspendedState() throws CoreException {
		disconnectTestTemplate(() -> getThread());
	}

	@Test(timeout = 5000)
	public void disconnectStackFrameInSuspendedState() throws CoreException {
		disconnectTestTemplate(() -> getTopmostStackFrame());
	}

	// ---------- watch expression tests ----------------------------------------------------------------------------------------------------

	@Test(timeout = 5000)
	public void evaluateWatchExpression() throws CoreException {
		final IWatchExpressionListener expressionListener = mock(IWatchExpressionListener.class);

		setBreakpoint(fFile, LAST_LINE_NUMBER);
		assertEquals(1, getBreakpoints().length);

		final RhinoDebuggerEngine engine = createEngine();

		engine.executeAsync(fFile);
		final int suspendedEvents = runUntilTerminated(engine, () -> {

			final EaseWatchExpressionDelegate expressionDelegate = new EaseWatchExpressionDelegate();
			expressionDelegate.evaluateExpression("primitiveInteger + 10", getTopmostStackFrame(), expressionListener);

			getDebugTarget().resume();
		});

		assertEquals(1, suspendedEvents);

		// extract watch result
		final ArgumentCaptor<IWatchExpressionResult> watchExpressionResult = ArgumentCaptor.forClass(IWatchExpressionResult.class);
		verify(expressionListener).watchEvaluationFinished(watchExpressionResult.capture());
		final IWatchExpressionResult variable = watchExpressionResult.getValue();

		assertEquals("primitiveInteger + 10", variable.getExpressionText());
		assertEquals("double", variable.getValue().getReferenceTypeName());
		assertEquals("52.0", variable.getValue().getValueString());
		assertFalse(variable.getValue().hasVariables());
		assertEquals(0, variable.getValue().getVariables().length);
	}

	// ---------- state enablements tests ----------------------------------------------------------------------------------------------------

	@Test(timeout = 5000)
	public void suspendedState() throws CoreException {
		setBreakpoint(fFile, LAST_LINE_NUMBER);
		assertEquals(1, getBreakpoints().length);

		final RhinoDebuggerEngine engine = createEngine();

		engine.executeAsync(fFile);
		final int suspendedEvents = runUntilTerminated(engine, () -> {

			assertFalse(getDebugTarget().isDisconnected());
			assertFalse(getProcess().isDisconnected());
			assertFalse(getThread().isDisconnected());
			assertFalse(getTopmostStackFrame().isDisconnected());

			assertFalse(getDebugTarget().isStepping());
			assertFalse(getProcess().isStepping());
			assertFalse(getThread().isStepping());
			assertFalse(getTopmostStackFrame().isStepping());

			assertTrue(getDebugTarget().isSuspended());
			assertTrue(getProcess().isSuspended());
			assertTrue(getThread().isSuspended());
			assertTrue(getTopmostStackFrame().isSuspended());

			assertFalse(getDebugTarget().isTerminated());
			assertFalse(getProcess().isTerminated());
			assertFalse(getThread().isTerminated());
			assertFalse(getTopmostStackFrame().isTerminated());

			assertTrue(getDebugTarget().canDisconnect());
			assertTrue(getProcess().canDisconnect());
			assertTrue(getThread().canDisconnect());
			assertTrue(getTopmostStackFrame().canDisconnect());

			assertTrue(getDebugTarget().canResume());
			assertTrue(getProcess().canResume());
			assertTrue(getThread().canResume());
			assertTrue(getTopmostStackFrame().canResume());

			assertTrue(getDebugTarget().canStepInto());
			assertTrue(getProcess().canStepInto());
			assertTrue(getThread().canStepInto());
			assertTrue(getTopmostStackFrame().canStepInto());

			assertTrue(getDebugTarget().canStepOver());
			assertTrue(getProcess().canStepOver());
			assertTrue(getThread().canStepOver());
			assertTrue(getTopmostStackFrame().canStepOver());

			assertTrue(getDebugTarget().canStepReturn());
			assertTrue(getProcess().canStepReturn());
			assertTrue(getThread().canStepReturn());
			assertTrue(getTopmostStackFrame().canStepReturn());

			assertFalse(getDebugTarget().canSuspend());
			assertFalse(getProcess().canSuspend());
			assertFalse(getThread().canSuspend());
			assertFalse(getTopmostStackFrame().canSuspend());

			assertTrue(getDebugTarget().canTerminate());
			assertTrue(getProcess().canTerminate());
			assertTrue(getThread().canTerminate());
			assertTrue(getTopmostStackFrame().canTerminate());

			getDebugTarget().resume();
		});

		assertEquals(1, suspendedEvents);
	}

	@Test(timeout = 5000)
	public void terminatedState() throws CoreException {

		final RhinoDebuggerEngine engine = createEngine();

		engine.executeAsync(fFile);
		final int suspendedEvents = runUntilTerminated(engine, () -> {
		});

		assertEquals(0, suspendedEvents);
		assertEquals(0, getStackFrames().length);

		assertTrue(getDebugTarget().isDisconnected());
		assertTrue(getProcess().isDisconnected());
		assertTrue(getThread().isDisconnected());

		assertFalse(getDebugTarget().isStepping());
		assertFalse(getProcess().isStepping());
		assertFalse(getThread().isStepping());

		assertFalse(getDebugTarget().isSuspended());
		assertFalse(getProcess().isSuspended());
		assertFalse(getThread().isSuspended());

		assertTrue(getDebugTarget().isTerminated());
		assertTrue(getProcess().isTerminated());
		assertTrue(getThread().isTerminated());

		assertFalse(getDebugTarget().canDisconnect());
		assertFalse(getProcess().canDisconnect());
		assertFalse(getThread().canDisconnect());

		assertFalse(getDebugTarget().canResume());
		assertFalse(getProcess().canResume());
		assertFalse(getThread().canResume());

		assertFalse(getDebugTarget().canStepInto());
		assertFalse(getProcess().canStepInto());
		assertFalse(getThread().canStepInto());

		assertFalse(getDebugTarget().canStepOver());
		assertFalse(getProcess().canStepOver());
		assertFalse(getThread().canStepOver());

		assertFalse(getDebugTarget().canStepReturn());
		assertFalse(getProcess().canStepReturn());
		assertFalse(getThread().canStepReturn());

		assertFalse(getDebugTarget().canSuspend());
		assertFalse(getProcess().canSuspend());
		assertFalse(getThread().canSuspend());

		assertFalse(getDebugTarget().canTerminate());
		assertFalse(getProcess().canTerminate());
		assertFalse(getThread().canTerminate());
	}

	// ---------- variable tests ----------------------------------------------------------------------------------------------------

	@Test(timeout = 5000)
	public void primitiveDoubleVariable() throws CoreException {
		setBreakpoint(fFile, LAST_LINE_NUMBER);
		assertEquals(1, getBreakpoints().length);

		final RhinoDebuggerEngine engine = createEngine();

		engine.executeAsync(fFile);
		final int suspendedEvents = runUntilTerminated(engine, () -> {
			try {
				final IStackFrame stackFrame = getTopmostStackFrame();
				assertNotNull(stackFrame);

				final IVariable variable = getVariable("primitiveInteger");
				assertEquals("double", variable.getReferenceTypeName());
				assertEquals("double", variable.getValue().getReferenceTypeName());
				assertEquals("42.0", variable.getValue().getValueString());
				assertFalse(variable.getValue().hasVariables());
				assertEquals(0, variable.getValue().getVariables().length);

				getDebugTarget().terminate();

			} catch (final DebugException e) {
				fail(e.getMessage());
			}
		});

		assertEquals(1, suspendedEvents);
	}

	@Test(timeout = 5000)
	public void primitiveStringVariable() throws CoreException {
		setBreakpoint(fFile, LAST_LINE_NUMBER);
		assertEquals(1, getBreakpoints().length);

		final RhinoDebuggerEngine engine = createEngine();

		engine.executeAsync(fFile);
		final int suspendedEvents = runUntilTerminated(engine, () -> {
			try {
				final IStackFrame stackFrame = getTopmostStackFrame();
				assertNotNull(stackFrame);

				final IVariable variable = getVariable("primitiveString");
				assertEquals("Java Object", variable.getReferenceTypeName());
				assertEquals("String", variable.getValue().getReferenceTypeName());
				assertEquals("\"Hello world\" (id=0)", variable.getValue().getValueString());
				assertTrue(variable.getValue().hasVariables());
				assertEquals(2, variable.getValue().getVariables().length);

				getDebugTarget().terminate();

			} catch (final DebugException e) {
				fail(e.getMessage());
			}
		});

		assertEquals(1, suspendedEvents);
	}

	@Test(timeout = 5000)
	public void nullVariable() throws CoreException {
		setBreakpoint(fFile, LAST_LINE_NUMBER);
		assertEquals(1, getBreakpoints().length);

		final RhinoDebuggerEngine engine = createEngine();

		engine.executeAsync(fFile);
		final int suspendedEvents = runUntilTerminated(engine, () -> {
			try {
				final IStackFrame stackFrame = getTopmostStackFrame();
				assertNotNull(stackFrame);

				final IVariable variable = getVariable("nullValue");
				assertEquals("", variable.getReferenceTypeName());
				assertEquals("", variable.getValue().getReferenceTypeName());
				assertEquals("null", variable.getValue().getValueString());
				assertFalse(variable.getValue().hasVariables());
				assertEquals(0, variable.getValue().getVariables().length);

				getDebugTarget().terminate();

			} catch (final DebugException e) {
				fail(e.getMessage());
			}
		});

		assertEquals(1, suspendedEvents);
	}

	@Test(timeout = 5000)
	public void nativeArrayVariable() throws CoreException {
		setBreakpoint(fFile, LAST_LINE_NUMBER);
		assertEquals(1, getBreakpoints().length);

		final RhinoDebuggerEngine engine = createEngine();

		engine.executeAsync(fFile);
		final int suspendedEvents = runUntilTerminated(engine, () -> {
			try {
				final IStackFrame stackFrame = getTopmostStackFrame();
				assertNotNull(stackFrame);

				IVariable variable = getVariable("nativeArray");
				assertEquals("JavaScript Array", variable.getReferenceTypeName());
				assertEquals("NativeArray", variable.getValue().getReferenceTypeName());
				assertEquals("array[3]", variable.getValue().getValueString());
				assertTrue(variable.getValue().hasVariables());
				assertEquals(3, variable.getValue().getVariables().length);

				final IVariable[] childVariables = variable.getValue().getVariables();
				variable = childVariables[0];
				assertEquals("[0]", variable.getName());
				assertEquals("double", variable.getReferenceTypeName());
				assertEquals("double", variable.getValue().getReferenceTypeName());
				assertEquals("1.0", variable.getValue().getValueString());
				assertFalse(variable.getValue().hasVariables());
				assertEquals(0, variable.getValue().getVariables().length);

				variable = childVariables[1];
				assertEquals("[1]", variable.getName());
				assertEquals("Java Object", variable.getReferenceTypeName());
				assertEquals("String", variable.getValue().getReferenceTypeName());
				assertEquals("\"foo\" (id=0)", variable.getValue().getValueString());
				assertTrue(variable.getValue().hasVariables());
				assertEquals(2, variable.getValue().getVariables().length);

				variable = childVariables[2];
				assertEquals("[2]", variable.getName());
				assertEquals("", variable.getReferenceTypeName());
				assertEquals("", variable.getValue().getReferenceTypeName());
				assertEquals("null", variable.getValue().getValueString());
				assertFalse(variable.getValue().hasVariables());
				assertEquals(0, variable.getValue().getVariables().length);

				getDebugTarget().terminate();

			} catch (final DebugException e) {
				fail(e.getMessage());
			}
		});

		assertEquals(1, suspendedEvents);
	}

	@Test(timeout = 5000)
	public void arrayVariableSorting() throws CoreException {
		setBreakpoint(fFile, LAST_LINE_NUMBER);
		assertEquals(1, getBreakpoints().length);

		final RhinoDebuggerEngine engine = createEngine();

		engine.executeAsync(fFile);
		final int suspendedEvents = runUntilTerminated(engine, () -> {
			try {
				final IStackFrame stackFrame = getTopmostStackFrame();
				assertNotNull(stackFrame);

				final IVariable parentVariable = getVariable("bigArray");
				final IVariable[] variables = parentVariable.getValue().getVariables();

				assertEquals(11, variables.length);

				for (int index = 0; index < variables.length; index++)
					assertEquals("[" + index + "]", variables[index].getName());

				getDebugTarget().terminate();

			} catch (final DebugException e) {
				fail(e.getMessage());
			}
		});

		assertEquals(1, suspendedEvents);
	}

	@Test(timeout = 5000)
	public void nativeObjectVariable() throws CoreException {
		setBreakpoint(fFile, LAST_LINE_NUMBER);
		assertEquals(1, getBreakpoints().length);

		final RhinoDebuggerEngine engine = createEngine();

		engine.executeAsync(fFile);
		final int suspendedEvents = runUntilTerminated(engine, () -> {
			try {
				final IStackFrame stackFrame = getTopmostStackFrame();
				assertNotNull(stackFrame);

				IVariable variable = getVariable("nativeObject");
				assertEquals("JavaScript Object", variable.getReferenceTypeName());
				assertEquals("NativeObject", variable.getValue().getReferenceTypeName());
				assertEquals("object{2}", variable.getValue().getValueString());
				assertTrue(variable.getValue().hasVariables());
				assertEquals(2, variable.getValue().getVariables().length);

				final IVariable[] childVariables = variable.getValue().getVariables();
				variable = childVariables[0];
				assertEquals("firstname", variable.getName());
				assertEquals("Java Object", variable.getReferenceTypeName());
				assertEquals("String", variable.getValue().getReferenceTypeName());
				assertEquals("\"John\" (id=0)", variable.getValue().getValueString());
				assertTrue(variable.getValue().hasVariables());
				assertEquals(2, variable.getValue().getVariables().length);

				variable = childVariables[1];
				assertEquals("lastname", variable.getName());
				assertEquals("Java Object", variable.getReferenceTypeName());
				assertEquals("String", variable.getValue().getReferenceTypeName());
				assertEquals("\"Doe\" (id=1)", variable.getValue().getValueString());
				assertTrue(variable.getValue().hasVariables());
				assertEquals(2, variable.getValue().getVariables().length);

				getDebugTarget().terminate();

			} catch (final DebugException e) {
				fail(e.getMessage());
			}
		});

		assertEquals(1, suspendedEvents);
	}

	@Test(timeout = 5000)
	public void javaClassVariable() throws CoreException {
		setBreakpoint(fFile, LAST_LINE_NUMBER);
		assertEquals(1, getBreakpoints().length);

		final RhinoDebuggerEngine engine = createEngine();

		engine.executeAsync(fFile);
		final int suspendedEvents = runUntilTerminated(engine, () -> {
			try {
				final IStackFrame stackFrame = getTopmostStackFrame();
				assertNotNull(stackFrame);

				IVariable variable = getVariable("file");
				assertEquals("Java Object", variable.getReferenceTypeName());
				assertEquals("File", variable.getValue().getReferenceTypeName());
				assertEquals("File (id=0)", variable.getValue().getValueString());
				assertTrue(variable.getValue().hasVariables());
				assertEquals(2, variable.getValue().getVariables().length);

				IVariable[] childVariables = variable.getValue().getVariables();
				variable = childVariables[0];
				assertEquals("path", variable.getName());
				assertEquals("IPath", variable.getReferenceTypeName());
				assertEquals("Path", variable.getValue().getReferenceTypeName());
				assertEquals("Path (id=1)", variable.getValue().getValueString());
				assertTrue(variable.getValue().hasVariables());
				assertEquals(3, variable.getValue().getVariables().length);

				variable = childVariables[1];
				assertEquals("workspace", variable.getName());
				assertEquals("Workspace", variable.getReferenceTypeName());
				assertEquals("Workspace", variable.getValue().getReferenceTypeName());
				assertEquals("Workspace (id=2)", variable.getValue().getValueString());
				assertTrue(variable.getValue().hasVariables());

				// verify nested variables from field 'path'
				childVariables = childVariables[0].getValue().getVariables();
				variable = childVariables[0];
				assertEquals("device", variable.getName());
				assertEquals("String", variable.getReferenceTypeName());
				assertEquals("", variable.getValue().getReferenceTypeName());
				assertEquals("null", variable.getValue().getValueString());
				assertFalse(variable.getValue().hasVariables());
				assertEquals(0, variable.getValue().getVariables().length);

				variable = childVariables[1];
				assertEquals("flags", variable.getName());
				assertEquals("int", variable.getReferenceTypeName());
				assertEquals("int", variable.getValue().getReferenceTypeName());
				assertFalse(variable.getValue().getValueString().isEmpty());
				assertFalse(variable.getValue().hasVariables());
				assertEquals(0, variable.getValue().getVariables().length);

				variable = childVariables[2];
				assertEquals("segments", variable.getName());
				assertEquals("String[]", variable.getReferenceTypeName());
				assertEquals("String[]", variable.getValue().getReferenceTypeName());
				assertEquals("String[2] (id=3)", variable.getValue().getValueString());
				assertTrue(variable.getValue().hasVariables());

				getDebugTarget().terminate();

			} catch (final DebugException e) {
				fail(e.getMessage());
			}
		});

		assertEquals(1, suspendedEvents);
	}

	// ---------- helper methods ----------------------------------------------------------------------------------------------------

	private EaseDebugVariable[] getVariables() {
		final EaseDebugStackFrame stackFrame = getTopmostStackFrame();

		// variables get populated via an asynchronous event, we need to wait until they are ready
		while (stackFrame.getVariables().length == 0)
			sleep(100);

		return stackFrame.getVariables();
	}

	private EaseDebugVariable getVariable(String name) {
		for (final EaseDebugVariable variable : getVariables()) {
			if (variable.getName().equals(name))
				return variable;
		}

		return null;
	}

	private int runUntilTerminated(RhinoDebuggerEngine engine, Runnable runOnSuspended) {
		int suspendedEvents = 0;

		engine.schedule();

		while (!engine.isFinished()) {
			if (getDebugTarget().isSuspended()) {
				suspendedEvents++;
				runOnSuspended.run();
			}

			sleep(100);
		}

		return suspendedEvents;
	}

	private RhinoDebuggerEngine createEngine() {
		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);
		final EngineDescription engineDescription = scriptService.getEngineByID(RhinoDebuggerEngine.ENGINE_ID);
		final RhinoDebuggerEngine engine = (RhinoDebuggerEngine) engineDescription.createEngine();
		engine.setupDebugger(fLaunchMock, false, false, false);

		return engine;
	}

	private EaseDebugTarget getDebugTarget() {
		if (fDebugTarget == null) {
			final ArgumentCaptor<IDebugTarget> debugTargetCaptor = ArgumentCaptor.forClass(IDebugTarget.class);
			verify(fLaunchMock).addDebugTarget(debugTargetCaptor.capture());
			fDebugTarget = debugTargetCaptor.getValue();
		}

		return (EaseDebugTarget) fDebugTarget;
	}

	private EaseDebugProcess getProcess() {
		return (EaseDebugProcess) getDebugTarget().getProcess();
	}

	private EaseDebugThread getThread() {
		IThread[] threads = getDebugTarget().getThreads();
		while ((threads == null) || (threads.length == 0)) {
			sleep(100);
			threads = getDebugTarget().getThreads();
		}

		return (EaseDebugThread) threads[0];
	}

	private IStackFrame[] getStackFrames() {
		return getThread().getStackFrames();
	}

	private EaseDebugStackFrame getTopmostStackFrame() {
		IStackFrame[] stackFrames = getStackFrames();
		while ((stackFrames == null) || (stackFrames.length == 0)) {
			sleep(100);
			stackFrames = getStackFrames();
		}

		return (EaseDebugStackFrame) stackFrames[0];
	}
}
