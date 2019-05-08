package org.eclipse.ease.tools;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RunnableWithResultTest {

	@Test
	public void testResult() {
		final String expected = "Hello world";

		final RunnableWithResult<String> runnable = new RunnableWithResult<String>() {

			@Override
			public String runWithTry() throws Throwable {
				return expected;
			}
		};

		runnable.run();

		assertEquals(expected, runnable.getResult());
	}

	@Test
	public void testException() {
		final Exception exception = new Exception("Test Exception");

		final RunnableWithResult<String> runnable = new RunnableWithResult<String>() {

			@Override
			public String runWithTry() throws Throwable {
				throw exception;
			}
		};

		runnable.run();

		try {
			runnable.getResult();
		} catch (final RuntimeException e) {
			assertEquals(exception, e.getCause());
		}

		try {
			runnable.getResultOrThrow();
		} catch (final Throwable e) {
			assertEquals(exception, e);
		}
	}
}
