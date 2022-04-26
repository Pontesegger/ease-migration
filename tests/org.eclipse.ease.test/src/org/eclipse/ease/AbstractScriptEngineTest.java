/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
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
package org.eclipse.ease;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.ease.ISecurityCheck.ActionType;
import org.eclipse.ease.service.EngineDescription;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AbstractScriptEngineTest {

	protected static final CharSequence ERROR_MARKER = "ERROR";
	protected static final CharSequence INJECT_MARKER = "INJECT";

	private AbstractScriptEngine fTestEngine;

	@BeforeEach
	public void setup() {
		fTestEngine = new MockedScriptEngine();
	}

	@AfterEach
	public void teardown() throws InterruptedException {
		if (fTestEngine.getState() != Job.NONE) {
			fTestEngine.terminate();
			fTestEngine.joinEngine();
		}
	}

	@Test
	@DisplayName("Constructor sets job name")
	public void constructor_sets_job_name() {
		assertEquals("[EASE Mocked Engine]", fTestEngine.getName());
	}

	@Test
	@DisplayName("getDescription() = null by default")
	public void getDescription_returns_null_by_default() {
		assertNull(fTestEngine.getDescription());
	}

	@Test
	@DisplayName("getDescription() returns engine description")
	public void getDescription_returns_engine_description() {
		final EngineDescription description = mock(EngineDescription.class);
		fTestEngine.setEngineDescription(description);
		assertEquals(description, fTestEngine.getDescription());
	}

	@Test
	@DisplayName("execute() schedules script")
	public void execute_schedules_script() {
		final ScriptResult result = fTestEngine.execute("foo");
		assertFalse(result.isDone());
	}

	@Test
	@DisplayName("execute() processes script when engine is started")
	public void execute_processes_script_when_engine_is_started() throws ExecutionException {
		final ScriptResult result = fTestEngine.execute("foo");
		assertFalse(result.isDone());

		fTestEngine.schedule();

		assertEquals("foo", result.get());
	}

	@Test
	@DisplayName("inject() executes code in the middle of a script")
	public void inject_executes_code_in_the_middle_of_a_script() throws ExecutionException {
		final ScriptResult result = fTestEngine.execute("start-" + INJECT_MARKER + "-done");
		assertFalse(result.isDone());

		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		fTestEngine.setOutputStream(out);
		fTestEngine.schedule();

		assertEquals("start-" + INJECT_MARKER + "-done", result.get());
		assertEquals("(injected code)start-INJECT-done", out.toString());
	}

	@Test
	@DisplayName("terminate() does nothing for non-launched engine")
	public void terminate_does_nothing_for_non_launched_engine() {
		fTestEngine.execute("not started");
		fTestEngine.terminate();
	}

	@Test
	@DisplayName("isFinished() = false for not started engine")
	public void isFinished_equals_false_for_not_started_engine() {
		assertFalse(fTestEngine.isFinished());
	}

	@Test
	@DisplayName("isFinished() = true for finished engine")
	public void isFinished_equals_true_for_finished_engine() throws ExecutionException {
		final ScriptResult result = fTestEngine.execute("not started");
		fTestEngine.schedule();
		result.get();

		// wait for job to terminate
		while (fTestEngine.getState() != Job.NONE)
			Thread.yield();

		assertTrue(fTestEngine.isFinished());
	}

	@Test
	@DisplayName("joinEngine() waits for engine to terminate")
	public void joinEngine_waits_for_engine_to_terminate() throws InterruptedException {
		fTestEngine.execute("not started");
		fTestEngine.schedule(1000);
		fTestEngine.joinEngine();
		assertTrue(fTestEngine.isFinished());
	}

	@Test
	@DisplayName("joinEngine(x) waits for x ms")
	public void joinEngine_waits_for_x_ms() throws InterruptedException {
		fTestEngine.execute("not started");
		fTestEngine.schedule(1000);

		fTestEngine.joinEngine(100);
		assertFalse(fTestEngine.isFinished());
	}

	@Test
	@DisplayName("getMonitor() = null for not started engine")
	public void getMonitor_equals_null_for_not_started_engine() {
		assertNull(fTestEngine.getMonitor());
	}

	@Test
	@DisplayName("getMonitor() = null for terminated engine")
	public void getMonitor_equals_null_for_terminated_engine() throws InterruptedException {
		fTestEngine.execute("not started");
		fTestEngine.schedule();
		fTestEngine.joinEngine();

		assertNull(fTestEngine.getMonitor());
	}

	@Test
	@DisplayName("getMonitor() != null for running engine")
	public void getMonitor_not_equals_null_for_running_engine() throws InterruptedException, ExecutionException {

		final MockedScriptEngine engine = new MockedScriptEngine() {
			@Override
			protected Object execute(Script script, String fileName, boolean uiThread) throws Throwable {
				return getMonitor() != null;
			}
		};

		final ScriptResult result = engine.execute("not started");
		engine.schedule();
		engine.joinEngine();

		assertEquals(Boolean.TRUE, result.get());
	}

	@Test
	@DisplayName("getInputStream() defaults to System.in")
	public void getInputStream_defaults_to_system_in() {
		assertEquals(System.in, fTestEngine.getInputStream());
	}

	@Test
	@DisplayName("setInputStream() changes input stream")
	public void setInputStream_changes_input_stream() {
		final ByteArrayInputStream in = new ByteArrayInputStream("".getBytes());
		fTestEngine.setInputStream(in);

		assertEquals(in, fTestEngine.getInputStream());
	}

	@Test
	@DisplayName("setInputStream(null) resets input stream")
	public void setInputStream_to_null_resets_input_stream() {
		final ByteArrayInputStream in = new ByteArrayInputStream("".getBytes());
		fTestEngine.setInputStream(in);
		fTestEngine.setInputStream(null);

		assertEquals(System.in, fTestEngine.getInputStream());
	}

	@Test
	@DisplayName("getOutputStream() defaults to System.out")
	public void getOutputStream_defaults_to_system_in() {
		assertEquals(System.out, fTestEngine.getOutputStream());
	}

	@Test
	@DisplayName("setOutputStream() changes output stream")
	public void setOutputStream_changes_output_stream() {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		fTestEngine.setOutputStream(out);
		fTestEngine.getOutputStream().print("out");

		assertEquals("out", out.toString());
	}

	@Test
	@DisplayName("setOutputStream(null) resets output stream")
	public void setOutputStream_to_null_resets_output_stream() {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		fTestEngine.setOutputStream(out);
		fTestEngine.setOutputStream(null);

		assertEquals(System.out, fTestEngine.getOutputStream());
	}

	@Test
	@DisplayName("getErrorStream() defaults to System.err")
	public void getErrorStream_defaults_to_system_err() {
		assertEquals(System.err, fTestEngine.getErrorStream());
	}

	@Test
	@DisplayName("setErrorStream() changes error stream")
	public void setErrorStream_changes_error_stream() {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		fTestEngine.setErrorStream(out);

		fTestEngine.getErrorStream().print("err");
		assertEquals("err", out.toString());
	}

	@Test
	@DisplayName("setErrorStream(null) resets error stream")
	public void setErrorStream_to_null_resets_error_stream() {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		fTestEngine.setErrorStream(out);
		fTestEngine.setErrorStream(null);

		assertEquals(System.err, fTestEngine.getErrorStream());
	}

	@Test
	@DisplayName("setCloseStreamsOnTerminate(true) closes streams")
	public void setCloseStreamsOnTerminate_closes_streams() throws InterruptedException, IOException {
		final OutputStream out = mock(OutputStream.class);
		final OutputStream err = mock(OutputStream.class);
		final InputStream in = mock(InputStream.class);

		fTestEngine.setOutputStream(out);
		fTestEngine.setErrorStream(err);
		fTestEngine.setInputStream(in);
		fTestEngine.setCloseStreamsOnTerminate(true);

		fTestEngine.schedule();
		fTestEngine.joinEngine();

		verify(out, times(1)).close();
		verify(err, times(1)).close();
		verify(in, times(1)).close();
	}

	@Test
	@DisplayName("setCloseStreamsOnTerminate(false) keeps streams open")
	public void setCloseStreamsOnTerminate_keeps_streams_open() throws InterruptedException, IOException {
		final OutputStream out = mock(OutputStream.class);
		final OutputStream err = mock(OutputStream.class);
		final InputStream in = mock(InputStream.class);

		fTestEngine.setOutputStream(out);
		fTestEngine.setErrorStream(err);
		fTestEngine.setInputStream(in);
		fTestEngine.setCloseStreamsOnTerminate(false);

		fTestEngine.schedule();
		fTestEngine.joinEngine();

		verify(out, never()).close();
		verify(err, never()).close();
		verify(in, never()).close();
	}

	@Test
	@DisplayName("streams are not closed by default on engine termination")
	public void streams_are_not_closed_by_default_on_engine_termination() throws InterruptedException, IOException {
		final OutputStream out = mock(OutputStream.class);
		final OutputStream err = mock(OutputStream.class);
		final InputStream in = mock(InputStream.class);

		fTestEngine.setOutputStream(out);
		fTestEngine.setErrorStream(err);
		fTestEngine.setInputStream(in);

		fTestEngine.schedule();
		fTestEngine.joinEngine();

		verify(out, never()).close();
		verify(err, never()).close();
		verify(in, never()).close();
	}

	@Test
	@DisplayName("addExecutionListener() adds an engine listener")
	public void addExecutionListener_adds_an_engine_listener() throws InterruptedException {
		final IExecutionListener listener = mock(IExecutionListener.class);

		fTestEngine.addExecutionListener(listener);
		fTestEngine.schedule();
		fTestEngine.joinEngine();

		verify(listener, atLeast(1)).notify(eq(fTestEngine), any(), anyInt());
	}

	@Test
	@DisplayName("removeExecutionListener() removes an engine listener")
	public void removeExecutionListener_removes_an_engine_listener() throws InterruptedException {
		final IExecutionListener listener = mock(IExecutionListener.class);

		fTestEngine.addExecutionListener(listener);
		fTestEngine.removeExecutionListener(listener);
		fTestEngine.schedule();
		fTestEngine.joinEngine();

		verify(listener, never()).notify(any(), any(), anyInt());
	}

	@Test
	@DisplayName("getStackTrace() returns empty trace for fresh engine")
	public void getStackTrace_returns_empty_trace_for_fresh_engine() {
		assertTrue(fTestEngine.getStackTrace().isEmpty());
	}

	@Test
	@DisplayName("getStackTrace() returns empty trace for terminated engine")
	public void getStackTrace_returns_empty_trace_for_terminated_engine() throws InterruptedException {
		fTestEngine.execute("foo");
		fTestEngine.schedule();
		fTestEngine.joinEngine();

		assertTrue(fTestEngine.getStackTrace().isEmpty());
	}

	@Test
	@DisplayName("getStackTrace() returns stack of size 1 during execution")
	public void getStackTrace_returns_stack_of_size_1_during_execution() throws ExecutionException {
		final MockedScriptEngine engine = new MockedScriptEngine() {
			@Override
			protected Object execute(Script script, String fileName, boolean uiThread) throws Throwable {
				// TODO Auto-generated method stub
				return getStackTrace().size();
			}
		};

		final ScriptResult result = engine.execute("foo");
		engine.schedule();

		assertEquals(1, result.get());
	}

	@Test
	@DisplayName("getExecutedFile() returns script resource")
	public void getExecutedFile_returns_script_resource() throws ExecutionException {

		final MockedScriptEngine engine = new MockedScriptEngine() {
			@Override
			protected Object execute(Script script, String fileName, boolean uiThread) throws Throwable {
				// TODO Auto-generated method stub
				return getExecutedFile();
			}
		};

		final IFile file = mock(IFile.class);
		when(file.getFullPath()).thenReturn(new Path("mockedfile.js"));

		final Script script = new Script(file) {
			@Override
			public String getCode() {
				return "code";
			}
		};

		final ScriptResult result = engine.execute(script);
		engine.schedule();

		assertEquals(file, result.get());
	}

	@Test
	@DisplayName("getExecutedFile() returns execution root file")
	public void getExecutedFile_returns_execution_root_file() throws ExecutionException {

		final MockedScriptEngine engine = new MockedScriptEngine() {
			@Override
			protected Object execute(Script script, String fileName, boolean uiThread) throws Throwable {
				// TODO Auto-generated method stub
				return getExecutedFile();
			}
		};

		final IFile file = mock(IFile.class);
		engine.setExecutionRootFile(file);

		final ScriptResult result = engine.execute(new Script("code"));
		engine.schedule();

		assertEquals(file, result.get());
	}

	@Test
	@DisplayName("getExecutedFile() = null for dynamic code")
	public void getExecutedFile_returns_null_for_dynamic_code() throws ExecutionException {

		final MockedScriptEngine engine = new MockedScriptEngine() {
			@Override
			protected Object execute(Script script, String fileName, boolean uiThread) throws Throwable {
				// TODO Auto-generated method stub
				return getExecutedFile();
			}
		};

		final ScriptResult result = engine.execute(new Script("code"));
		engine.schedule();

		assertNull(result.get());
	}

	@Test
	@DisplayName("hasVariable() = false when no variable is set")
	public void hasVariable_is_false_when_no_variable_is_set() {
		assertFalse(fTestEngine.hasVariable("foo"));
	}

	@Test
	@DisplayName("hasVariable() = true when variable is set")
	public void hasVariable_is_true_when_variable_is_set() {
		fTestEngine.setVariable("foo", 42);

		assertTrue(fTestEngine.hasVariable("foo"));
	}

	@Test
	@DisplayName("getVariable() = null for non existing variable")
	public void getVariable_is_null_for_non_existing_variable() {
		assertNull(fTestEngine.getVariable("foo"));
	}

	@Test
	@DisplayName("getVariable() returns buffered variable")
	public void getVariable_returns_buffered_variable() {
		fTestEngine.setVariable("foo", 42);

		assertEquals(42, fTestEngine.getVariable("foo"));
	}

	@Test
	@DisplayName("getVariables() returns all buffered variables")
	public void getVariables_returns_all_buffered_variables() {
		fTestEngine.setVariable("foo", 42);
		fTestEngine.setVariable("bar", 84);

		assertEquals(2, fTestEngine.getVariables().size());
		assertEquals(42, fTestEngine.getVariables().get("foo"));
		assertEquals(84, fTestEngine.getVariables().get("bar"));
	}

	@Test
	@DisplayName("buffered variables are injected into the eninge")
	public void buffered_variables_are_injected_into_the_engine() throws ExecutionException {

		final MockedScriptEngine engine = new MockedScriptEngine() {
			@Override
			protected Object execute(Script script, String fileName, boolean uiThread) throws Throwable {
				// TODO Auto-generated method stub
				return getVariable("foo");
			}
		};

		engine.setVariable("foo", 42);
		final ScriptResult result = engine.execute("code");
		engine.schedule();

		assertEquals(42, result.get());
	}

	@Test
	@DisplayName("addSecurityCheck() adds passing check")
	public void addSecurityCheck_adds_passing_check() throws InterruptedException {
		final ISecurityCheck securityCheck = mock(ISecurityCheck.class);
		when(securityCheck.doIt(any(), any())).thenReturn(true);
		fTestEngine.addSecurityCheck(ActionType.INJECT_CODE, securityCheck);

		final ScriptResult result = fTestEngine.execute("foo");
		fTestEngine.schedule();
		fTestEngine.joinEngine();

		assertDoesNotThrow(() -> result.get());
		verify(securityCheck, times(1)).doIt(any(), any());
	}

	@Test
	@DisplayName("addSecurityCheck() adds failing check")
	public void addSecurityCheck_adds_failing_check() throws InterruptedException {
		fTestEngine.addSecurityCheck(ActionType.INJECT_CODE, (action, data) -> false);

		final ScriptResult result = fTestEngine.execute("foo");
		fTestEngine.schedule();
		fTestEngine.joinEngine();

		assertThrows(ScriptExecutionException.class, () -> result.get());
	}

	@Test
	@DisplayName("getLaunch() = null by default")
	public void getLaunch_is_null_by_default() {
		assertNull(fTestEngine.getLaunch());
	}

	@Test
	@DisplayName("getLaunch() returns previously set launch")
	public void getLaunch_returns_previously_set_launch() {
		final ILaunch launch = mock(ILaunch.class);

		fTestEngine.setLaunch(launch);
		assertEquals(launch, fTestEngine.getLaunch());
	}

	@Test
	@DisplayName("instance extends Job.class")
	public void isJob() {
		assertTrue(fTestEngine instanceof Job);
	}

	@Test
	@DisplayName("execute valid code and terminate")
	public void execute_valid_code_and_terminate() throws ExecutionException, InterruptedException {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		fTestEngine.setOutputStream(bos);

		final ScriptResult result1 = fTestEngine.execute("1");
		final ScriptResult result2 = fTestEngine.execute("2");
		fTestEngine.schedule();

		fTestEngine.joinEngine();

		assertTrue(fTestEngine.isFinished());
		assertEquals("12", bos.toString());

		assertTrue(result1.isDone());
		assertEquals("1", result1.get());

		assertTrue(result2.isDone());
		assertEquals("2", result2.get());
	}

	@Test
	@DisplayName("execute errorous code and terminate")
	public void execute_errorous_code_and_terminate() throws InterruptedException, ExecutionException {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		fTestEngine.setOutputStream(bos);

		final ScriptResult result1 = fTestEngine.execute("1");
		final ScriptResult result2 = fTestEngine.execute(ERROR_MARKER);
		fTestEngine.schedule();

		fTestEngine.joinEngine();

		assertTrue(fTestEngine.isFinished());
		assertEquals("1", bos.toString());

		assertTrue(result1.isDone());
		assertEquals("1", result1.get());

		assertThrows(ScriptExecutionException.class, () -> result2.get());
	}

	@Test
	@DisplayName("terminate() stops running engine")
	public void terminate_stops_running_engine() throws InterruptedException {
		final MockedScriptEngine engine = new MockedScriptEngine() {
			@Override
			protected Object execute(Script script, String fileName, boolean uiThread) throws Throwable {
				Thread.sleep(100);
				return super.execute(script, fileName, uiThread);
			}
		};

		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		engine.setOutputStream(bos);

		ScriptResult scriptResult = null;
		for (int loop = 0; loop <= 100; loop++)
			scriptResult = engine.execute("Loop " + loop + "\n");

		engine.schedule();

		// wait for engine to produce output
		while (bos.toString().isEmpty())
			Thread.yield();

		engine.terminate();
		engine.joinEngine();

		assertFalse(bos.toString().contains("Loop 100"));

		final ScriptResult result = scriptResult;
		assertTrue(result.isDone());
		assertThrows(ScriptExecutionException.class, () -> result.get());
	}

	@Test
	@DisplayName("terminate via monitor cancellation")
	public void terminate_via_monitor_cancellation() throws InterruptedException {
		final MockedScriptEngine engine = new MockedScriptEngine() {
			@Override
			protected Object execute(Script script, String fileName, boolean uiThread) throws Throwable {
				Thread.sleep(100);
				return super.execute(script, fileName, uiThread);
			}
		};

		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		engine.setOutputStream(bos);

		ScriptResult scriptResult = null;
		for (int loop = 0; loop <= 100; loop++)
			scriptResult = engine.execute("Loop " + loop + "\n");

		engine.schedule();

		// wait for engine to produce output
		while (bos.toString().isEmpty())
			Thread.yield();

		engine.getMonitor().setCanceled(true);
		engine.joinEngine();

		assertFalse(bos.toString().contains("Loop 100"));

		final ScriptResult result = scriptResult;
		assertTrue(result.isDone(), "result " + scriptResult.hashCode() + " is not ready");
		assertThrows(ScriptExecutionException.class, () -> result.get());
	}

	public static class MockedScriptEngine extends AbstractScriptEngine {

		private final Map<String, Object> fBufferedVariables = new HashMap<>();

		private MockedScriptEngine() {
			super("Mocked");
		}

		@Override
		public void terminateCurrent() {
		}

		@Override
		public void registerJar(URL url) {
		}

		@Override
		protected Object internalGetVariable(String name) {
			return fBufferedVariables.get(name);
		}

		@Override
		protected Map<String, Object> internalGetVariables() {
			return fBufferedVariables;
		}

		@Override
		protected boolean internalHasVariable(String name) {
			return fBufferedVariables.containsKey(name);
		}

		@Override
		protected void internalSetVariable(String name, Object content) {
			fBufferedVariables.put(name, content);
		}

		@Override
		protected void setupEngine() throws ScriptEngineException {
		}

		@Override
		protected void teardownEngine() throws ScriptEngineException {
		}

		@Override
		protected Object execute(Script script, String fileName, boolean uiThread) throws Throwable {
			final String input = script.getCommand().toString();
			if (input.contains(ERROR_MARKER))
				throw new RuntimeException(input);

			if (input.contains(INJECT_MARKER))
				inject("(injected code)", false);

			getOutputStream().write(input.getBytes());

			return input;
		}
	}
}
