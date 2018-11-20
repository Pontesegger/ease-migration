package org.eclipse.ease;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class AbstractScriptEngineTest {

	private AbstractScriptEngine fAbstractScriptEngineTest;

	@Before
	public void setup() {
		fAbstractScriptEngineTest = mock(AbstractScriptEngine.class, Mockito.CALLS_REAL_METHODS);
	}

	@Test
	public void setNotNullOutputStream() throws IOException {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();

		fAbstractScriptEngineTest.setOutputStream(bos);

		assertNotNull(fAbstractScriptEngineTest.getOutputStream());
		fAbstractScriptEngineTest.getOutputStream().print("test");
		Arrays.equals("test".getBytes(), bos.toByteArray());
	}

	@Test
	public void setNullOutputStream() throws IOException {
		fAbstractScriptEngineTest.setOutputStream(null);

		assertEquals(System.out, fAbstractScriptEngineTest.getOutputStream());
	}

	@Test
	public void setNotNullErrorStream() throws IOException {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();

		assertNotNull(fAbstractScriptEngineTest.getErrorStream());
		fAbstractScriptEngineTest.getErrorStream().print("test");
		Arrays.equals("test".getBytes(), bos.toByteArray());
	}

	@Test
	public void setNullErrorStream() {
		fAbstractScriptEngineTest.setErrorStream(null);

		assertEquals(System.err, fAbstractScriptEngineTest.getErrorStream());
	}
}
