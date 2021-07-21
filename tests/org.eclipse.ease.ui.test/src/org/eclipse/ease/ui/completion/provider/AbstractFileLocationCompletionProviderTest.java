/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
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

package org.eclipse.ease.ui.completion.provider;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.ui.completion.BasicContext;
import org.eclipse.ease.ui.completion.ScriptCompletionProposal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AbstractFileLocationCompletionProviderTest {

	private AbstractFileLocationCompletionProvider fProvider;

	// private static final String TEST_FILE = "test.file";
	private static final String PROJECT_NAME = "Sample project";
	private static final String FOLDER_NAME = "Subfolder";
	private static final String FILE1_NAME = "test file.txt";
	private static final String FILE2_NAME = "test file b.txt";
	private static final String NEW_FILE_NAME = "another_file.txt";

	private static final String FILE1_CONTENT = "Hello world";

	private static final String DOES_NOT_EXIST = "does_not_exist";

	private IWorkspaceRoot fWorkspace;
	private IProject fProject;
	private IFolder fFolder;
	private IFile fFile1;
	private IFile fFile2;

	private File fFsWorkspace;
	private File fFsProject;
	private File fFsFolder;
	private File fFsFile1;
	private File fFsFile2;

	public static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().startsWith("windows");
	}

	public static boolean isLinux() {
		return System.getProperty("os.name").toLowerCase().startsWith("linux");
	}

	@BeforeEach
	public void setUp() throws Exception {

		// create workspace sample project
		fWorkspace = ResourcesPlugin.getWorkspace().getRoot();

		fProject = fWorkspace.getProject(PROJECT_NAME);
		if (!fProject.exists())
			fProject.create(null);

		if (!fProject.isOpen())
			fProject.open(null);

		fFolder = fProject.getFolder(FOLDER_NAME);
		if (!fFolder.exists())
			fFolder.create(0, true, null);

		fFile1 = fProject.getFile(FILE1_NAME);
		if (!fFile1.exists())
			fFile1.create(new ByteArrayInputStream(FILE1_CONTENT.getBytes("UTF-8")), false, null);

		fFile2 = fFolder.getFile(FILE2_NAME);
		if (!fFile2.exists())
			fFile2.create(new ByteArrayInputStream("Hello eclipse".getBytes("UTF-8")), false, null);

		fFsFile1 = fFile1.getLocation().toFile();
		fFsFile2 = fFile2.getLocation().toFile();
		fFsFolder = fFsFile1.getParentFile();
		fFsProject = fFsFolder.getParentFile();
		fFsWorkspace = fWorkspace.getRawLocation().toFile();

		fProvider = new AbstractFileLocationCompletionProvider() {
		};
	}

	@AfterAll
	public static void tearDown() throws CoreException {
		final IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
		if (workspace != null) {
			final IProject project = workspace.getProject(PROJECT_NAME);
			if (project != null)
				project.delete(true, true, null);
		}
	}

	@Test
	@DisplayName("getProposals('') contains 'workspace://'")
	public void getProposals_contains_workspace_root() {
		final Collection<ScriptCompletionProposal> proposals = fProvider.getProposals(createContext(""));
		assertNotNull(findProposal(proposals, "workspace://"));
	}

	@Test
	@DisplayName("getProposals('') contains 'file://'")
	public void getProposals_contains_filesystem_root() {
		final Collection<ScriptCompletionProposal> proposals = fProvider.getProposals(createContext(""));
		assertNotNull(findProposal(proposals, "file://"));
	}

	@Test
	@DisplayName("getProposals('') does not contain 'project://' without resource")
	public void getProposals_does_not_contain_project_root_without_resource() {
		final Collection<ScriptCompletionProposal> proposals = fProvider.getProposals(createContext(""));
		assertNull(findProposal(proposals, "project://"));
	}

	@Test
	@DisplayName("getProposals('') does not contain 'project://' with file resource")
	public void getProposals_does_not_contain_project_root_with_file_resource() {
		final Collection<ScriptCompletionProposal> proposals = fProvider.getProposals(createContext("", fFsFile1));
		assertNull(findProposal(proposals, "project://"));
	}

	@Test
	@DisplayName("getProposals('') contains 'project://' with workspace resource")
	public void getProposals_contains_project_root_with_workspace_resource() {
		final Collection<ScriptCompletionProposal> proposals = fProvider.getProposals(createContext("", fFile1));
		assertNotNull(findProposal(proposals, "project://"));
	}

	@Test
	@DisplayName("getProposals('workspace://') contains projects")
	public void getProposals_contains_projects_for_workspace_root() {
		final Collection<ScriptCompletionProposal> proposals = fProvider.getProposals(createContext("workspace://"));
		assertNotNull(findProposal(proposals, fProject.getName()));
	}

	@Test
	@DisplayName("getProposals('workspace://') does not contain contains '..'")
	public void getProposals_doe_not_contain_back_proposal() {
		final Collection<ScriptCompletionProposal> proposals = fProvider.getProposals(createContext("workspace://"));
		assertNull(findProposal(proposals, ".."));
	}

	@Test
	@DisplayName("getProposals('workspace://<project>') contains project root folders")
	public void getProposals_contains_project_root_folders_for_workspace_project() {
		final Collection<ScriptCompletionProposal> proposals = fProvider.getProposals(createContext("workspace://" + fProject.getName() + "/"));
		assertNotNull(findProposal(proposals, fFolder.getName()));
	}

	@Test
	@DisplayName("getProposals('workspace://<project>') contains project root files")
	public void getProposals_contains_project_root_files_for_workspace_project() {
		final Collection<ScriptCompletionProposal> proposals = fProvider.getProposals(createContext("workspace://" + fProject.getName() + "/"));
		assertNotNull(findProposal(proposals, fFile1.getName()));
	}

	@Test
	@DisplayName("getProposals('workspace://<project>/<folder>') contains folder files")
	public void getProposals_contains_files_for_workspace_folder() {
		final Collection<ScriptCompletionProposal> proposals = fProvider
				.getProposals(createContext("workspace://" + fProject.getName() + "/" + fFolder.getName() + "/"));
		assertNotNull(findProposal(proposals, fFile2.getName()));
	}

	@Test
	@DisplayName("getProposals('workspace://<project>/<folder>') contains '..' proposal")
	public void getProposals_contains_back_for_workspace_folder() {
		final Collection<ScriptCompletionProposal> proposals = fProvider
				.getProposals(createContext("workspace://" + fProject.getName() + "/" + fFolder.getName() + "/"));
		assertNotNull(findProposal(proposals, ".."));
	}

	@DisplayName("getProposals('project://') contains project root folders")
	public void getProposals_contains_project_root_folders_for_project_root() {
		final Collection<ScriptCompletionProposal> proposals = fProvider.getProposals(createContext("project://", fFile2));
		assertNotNull(findProposal(proposals, fFolder.getName()));
	}

	@Test
	@DisplayName("getProposals('project://') contains project root files")
	public void getProposals_contains_project_root_files_for_project_root() {
		final Collection<ScriptCompletionProposal> proposals = fProvider.getProposals(createContext("project://", fFile2));
		assertNotNull(findProposal(proposals, fFile1.getName()));
	}

	@Test
	@DisplayName("getProposals('project://<folder>') contains folder files")
	public void getProposals_contains_files_for_project_folder() {
		final Collection<ScriptCompletionProposal> proposals = fProvider.getProposals(createContext("project://" + fFolder.getName() + "/", fFile2));
		assertNotNull(findProposal(proposals, fFile2.getName()));
	}

	@Test
	@DisplayName("getProposals('project://<folder>') contains '..' proposal")
	public void getProposals_contains_back_for_project_folder() {
		final Collection<ScriptCompletionProposal> proposals = fProvider.getProposals(createContext("project://" + fFolder.getName() + "/", fFile2));
		assertNotNull(findProposal(proposals, ".."));
	}

	@Test
	@DisplayName("getProposals('file://') contains root file systems")
	public void getProposals_contains_root_file_systems() {
		final Collection<ScriptCompletionProposal> proposals = fProvider.getProposals(createContext("file://"));
		for (final File root : File.listRoots())
			assertNotNull(findProposal(proposals, root.getName()), String.format("Root entry not found for %s", root.getName()));
	}

	@Test
	@DisplayName("getProposals('') contains files for project")
	public void getProposals_contains_files_for_project() {
		final Collection<ScriptCompletionProposal> proposals = fProvider.getProposals(createContext("", fProject));
		assertNotNull(findProposal(proposals, fFile1.getName()));
	}

	@Test
	@DisplayName("getProposals('') contains '..' for project")
	public void getProposals_contains_back_for_project() {
		final Collection<ScriptCompletionProposal> proposals = fProvider.getProposals(createContext("", fProject));
		assertNotNull(findProposal(proposals, ".."));
	}

	@Test
	@DisplayName("getProposals('') contains files for filesystem")
	public void getProposals_contains_files_for_filesystem() {
		final Collection<ScriptCompletionProposal> proposals = fProvider.getProposals(createContext("", fFsFile1));
		assertNotNull(findProposal(proposals, fFsFile1.getName()));
	}

	@Test
	@DisplayName("getProposals('') contains '..' for filesystem")
	public void getProposals_contains_back_for_filesystem() {
		final Collection<ScriptCompletionProposal> proposals = fProvider.getProposals(createContext("", fFsFile1));
		assertNotNull(findProposal(proposals, ".."));
	}

	private ScriptCompletionProposal findProposal(Collection<ScriptCompletionProposal> proposals, String displayString) {
		return proposals.stream().filter(p -> p.getDisplayString().startsWith(displayString)).findFirst().orElseGet(() -> null);
	}

	private ICompletionContext createContext(String input) {
		return createContext(input, null);
	}

	private ICompletionContext createContext(String input, Object resource) {
		return new BasicContext(null, resource, "\"" + input, input.length() + 1);
	}
}
