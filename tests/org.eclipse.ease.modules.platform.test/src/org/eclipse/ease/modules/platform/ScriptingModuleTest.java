/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License_Identifier: EPL-2.0
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.modules.platform;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.eclipse.ease.IExecutionListener;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.ScriptResult;
import org.eclipse.ease.lang.javascript.rhino.RhinoScriptEngine;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.ArgumentCaptor;

public class ScriptingModuleTest {

	@Timeout(value = 2, unit = TimeUnit.SECONDS)
	public void forkWithoutDebugMode() throws MalformedURLException, InterruptedException, ExecutionException {
		final IScriptService scriptService = ScriptService.getService();
		final EngineDescription description = scriptService.getEngineByID(RhinoScriptEngine.ENGINE_ID);
		final IScriptEngine engine = description.createEngine();

		final ScriptingModule creatorModule = new ScriptingModule();
		creatorModule.initialize(engine, null);
		final URL location = new URL("platform:/plugin/org.eclipse.ease.modules.platform.test/resources/test.js");
		final ScriptResult result = creatorModule.fork(location, "", RhinoScriptEngine.ENGINE_ID);
		assertNotNull(result);
		assertEquals("testing fork command", result.get().toString());
	}

	@Test
	public void storeTemporaryObject() throws IllegalAccessException {
		final Object testObject = new Object();

		// mocked script engine
		final IScriptEngine mockEngine = mock(IScriptEngine.class);

		// initialize module
		final ScriptingModule module = new ScriptingModule();
		module.initialize(mockEngine, null);

		// set the object
		module.setSharedObject("temp", testObject, false, false);

		// retrieve the object
		assertEquals(testObject, module.getSharedObject("temp"));

		// capture execution listener
		final ArgumentCaptor<IExecutionListener> argument = ArgumentCaptor.forClass(IExecutionListener.class);
		verify(mockEngine).addExecutionListener(argument.capture());

		// terminate engine
		argument.getValue().notify(mockEngine, null, IExecutionListener.ENGINE_END);

		// make sure the object got removed
		assertNull(module.getSharedObject("temp"));
	}

	@Test
	public void storePermanentObject() throws IllegalAccessException {
		final Object testObject = new Object();

		// mocked script engine
		final IScriptEngine mockEngine = mock(IScriptEngine.class);

		// initialize module
		final ScriptingModule module = new ScriptingModule();
		module.initialize(mockEngine, null);

		// set the object
		module.setSharedObject("perm", testObject, true, false);

		// set another temp object to make sure the execution listener gets installed
		module.setSharedObject("anotherTemp", testObject, false, false);

		// retrieve the object
		assertEquals(testObject, module.getSharedObject("perm"));

		// capture execution listener
		final ArgumentCaptor<IExecutionListener> argument = ArgumentCaptor.forClass(IExecutionListener.class);
		verify(mockEngine).addExecutionListener(argument.capture());

		// terminate engine
		argument.getValue().notify(mockEngine, null, IExecutionListener.ENGINE_END);

		// make sure the object got removed
		assertEquals(testObject, module.getSharedObject("perm"));
	}

	@Test
	public void overwriteForeignObject() throws IllegalAccessException {
		final Object testObject = new Object();

		// mocked script engine
		final IScriptEngine creatorEngine = mock(IScriptEngine.class);
		final IScriptEngine modifierEngine = mock(IScriptEngine.class);

		// initialize modules
		final ScriptingModule creatorModule = new ScriptingModule();
		creatorModule.initialize(creatorEngine, null);

		final ScriptingModule modifierModule = new ScriptingModule();
		modifierModule.initialize(modifierEngine, null);

		// set the object
		creatorModule.setSharedObject("foreign", testObject, false, false);

		// try to overwrite
		assertThrows(IllegalAccessException.class, () -> modifierModule.setSharedObject("foreign", testObject, false, false));
	}

	@Test
	public void overwriteForeignUnlockedObject() throws IllegalAccessException {
		final Object testObject = new Object();

		// mocked script engine
		final IScriptEngine creatorEngine = mock(IScriptEngine.class);
		final IScriptEngine modifierEngine = mock(IScriptEngine.class);

		// initialize modules
		final ScriptingModule creatorModule = new ScriptingModule();
		creatorModule.initialize(creatorEngine, null);

		final ScriptingModule modifierModule = new ScriptingModule();
		modifierModule.initialize(modifierEngine, null);

		// set the object
		creatorModule.setSharedObject("foreignShared", testObject, false, true);

		// reset the object
		modifierModule.setSharedObject("foreignShared", testObject, false, false);
	}

	@Test
	public void extractEmptyArguments() {
		assertEquals(0, ScriptingModule.extractArguments(null).length);
		assertEquals(0, ScriptingModule.extractArguments("").length);
		assertEquals(0, ScriptingModule.extractArguments("    ").length);
		assertEquals(0, ScriptingModule.extractArguments("\t\t").length);
	}

	@Test
	public void extractArguments() {
		assertArrayEquals(new String[] { "one" }, ScriptingModule.extractArguments("one"));
		assertArrayEquals(new String[] { "one with spaces" }, ScriptingModule.extractArguments("one with spaces"));
		assertArrayEquals(new String[] { "one", "and", "another" }, ScriptingModule.extractArguments("one,and, another"));
	}
}
