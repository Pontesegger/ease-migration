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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ease.ScriptResult;
import org.eclipse.ease.lang.unittest.runtime.ITestContainer;
import org.eclipse.ease.lang.unittest.runtime.ITestEntity;
import org.eclipse.ease.lang.unittest.runtime.ITestSuite;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;
import org.junit.jupiter.api.BeforeAll;
import org.osgi.framework.Bundle;

public class TestBase {

	public static IProject TEST_PROJECT;

	@BeforeAll
	public static void importTestProject() throws URISyntaxException, IOException, CoreException, InvocationTargetException, InterruptedException {
		final Bundle bundle = Platform.getBundle("org.eclipse.ease.lang.unittest.test");
		final URL fileURL = bundle.getEntry("resources/UnitTest");
		final File sourceFolder = new File(FileLocator.resolve(fileURL).toURI());

		// create project that links to /this/resources/UnitTest
		final Path projectPath = new Path(sourceFolder.getAbsoluteFile() + File.separator + ".project");
		final IProjectDescription projectDescription = ResourcesPlugin.getWorkspace().loadProjectDescription(projectPath);
		projectDescription.setLocation(new Path(sourceFolder.getAbsolutePath()));

		TEST_PROJECT = ResourcesPlugin.getWorkspace().getRoot().getProject(projectDescription.getName());
		if (!TEST_PROJECT.exists()) {
			TEST_PROJECT.create(projectDescription, new NullProgressMonitor());
			TEST_PROJECT.open(new NullProgressMonitor());
		}
	}

	public static ITestEntity getTestEntity(ITestContainer root, IPath path) {
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

	private String fTestOutput;
	private String fErrorOutput;

	public ITestSuite runSuite(IFile suiteFile) throws ExecutionException {
		final IScriptService scriptService = ScriptService.getService();
		final TestSuiteScriptEngine engine = (TestSuiteScriptEngine) scriptService.getEngineByID(TestSuiteScriptEngine.ENGINE_ID).createEngine();

		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		engine.setOutputStream(outputStream);
		final ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
		engine.setErrorStream(errorStream);

		final ScriptResult result = engine.execute(suiteFile);
		engine.schedule();

		final ITestSuite testSuite = (ITestSuite) result.get();
		setTestOutput(outputStream.toString());
		setTestErrorOutput(errorStream.toString());

		return testSuite;
	}

	private void setTestErrorOutput(String errorOutput) {
		fErrorOutput = errorOutput;
	}

	private void setTestOutput(String output) {
		fTestOutput = output;
	}

	public String getTestOutput() {
		return fTestOutput;
	}
}
