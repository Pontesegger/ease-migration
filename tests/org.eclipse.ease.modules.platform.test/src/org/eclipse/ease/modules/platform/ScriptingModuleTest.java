/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.modules.platform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.net.URL;

import org.eclipse.ease.IExecutionListener;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.ScriptResult;
import org.eclipse.ease.lang.javascript.rhino.RhinoScriptEngine;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class ScriptingModuleTest {

	@Test(timeout = 2000)
	public void forkWithoutDebugMode() throws Exception {
		final IScriptService scriptService = ScriptService.getService();
		final EngineDescription description = scriptService.getEngineByID(RhinoScriptEngine.ENGINE_ID);
		final IScriptEngine engine = description.createEngine();

		final ScriptingModule creatorModule = new ScriptingModule();
		creatorModule.initialize(engine, null);
		final URL location = new URL("platform:/plugin/org.eclipse.ease.modules.platform.test/resources/test.js");
		final ScriptResult result = creatorModule.fork(location, "", RhinoScriptEngine.ENGINE_ID);
		assertNotNull(result);
		result.waitForResult();
		assertEquals("testing fork command", result.getResult().toString());
	}

	@Test
	public void storeTemporaryObject() {
		final Object testObject = new Object();

		// mocked script engine
		final IScriptEngine mockEngine = mock(IScriptEngine.class);

		// initialize module
		final ScriptingModule module = new ScriptingModule();
		module.initialize(mockEngine, null);

		// set the object
		try {
			module.setSharedObject("temp", testObject, false, false);
		} catch (final IllegalAccessException e) {
			fail(e.getMessage());
		}

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
	public void storePermanentObject() {
		final Object testObject = new Object();

		// mocked script engine
		final IScriptEngine mockEngine = mock(IScriptEngine.class);

		// initialize module
		final ScriptingModule module = new ScriptingModule();
		module.initialize(mockEngine, null);

		// set the object
		try {
			module.setSharedObject("perm", testObject, true, false);

			// set another temp object to make sure the execution listener gets installed
			module.setSharedObject("anotherTemp", testObject, false, false);
		} catch (final IllegalAccessException e) {
			fail(e.getMessage());
		}

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

	@Test(expected = IllegalAccessException.class)
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
		try {
			creatorModule.setSharedObject("foreign", testObject, false, false);
		} catch (final IllegalAccessException e) {
			fail(e.getMessage());
		}

		modifierModule.setSharedObject("foreign", testObject, false, false);
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
}
