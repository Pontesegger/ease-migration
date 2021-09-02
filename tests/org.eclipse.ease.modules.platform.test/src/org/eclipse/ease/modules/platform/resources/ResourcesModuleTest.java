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

package org.eclipse.ease.modules.platform.resources;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ease.IExecutionListener;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.modules.platform.AbstractModuleTest;
import org.eclipse.ease.tools.ResourceTools;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;

public class ResourcesModuleTest extends AbstractModuleTest {

	private static final String PROJECT_NAME = "Test Project";
	private static final String PROJECT_LOCATION = "workspace://" + PROJECT_NAME;
	private static final String EXISTING_FILE_LOCATION = PROJECT_LOCATION + "/Test file.txt";
	private static final String EXISTING_FOLDER_LOCATION = PROJECT_LOCATION + "/existingFolder";

	private static final String TEMP_PREFIX = "ease_unittest_";

	public static File getResource(String path) throws URISyntaxException, IOException {
		final Bundle bundle = Platform.getBundle("org.eclipse.ease.modules.platform.test");
		final URL entry = bundle.getEntry(path);

		return new File(FileLocator.resolve(entry).toURI()).getAbsoluteFile();
	}

	private IProject fTestProject;

	private File fExistingFileSystemFile;
	private File fExistingFileSystemFolder;

	private ResourcesModule fModule;

	@BeforeEach
	public void beforeEach() throws URISyntaxException, IOException, CoreException, InvocationTargetException, InterruptedException {
		fTestProject = ResourceTestHelper.createTestProject(PROJECT_NAME);
	}

	@BeforeEach
	public void createTempFile() throws IOException {

		ResourceTestHelper.cleanupTempFiles(TEMP_PREFIX);
		fExistingFileSystemFile = ResourceTestHelper.createTempFile(TEMP_PREFIX);
		fExistingFileSystemFolder = ResourceTestHelper.createTempFolder(TEMP_PREFIX);

		// mocked script engine
		final IScriptEngine mockEngine = mock(IScriptEngine.class);
		when(mockEngine.getExecutedFile()).thenReturn(fExistingFileSystemFile);

		// initialize module
		fModule = new ResourcesModule();
		fModule.initialize(mockEngine, null);
	}

	@AfterEach
	public void afterEach() throws IOException {
		ResourceTestHelper.cleanupTempFiles(TEMP_PREFIX);

		fModule.notify(null, null, IExecutionListener.ENGINE_END);
	}

	@Override
	protected Object getModuleClass() {
		return ResourcesModule.class;
	}

	@Override
	protected Object getModuleID() {
		return ResourcesModule.MODULE_ID;
	}

	@Test
	@DisplayName("getWorkspace() returns workspace root")
	public void getWorkspace_returns_workspace_root() {
		assertNotNull(ResourcesModule.getWorkspace());
	}

	@Test
	@DisplayName("getProject() returns non-existing project")
	public void getProject_returns_non_existing_project() {
		final IProject project = ResourcesModule.getProject("notExisting");

		assertNotNull(project);
		assertFalse(project.exists());
	}

	@Test
	@DisplayName("getProject() returns existing project")
	public void getProject_returns_existing_project() {
		final IProject project = ResourcesModule.getProject(PROJECT_NAME);

		assertNotNull(project);
		assertTrue(project.exists());
	}

	@Test
	@DisplayName("getFile() returns existing FS file")
	public void getFile_returns_existing_FS_file() throws IOException {
		assertEquals(File.class, fModule.getFile(fExistingFileSystemFile.toString(), true).getClass());
		assertEquals(File.class, fModule.getFile(fExistingFileSystemFile.toString(), false).getClass());
	}

	@Test
	@DisplayName("getFile() returns non-existing FS file")
	public void getFile_returns_non_existing_FS_file() throws IOException {
		assertEquals(File.class, fModule.getFile(fExistingFileSystemFile.toString() + ".notThere", false).getClass());
	}

