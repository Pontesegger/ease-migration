/*******************************************************************************
 * Copyright (c) 2016 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License_Identifier: EPL-2.0
 *
 * Contributors:
 *     Jonah Graham (Kichwa Coders) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.lang.python.py4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ease.IReplEngine;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.ScriptResult;
import org.eclipse.ease.lang.python.py4j.internal.Py4jScriptEngine;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;
import org.junit.jupiter.api.Timeout;

@Timeout(value = 30, unit = TimeUnit.SECONDS)
public abstract class EaseTestBase {

	protected static class TeeOutput extends OutputStream {

		private final OutputStream[] fStreams;

		public TeeOutput(OutputStream... streams) {
			fStreams = streams;
		}

		@Override
		public void write(int b) throws IOException {
			for (final OutputStream outputStream : fStreams) {
				outputStream.write(b);
			}
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			for (final OutputStream outputStream : fStreams) {
				outputStream.write(b, off, len);
			}
		}

		@Override
		public void flush() throws IOException {
			for (final OutputStream outputStream : fStreams) {
				outputStream.flush();
			}
		}

		@Override
		public void close() throws IOException {
			for (final OutputStream outputStream : fStreams) {
				outputStream.close();
			}
		}
	}

	/**
	 * Extension of PrintStream that uses a {@link ByteArrayOutputStream}.
	 *
	 * Provides some methods to aid testability.
	 */
	protected static class ByteArrayPrintStream extends PrintStream {
		public static final String ENCODING = "UTF-8";
		private final ByteArrayOutputStream fByteArrayOutputStream;

		protected static ByteArrayPrintStream build(OutputStream tee) throws UnsupportedEncodingException {
			final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			OutputStream out = byteArrayOutputStream;
			// Uncomment this line of code to "tee" the output to stdout/err
			out = new TeeOutput(byteArrayOutputStream, tee);
			return new ByteArrayPrintStream(out, byteArrayOutputStream);
		}

		private ByteArrayPrintStream(OutputStream out, ByteArrayOutputStream byteArrayOutputStream) throws UnsupportedEncodingException {
			super(out, true, ENCODING);
			fByteArrayOutputStream = byteArrayOutputStream;
		}

		public void assertNoOutput() throws Exception {
			assertEquals("", getAndClearOutput());
		}

		public String getAndClearOutput() throws Exception {
			synchronized (fByteArrayOutputStream) {
				final String streamString = getOutput();
				fByteArrayOutputStream.reset();
				return streamString;
			}
		}

		public String getOutput() throws Exception {
			final byte[] streamBytes = fByteArrayOutputStream.toByteArray();
			// We do the conversion to String so that we get to compare strings in the asserts instead of arrays
			final String streamString = new String(streamBytes, ByteArrayPrintStream.ENCODING);
			return streamString;
		}
	}

	protected void setStreamsForTest(IScriptEngine engine) throws UnsupportedEncodingException {
		engine.setInputStream(new ByteArrayInputStream(new byte[0]));
		engine.setOutputStream(ByteArrayPrintStream.build(engine.getOutputStream()));
		engine.setErrorStream(ByteArrayPrintStream.build(engine.getErrorStream()));
	}

	protected IReplEngine createEngineWithoutBootstrap() throws Exception {
		final IScriptService scriptService = ScriptService.getService();
		final EngineDescription description = scriptService.getEngineByID(Py4jScriptEngine.ENGINE_ID);

		// this is what description.createEngine() does, excluding the bootstrapping
		final Py4jScriptEngine engine = new Py4jScriptEngine();
		engine.setEngineDescription(description);
		setStreamsForTest(engine);
		return engine;
	}

	protected IReplEngine createEngine() throws Exception {
		// we need to retrieve the service singleton as the workspace is not available in headless tests
		final IScriptService scriptService = ScriptService.getService();
		final EngineDescription description = scriptService.getEngineByID(Py4jScriptEngine.ENGINE_ID);
		final IScriptEngine engine = description.createEngine();
		setStreamsForTest(engine);
		return (IReplEngine) engine;
	}

	protected void waitUntil(long millis, Callable<Boolean> callable) throws Exception {
		final long endTime = System.currentTimeMillis() + millis;
		while (!callable.call() && (System.currentTimeMillis() < endTime)) {
			Thread.sleep(100);
		}
		assertTrue(callable.call());
	}

	protected void waitUntil(Callable<Boolean> callable) throws Exception {
		waitUntil(10000, callable);
	}

	protected void assertRunning(IScriptEngine engine) throws Exception {
		final IAdaptable adaptable = (IAdaptable) engine;
		waitUntil(() -> ((adaptable.getAdapter(Process.class) != null) && adaptable.getAdapter(Process.class).isAlive()) && !engine.isFinished());
	}

	protected void assertNotStarted(IScriptEngine engine) throws Exception {
		final IAdaptable adaptable = (IAdaptable) engine;
		final Process adapter = adaptable.getAdapter(Process.class);
		waitUntil(() -> ((adaptable.getAdapter(Process.class) == null) || !adapter.isAlive()) && !engine.isFinished());
	}

	protected void assertEngineTerminated(IScriptEngine engine) throws Exception {
		final IAdaptable adaptable = (IAdaptable) engine;
		final Process adapter = adaptable.getAdapter(Process.class);
		waitUntil(() -> ((adaptable.getAdapter(Process.class) == null) || !adapter.isAlive()) && engine.isFinished());
	}

	protected void assertResultIsNone(ScriptResult result) throws ExecutionException {
		assertNull(result.get());
	}

}
