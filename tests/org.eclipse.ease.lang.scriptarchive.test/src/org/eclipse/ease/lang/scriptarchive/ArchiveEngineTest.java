package org.eclipse.ease.lang.scriptarchive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.ScriptExecutionException;
import org.eclipse.ease.ScriptResult;
import org.eclipse.ease.service.EngineDescription;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;
import org.junit.jupiter.api.Test;

public class ArchiveEngineTest {

	@Test
	public void zipFileRegistration() {
		final IScriptService scriptService = ScriptService.getInstance();
		final EngineDescription engine = scriptService.getEngine(scriptService.getScriptType("foo.zip").getName());
		assertEquals("org.eclipse.ease.lang.scriptarchive.engine", engine.getID());
	}

	@Test
	public void jarFileRegistration() {
		final IScriptService scriptService = ScriptService.getInstance();
		final EngineDescription engine = scriptService.getEngine(scriptService.getScriptType("foo.jar").getName());
		assertEquals("org.eclipse.ease.lang.scriptarchive.engine", engine.getID());
	}

	@Test
	public void sarFileRegistration() {
		final IScriptService scriptService = ScriptService.getInstance();
		final EngineDescription engine = scriptService.getEngine(scriptService.getScriptType("foo.sar").getName());
		assertEquals("org.eclipse.ease.lang.scriptarchive.engine", engine.getID());
	}

	@Test
	public void engineIdRegistration() {
		final IScriptService scriptService = ScriptService.getInstance();
		final EngineDescription engine = scriptService.getEngineByID("org.eclipse.ease.lang.scriptarchive.engine");
		assertEquals("org.eclipse.ease.lang.scriptarchive.engine", engine.getID());
	}

	@Test
	public void executeWithManifest() throws MalformedURLException, ExecutionException {
		final IScriptService scriptService = ScriptService.getInstance();

		final EngineDescription engineDescription = scriptService.getEngine(scriptService.getScriptType("foo.sar").getName());
		final IScriptEngine engine = engineDescription.createEngine();

		final URL location = new URL("platform:/plugin/org.eclipse.ease.lang.scriptarchive.test/resources/manifest.sar");
		final ScriptResult result = engine.execute(location);
		engine.schedule();

		assertEquals(42.0, Double.parseDouble(result.get().toString()), 0.1);
	}

	@Test
	public void executeWithIncludes() throws MalformedURLException, ExecutionException {
		final IScriptService scriptService = ScriptService.getInstance();

		final EngineDescription engineDescription = scriptService.getEngine(scriptService.getScriptType("foo.sar").getName());
		final IScriptEngine engine = engineDescription.createEngine();

		final URL location = new URL("platform:/plugin/org.eclipse.ease.lang.scriptarchive.test/resources/with_includes.sar");
		final ScriptResult result = engine.execute(location);
		engine.schedule();

		assertEquals(6.0, Double.parseDouble(result.get().toString()), 0.1);

		// make sure no temporary projects remain
		for (final IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects())
			assertFalse(project.getName().contains("__EASE"));
	}

	@Test
	public void executeWithErrors() throws MalformedURLException, InterruptedException {
		final IScriptService scriptService = ScriptService.getInstance();

		final EngineDescription engineDescription = scriptService.getEngine(scriptService.getScriptType("foo.sar").getName());
		final IScriptEngine engine = engineDescription.createEngine();

		final URL location = new URL("platform:/plugin/org.eclipse.ease.lang.scriptarchive.test/resources/with_errors.sar");
		final ScriptResult result = engine.execute(location);
		engine.schedule();

		assertThrows(ScriptExecutionException.class, () -> result.get());

		// make sure no temporary projects remain
		for (final IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects())
			assertFalse(project.getName().contains("__EASE"));
	}
}