	@Test
	@DisplayName("getFile() throws for non-existing FS file")
	public void getFile_throws_for_null_for_non_existing_FS_file() {
		assertThrows(FileNotFoundException.class, () -> fModule.getFile(fExistingFileSystemFile.toString() + ".notThere", true));
	}

	@Test
	@DisplayName("getFile() returns existing WS file")
	public void getFile_retuns_existing_WS_file() throws IOException {
		assertTrue(fModule.getFile(EXISTING_FILE_LOCATION, true) instanceof IFile);
		assertTrue(fModule.getFile(EXISTING_FILE_LOCATION, false) instanceof IFile);
	}

	@Test
	@DisplayName("getFile() returns non-existing WS file")
	public void getFile_returns_non_existing_WS_file() throws IOException {
		assertTrue(fModule.getFile(EXISTING_FILE_LOCATION + ".notThere", false) instanceof IFile);
	}

	@Test
	@DisplayName("getFile() throws for non-existing WS file")
	public void getFile_throws_for_non_existing_WS_file() {
		assertThrows(FileNotFoundException.class, () -> fModule.getFile(EXISTING_FILE_LOCATION + ".notThere", true));
	}

	@Test
	@DisplayName("createProject() creates and opens new project")
	public void createProject_creates_and_opens_new_project() throws IOException, CoreException {
		final IProject project = ResourcesModule.getProject("dynamic project");
		if (project.exists())
			project.delete(true, new NullProgressMonitor());

		assertFalse(project.exists());

		assertTrue(ResourcesModule.createProject("dynamic project").isAccessible());
	}

	@Test
	@DisplayName("createProject() returns existing project")
	public void createProject_returns_existing_project() throws IOException {
		assertEquals(fTestProject, ResourcesModule.createProject(PROJECT_NAME));
	}

	@Test
	@DisplayName("createFolder() creates new folder on FS")
	public void createFolder_creates_new_folder_on_FS() throws IOException {
		final File folder = new File(fExistingFileSystemFile.getParent() + File.separator + TEMP_PREFIX + "createFolder");
		final Object createdFolder = fModule.createFolder(folder);

		assertEquals(folder, createdFolder);
		assertTrue(((File) createdFolder).exists());
		assertTrue(((File) createdFolder).isDirectory());
	}

	@Test
	@DisplayName("createFolder() creates new folder in WS")
	public void createFolder_creates_new_folder_in_WS() throws IOException {
		final Object createdFolder = fModule.createFolder(PROJECT_LOCATION + "/createFolder");

		assertTrue(createdFolder instanceof IContainer);
		assertTrue(((IContainer) createdFolder).exists());
	}

	@Test
	@DisplayName("createFolder() throws on invalid location")
	public void createFolder_throws_on_invalid_location() throws IOException {
		assertThrows(IllegalArgumentException.class, () -> fModule.createFolder("invalidUrl://Test Project/newFile.txt"));
	}

	@Test
	@DisplayName("createFile() creates new file on FS")
	public void createFile_creates_new_file_on_FS() throws IOException {
		final File file = new File(fExistingFileSystemFile.getParent() + File.separator + TEMP_PREFIX + "createFile.txt");
		final File createdFile = (File) fModule.createFile(file);

		assertEquals(file, createdFile);
		assertTrue(createdFile.isFile());
	}

	@Test
	@DisplayName("createFile() creates new file in WS")
	public void createFile_creates_new_file_in_WS() throws IOException {
		final IFile createdFile = (IFile) fModule.createFile(PROJECT_LOCATION + "/createFile.txt");

		assertTrue(createdFile.exists());
	}

	@Test
	@DisplayName("createFile() throws on invalid location")
	public void createFile_throws_on_invalid_location() throws IOException {
		assertThrows(IllegalArgumentException.class, () -> fModule.createFile("invalidUrl://Test Project/newFile.txt"));
	}

