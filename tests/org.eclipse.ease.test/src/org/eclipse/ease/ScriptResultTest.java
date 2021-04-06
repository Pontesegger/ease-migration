package org.eclipse.ease;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ScriptResultTest {

	private static final Object RESULT = "done";
	private static final ScriptExecutionException EXCEPTION = new ScriptExecutionException("some error");

	@Test
	public void isReady() {
		assertTrue(new ScriptResult(RESULT).isReady());

		ScriptResult result = new ScriptResult();
		assertFalse(result.isReady());

		result.setResult(RESULT);
		assertTrue(result.isReady());

		result = new ScriptResult();
		result.setException(new ScriptExecutionException());
		assertTrue(result.isReady());
	}

	@Test
	public void getResult() {
		final ScriptResult result = new ScriptResult(RESULT);

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
