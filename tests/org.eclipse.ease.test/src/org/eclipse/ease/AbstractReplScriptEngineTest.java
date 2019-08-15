package org.eclipse.ease;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Map;

import org.eclipse.core.runtime.jobs.Job;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractReplScriptEngineTest extends AbstractScriptEngineTest {

	private static final String SAMPLE_CODE = "Hello world";

	private class MockedScriptEngine extends AbstractReplScriptEngine {

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
		protected Object execute(Script script, Object reference, String fileName, boolean uiThread) throws Throwable {
			final String input = script.getCommand().toString();
			if (input.contains(ERROR_MARKER))
				throw new RuntimeException(input);
			else
				getOutputStream().write(input.getBytes());

			return input;
		}
	}

	@Override
	@Before
	public void setup() {
		fTestEngine = new MockedScriptEngine();
	}

	private AbstractReplScriptEngine getTestEngine() {
		return (AbstractReplScriptEngine) fTestEngine;
	}

	@Test(timeout = TEST_TIMEOUT)
	public void terminateOnIdleAfterSchedule() throws InterruptedException {
		getTestEngine().setTerminateOnIdle(false);
		getTestEngine().schedule();
		while (true) {
			Thread.sleep(10);
			if (getTestEngine().getState() != Job.RUNNING) {
				// eclipse job has not started yet
				continue;
			}
			if (getTestEngine().getThread().getState() != Thread.State.WAITING) {
				// thread is still running, we want it to be waiting
				continue;
			}
			break;
		}
		getTestEngine().setTerminateOnIdle(true);
		getTestEngine().joinEngine();

		// test valid if it terminates within the timeout period
	}

	@Test(timeout = TEST_TIMEOUT)
	public void keepRunningOnIdle() throws InterruptedException {
		getTestEngine().setTerminateOnIdle(false);
		getTestEngine().executeAsync(SAMPLE_CODE);
		getTestEngine().schedule();

		final ScriptResult result = getTestEngine().executeSync(SAMPLE_CODE);
		assertTrue(result.isReady());
	}

	@Test(timeout = TEST_TIMEOUT)
	public void terminateEngine() throws InterruptedException {
		getTestEngine().setTerminateOnIdle(false);
		getTestEngine().schedule();

		getTestEngine().terminate();
		getTestEngine().joinEngine();

		assertEquals(Job.NONE, getTestEngine().getState());
	}
}