	@Test
	@DisplayName("openFile() returns WS handle for existing file")
	public void openFile_returns_WS_file_Handle() throws IOException {
		final IFileHandle handle = fModule.openFile(EXISTING_FILE_LOCATION, ResourcesModule.READ, true);
		assertNotNull(handle);
		assertTrue(handle instanceof ResourceHandle);

		handle.close();
	}

	@Test
	@DisplayName("openFile() returns existing handle for WS")
	public void openFile_returns_existing_handle_for_WS() throws IOException {
		final IFileHandle handle = fModule.openFile(EXISTING_FILE_LOCATION, ResourcesModule.READ, true);

		assertEquals(handle, fModule.openFile(EXISTING_FILE_LOCATION, ResourcesModule.READ, true));
	}

	@Test
	@DisplayName("openFile() returns FS handle for existing file")
	public void openFile_returns_FS_file_Handle() throws IOException {
		final IFileHandle handle = fModule.openFile(fExistingFileSystemFile, ResourcesModule.READ, true);
		assertNotNull(handle);
		assertTrue(handle instanceof FilesystemHandle);

		handle.close();
	}

	@Test
	@DisplayName("openFile() returns existing handle for FS")
	public void openFile_returns_existing_handle_for_FS() throws IOException {
		final IFileHandle handle = fModule.openFile(fExistingFileSystemFile.toString(), ResourcesModule.READ, true);

		assertEquals(handle, fModule.openFile(fExistingFileSystemFile.toString(), ResourcesModule.READ, true));
	}

	@Test
	@DisplayName("openFile() throws for non-existing WS file in READ mode")
	public void openFile_throws_for_non_existing_WS_file_in_read_mode() throws IOException {
		assertThrows(FileNotFoundException.class, () -> fModule.openFile(EXISTING_FILE_LOCATION + ".notThere", ResourcesModule.READ, true));
	}

	@Test
	@DisplayName("openFile() throws for non-existing FS file in READ mode")
	public void openFile_throws_for_non_existing_FS_file_in_read_mode() throws IOException {
		final String notExistingFile = fExistingFileSystemFile.toString() + ".notThere";
		assertThrows(FileNotFoundException.class, () -> fModule.openFile(notExistingFile, ResourcesModule.READ, true));
	}

	@Test
	@DisplayName("openFile() creates non-existing WS file in WRITE mode")
	public void openFile_creates_non_existing_WS_file_in_write_mode() throws IOException {
		final String location = EXISTING_FILE_LOCATION + ".openFileInWriteMode";
		final IFileHandle handle = fModule.openFile(location, ResourcesModule.WRITE, true);

		assertNotNull(handle);
		assertTrue(handle instanceof ResourceHandle);
		handle.close();

		final Object resolvedFile = ResourceTools.resolve(location);
		assertTrue(resolvedFile instanceof IFile);
		assertTrue(((IFile) resolvedFile).exists());
	}

	@Test
	@DisplayName("openFile() creates non-existing FS file in WRITE mode")
	public void openFile_creates_non_existing_FS_file_in_write_mode() throws IOException {
		final String notExistingFile = fExistingFileSystemFile.getParent() + File.separator + "openFile.inWriteMode";
		final IFileHandle handle = fModule.openFile(notExistingFile, ResourcesModule.WRITE, true);

		assertNotNull(handle);
		assertTrue(handle instanceof FilesystemHandle);
		handle.close();

		final Object resolvedFile = ResourceTools.resolve(notExistingFile);
		assertTrue(resolvedFile instanceof File);
		assertTrue(((File) resolvedFile).exists());

		((File) resolvedFile).deleteOnExit();
	}

	@Test
	@DisplayName("fileExists() = true for existing WS file")
	public void fileExists_equals_true_for_existing_WS_file() {
		assertTrue(fModule.fileExists(EXISTING_FILE_LOCATION));
	}

