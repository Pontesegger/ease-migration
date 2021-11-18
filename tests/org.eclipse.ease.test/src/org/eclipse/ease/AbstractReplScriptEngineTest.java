package org.eclipse.ease;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.ease.debugging.model.EaseDebugVariable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AbstractReplScriptEngineTest extends AbstractScriptEngineTest {

	private AbstractReplScriptEngine fTestEngine;

	@Override
	@BeforeEach
	public void setup() {
		super.setup();

		fTestEngine = new MockedScriptEngine();
	}

	@Test
	@DisplayName("setTerminateOnIdle(false) keeps engine running")
	public void setTerminateOnIdle_keeps_engine_running() throws InterruptedException {
		fTestEngine.setTerminateOnIdle(false);
		fTestEngine.schedule();

		fTestEngine.joinEngine(1000);
		assertFalse(fTestEngine.isFinished());
	}

	@Test
	@DisplayName("interactive engine executes incoming code")
	public void interactive_engine_executes_incoming_code() throws InterruptedException, ExecutionException {
		fTestEngine.setTerminateOnIdle(false);
		fTestEngine.schedule();

		fTestEngine.execute("foo").get();

		assertFalse(fTestEngine.isFinished());
	}

	@Test
	@DisplayName("terminate() stops interactive engine")
	public void terminate_stops_interactive_engine() throws InterruptedException {
		fTestEngine.setTerminateOnIdle(false);
		fTestEngine.schedule();

		fTestEngine.terminate();
		fTestEngine.joinEngine();

		assertTrue(fTestEngine.isFinished());
	}

	@Test
	@DisplayName("getDefinedVariables() returns debug variables")
	public void getDefinedVariables_returns_debug_variables() {
		fTestEngine.setVariable("foo", 42);

		final Collection<EaseDebugVariable> variables = fTestEngine.getDefinedVariables();

		assertEquals(1, variables.size());
		final EaseDebugVariable variable = variables.iterator().next();
		assertEquals("foo", variable.getName());
		assertEquals(42, variable.getValue().getValue());
	}

	@Test
	@DisplayName("getType(Integer) = JAVA_PRIMITIVE")
	public void getType_for_Integer_returns_JAVA_PRIMITIVE() {
		assertEquals(ScriptObjectType.JAVA_PRIMITIVE, fTestEngine.getType(Integer.valueOf(1)));
	}

	@Test
	@DisplayName("getType(Byte) = JAVA_PRIMITIVE")
	public void getType_for_Byte_returns_JAVA_PRIMITIVE() {
		assertEquals(ScriptObjectType.JAVA_PRIMITIVE, fTestEngine.getType(Byte.valueOf((byte) 1)));
	}

	@Test
	@DisplayName("getType(Short) = JAVA_PRIMITIVE")
	public void getType_for_Short_returns_JAVA_PRIMITIVE() {
		assertEquals(ScriptObjectType.JAVA_PRIMITIVE, fTestEngine.getType(Short.valueOf((short) 1)));
	}

	@Test
	@DisplayName("getType(Boolean) = JAVA_PRIMITIVE")
	public void getType_for_Boolean_returns_JAVA_PRIMITIVE() {
		assertEquals(ScriptObjectType.JAVA_PRIMITIVE, fTestEngine.getType(Boolean.TRUE));
	}

	@Test
	@DisplayName("getType(Character) = JAVA_PRIMITIVE")
	public void getType_for_Character_returns_JAVA_PRIMITIVE() {
		assertEquals(ScriptObjectType.JAVA_PRIMITIVE, fTestEngine.getType(Character.valueOf('c')));
	}

	@Test
	@DisplayName("getType(Long) = JAVA_PRIMITIVE")
	public void getType_for_Long_returns_JAVA_PRIMITIVE() {
		assertEquals(ScriptObjectType.JAVA_PRIMITIVE, fTestEngine.getType(Long.valueOf(1)));
	}

	@Test
	@DisplayName("getType(Double) = JAVA_PRIMITIVE")
	public void getType_for_Double_returns_JAVA_PRIMITIVE() {
		assertEquals(ScriptObjectType.JAVA_PRIMITIVE, fTestEngine.getType(Double.valueOf(1)));
	}

	@Test
	@DisplayName("getType(Float) = JAVA_PRIMITIVE")
	public void getType_for_Float_returns_JAVA_PRIMITIVE() {
		assertEquals(ScriptObjectType.JAVA_PRIMITIVE, fTestEngine.getType(Float.valueOf(1)));
	}

	@Test
	@DisplayName("getType(VOID) = VOID")
	public void getType_for_VOID_returns_VOID() {
		assertEquals(ScriptObjectType.VOID, fTestEngine.getType(ScriptResult.VOID));
	}

	@Test
	@DisplayName("getType(null) = NULL")
	public void getType_for_null_returns_NULL() {
		assertEquals(ScriptObjectType.NULL, fTestEngine.getType(null));
	}

	@Test
	@DisplayName("getType(String) = JAVA_OBJECT")
	public void getType_for_String_returns_JAVA_OBJECT() {
		assertEquals(ScriptObjectType.JAVA_OBJECT, fTestEngine.getType("test"));
	}

	@Test
	@DisplayName("getLastExecutionResult() = null on new engine")
	public void getLastExecutionResult_is_null_on_new_engine() {
		assertNull(fTestEngine.getLastExecutionResult().getValue().getValue());
	}

	@Test
	@DisplayName("getLastExecutionResult() returns last result")
	public void getLastExecutionResult_returns_last_result() throws ExecutionException {
		fTestEngine.setTerminateOnIdle(false);
		fTestEngine.schedule();
		final ScriptResult result = fTestEngine.execute("foo");

		final Object lastResult = result.get();

		assertEquals(lastResult, fTestEngine.getLastExecutionResult().getValue().getValue());
	}

	@Test
	@DisplayName("getLastExecutionResult() = null after engine termination")
	public void getLastExecutionResult_is_null_after_engine_termination() throws ExecutionException, InterruptedException {
		fTestEngine.setTerminateOnIdle(false);
		fTestEngine.schedule();
		fTestEngine.execute("foo").get();

		fTestEngine.terminate();
		fTestEngine.joinEngine();

		assertNull(fTestEngine.getLastExecutionResult().getValue().getValue());
	}

	private static final class MockedScriptEngine extends AbstractReplScriptEngine {

		private final Map<String, Object> fBufferedVariables = new HashMap<>();

		private MockedScriptEngine() {
			super("Mocked");
		}

		@Override
		public void terminateCurrent() {
			// nothing to do
		}

		@Override
		public void registerJar(URL url) {
			// nothing to do
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
			// nothing to do
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
