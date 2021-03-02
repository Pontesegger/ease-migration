/*******************************************************************************
 * Copyright (c) 2018 Bachmann electronic GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Bachmann electronic GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.modules.platform.build;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test Class for the {@link BuildModule}
 */
public class BuildModuleTest {

	private BuildModule fModule;
	private IWorkspaceRoot fRoot;
	private IProject fProject;

	@BeforeEach
	public void setup() {
		fProject = mock(IProject.class);
		fRoot = mock(IWorkspaceRoot.class);
		fModule = new BuildModule(fRoot);
		when(fRoot.getProject("MyProject")).thenReturn(fProject);
	}

	@Test
	public void buildDoesCallProjectsBuildMethodIfProjectExists() throws CoreException {
		when(fProject.isAccessible()).thenReturn(true);
		fModule.build("MyProject", BuildModule.CLEAN_BUILD);
		verify(fProject, times(1)).build(eq(IncrementalProjectBuilder.CLEAN_BUILD), any(NullProgressMonitor.class));
	}

	@Test
	public void buildDoesNotCallProjectsBuildMethodIfProjectNotExists() throws CoreException {
		when(fProject.isAccessible()).thenReturn(false);
		fModule.build("MyProject", BuildModule.CLEAN_BUILD);
		verify(fProject, never()).build(eq(IncrementalProjectBuilder.CLEAN_BUILD), any(NullProgressMonitor.class));
	}

}
