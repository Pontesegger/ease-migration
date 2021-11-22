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

package org.eclipse.ease.modules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.ease.ExitException;
import org.eclipse.ease.ICodeFactory;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Script;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.ScriptType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class EnvironmentModuleTest {

	private IScriptEngine fEngine;
	private EnvironmentModule fModule;

	@BeforeEach
	public void beforeEach() {
		fEngine = mock(IScriptEngine.class);

		fModule = new EnvironmentModule();
		fModule.initialize(fEngine, fModule);
	}

	@Test
	@DisplayName("getModuleDefinition(env) returns environment definition")
	public void getModuleDefinition_returns_environment_definition() {
		assertEquals(new Path(EnvironmentModule.MODULE_NAME).lastSegment(), fModule.getModuleDefinition(fModule).getName());
	}

	@Test
	@DisplayName("loadModule() throws on unknown module")
	public void loadModule_throws_on_unknown_module() {
		assertThrows(IllegalArgumentException.class, () -> fModule.loadModule("unknown", false));
	}

	@Test
	@DisplayName("getScriptEngine() returns engine instance")
	public void getScriptEngine_returns_engine_instance() {
		assertEquals(fEngine, fModule.getScriptEngine());
	}

	@Test
	@DisplayName("include() executes referenced resource")
	public void include_executes_referenced_resource() throws ExecutionException {
		when(fEngine.inject(any(), anyBoolean())).thenReturn("done");
		final IFile resource = mock(IFile.class);

		assertEquals("done", fModule.include(resource));
	}

	@Test
	@DisplayName("include() throws on invalid resource")
	public void include_throws_on_invalid_resource() throws ExecutionException {
		assertThrows(IllegalArgumentException.class, () -> fModule.include("does not exist"));
	}

	@Test
	@DisplayName("exit() throws exception")
	public void exit_throws_exception() {
		assertThrows(ExitException.class, () -> fModule.exit(0));
	}

	@Test
	@DisplayName("exit() sets script return code")
	public void exit_sets_script_return_code() {
		try {
			fModule.exit("condition");
			fail("ExitException expected");

		} catch (final ExitException e) {
			assertEquals("condition", e.getCondition());
		}
	}

	@Test
	@DisplayName("execute() injects script code")
	public void execute_injects_script_code() throws ExecutionException {
		when(fEngine.inject(any(), anyBoolean())).thenReturn("done");

		assertEquals("done", fModule.execute("2+2"));

		final ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
		verify(fEngine).inject(captor.capture(), anyBoolean());
		assertEquals("2+2", captor.getValue());
	}

	@Test
	@DisplayName("getWrappedVariableName() returns unique name per class")
	public void getWrappedVariableName_returns_unique_name_per_class() {
		assertEquals("__EASE_MOD_java_lang_String", EnvironmentModule.getWrappedVariableName(""));
	}

	@Test
	@DisplayName("wrap() wraps instance methods")
	public void wrap_wraps_instance_methods() throws ExecutionException {
		mockDummyFactory();
		when(fEngine.hasVariable(any())).thenReturn(false);

		final String objectToWrap = "";
		assertEquals(objectToWrap, fModule.wrap(objectToWrap, false));

		final ArgumentCaptor<String> variableNameCaptor = ArgumentCaptor.forClass(String.class);
		final ArgumentCaptor<Object> variableInstanceCaptor = ArgumentCaptor.forClass(Object.class);
		verify(fEngine).setVariable(variableNameCaptor.capture(), variableInstanceCaptor.capture());

		assertEquals("saveName", variableNameCaptor.getValue());
		assertEquals(objectToWrap, variableInstanceCaptor.getValue());

		final ArgumentCaptor<Script> scriptCaptor = ArgumentCaptor.forClass(Script.class);
		verify(fEngine).inject(scriptCaptor.capture(), anyBoolean());

		assertEquals("wrappedCode", scriptCaptor.getValue().getCommand());
	}

	@Test
	@DisplayName("readInput(non-blocking) = '' when no data is available")
	public void readInput_nonBlocking_is_empty_when_no_data_is_availble() throws IOException {
		when(fEngine.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes()));

		assertEquals("", fModule.readInput(false));
	}

	@Test
	@DisplayName("readInput(non-blocking) = 'firstLine' when  data is available")
	public void readInput_nonBlocking_is_firstLine_when_data_is_availble() throws IOException {
		when(fEngine.getInputStream()).thenReturn(new ByteArrayInputStream("firstLine\nsecondLine".getBytes()));

		assertEquals("firstLine", fModule.readInput(false));
	}

	@Test
	@DisplayName("readInput() filters carriage return")
	public void readInput_filters_carriage_return() throws IOException {
		when(fEngine.getInputStream()).thenReturn(new ByteArrayInputStream("firstLine\r\nsecondLine".getBytes()));

		assertEquals("firstLine", fModule.readInput(false));
	}

	@Test
	@DisplayName("readInput(blocking) reads single line on each call")
	public void readInput_blocking_reads_single_line_on_each_call() throws IOException {
		final InputStream input = new ByteArrayInputStream("firstLine\nsecondLine\n".getBytes());
		when(fEngine.getInputStream()).thenReturn(input);

		assertEquals("firstLine", fModule.readInput(true));
		assertEquals("secondLine", fModule.readInput(true));
	}

	@Test
	@DisplayName("readInput(blocking) stops when stream is closed")
	public void readInput_blocking_stops_when_stream_is_closed() throws IOException {
		final InputStream input = new ByteArrayInputStream("firstLine".getBytes());
		when(fEngine.getInputStream()).thenReturn(input);

		assertEquals("firstLine", fModule.readInput(true));
	}

	@Test
	@DisplayName("printError() writes to error stream")
	public void printError_writes_to_error_stream() {
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		final PrintStream out = new PrintStream(outputStream);
		when(fEngine.getErrorStream()).thenReturn(out);

		fModule.printError("error text", false);

		assertEquals(String.format("error text%n"), outputStream.toString());
	}

	@Test
	@DisplayName("printError(once) prints same error only once")
	public void printError_once_prints_same_error_only_once() {
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		final PrintStream out = new PrintStream(outputStream);
		when(fEngine.getErrorStream()).thenReturn(out);

		fModule.printError("error text", true);
		fModule.printError("error text", true);

		assertEquals(String.format("error text%n"), outputStream.toString());
	}

	@Test
	@DisplayName("printError(multiple) prints same error multiple times")
	public void printError_multiple_prints_same_error_multiple_times() {
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		final PrintStream out = new PrintStream(outputStream);
		when(fEngine.getErrorStream()).thenReturn(out);

		fModule.printError("error text", false);
		fModule.printError("error text", false);

		assertEquals(String.format("error text%nerror text%n"), outputStream.toString());
	}

	@Test
	@DisplayName("print(false) writes to output stream")
	public void print_false_writes_to_output_string() {
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		final PrintStream out = new PrintStream(outputStream);
		when(fEngine.getOutputStream()).thenReturn(out);

		fModule.print("output text", false);

		assertEquals("output text", outputStream.toString());
	}

	@Test
	@DisplayName("print(true) appends line feed")
	public void print_true_appends_line_feed() {
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		final PrintStream out = new PrintStream(outputStream);
		when(fEngine.getOutputStream()).thenReturn(out);

		fModule.print("output text", true);

		assertEquals(String.format("output text%n"), outputStream.toString());
	}

	@Test
	@DisplayName("getModules() contains environment module")
	public void getModules_contains_environment_module() {
		assertEquals(1, fModule.getModules().size());
		assertEquals(fModule, fModule.getModules().get(0));
	}

	@Test
	@DisplayName("getModules() contains loaded module")
	public void getModules_contains_loaded_module() throws ExecutionException {
		mockDummyFactory();

		final Object testModule = fModule.loadModule("TestModule", false);
		assertEquals(2, fModule.getModules().size());
		assertTrue(fModule.getModules().contains(testModule));
	}

	@Test
	@DisplayName("getModule(class) finds Environment module")
	public void getModule_by_class_finds_environment_module() throws ExecutionException {
		assertEquals(fModule, fModule.getModule(EnvironmentModule.class));
	}

	@Test
	@DisplayName("getModule(class) finds loaded module")
	public void getModule_by_class_finds_loaded_module() throws ExecutionException {
		mockDummyFactory();

		final Object testModule = fModule.loadModule("TestModule", false);
		assertEquals(testModule, fModule.getModule(TestModule.class));
	}

	@Test
	@DisplayName("getModule(class) = null for unknown module")
	public void getModule_by_class_returns_null_for_unknown_module() throws ExecutionException {
		assertNull(fModule.getModule(EnvironmentModuleTest.class));
	}

	@Test
	@DisplayName("getModule(name) finds Environment module")
	public void getModule_by_name_finds_environment_module() throws ExecutionException {
		assertEquals(fModule, fModule.getModule("Environment"));
	}

	@Test
	@DisplayName("getModule(name) finds loaded module")
	public void getModule_by_name_finds_loaded_module() throws ExecutionException {
		mockDummyFactory();

		final Object testModule = fModule.loadModule("TestModule", false);
		assertEquals(testModule, fModule.getModule("TestModule"));
	}

	@Test
	@DisplayName("getModule(name) = null for unknown module")
	public void getModule_by_name_returns_null_for_unknonw_module() throws ExecutionException {
		assertNull(fModule.getModule("unknown"));
	}

	@Test
	@DisplayName("addModuleListener() informs listener on module loading")
	public void addModuleListener_informs_listener_on_module_load() throws ExecutionException {
		mockDummyFactory();

		final IModuleListener listener = mock(IModuleListener.class);
		fModule.addModuleListener(listener);

		final Object testModule = fModule.loadModule("TestModule", false);

		final ArgumentCaptor<Object> moduleCaptor = ArgumentCaptor.forClass(Object.class);
		final ArgumentCaptor<Integer> modeCaptor = ArgumentCaptor.forClass(int.class);

		verify(listener).notifyModule(moduleCaptor.capture(), modeCaptor.capture());
		assertEquals(testModule, moduleCaptor.getValue());
		assertEquals(IModuleListener.LOADED, modeCaptor.getValue());
	}

	@Test
	@DisplayName("addModuleListener() sends reloaded event")
	public void addModuleListener_sends_reloaded_event() throws ExecutionException {
		mockDummyFactory();
		when(fEngine.hasVariable(any())).thenReturn(true);

		final IModuleListener listener = mock(IModuleListener.class);
		fModule.addModuleListener(listener);

		final Object testModule = fModule.loadModule("Environment", false);

		final ArgumentCaptor<Object> moduleCaptor = ArgumentCaptor.forClass(Object.class);
		final ArgumentCaptor<Integer> modeCaptor = ArgumentCaptor.forClass(int.class);

		verify(listener).notifyModule(moduleCaptor.capture(), modeCaptor.capture());
		assertEquals(testModule, moduleCaptor.getValue());
		assertEquals(IModuleListener.RELOADED, modeCaptor.getValue());
	}

	@Test
	@DisplayName("removeModuleListener() stops sending events to listener")
	public void removeModuleListener_stops_sending_events_to_listener() throws ExecutionException {
		mockDummyFactory();

		final IModuleListener listener = mock(IModuleListener.class);
		fModule.addModuleListener(listener);
		fModule.removeModuleListener(listener);
		fModule.loadModule("TestModule", false);

		verify(listener, never()).notifyModule(any(), anyInt());
	}

	@Test
	@DisplayName("hasMethodCallback() = true when a callbackProvider is registered for a method")
	public void hasMethodCallback_is_true_when_a_callbackProvider_is_registered_for_a_method() throws NoSuchMethodException, SecurityException {
		final IModuleCallbackProvider callbackProvider = mock(IModuleCallbackProvider.class);
		when(callbackProvider.hasPreExecutionCallback(any())).thenReturn(true);

		final Method method = EnvironmentModule.class.getMethod("print", Object.class, boolean.class);
		final String methodToken = fModule.registerMethod(method);
		fModule.addModuleCallback(callbackProvider);

		assertTrue(fModule.hasMethodCallback(methodToken));
	}

	@Test
	@DisplayName("hasMethodCallback() = false when no callbackProvider is registered")
	public void hasMethodCallback_is_false_when_no_callbackProvider_is_registered() throws NoSuchMethodException, SecurityException {

		final Method method = EnvironmentModule.class.getMethod("print", Object.class, boolean.class);
		final String methodToken = fModule.registerMethod(method);

		assertFalse(fModule.hasMethodCallback(methodToken));
	}

	@Test
	@DisplayName("hasMethodCallback() = false when method is not registered")
	public void hasMethodCallback_is_false_when_method_is_not_registered() throws NoSuchMethodException, SecurityException {
		final IModuleCallbackProvider callbackProvider = mock(IModuleCallbackProvider.class);
		fModule.addModuleCallback(callbackProvider);

		assertFalse(fModule.hasMethodCallback("unknown"));
	}

	@Test
	@DisplayName("preMethodCallback() calls callbackProvider")
	public void preMethodCallback_calls_callbackProvider() throws NoSuchMethodException, SecurityException {
		final IModuleCallbackProvider callbackProvider = mock(IModuleCallbackProvider.class);
		when(callbackProvider.hasPreExecutionCallback(any())).thenReturn(true);

		final Method method = EnvironmentModule.class.getMethod("print", Object.class, boolean.class);
		final String methodToken = fModule.registerMethod(method);
		fModule.addModuleCallback(callbackProvider);
		fModule.preMethodCallback(methodToken, new Object[0]);

		verify(callbackProvider).preExecutionCallback(eq(method), any());
	}

	@Test
	@DisplayName("postMethodCallback() calls callbackProvider")
	public void postMethodCallback_calls_callbackProvider() throws NoSuchMethodException, SecurityException {
		final IModuleCallbackProvider callbackProvider = mock(IModuleCallbackProvider.class);
		when(callbackProvider.hasPostExecutionCallback(any())).thenReturn(true);

		final Method method = EnvironmentModule.class.getMethod("print", Object.class, boolean.class);
		final String methodToken = fModule.registerMethod(method);
		fModule.addModuleCallback(callbackProvider);
		fModule.postMethodCallback(methodToken, new Object[0]);

		verify(callbackProvider).postExecutionCallback(eq(method), any());
	}

	@Test
	@DisplayName("loadJar() registers URL")
	public void loadJar_registers_URL() throws MalformedURLException {
		final URL url = new URL("http://eclipse.org/notThere.jar");
		assertTrue(fModule.loadJar(url));

		verify(fEngine).registerJar(url);
	}

	private void mockDummyFactory() {
		final ICodeFactory codeFactory = mock(ICodeFactory.class);
		when(codeFactory.getSaveVariableName(any())).thenReturn("saveName");
		when(codeFactory.createWrapper(any(), any(), any(), anyBoolean(), any())).thenReturn("wrappedCode");

		final ScriptType scriptType = mock(ScriptType.class);
		when(scriptType.getCodeFactory()).thenReturn(codeFactory);

		final EngineDescription description = mock(EngineDescription.class);
		when(description.getSupportedScriptTypes()).thenReturn(List.of(scriptType));

		when(fEngine.getDescription()).thenReturn(description);
	}
}
