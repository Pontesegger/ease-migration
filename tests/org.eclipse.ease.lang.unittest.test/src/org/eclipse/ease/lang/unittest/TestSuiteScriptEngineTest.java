/*******************************************************************************
 * Copyright (c) 2019 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.unittest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ease.lang.unittest.runtime.ITestContainer;
import org.eclipse.ease.lang.unittest.runtime.ITestEntity;
import org.eclipse.ease.lang.unittest.runtime.ITestSuite;
import org.eclipse.ease.lang.unittest.runtime.TestStatus;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;

public class TestSuiteScriptEngineTest {

	private static ITestSuite fTestSuite;
	private static String fTestOutput;

	@BeforeClass
	public static void runTestSuite() throws InvocationTargetException, URISyntaxException, IOException, CoreException, InterruptedException {
		final IProject project = importTestProject();

		final IScriptService scriptService = ScriptService.getService();
		final TestSuiteScriptEngine engine = (TestSuiteScriptEngine) scriptService.getEngineByID(TestSuiteScriptEngine.ENGINE_ID).createEngine();

		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		engine.setOutputStream(outputStream);

		fTestSuite = (ITestSuite) engine.executeSync(project.getFile("Testsuite.suite")).getResult();
		fTestOutput = outputStream.toString();
	}

	private static IProjectDescription getProjectDescription(File rootPath) throws CoreException {
		final Path projectPath = new Path(rootPath.getAbsoluteFile() + File.separator + ".project");
		return ResourcesPlugin.getWorkspace().loadProjectDescription(projectPath);
	}

	private static IProject importTestProject() throws URISyntaxException, IOException, CoreException, InvocationTargetException, InterruptedException {
		final Bundle bundle = Platform.getBundle("org.eclipse.ease.lang.unittest.test");
		final URL fileURL = bundle.getEntry("resources/UnitTest");
		final File sourceFolder = new File(FileLocator.resolve(fileURL).toURI());

		// copy files

		final IProjectDescription projectDescription = getProjectDescription(sourceFolder);
		final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectDescription.getName());
		project.create(new NullProgressMonitor());
		project.open(new NullProgressMonitor());

		final ImportOperation importOperation = new ImportOperation(project.getFullPath(), sourceFolder, FileSystemStructureProvider.INSTANCE,
				file -> IOverwriteQuery.ALL);
		importOperation.setCreateContainerStructure(false);
		importOperation.run(new NullProgressMonitor());

		return project;
	}

	private static ITestEntity getTestEntity(ITestContainer root, IPath path) {
		final String needle = path.segment(0);

		for (final ITestEntity child : root.getChildren()) {
			if (child.getName().equals(needle)) {
				if (path.segmentCount() == 1)
					return child;
				else if (child instanceof ITestContainer)
					return getTestEntity((ITestContainer) child, path.removeFirstSegments(1));
			}
		}

		return null;
	}

	private static ITestEntity getTestEntity(IPath path) {
		final ITestEntity entity = getTestEntity(fTestSuite, path);
		if (entity != null)
			return entity;

		throw new RuntimeException("Could not find test entity: " + path);
	}

	@Test
	public void suiteStatus() {
		assertEquals(TestStatus.ERROR, fTestSuite.getStatus());
	}

	@Test
	public void folderStatus() {
		ITestContainer test = (ITestContainer) getTestEntity(new Path("/workspace/UnitTest/tests/JavaScript/Classic"));

		while (test != null) {
			assertEquals(TestStatus.ERROR, fTestSuite.getStatus());
			test = test.getParent();
		}
	}

	@Test
	public void classicJSPassTestStatus() {
		final ITestEntity test = getTestEntity(new Path("/workspace/UnitTest/tests/JavaScript/Classic/Pass.js"));
		assertEquals(TestStatus.PASS, test.getStatus());
	}

	@Test
	public void classicJSIgnoreTestStatus() {
		final ITestEntity test = getTestEntity(new Path("/workspace/UnitTest/tests/JavaScript/Classic/Ignore.js"));
		assertEquals(TestStatus.DISABLED, test.getStatus());
	}

	@Test
	public void classicJSFailureTestStatus() {
		final ITestEntity test = getTestEntity(new Path("/workspace/UnitTest/tests/JavaScript/Classic/Failure.js"));
		assertEquals(TestStatus.FAILURE, test.getStatus());
	}

	@Test
	public void classicJSErrorTestStatus() {
		ITestEntity test = getTestEntity(new Path("/workspace/UnitTest/tests/JavaScript/Classic/Java Error.js"));
		assertEquals(TestStatus.ERROR, test.getStatus());

		test = getTestEntity(new Path("/workspace/UnitTest/tests/JavaScript/Classic/JavaScript Error.js"));
		assertEquals(TestStatus.ERROR, test.getStatus());
	}

	@Test
	public void verifyOutput() {
		assertTrue(fTestOutput.contains("one"));
		assertTrue(fTestOutput.contains("two"));
		assertTrue(fTestOutput.contains("three"));
		assertTrue(fTestOutput.contains("four"));
		assertTrue(fTestOutput.contains("five"));
		assertTrue(fTestOutput.contains("six"));
		assertTrue(fTestOutput.contains("seven"));
		assertTrue(fTestOutput.contains("eight"));

		assertFalse(fTestOutput.contains("Never to be reached"));
	}
}
