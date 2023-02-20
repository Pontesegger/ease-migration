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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class FilesystemToolsTest {

	private static final String TEMP_PREFIX = "ease_unittest_";

	private File fTestFile;
	private File fTestFolder;

	@BeforeEach
	public void createTempFile() throws IOException {
		ResourceTestHelper.cleanupTempFiles(TEMP_PREFIX);

		fTestFile = ResourceTestHelper.createTempFile(TEMP_PREFIX);
		fTestFolder = ResourceTestHelper.createTempFolder(TEMP_PREFIX);
	}

	@AfterEach
	public void afterEach() throws IOException {
		ResourceTestHelper.cleanupTempFiles(TEMP_PREFIX);
	}

	@Test
	@DisplayName("createFolder() creates new folder")
	public void createFolder_creates_new_folder() throws IOException {
		final File folder = new File(fTestFile.getParentFile().toString() + File.separator + TEMP_PREFIX + "createFolder");
		final File createdFolder = FilesystemTools.createFolder(folder);

		assertEquals(folder, createdFolder);
		assertTrue(createdFolder.exists());
		assertTrue(createdFolder.isDirectory());
	}

	@Test
	@DisplayName("createFolder() creates new folders")
	public void createFolder_creates_new_folders() throws IOException {
		final File folder = new File(
				fTestFile.getParentFile().toString() + File.separator + TEMP_PREFIX + "createFoldersRecursively" + File.separator + "1" + File.separator + "2");
		final File createdFolder = FilesystemTools.createFolder(folder);

		assertEquals(folder, createdFolder);
		assertTrue(createdFolder.exists());
		assertTrue(createdFolder.isDirectory());
	}

	@Test
	@DisplayName("createFolder() returns existing folder")
	public void createFolder_returns_existing_folder() throws IOException {
		final File createdFolder = FilesystemTools.createFolder(fTestFolder);

		assertEquals(fTestFolder, createdFolder);
		assertTrue(createdFolder.exists());
		assertTrue(createdFolder.isDirectory());
	}

	@Test
	@DisplayName("createFolder() throws when file with same name exists")
	public void createFolder_throws_when_file_with_same_name_exists() throws IOException {
		assertThrows(IOException.class, () -> FilesystemTools.createFolder(fTestFile));
	}

	@Test
	@DisplayName("createFile() creates new, empty file")
	public void createFile_creates_new_empty_file() throws IOException {
		final File file = new File(fTestFile.toString() + ".createFile");
		final File createdFile = FilesystemTools.createFile(file);

		assertTrue(createdFile.exists());
		assertEquals(0, createdFile.length());
	}

	@Test
	@DisplayName("createFile() creates new file in new folder")
	public void createFile_creates_new_file_in_new_folder() throws IOException {
		final File file = new File(fTestFolder.toString() + File.separator + "subfolder" + File.separator + "createFile.inSubfolder");
		final File createdFile = FilesystemTools.createFile(file);

		assertTrue(createdFile.exists());
		assertEquals(0, createdFile.length());
	}

	@Test
	@DisplayName("createFile() throws when file already exists")
	public void createFile_throws_when_file_already_exists() throws IOException {
		assertThrows(IOException.class, () -> FilesystemTools.createFile(fTestFile));
	}

	@Test
	@DisplayName("deleteFile() deletes existing file")
	public void deleteFile_deletes_existing_file() throws IOException {
		FilesystemTools.deleteFile(fTestFile);

		assertFalse(fTestFile.exists());
	}

	@Test
	@DisplayName("deleteFile() does not throw when file does not exist")
	public void deleteFile_does_not_throw_when_file_does_not_exist() throws IOException {
		assertDoesNotThrow(() -> FilesystemTools.deleteFile(new File(fTestFile.getParent() + File.separator + "deleteFile.notThere")));
	}

	@Test
	@DisplayName("deleteFile() throws when resource is a folder")
	public void deleteFile_throws_when_resource_is_a_folder() throws IOException {
		assertThrows(IOException.class, () -> FilesystemTools.deleteFile(fTestFolder));
	}

	@Test
	@DisplayName("deleteFolder() deletes existing folder")
	public void deleteFolder_deletes_existing_folder() throws IOException {
		FilesystemTools.deleteFolder(fTestFolder);

		assertFalse(fTestFolder.exists());
	}

	@Test
	@DisplayName("deleteFolder() deletes folder recursively")
	public void deleteFolder_deletes_FS_folder_recursively() throws IOException {
		FilesystemTools.createFile(new File(fTestFolder + File.separator + "subFolder" + File.separator + "file.txt"));

		FilesystemTools.deleteFolder(fTestFolder);

		assertFalse(fTestFolder.exists());
	}

	@Test
	@DisplayName("deleteFolder() does not throw when folder does not exist")
	public void deleteFolder_does_not_throw_when_FS_folder_does_not_exist() throws IOException {
		assertDoesNotThrow(() -> FilesystemTools.deleteFolder(new File(fTestFile.toString() + ".notThere")));
	}

	@Test
	@DisplayName("deleteFolder() throws when resource is a file")
	public void deleteFolder_throws_when_resource_is_a_file() throws IOException {
		assertThrows(IOException.class, () -> FilesystemTools.deleteFolder(fTestFile));
	}

	@Test
	@DisplayName("findFiles() returns empty array for no matches")
	public void findFiles_returns_empty_array_for_no_matches() throws IOException {
		assertArrayEquals(new Object[0], FilesystemTools.findFiles(Pattern.compile("notThere\\.noFile"), fTestFolder, true));
	}

	@DisplayName("findFiles() ignores folders")
	public void findFiles_ignores_folders() throws IOException {
		assertArrayEquals(new String[0], FilesystemTools.findFiles(Pattern.compile(fTestFolder.getName()), fTestFolder.getParentFile(), true));
	}

	@Test
	@DisplayName("findFiles() detects file")
	public void findFiles_detects_project_file() throws IOException {
		assertArrayEquals(new String[] { fTestFile.getAbsolutePath() },
				FilesystemTools.findFiles(Pattern.compile(fTestFile.getName()), fTestFile.getParentFile(), true));
	}

	@Test
	@DisplayName("findFiles() detects file with wildcards")
	public void findFiles_detects_file_with_wildcard() throws IOException {
		assertArrayEquals(new String[] { fTestFile.getAbsolutePath() },
				FilesystemTools.findFiles(Pattern.compile(TEMP_PREFIX + ".*"), fTestFile.getParentFile(), true));
	}

	@Test
	@DisplayName("findFiles() detects multiple files")
	public void findFiles_detects_multiple_files() throws IOException, CoreException {

		final File file2 = ResourceTestHelper.createTempFile(TEMP_PREFIX);

		final File file3 = new File(fTestFolder.getAbsolutePath() + File.separator + TEMP_PREFIX + "findFiles.txt");
		try (OutputStream out = Files.newOutputStream(file3.toPath())) {
			out.write("Hello world!".getBytes());
		}

		final File file4 = new File(fTestFolder.getAbsolutePath() + File.separator + "findFiles.txt");
		try (OutputStream out = Files.newOutputStream(file4.toPath())) {
			out.write("Hello world!".getBytes());
		}

		final List<String> foundFiles = Arrays.asList(FilesystemTools.findFiles(Pattern.compile(TEMP_PREFIX + ".*"), fTestFile.getParentFile(), true));

		assertEquals(3, foundFiles.size());
		assertTrue(foundFiles.contains(fTestFile.getAbsolutePath()));
		assertTrue(foundFiles.contains(file2.getAbsolutePath()));
		assertTrue(foundFiles.contains(file3.getAbsolutePath()));
	}

	@Test
	@DisplayName("findFiles() detects files in current folder only")
	public void findFiles_detects_files_in_current_folder_only() throws IOException, CoreException {
		final File file2 = ResourceTestHelper.createTempFile(TEMP_PREFIX);

		final File file3 = new File(fTestFolder.getAbsolutePath() + File.separator + TEMP_PREFIX + "findFiles.txt");
		try (OutputStream out = Files.newOutputStream(file3.toPath())) {
			out.write("Hello world!".getBytes());
		}

		final File file4 = new File(fTestFolder.getAbsolutePath() + File.separator + "findFiles.txt");
		try (OutputStream out = Files.newOutputStream(file4.toPath())) {
			out.write("Hello world!".getBytes());
		}

		final List<String> foundFiles = Arrays.asList(FilesystemTools.findFiles(Pattern.compile(TEMP_PREFIX + ".*"), fTestFile.getParentFile(), false));

		assertEquals(2, foundFiles.size());
		assertTrue(foundFiles.contains(fTestFile.getAbsolutePath()));
		assertTrue(foundFiles.contains(file2.getAbsolutePath()));
	}

	@Test
	@DisplayName("findFiles() throws when root is a file")
	public void findFiles_throws_when_root_is_a_file() throws IOException, CoreException {
		assertThrows(IOException.class, () -> FilesystemTools.findFiles(Pattern.compile(".*"), fTestFile, true));
	}

}
