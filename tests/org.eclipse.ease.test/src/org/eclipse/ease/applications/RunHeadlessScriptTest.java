/*******************************************************************************
 * Copyright (c) 2021 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.applications;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplicationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;

public class RunHeadlessScriptTest {

	private static File getFile(String location) throws URISyntaxException, IOException {
		final Bundle bundle = Platform.getBundle("org.eclipse.ease.test");
		final URL fileURL = bundle.getEntry(location);
		return new File(FileLocator.resolve(fileURL).toURI());
	}

	private Map<Object, Object> fArguments;
	private IApplicationContext fContext;

	@BeforeEach
	public void beforeEach() {
		fArguments = new HashMap<>();

		fContext = mock(IApplicationContext.class);
		when(fContext.getArguments()).thenReturn(fArguments);
	}

	@Test
	@DisplayName("start() == -1 without arguments")
	public void start_script() throws Exception {
		assertEquals(-1, new RunHeadlessScript().start(fContext));
	}

	@Test
	@DisplayName("start() == -1 when script location is missing")
	public void start_error_when_script_location_is_missing() throws Exception {
		fArguments.put(IApplicationContext.APPLICATION_ARGS, new String[] { "-script" });
		assertEquals(-1, new RunHeadlessScript().start(fContext));
	}

	@Test
	@DisplayName("start() == -1 when script location is invalid")
	public void start_error_when_script_location_is_invalid() throws Exception {
		fArguments.put(IApplicationContext.APPLICATION_ARGS, new String[] { "-script", "notThere" });
		assertEquals(-1, new RunHeadlessScript().start(fContext));
	}

	@Test
	@DisplayName("start() == -1 when workspace location is missing")
	public void start_error_when_workspace_location_is_missing() throws Exception {
		fArguments.put(IApplicationContext.APPLICATION_ARGS, new String[] { "-workspace" });
		assertEquals(-1, new RunHeadlessScript().start(fContext));
	}

	@Test
	@DisplayName("start() == -1 when engine ID is missing")
	public void start_error_when_engine_ID_is_missing() throws Exception {
		fArguments.put(IApplicationContext.APPLICATION_ARGS, new String[] { "-engine" });
		assertEquals(-1, new RunHeadlessScript().start(fContext));
	}

	@Test
	@DisplayName("start() == -1 when engine ID is invalid")
	public void start_error_when_engine_ID_is_invalid() throws Exception {
		fArguments.put(IApplicationContext.APPLICATION_ARGS,
				new String[] { "-engine", "none", "-script", getFile("resources/helloWorld.js").getAbsolutePath() });
		assertEquals(-1, new RunHeadlessScript().start(fContext));
	}

	@Test
	@DisplayName("start() == -1 when argument is unknown")
	public void start_error_when_argument_is_unknown() throws Exception {
		fArguments.put(IApplicationContext.APPLICATION_ARGS, new String[] { "-unknown" });
		assertEquals(-1, new RunHeadlessScript().start(fContext));
	}

	@Test
	@DisplayName("start() == 0 for helloWorld.js")
	public void start_executes_hello_world() throws Exception {
		fArguments.put(IApplicationContext.APPLICATION_ARGS, new String[] { "-script", getFile("resources/helloWorld.js").getAbsolutePath() });
		assertEquals(0, new RunHeadlessScript().start(fContext));
	}

	@Test
	@DisplayName("start() == 4 for scriptArguments.js")
	public void start_executes_script_arguments() throws Exception {
		fArguments.put(IApplicationContext.APPLICATION_ARGS,
				new String[] { "-script", getFile("resources/scriptArguments.js").getAbsolutePath(), "1", "2", "3" });
		assertEquals(4, new RunHeadlessScript().start(fContext));
	}

	@Test
	@DisplayName("start() == 10 for returnValue.js")
	public void start_executes_script_with_return_value() throws Exception {
		fArguments.put(IApplicationContext.APPLICATION_ARGS, new String[] { "-script", getFile("resources/returnValue.js").getAbsolutePath() });
		assertEquals(10, new RunHeadlessScript().start(fContext));
	}

	@Test
	@DisplayName("start() == -1 when script throws")
	public void start_error_when_script_throws() throws Exception {
		fArguments.put(IApplicationContext.APPLICATION_ARGS, new String[] { "-script", getFile("resources/throwsException.js").getAbsolutePath() });
		assertEquals(-1, new RunHeadlessScript().start(fContext));
	}

	@Test
	@DisplayName("start() == -1 when -help is set")
	public void start_error_when_help_is_set() throws Exception {
		fArguments.put(IApplicationContext.APPLICATION_ARGS, new String[] { "-help" });
		assertEquals(-1, new RunHeadlessScript().start(fContext));
	}
}