	@Test
	@DisplayName("fileExists() = false for non-existing WS file")
	public void fileExists_equals_false_for_non_existing_WS_file() {
		assertFalse(fModule.fileExists(EXISTING_FILE_LOCATION + ".notThere"));
	}

	@Test
	@DisplayName("fileExists() = false for existing WS folder")
	public void fileExists_equals_false_for_existing_WS_folder() {
		assertFalse(fModule.fileExists(EXISTING_FOLDER_LOCATION));
	}

	@Test
	@DisplayName("fileExists() = true for existing FS file")
	public void fileExists_equals_true_for_existing_FS_file() {
		assertTrue(fModule.fileExists(fExistingFileSystemFile.toString()));
	}

	@Test
	@DisplayName("fileExists() = false for non-existing FS file")
	public void fileExists_equals_false_for_non_existing_FS_file() {
		assertFalse(fModule.fileExists(fExistingFileSystemFile.toString() + ".notThere"));
	}

	@Test
	@DisplayName("fileExists() = false for existing FS folder")
	public void fileExists_equals_false_for_existing_FS_folder() {
		assertFalse(fModule.fileExists(fExistingFileSystemFile.getParent()));
	}

	@Test
	@DisplayName("closeFile() closes existing file handle")
	public void closeFile_closes_existing_file_handle() throws IOException {
		final IFileHandle firstHandle = fModule.openFile(EXISTING_FILE_LOCATION, ResourcesModule.READ, false);

		fModule.closeFile(firstHandle);

		assertNotEquals(firstHandle, fModule.openFile(EXISTING_FILE_LOCATION, ResourcesModule.READ, false));
	}

	@Test
	@DisplayName("readFile() reads whole file content")
	public void readFile_reads_whole_file_content() throws IOException {
		assertEquals("Hello world!", fModule.readFile(EXISTING_FILE_LOCATION, -1));
	}

	@Test
	@DisplayName("readFile() reads bytes")
	public void readFile_reads_bytes() throws IOException {
		assertEquals("Hello", fModule.readFile(EXISTING_FILE_LOCATION, 5));
	}

	@Test
	@DisplayName("readFile() restarts reading bytes for location")
	public void readFile_restarts_reading_bytes_for_location() throws IOException {
		assertEquals("Hello", fModule.readFile(EXISTING_FILE_LOCATION, 5));
		assertEquals("Hello world!", fModule.readFile(EXISTING_FILE_LOCATION, -1));
	}

	@Test
	@DisplayName("readFile() continues reading bytes for FileHandle")
	public void readFile_continues_reading_bytes_for_FileHandle() throws IOException {
		final IFileHandle handle = fModule.openFile(EXISTING_FILE_LOCATION, ResourcesModule.READ, true);
		assertEquals("Hello", fModule.readFile(handle, 5));
		assertEquals(" world!", fModule.readFile(handle, -1));
	}

	@Test
	@DisplayName("copyFile() copies existing file")
	public void copyFile_copies_existing_file() throws IOException {
		fModule.copyFile(EXISTING_FILE_LOCATION, EXISTING_FILE_LOCATION + ".copy");

		assertTrue(((IFile) ResourceTools.resolve(EXISTING_FILE_LOCATION + ".copy")).exists());
	}

	@Test
	@DisplayName("copyFile() throws when source does not exist")
	public void copyFile_throws_when_source_does_not_exist() throws IOException {
		assertThrows(FileNotFoundException.class, () -> fModule.copyFile(EXISTING_FILE_LOCATION + ".notThere", EXISTING_FILE_LOCATION + ".copy"));
	}

	@Test
	@DisplayName("copyFile() overwrites existing file")
	public void copyFile_overwrites_existing_file() throws IOException {
		fModule.copyFile(EXISTING_FILE_LOCATION, EXISTING_FILE_LOCATION + ".copy");
		assertDoesNotThrow(() -> fModule.copyFile(EXISTING_FILE_LOCATION, EXISTING_FILE_LOCATION + ".copy"));
	}

