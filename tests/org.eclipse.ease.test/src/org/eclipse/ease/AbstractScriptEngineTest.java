package org.eclipse.ease;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.core.runtime.jobs.Job;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AbstractScriptEngineTest {
	protected static final int TEST_TIMEOUT = 3000;

	protected static final String VALID_SAMPLE_CODE = "1";

	protected static final CharSequence ERROR_MARKER = "ERROR";

	private class MockedScriptEngine extends AbstractScriptEngine {

		public MockedScriptEngine() {
			super("Mocked Engine");
		}

		@Override
		public void terminateCurrent() {
		}

		@Override
		public void registerJar(URL url) {
		}

		@Override
		protected Object internalGetVariable(String name) {
			return null;
		}

		@Override
		protected Map<String, Object> internalGetVariables() {
			return null;
		}

		@Override
		protected boolean internalHasVariable(String name) {
			return false;
		}

		@Override
		protected void internalSetVariable(String name, Object content) {
		}

		@Override
		protected void setupEngine() throws ScriptEngineException {
		}

		@Override
		protected void teardownEngine() throws ScriptEngineException {
		}

		@Override
		protected Object execute(Script script, Object reference, String fileName, boolean uiThread) throws Throwable {
			final String input = script.getCommand().toString();
			if (input.contains(ERROR_MARKER))
				throw new RuntimeException(input);
			else
				getOutputStream().write(input.getBytes());

			return input;
		}
	}

	protected AbstractScriptEngine fTestEngine;

	@Before
	public void setup() {
		fTestEngine = new MockedScriptEngine();
	}

	@After
	public void teardown() throws InterruptedException {
		if (fTestEngine.getState() != Job.NONE) {
			fTestEngine.terminate();
			fTestEngine.joinEngine();
		}
	}

	@Test
	public void streamsAvailable() {
		assertNotNull(fTestEngine.getOutputStream());
		assertNotNull(fTestEngine.getErrorStream());
		assertNotNull(fTestEngine.getInputStream());
	}

	@Test
	public void setNotNullOutputStream() throws IOException {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();

		fTestEngine.setOutputStream(bos);

		assertNotNull(fTestEngine.getOutputStream());
		fTestEngine.getOutputStream().print("test");
		Arrays.equals("test".getBytes(), bos.toByteArray());
	}

	@Test
	public void setNullOutputStream() throws IOException {
		fTestEngine.setOutputStream(null);

		assertEquals(System.out, fTestEngine.getOutputStream());
	}

	@Test
	public void setNotNullErrorStream() throws IOException {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();

		assertNotNull(fTestEngine.getErrorStream());
		fTestEngine.getErrorStream().print("test");
		Arrays.equals("test".getBytes(), bos.toByteArray());
	}

	@Test
	public void setNullErrorStream() {
		fTestEngine.setErrorStream(null);

		assertEquals(System.err, fTestEngine.getErrorStream());
	}

	@Test
	public void isJob() {
		assertTrue(fTestEngine instanceof Job);
	}

	@Test
	public void executeValidCodeAndTerminate() throws InterruptedException {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		fTestEngine.setOutputStream(bos);

		final ScriptResult result1 = fTestEngine.executeAsync(VALID_SAMPLE_CODE);
		final ScriptResult result2 = fTestEngine.executeAsync("2");
		fTestEngine.schedule();

		fTestEngine.joinEngine();

		assertTrue(fTestEngine.isFinished());
		assertEquals("12", bos.toString());

		assertTrue(result1.isReady());
		assertEquals(VALID_SAMPLE_CODE, result1.getResult());
		assertFalse(result1.hasException());
		assertNull(result1.getException());

		assertTrue(result2.isReady());
		assertEquals("2", result2.getResult());
		assertFalse(result2.hasException());
		assertNull(result2.getException());
	}

	@Test
	public void executeErrorCodeAndTerminate() throws InterruptedException {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		fTestEngine.setOutputStream(bos);

		final ScriptResult result1 = fTestEngine.executeAsync(VALID_SAMPLE_CODE);
		final ScriptResult result2 = fTestEngine.executeAsync("ERROR");
		fTestEngine.schedule();

		fTestEngine.joinEngine();

		assertTrue(fTestEngine.isFinished());
		assertEquals(VALID_SAMPLE_CODE, bos.toString());

		assertTrue(result1.isReady());
		assertEquals(VALID_SAMPLE_CODE, result1.getResult());
		assertFalse(result1.hasException());
		assertNull(result1.getException());

		assertTrue(result2.isReady());
		assertNull(result2.getResult());
		assertTrue(result2.hasException());
		assertEquals(RuntimeException.class, result2.getException().getClass());
	}

	@Test
	public void executeSync() throws InterruptedException {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		fTestEngine.setOutputStream(bos);

		final ScriptResult result1 = fTestEngine.executeSync(VALID_SAMPLE_CODE);

		fTestEngine.joinEngine();

		assertTrue(fTestEngine.isFinished());
		assertEquals(VALID_SAMPLE_CODE, bos.toString());

		assertTrue(result1.isReady());
		assertEquals(VALID_SAMPLE_CODE, result1.getResult());
		assertFalse(result1.hasException());
		assertNull(result1.getException());
	}

	@Test(timeout = TEST_TIMEOUT)
	public void inject() throws InterruptedException {
		assertEquals(VALID_SAMPLE_CODE, fTestEngine.inject(VALID_SAMPLE_CODE));
	}

	@Test(timeout = TEST_TIMEOUT)
	public void engineTerminatesWhenIdle() throws InterruptedException {
		fTestEngine.schedule();
		fTestEngine.joinEngine();
	}

	@Test(timeout = TEST_TIMEOUT)
	public void terminateViaTerminateMethod() throws InterruptedException {
		final MockedScriptEngine engine = new MockedScriptEngine() {
			@Override
			protected Object execute(Script script, Object reference, String fileName, boolean uiThread) throws Throwable {
				Thread.sleep(100);
				return super.execute(script, reference, fileName, uiThread);
			}
		};

		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		engine.setOutputStream(bos);

		ScriptResult scriptResult = null;
		for (int loop = 0; loop <= 100; loop++)
			scriptResult = engine.executeAsync("Loop " + loop + "\n");

		engine.schedule();

		// wait for engine to produce output
		while (bos.toString().isEmpty())
			Thread.yield();

		engine.terminate();
		engine.joinEngine();

		assertFalse(bos.toString().contains("Loop 100"));

		assertTrue(scriptResult.isReady());
		assertNull(scriptResult.getResult());
		assertTrue(scriptResult.hasException());
		assertEquals(ScriptExecutionException.class, scriptResult.getException().getClass());
	}

	@Test(timeout = TEST_TIMEOUT)
	public void terminateViaMonitorCancellation() throws InterruptedException {
		final MockedScriptEngine engine = new MockedScriptEngine() {
			@Override
			protected Object execute(Script script, Object reference, String fileName, boolean uiThread) throws Throwable {
				Thread.sleep(100);
				return super.execute(script, reference, fileName, uiThread);
			}
		};

		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		engine.setOutputStream(bos);

		ScriptResult scriptResult = null;
		for (int loop = 0; loop <= 100; loop++)
			scriptResult = engine.executeAsync("Loop " + loop + "\n");

		engine.schedule();

		// wait for engine to produce output
		while (bos.toString().isEmpty())
			Thread.yield();

		engine.getMonitor().setCanceled(true);
		engine.joinEngine();

		assertFalse(bos.toString().contains("Loop 100"));

		assertTrue("result " + scriptResult.hashCode() + " is not ready", scriptResult.isReady());
		assertNull(scriptResult.getResult());
		assertTrue(scriptResult.hasException());
		assertEquals(ScriptExecutionException.class, scriptResult.getException().getClass());
	}

	@Test(timeout = TEST_TIMEOUT)
	public void terminateViaMethodCallback() throws InterruptedException {
		final MockedScriptEngine engine = new MockedScriptEngine() {
			@Override
			protected Object execute(Script script, Object reference, String fileName, boolean uiThread) throws Throwable {
				getMonitor().setCanceled(true);
				checkForCancellation();
				return super.execute(script, reference, fileName, uiThread);
			}
		};

		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		engine.setOutputStream(bos);

		final ScriptResult scriptResult = engine.executeAsync(VALID_SAMPLE_CODE);

		engine.schedule();
		engine.joinEngine();

		assertTrue(bos.toString().isEmpty());

		assertTrue(scriptResult.isReady());
		assertNull(scriptResult.getResult());
		assertTrue(scriptResult.hasException());
		assertEquals(ScriptExecutionException.class, scriptResult.getException().getClass());
	}

	@Test(timeout = TEST_TIMEOUT)
	public void terminateMultipleTimes() {
		final MockedScriptEngine engine = new MockedScriptEngine() {
			@Override
			protected Object execute(Script script, Object reference, String fileName, boolean uiThread) throws Throwable {
				Thread.sleep(300);
				return super.execute(script, reference, fileName, uiThread);
			}
		};

		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		engine.setOutputStream(bos);

		for (int loop = 0; loop < 10; loop++)
			engine.executeAsync("Loop " + loop + "\n");

		engine.schedule();

		// wait for engine to produce output
		while (bos.toString().isEmpty())
			Thread.yield();

		while (engine.getState() != Job.NONE)
			engine.terminate();

		// this test is pass when it does not throw an Exception
	}

	@Test
	public void extractEmptyArguments() {
		assertEquals(0, AbstractScriptEngine.extractArguments(null).length);
		assertEquals(0, AbstractScriptEngine.extractArguments("").length);
		assertEquals(0, AbstractScriptEngine.extractArguments("    ").length);
		assertEquals(0, AbstractScriptEngine.extractArguments("\t\t").length);
	}

	@Test
	public void extractArguments() {
		assertArrayEquals(new String[] { "one" }, AbstractScriptEngine.extractArguments("one"));
		assertArrayEquals(new String[] { "one with spaces" }, AbstractScriptEngine.extractArguments("one with spaces"));
		assertArrayEquals(new String[] { "one", "and", "another" }, AbstractScriptEngine.extractArguments("one,and, another"));
	}
}
