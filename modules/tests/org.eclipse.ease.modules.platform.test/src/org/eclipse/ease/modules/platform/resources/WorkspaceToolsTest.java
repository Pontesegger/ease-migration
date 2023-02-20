/*******************************************************************************
 * Copyright (c) 2021 Christian Pontesegger and others.
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

package org.eclipse.ease.modules.platform.resources;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ease.tools.ResourceTools;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WorkspaceToolsTest {

	private static final String PROJECT_NAME = "Test Project";
	private static final String PROJECT_LOCATION = "workspace://" + PROJECT_NAME;
	private static final String EXISTING_FILE_LOCATION = PROJECT_LOCATION + "/Test file.txt";
	private static final String EXISTING_FOLDER_LOCATION = PROJECT_LOCATION + "/existingFolder";

	@BeforeEach
	public void beforeEach() throws CoreException {
		ResourceTestHelper.createTestProject(PROJECT_NAME);
	}

	@Test
	@DisplayName("createFolder() creates new folder")
	public void createFolder_creates_new_folder() throws IOException {
		final IResource folder = (IResource) ResourceTools.resolve(PROJECT_LOCATION + "/testFolder", ".");
		final IContainer createdFolder = WorkspaceTools.createFolder(folder);

		assertNotNull(createdFolder);
		assertTrue(createdFolder.exists());
	}

	@Test
	@DisplayName("createFolder() creates new folders")
	public void createFolder_creates_new_folders() throws IOException {
		final IResource folder = (IResource) ResourceTools.resolve(PROJECT_LOCATION + "/testFolder/1/2", ".");
		final IContainer createdFolder = WorkspaceTools.createFolder(folder);

		assertNotNull(createdFolder);
		assertTrue(createdFolder.exists());
	}

	@Test
	@DisplayName("createFolder() returns existing folder")
	public void createFolder_returns_existing_folder() throws IOException {
		final IResource folder = (IResource) ResourceTools.resolve(PROJECT_LOCATION + "/testFolder", ".");
		IContainer createdFolder = WorkspaceTools.createFolder(folder);
		assertNotNull(createdFolder);

		createdFolder = WorkspaceTools.createFolder(folder);

		assertNotNull(createdFolder);
		assertTrue(createdFolder.exists());
	}

	@Test
	@DisplayName("createFolder() throws when file with same name exists")
	public void createFolder_throws_when_file_with_same_name_exists() throws IOException {
		final IResource file = (IResource) ResourceTools.resolve(EXISTING_FILE_LOCATION, ".");
		assertThrows(IOException.class, () -> WorkspaceTools.createFolder(file));
	}

	@Test
	@DisplayName("createFile() creates new, empty file")
	public void createFile_creates_new_empty_file() throws IOException, CoreException {
		final IFile file = (IFile) ResourceTools.resolve(PROJECT_LOCATION + "/newFile.txt", ".");
		final IFile createdFile = WorkspaceTools.createFile(file);

		assertTrue(createdFile.exists());
		assertEquals(0, createdFile.getContents().available());
	}

	@Test
	@DisplayName("createFile() creates new file in new folder")
	public void createFile_creates_new_file_in_new_folder() throws IOException, CoreException {
		final IFile file = (IFile) ResourceTools.resolve(PROJECT_LOCATION + "/newFolder/newFile.txt", ".");
		final IFile createdFile = WorkspaceTools.createFile(file);

		assertTrue(createdFile.exists());
		assertEquals(0, createdFile.getContents().available());
	}

	@Test
	@DisplayName("createFile() throws when file already exists")
	public void createFolder_throws_when_file_already_exists() throws IOException {
		final IFile file = (IFile) ResourceTools.resolve(EXISTING_FILE_LOCATION, ".");
		assertThrows(IOException.class, () -> WorkspaceTools.createFile(file));
	}

	@Test
	@DisplayName("deleteFile() deletes existing file")
	public void deleteFile_deletes_existing_file() throws IOException {
		final IFile file = (IFile) ResourceTools.resolve(EXISTING_FILE_LOCATION, ".");
		WorkspaceTools.deleteFile(file);

		assertFalse(file.exists());
	}

	@Test
	@DisplayName("deleteFile() does not throw when file does not exist")
	public void deleteFile_does_not_throw_when_file_does_not_exist() throws IOException {
		final IFile file = (IFile) ResourceTools.resolve(EXISTING_FILE_LOCATION + ".notThere", ".");

		assertDoesNotThrow(() -> WorkspaceTools.deleteFile(file));
	}

	@Test
	@DisplayName("deleteFile() throws when resource is a folder")
	public void deleteFile_throws_when_resource_is_a_WS_folder() {
		final IResource resource = (IResource) ResourceTools.resolve(EXISTING_FOLDER_LOCATION, ".");

		assertThrows(IOException.class, () -> WorkspaceTools.deleteFile(resource));
	}

	@Test
	@DisplayName("deleteFolder() deletes empty folder")
	public void deleteFolder_deletes_WS_folder() throws IOException {
		final IFolder folder = (IFolder) ResourceTools.resolve(EXISTING_FOLDER_LOCATION, ".");
		WorkspaceTools.deleteFolder(folder);

		assertFalse(folder.exists());
	}

	@Test
	@DisplayName("deleteFolder() deletes folder recursively")
	public void deleteFolder_deletes_WS_folder_recursively() throws IOException {
		final IFolder folder = (IFolder) ResourceTools.resolve(EXISTING_FOLDER_LOCATION, ".");
		WorkspaceTools.createFile(folder.getFolder("subFolder").getFile("File.txt"));

		WorkspaceTools.deleteFolder(folder);

		assertFalse(folder.exists());
	}

	@Test
	@DisplayName("deleteFolder() does not throw when folder does not exist")
	public void deleteFolder_does_not_throw_when_folder_does_not_exist() throws IOException {
		final IResource folder = (IResource) ResourceTools.resolve(EXISTING_FOLDER_LOCATION + ".notThere", ".");
		assertDoesNotThrow(() -> WorkspaceTools.deleteFolder(folder));
	}

	@Test
	@DisplayName("deleteFolder() throws when resource is a WS file")
	public void deleteFolder_throws_when_resource_is_a_WS_file() {
		final IResource resource = (IResource) ResourceTools.resolve(EXISTING_FILE_LOCATION, ".");

		assertThrows(IOException.class, () -> WorkspaceTools.deleteFolder(resource));
	}

	@Test
	@DisplayName("getWorkspace() returns workspace root")
	public void getWorkspace_returns_workspace_root() {
		assertEquals(ResourcesPlugin.getWorkspace().getRoot(), WorkspaceTools.getWorkspace());
	}

	@Test
	@DisplayName("findFiles() returns empty array for no matches")
	public void findFiles_returns_empty_array_for_no_matches() throws IOException {
		assertArrayEquals(new Object[0], WorkspaceTools.findFiles(Pattern.compile("notThere\\.noFile"), ResourcesPlugin.getWorkspace().getRoot(), true));
	}

	@DisplayName("findFiles() ignores folders")
	public void findFiles_ignores_folders() throws IOException {
		assertArrayEquals(new String[0], WorkspaceTools.findFiles(Pattern.compile("existingFolder"), ResourcesPlugin.getWorkspace().getRoot(), true));
	}

	@Test
	@DisplayName("findFiles() detects project file")
	public void findFiles_detects_project_file() throws IOException {
		assertArrayEquals(new String[] { EXISTING_FILE_LOCATION },
				WorkspaceTools.findFiles(Pattern.compile("Test file\\.txt"), ResourcesPlugin.getWorkspace().getRoot(), true));
	}

	@Test
	@DisplayName("findFiles() detects file with wildcards")
	public void findFiles_detects_file_with_wildcard() throws IOException {
		assertArrayEquals(new String[] { EXISTING_FILE_LOCATION },
				WorkspaceTools.findFiles(Pattern.compile("T.st .*l.\\..*"), ResourcesPlugin.getWorkspace().getRoot(), true));
	}

	@Test
	@DisplayName("findFiles() detects multiple files")
	public void findFiles_detects_multiple_files() throws IOException, CoreException {
		final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);

		IFile file = project.getFile("Test file 2.txt");
		file.create(new ByteArrayInputStream("Hello world!".getBytes()), false, null);

		file = project.getFile("Another file.txt");
		file.create(new ByteArrayInputStream("Hello world!".getBytes()), false, null);

		file = project.getFolder("existingFolder").getFile("Test file 3.txt");
		file.create(new ByteArrayInputStream("Hello world!".getBytes()), false, null);

		final List<String> foundFiles = Arrays.asList(WorkspaceTools.findFiles(Pattern.compile("Test.*"), ResourcesPlugin.getWorkspace().getRoot(), true));

		assertEquals(3, foundFiles.size());
		assertTrue(foundFiles.contains(EXISTING_FILE_LOCATION));
		assertTrue(foundFiles.contains(EXISTING_FILE_LOCATION.replace("Test file.txt", "Test file 2.txt")));
		assertTrue(foundFiles.contains(EXISTING_FOLDER_LOCATION + "/Test file 3.txt"));
	}

	@Test
	@DisplayName("findFiles() detects files in current folder only")
	public void findFiles_detects_files_in_current_folder_only() throws IOException, CoreException {
		final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);

		IFile file = project.getFile("Test file 2.txt");
		file.create(new ByteArrayInputStream("Hello world!".getBytes()), false, null);

		file = project.getFile("Another file.txt");
		file.create(new ByteArrayInputStream("Hello world!".getBytes()), false, null);

		file = project.getFolder("existingFolder").getFile("Test file 3.txt");
		file.create(new ByteArrayInputStream("Hello world!".getBytes()), false, null);

		final List<String> foundFiles = Arrays.asList(WorkspaceTools.findFiles(Pattern.compile("Test.*"), project, false));

		assertEquals(2, foundFiles.size());
		assertTrue(foundFiles.contains(EXISTING_FILE_LOCATION));
		assertTrue(foundFiles.contains(EXISTING_FILE_LOCATION.replace("Test file.txt", "Test file 2.txt")));
	}

	@Test
	@DisplayName("linkProject() links existing project folder into workspace")
	public void linkProject_links_existing_project_into_workspace() throws IOException, URISyntaxException, CoreException {
		final IProject project = WorkspaceTools.linkProject(ResourcesModuleTest.getResource("/resources/externalProject"));

		assertEquals("External Project", project.getName());

		project.delete(false, true, new NullProgressMonitor());
	}

	@Test
	@DisplayName("linkProject() throws when .project file is missing")
	public void linkProject_throws_when_project_file_is_missing() throws IOException, URISyntaxException {
		assertThrows(IOException.class, () -> WorkspaceTools.linkProject(ResourcesModuleTest.getResource("/resources/externalProject").getParentFile()));
	}

	@Test
	@DisplayName("linkProject() throws when target is a file")
	public void linkProject_throws_when_target_is_a_file() throws IOException, URISyntaxException {
		assertThrows(IOException.class, () -> WorkspaceTools.linkProject(ResourcesModuleTest.getResource("/resources/externalProject/.project")));
	}

	@Test
	@DisplayName("importProject() imports existing project folder into workspace")
	public void importProject_imports_existing_project_into_workspace() throws IOException, URISyntaxException, CoreException {
		final IProject project = WorkspaceTools.importProject(ResourcesModuleTest.getResource("/resources/externalProject"));

		assertEquals("External Project", project.getName());

		project.delete(true, new NullProgressMonitor());
	}

	@Test
	@DisplayName("importProject() throws when .project file is missing")
	public void importProject_throws_when_project_file_is_missing() throws IOException, URISyntaxException {
		assertThrows(IOException.class, () -> WorkspaceTools.importProject(ResourcesModuleTest.getResource("/resources/externalProject").getParentFile()));
	}

	@Test
	@DisplayName("importProject() throws when target is a file")
	public void importProject_throws_when_target_is_a_file() throws IOException, URISyntaxException {
		assertThrows(IOException.class, () -> WorkspaceTools.importProject(ResourcesModuleTest.getResource("/resources/externalProject/.project")));
	}
}