	@Test
	@DisplayName("deleteFile() deletes WS file")
	public void deleteFile_deletes_WS_file() throws IOException {
		fModule.deleteFile(EXISTING_FILE_LOCATION);

		final IFile file = (IFile) ResourceTools.resolve(EXISTING_FILE_LOCATION, ".");
		assertFalse(file.exists());
	}

	@Test
	@DisplayName("deleteFile() deletes FS file")
	public void deleteFile_deletes_FS_file() throws IOException {
		fModule.deleteFile(fExistingFileSystemFile.toString());

		final File file = (File) ResourceTools.resolve(fExistingFileSystemFile.toString(), ".");
		assertFalse(file.exists());
	}

	@Test
	@DisplayName("deleteFolder() deletes WS folder")
	public void deleteFolder_deletes_WS_folder() throws IOException {
		fModule.deleteFolder(EXISTING_FOLDER_LOCATION);

		final IResource folder = (IResource) ResourceTools.resolve(EXISTING_FOLDER_LOCATION, ".");
		assertFalse(folder.exists());
	}

	@Test
	@DisplayName("deleteFolder() deletes FS folder")
	public void deleteFolder_deletes_FS_folder() throws IOException {
		fModule.deleteFolder(fExistingFileSystemFolder.toString());

		final File folder = (File) ResourceTools.resolve(fExistingFileSystemFolder.toString(), ".");
		assertFalse(folder.exists());
	}

