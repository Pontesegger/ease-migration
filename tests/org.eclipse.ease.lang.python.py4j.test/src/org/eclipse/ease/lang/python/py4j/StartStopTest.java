/*******************************************************************************
 * Copyright (c) 2016 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jonah Graham (Kichwa Coders) - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.python.py4j;

import org.eclipse.ease.IReplEngine;
import org.junit.Test;

public class StartStopTest extends EaseTestBase {

	@Test
	public void startStop() throws Exception {
		final IReplEngine engine = createEngine();
		final ByteArrayPrintStream errorStream = ((ByteArrayPrintStream) engine.getErrorStream());
		final ByteArrayPrintStream outputStream = ((ByteArrayPrintStream) engine.getErrorStream());
		engine.setTerminateOnIdle(false);
		assertNotStarted(engine);
		engine.schedule();
		assertRunning(engine);
		engine.setTerminateOnIdle(true);
		assertEngineTerminated(engine);
		errorStream.assertNoOutput();
		outputStream.assertNoOutput();
	}

	@Test
	public void startStopShellNoBootstrap() throws Exception {
		final IReplEngine engine = createEngineWithoutBootstrap();
		final ByteArrayPrintStream errorStream = ((ByteArrayPrintStream) engine.getErrorStream());
		final ByteArrayPrintStream outputStream = ((ByteArrayPrintStream) engine.getErrorStream());
		engine.setTerminateOnIdle(false);
		assertNotStarted(engine);
		engine.schedule();
		assertRunning(engine);
		engine.setTerminateOnIdle(true);
		assertEngineTerminated(engine);
		errorStream.assertNoOutput();
		outputStream.assertNoOutput();
	}

	@Test
	public void multiStartStop() throws Exception {
		final IReplEngine engine1 = createEngine();
		final IReplEngine engine2 = createEngine();
		final ByteArrayPrintStream errorStream1 = ((ByteArrayPrintStream) engine1.getErrorStream());
		final ByteArrayPrintStream outputStream1 = ((ByteArrayPrintStream) engine1.getErrorStream());
		final ByteArrayPrintStream errorStream2 = ((ByteArrayPrintStream) engine2.getErrorStream());
		final ByteArrayPrintStream outputStream2 = ((ByteArrayPrintStream) engine2.getErrorStream());
		engine1.setTerminateOnIdle(false);
		engine2.setTerminateOnIdle(false);
		assertNotStarted(engine1);
		assertNotStarted(engine2);
		engine1.schedule();
		engine2.schedule();
		assertRunning(engine1);
		assertRunning(engine2);
		engine1.setTerminateOnIdle(true);
		engine2.setTerminateOnIdle(true);
		assertEngineTerminated(engine1);
		assertEngineTerminated(engine2);
		errorStream1.assertNoOutput();
		outputStream1.assertNoOutput();
		errorStream2.assertNoOutput();
		outputStream2.assertNoOutput();
	}
}
