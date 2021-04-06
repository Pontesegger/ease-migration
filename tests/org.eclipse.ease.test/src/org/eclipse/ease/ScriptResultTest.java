package org.eclipse.ease;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(value = 5, unit = TimeUnit.SECONDS)
public class ScriptResultTest {

	private static final Object RESULT = "done";
	private static final ScriptExecutionException EXCEPTION = new ScriptExecutionException("some error");

	@Test
	@DisplayName("isDone() == false on new ScriptResult()")
	public void isDone_false_on_new_ScriptResult() {
		assertFalse(new ScriptResult().isDone());
	}

	@Test
	@DisplayName("isDone() == true when result is ready")
	public void isDone_true_when_result_is_ready() {
		final ScriptResult result = new ScriptResult();
		result.setResult(RESULT);

		assertTrue(result.isDone());
	}

	@Test
	@DisplayName("isDone() == true when result is null")
	public void isDone_true_when_result_is_null() {
		final ScriptResult result = new ScriptResult();
		result.setResult(null);

		assertTrue(result.isDone());
	}

	@Test
	@DisplayName("isDone() == true when exception was thrown")
	public void isDone_true_when_exception_was_thrown() {
		final ScriptResult result = new ScriptResult();
		result.setException(EXCEPTION);

		assertTrue(result.isDone());
	}

	@Test
	@DisplayName("get() returns ready execution result")
	public void get_returns_ready_execution_result() throws InterruptedException, ExecutionException {
		final ScriptResult result = new ScriptResult();
		result.setResult(RESULT);

		assertEquals(RESULT, result.get());
	}

	@Test
	@DisplayName("get() throws execution exception")
	public void get_throws_execution_exception() {
		final ScriptResult result = new ScriptResult();
		result.setException(EXCEPTION);

		assertThrows(ScriptExecutionException.class, () -> result.get());
	}

	@Test
	@DisplayName("get() waits for execution result")
	public void get_waits_for_execution_result() throws InterruptedException, ExecutionException {
		final ScriptResult result = new ScriptResult();

		new Job("SetResult") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				result.setResult(RESULT);
				return Status.OK_STATUS;
			}
		}.schedule(500);

		assertEquals(RESULT, result.get());
	}

	@Test
	@DisplayName("get() waits for execution exception")
	public void get_waits_for_execution_exception() {
		final ScriptResult result = new ScriptResult();

		new Job("SetResult") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				result.setException(EXCEPTION);
				return Status.OK_STATUS;
			}
		}.schedule(500);

		assertThrows(ScriptExecutionException.class, () -> result.get());
	}

	@Test
	@DisplayName("get(timeout) waits for execution result")
	public void get_with_timeout_waits_for_execution_result() throws InterruptedException, ExecutionException, TimeoutException {
		final ScriptResult result = new ScriptResult();

		new Job("SetResult") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				result.setResult(RESULT);
				return Status.OK_STATUS;
			}
		}.schedule(500);

		assertEquals(RESULT, result.get(5, TimeUnit.SECONDS));
	}

	@Test
	@DisplayName("get(timeout) waits for execution exception")
	public void get_with_timeout_waits_for_execution_exception() {
		final ScriptResult result = new ScriptResult();

		new Job("SetResult") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				result.setException(EXCEPTION);
				return Status.OK_STATUS;
			}
		}.schedule(500);

		assertThrows(ScriptExecutionException.class, () -> result.get(5, TimeUnit.SECONDS));
	}

	@Test
	@DisplayName("get(timeout) times out")
	public void get_times_out() {
		final ScriptResult result = new ScriptResult();

		assertThrows(TimeoutException.class, () -> result.get(500, TimeUnit.MILLISECONDS));
	}

	@Test
	public void isReady() {
		ScriptResult result = new ScriptResult();
		result.setResult(RESULT);
		assertTrue(result.isReady());

		result = new ScriptResult();
		assertFalse(result.isReady());

		result.setResult(RESULT);
		assertTrue(result.isReady());

		result = new ScriptResult();
		result.setException(new ScriptExecutionException());
		assertTrue(result.isReady());
	}

	@Test
	public void getResult() {
		final ScriptResult result = new ScriptResult();
		result.setResult(RESULT);

		assertEquals(RESULT, result.getResult());
		assertNull(result.getException());
	}

	@Test
	public void getException() {
		final ScriptResult result = new ScriptResult();
		result.setException(EXCEPTION);

		assertNull(result.getResult());
		assertEquals(EXCEPTION, result.getException());
	}

	@Test
	public void hasException() {
		final ScriptResult result = new ScriptResult();
		assertFalse(result.hasException());

		result.setResult(RESULT);
		assertFalse(result.hasException());

		result.setException(EXCEPTION);
		assertTrue(result.hasException());
	}
}