	@Test
	@DisplayName("deleteProject() deletes existing project by resource reference")
	public void deleteProject_deletes_existing_project_by_resource_reference() throws IOException {
		fModule.deleteProject(PROJECT_LOCATION);

		assertFalse(ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME).exists());
	}

	@Test
	@DisplayName("deleteProject() deletes existing project by name reference")
	public void deleteProject_deletes_existing_project_by_name_reference() throws IOException {
		fModule.deleteProject(PROJECT_NAME);

		assertFalse(ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME).exists());
	}

	@Test
	@DisplayName("deleteProject() throws when project does not exist")
	public void deleteProject_throws_when_project_does_not_exist() throws IOException {
		assertThrows(FileNotFoundException.class, () -> fModule.deleteProject("notExistingProject"));
	}

	@Test
	@DisplayName("deleteProject() throws when input_is null")
	public void deleteProject_throws_when_input_is_null() throws IOException {
		assertThrows(IllegalArgumentException.class, () -> fModule.deleteProject(null));
	}

	@Test
	@DisplayName("readLine() reads first line of file")
	public void readLine_reads_first_line_of_file() throws IOException {
		assertEquals("Hello world!", fModule.readLine(EXISTING_FILE_LOCATION));
	}

	@Test
	@DisplayName("readLine() reads FileHandle line by line")
	public void readLine_reads_FileHandle_line_by_line() throws IOException, CoreException {
		final IFile file = (IFile) fModule.getFile(PROJECT_LOCATION + "/longContent.txt", false);
		file.create(new ByteArrayInputStream("Hello world!\nThis is line 2".getBytes()), false, null);

		final IFileHandle handle = fModule.openFile(PROJECT_LOCATION + "/longContent.txt", ResourcesModule.READ, true);

		assertEquals("Hello world!", fModule.readLine(handle));
		assertEquals("This is line 2", fModule.readLine(handle));
		assertNull(fModule.readLine(handle));
	}

	@Test
	@DisplayName("writeFile() appends to existing file")
	public void writeFile_appends_to_existing_file() throws IOException {
		final String newContent = "content";
		final long currentLength = fExistingFileSystemFile.length();
		final IFileHandle handle = fModule.writeFile(fExistingFileSystemFile, newContent, ResourcesModule.APPEND, true);
		handle.close();

		assertEquals(currentLength + newContent.length(), fExistingFileSystemFile.length());
	}

	@Test
	@DisplayName("writeFile() replaces existing file")
	public void writeFile_replaces_existing_file() throws IOException {
		final String newContent = "content";
		final IFileHandle handle = fModule.writeFile(fExistingFileSystemFile, newContent, ResourcesModule.WRITE, true);
		handle.close();

		assertEquals(newContent.length(), fExistingFileSystemFile.length());
	}

	@Test
	@DisplayName("writeFile() writes byte[]")
	public void writeFile_writes_byte_array() throws IOException {
		final String newContent = "content";
		final IFileHandle handle = fModule.writeFile(fExistingFileSystemFile, newContent.getBytes(), ResourcesModule.WRITE, true);
		handle.close();

		assertEquals(newContent, Files.readString(fExistingFileSystemFile.toPath()));
	}

	@Test
	@DisplayName("writeFile() creates non-existing file")
	public void writeFile_creates_non_existing_file() throws IOException {
		final String newFile = fExistingFileSystemFolder + File.separator + "writeFile_created_file.txt";
		final IFileHandle handle = fModule.writeFile(newFile, "content", ResourcesModule.WRITE, true);
		handle.close();

		assertTrue(new File(newFile).exists());
	}

	@Test
	@DisplayName("writeFile() creates non-existing file and folders")
	public void writeFile_creates_non_existing_file_and_folders() throws IOException {
		final String newFile = fExistingFileSystemFolder + File.separator + "writeFile_subfolder" + File.separator + "writeFile_created_file.txt";
		final IFileHandle handle = fModule.writeFile(newFile, "content", ResourcesModule.WRITE, true);
		handle.close();

		assertTrue(new File(newFile).exists());
	}

	@Test
	@DisplayName("writeLine() appends to existing file")
	public void writeLine_appends_to_existing_file() throws IOException {
		final String newContent = "content";
		final long currentLength = fExistingFileSystemFile.length();
		final IFileHandle handle = fModule.writeLine(fExistingFileSystemFile, newContent, ResourcesModule.APPEND, true);
		handle.close();

		assertEquals(currentLength + String.format("%s%n", newContent).length(), fExistingFileSystemFile.length());
	}

	@Test
	@DisplayName("findFiles() detects files in WS")
	public void findFiles_detects_files_in_WS() throws IOException {
		final String[] files = fModule.findFiles("*.txt", PROJECT_LOCATION, true);
		assertArrayEquals(new String[] { "workspace://Test Project/Test file.txt" }, files);
	}

	@Test
	@DisplayName("linkProject() links existing project folder into workspace")
	public void linkProject_links_existing_project_into_workspace() throws IOException, URISyntaxException, CoreException {
		final IProject project = fModule.linkProject(getResource("/resources/externalProject"));

		assertEquals("External Project", project.getName());

		project.delete(false, true, new NullProgressMonitor());
	}

	@Test
	@DisplayName("linkProject() throws for workspace resources")
	public void linkProject_throws_for_workspace_resources() throws IOException, URISyntaxException {
		assertThrows(IOException.class, () -> fModule.linkProject(PROJECT_LOCATION));
	}

	@Test
	@DisplayName("linkProject() throws for invalid location")
	public void linkProject_throws_for_invalid_location() throws IOException, URISyntaxException {
		assertThrows(IOException.class, () -> fModule.linkProject("workspace://Does Not Exist"));
	}

	@Test
	@DisplayName("importProject() imports existing project folder into workspace")
	public void importProject_imports_existing_project_into_workspace() throws IOException, URISyntaxException, CoreException {
		final IProject project = fModule.importProject(getResource("/resources/externalProject"));

		assertEquals("External Project", project.getName());

		project.delete(true, new NullProgressMonitor());
	}

	@Test
	@DisplayName("importProject() throws for workspace resources")
	public void importProject_throws_for_workspace_resources() throws IOException, URISyntaxException {
		assertThrows(IOException.class, () -> fModule.importProject(PROJECT_LOCATION));
	}

	@Test
	@DisplayName("importProject() throws for invalid location")
	public void importProject_throws_for_invalid_location() throws IOException, URISyntaxException {
		assertThrows(IOException.class, () -> fModule.importProject("workspace://Does Not Exist"));
	}

	@Test
	@DisplayName("refreshResource() detects file system changes in the workspace")
	public void refreshResource_detects_file_system_changes_in_the_workspace() throws IOException {
		final IFolder folder = (IFolder) ResourceTools.resolve(EXISTING_FOLDER_LOCATION);

		final File fileSystemFolder = new File(folder.getRawLocationURI());
		final File newFile = new File(fileSystemFolder + File.separator + "refreshResourcesTest.txt");
		Files.createFile(newFile.toPath());

		fModule.refreshResource(EXISTING_FOLDER_LOCATION);
		assertNotNull(ResourceTools.resolve(EXISTING_FOLDER_LOCATION + "/refreshResourcesTest.txt"));
	}

	@Test
	@DisplayName("readStream() converts an InputStream to a string")
	public void readStream_converts_an_InputStream_to_a_string() throws IOException {
		final String data = "Hello world";

		assertEquals(data, fModule.readStream(new ByteArrayInputStream(data.getBytes())));
	}

	@Test
	@DisplayName("createProblemMarker() creates a new marker on a file")
	public void createProblemMarker_creates_a_new_marker_on_a_file() throws IOException, CoreException {
		final IFile file = (IFile) ResourceTools.resolve(EXISTING_FILE_LOCATION);
		IMarker[] markers = file.findMarkers("org.eclipse.core.resources.problemmarker", false, IResource.DEPTH_ZERO);
		assertEquals(0, markers.length);

		fModule.createProblemMarker("error", EXISTING_FILE_LOCATION, 1, "Test error", "org.eclipse.core.resources.problemmarker", true);

		markers = file.findMarkers("org.eclipse.core.resources.problemmarker", false, IResource.DEPTH_ZERO);
		assertEquals(1, markers.length);
	}

	@Test
	@DisplayName("zip() creates a new zip file")
	public void zip_creates_a_new_zip_file() throws IOException {
		final File zip = (File) fModule.zip(PROJECT_LOCATION, fExistingFileSystemFolder.toString() + File.separator + "zip.zip");

		assertTrue(zip.isFile());
	}

	@Test
	@DisplayName("unzip() extracts zip file")
	public void unzip_extracts_zip_file() throws IOException {
		final File zip = (File) fModule.zip(PROJECT_LOCATION, fExistingFileSystemFolder.toString() + File.separator + "zip.zip");

		fModule.unzip(zip, EXISTING_FOLDER_LOCATION);

		final IFile file = (IFile) ResourceTools.resolve(EXISTING_FOLDER_LOCATION + "/.project");
		assertTrue(file.exists());
	}

	@Test
	@DisplayName("getChecksum() calculates MD5 checksum of file")
	public void getChecksum_calculates_MD5_checksum_of_file() throws IOException {
		assertEquals("86fb269d190d2c85f6e0468ceca42a20", fModule.getChecksum(EXISTING_FILE_LOCATION));
	}

	@Test
	@DisplayName("getChecksum() throws if resource is a folder")
	public void getChecksum_throws_if_resource_is_a_folder() throws IOException {
		assertThrows(IOException.class, () -> fModule.getChecksum(EXISTING_FOLDER_LOCATION));
	}

	@Test
	@DisplayName("getChecksum() throws if resource does not exist")
	public void getChecksum_throws_if_resource_does_not_exist() throws IOException {
		assertThrows(IOException.class, () -> fModule.getChecksum(EXISTING_FOLDER_LOCATION + "/getChecksum.notThere"));
	}
}
