package org.eclipse.ease.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ResourceToolsTest {

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

		fFile1 = fFolder.getFile(FILE1_NAME);
		if (!fFile1.exists())
			fFile1.create(new ByteArrayInputStream(FILE1_CONTENT.getBytes("UTF-8")), false, null);

		fFile2 = fFolder.getFile(FILE2_NAME);
		if (!fFile2.exists())
			fFile2.create(new ByteArrayInputStream("Hello eclipse".getBytes("UTF-8")), false, null);

		fFsFile1 = fFile1.getLocation().toFile();
		fFsFolder = fFsFile1.getParentFile();
		fFsProject = fFsFolder.getParentFile();
		fFsWorkspace = fWorkspace.getRawLocation().toFile();
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
	public void checkOperatingSystem() {
		assertTrue(isLinux() || isWindows(), "Operating system is: " + System.getProperty("os.name"));
	}

	// --------------------------------------------------------------------------------
	// isAbsolute()
	// --------------------------------------------------------------------------------
	@Test
	public void invalidIsAbsolute() {
		assertFalse(ResourceTools.isAbsolute("../someFile.txt"));
		assertFalse(ResourceTools.isAbsolute("someFile.txt"));
		assertFalse(ResourceTools.isAbsolute("folder/someFile.txt"));
		assertFalse(ResourceTools.isAbsolute("folder\\someFile.txt"));
	}

	@Test
	public void isAbsoluteWorkspacePath() {
		assertTrue(ResourceTools.isAbsolute("workspace://"));
		assertTrue(ResourceTools.isAbsolute("workspace://project/file.txt"));

		assertFalse(ResourceTools.isAbsolute("project://file.txt"));
	}

	@Test
	public void isAbsoluteFileSystemPathOnLinux() {
		if (isLinux()) {
			assertTrue(ResourceTools.isAbsolute("/home/user/file.txt"));
			assertTrue(ResourceTools.isAbsolute("/rootFile.txt"));

			assertTrue(ResourceTools.isAbsolute("file://home/user/file.txt")); // invalid locations, but often used by users
			assertTrue(ResourceTools.isAbsolute("file://rootFile.txt"));

			assertTrue(ResourceTools.isAbsolute("file:///home/user/file.txt"));
			assertTrue(ResourceTools.isAbsolute("file:///rootFile.txt"));
		}
	}

	@Test
	public void isAbsoluteFileSystemPathOnWindows() {
		if (isWindows()) {
			assertTrue(ResourceTools.isAbsolute("C:/home/user/file.txt"));
			assertTrue(ResourceTools.isAbsolute("C:/rootFile.txt"));

			assertTrue(ResourceTools.isAbsolute("C:\\home\\user\\file.txt"));
			assertTrue(ResourceTools.isAbsolute("C:\\rootFile.txt"));

			assertTrue(ResourceTools.isAbsolute("\\\\someServer\\rootFile.txt"));

			assertTrue(ResourceTools.isAbsolute("file://C:/home/user/file.txt")); // invalid locations, but often used by users
			assertTrue(ResourceTools.isAbsolute("file://C:/rootFile.txt"));

			assertTrue(ResourceTools.isAbsolute("file:///C:/home/user/file.txt")); // valid URI coding for local machine
			assertTrue(ResourceTools.isAbsolute("file:///C:/rootFile.txt"));

			assertTrue(ResourceTools.isAbsolute("file://someServer/home/user/file.txt")); // valid URI coding for server
			assertTrue(ResourceTools.isAbsolute("file://someServer/rootFile.txt"));

			assertTrue(ResourceTools.isAbsolute("//someServer/rootFile.txt"));
		}
	}

	@Test
	public void isAbsoluteURL() {
		assertTrue(ResourceTools.isAbsolute("http://eclipse.org/file.txt"));
		assertTrue(ResourceTools.isAbsolute("https://eclipse.org/file.txt"));
		assertTrue(ResourceTools.isAbsolute("svn://eclipse.org/file.txt"));
		assertTrue(ResourceTools.isAbsolute("platform:/plugin/org.eclipse.ease/some/location.png"));
	}

	// --------------------------------------------------------------------------------
	// resolve()
	// --------------------------------------------------------------------------------

	@Test
	public void resolveInWorkspace() {
		assertEquals(fWorkspace, ResourceTools.resolve(fWorkspace));
		assertEquals(fProject, ResourceTools.resolve(fProject));
		assertEquals(fFolder, ResourceTools.resolve(fFolder));
		assertEquals(fFile1, ResourceTools.resolve(fFile1));

		assertEquals(fWorkspace, ResourceTools.resolve("workspace://"));
		assertEquals(fProject, ResourceTools.resolve("workspace://" + PROJECT_NAME));
		assertEquals(fFolder, ResourceTools.resolve("workspace://" + PROJECT_NAME + "/" + FOLDER_NAME));
		assertEquals(fFile1, ResourceTools.resolve("workspace://" + PROJECT_NAME + "/" + FOLDER_NAME + "/" + FILE1_NAME));
	}

	@Test
	public void resolveAbsoluteInFileSystem() {
		assertEquals(fFsFile1, ResourceTools.resolve(fFsFile1));
		assertEquals(fFsFolder, ResourceTools.resolve(fFsFolder));
	}

	@Test
	public void resolveAbsoluteInFileSystemOnLinux() {
		if (isLinux()) {
			assertEquals(new File("/home/user/file.txt"), ResourceTools.resolve("/home/user/file.txt"));
			assertEquals(new File("/rootFile.txt"), ResourceTools.resolve("/rootFile.txt"));

			assertEquals(new File("/home/user/file.txt"), ResourceTools.resolve("file://home/user/file.txt"));
			assertEquals(new File("/rootFile.txt"), ResourceTools.resolve("file://rootFile.txt"));

			assertEquals(new File("/home/user/file.txt"), ResourceTools.resolve("file:///home/user/file.txt"));
			assertEquals(new File("/rootFile.txt"), ResourceTools.resolve("file:///rootFile.txt"));
		}
	}

	@Test
	public void resolveAbsoluteInFileSystemOnWindows() {
		if (isWindows()) {
			assertEquals(new File("C:/home/user/file with spaces.txt"), ResourceTools.resolve("C:\\home\\user\\file with spaces.txt"));
			assertEquals(new File("C:/rootFile.txt"), ResourceTools.resolve("C:\\rootFile.txt"));

			assertEquals(new File("C:/home/user/file with spaces.txt"), ResourceTools.resolve("C:/home/user/file with spaces.txt"));
			assertEquals(new File("C:/rootFile.txt"), ResourceTools.resolve("C:/rootFile.txt"));

			assertEquals(new File("C:/home/user/file with spaces.txt"), ResourceTools.resolve("file://C:/home/user/file with spaces.txt"));
			assertEquals(new File("C:/rootFile.txt"), ResourceTools.resolve("file://C:/rootFile.txt"));

			assertEquals(new File("C:/home/user/file with spaces.txt"), ResourceTools.resolve("file:///C:/home/user/file with spaces.txt"));
			assertEquals(new File("C:/rootFile.txt"), ResourceTools.resolve("file:///C:/rootFile.txt"));

			assertEquals(new File("//someServer/home/user/file with spaces.txt"), ResourceTools.resolve("file://someServer/home/user/file with spaces.txt"));
			assertEquals(new File("//someServer/rootFile.txt"), ResourceTools.resolve("file://someServer/rootFile.txt"));

			assertEquals(new File("//someServer/home/user/file with spaces.txt"), ResourceTools.resolve("\\\\someServer\\home\\user\\file with spaces.txt"));
			assertEquals(new File("//someServer/rootFile.txt"), ResourceTools.resolve("\\\\someServer\\rootFile.txt"));

			assertEquals(ResourceTools.VIRTUAL_WINDOWS_ROOT, ResourceTools.resolve("file://"));
			assertEquals(ResourceTools.VIRTUAL_WINDOWS_ROOT, ResourceTools.resolve("file:///"));
		}
	}

	@Test
	public void resolveURL() throws MalformedURLException {

		assertEquals(URI.create("http://eclipse.org/file.js"), ResourceTools.resolve("http://eclipse.org/file.js"));
		assertEquals(URI.create("https://eclipse.org/file.js"), ResourceTools.resolve("https://eclipse.org/file.js"));
		assertEquals(URI.create("svn://eclipse.org/file.js"), ResourceTools.resolve("svn://eclipse.org/file.js"));

		assertEquals(URI.create("http://eclipse.org/file%20with%20spaces.js"), ResourceTools.resolve("http://eclipse.org/file with spaces.js"));
	}

	@Test
	public void resolveInvalid() {
		assertNull(ResourceTools.resolve(null));
		assertNull(ResourceTools.resolve(""));
		assertNull(ResourceTools.resolve(" "));
	}

	@Test
	public void resolveWindowsRoot() {
		final File rootFile = File.listRoots()[0];
		final String rootName = rootFile.toURI().toString().replace("file:/", "file:///");

		assertEquals(rootFile, ResourceTools.resolve(rootName));
	}

	// --------------------------------------------------------------------------------
	// resolve() with parent
	// --------------------------------------------------------------------------------

	@Test
	public void resolveRelativeInWorkspace() {
		// absolute locations, should still work
		assertEquals(fWorkspace, ResourceTools.resolve(fWorkspace, null));
		assertEquals(fProject, ResourceTools.resolve(fProject, null));
		assertEquals(fFolder, ResourceTools.resolve(fFolder, null));
		assertEquals(fFile1, ResourceTools.resolve(fFile1, null));

		assertEquals(fWorkspace, ResourceTools.resolve("workspace://", null));
		assertEquals(fProject, ResourceTools.resolve("workspace://" + PROJECT_NAME, null));
		assertEquals(fFolder, ResourceTools.resolve("workspace://" + PROJECT_NAME + "/" + FOLDER_NAME, null));
		assertEquals(fFile1, ResourceTools.resolve("workspace://" + PROJECT_NAME + "/" + FOLDER_NAME + "/" + FILE1_NAME, null));

		// project relative resources
		assertEquals(fFile1, ResourceTools.resolve("project://" + FOLDER_NAME + "/" + FILE1_NAME, fFolder));
		assertEquals(fFolder, ResourceTools.resolve("project://" + FOLDER_NAME, fFile1));

		// relative resources, resolve files
		assertEquals(fFile2, ResourceTools.resolve(FILE2_NAME, fFile1));
		assertEquals(fFile2, ResourceTools.resolve("../" + FOLDER_NAME + "/" + FILE2_NAME, fFile1));
		assertEquals(fFile2, ResourceTools.resolve("../../" + PROJECT_NAME + "/" + FOLDER_NAME + "/" + FILE2_NAME, fFile1));

		assertEquals(fFile2, ResourceTools.resolve(FILE2_NAME, fFolder));
		assertEquals(fFile2, ResourceTools.resolve("../" + FOLDER_NAME + "/" + FILE2_NAME, fFolder));
		assertEquals(fFile2, ResourceTools.resolve("../../" + PROJECT_NAME + "/" + FOLDER_NAME + "/" + FILE2_NAME, fFolder));

		assertEquals(fFile2, ResourceTools.resolve(FOLDER_NAME + "/" + FILE2_NAME, fProject));
		assertEquals(fFile2, ResourceTools.resolve(PROJECT_NAME + "/" + FOLDER_NAME + "/" + FILE2_NAME, fWorkspace));

		// relative resources, resolve containers
		assertEquals(fFolder, ResourceTools.resolve(".", fFile1));
		assertEquals(fFolder, ResourceTools.resolve("./", fFile1));

		assertEquals(fProject, ResourceTools.resolve("..", fFile1));
		assertEquals(fProject, ResourceTools.resolve("../", fFile1));
		assertEquals(fProject, ResourceTools.resolve("..", fFolder));
		assertEquals(fProject, ResourceTools.resolve("../", fFolder));

		assertEquals(fWorkspace, ResourceTools.resolve("..", fProject));
		assertEquals(fWorkspace, ResourceTools.resolve("../../", fFile1));

		assertEquals(fProject, ResourceTools.resolve("project://", fFolder));
		assertEquals(fProject, ResourceTools.resolve("project://", fProject));
	}

	@Test
	public void resolveRelativeInFileSystem() {
		// absolute locations, should still work
		assertEquals(fFsFile1, ResourceTools.resolve(fFsFile1, null));
		assertEquals(fFsFolder, ResourceTools.resolve(fFsFolder, null));

		// relative resources
		assertEquals(fFsFolder, ResourceTools.resolve(".", fFsFile1));
		assertEquals(fFsFolder, ResourceTools.resolve("./", fFsFile1));

		assertEquals(fFsProject, ResourceTools.resolve("..", fFsFile1));
		assertEquals(fFsProject, ResourceTools.resolve("../", fFsFile1));
		assertEquals(fFsProject, ResourceTools.resolve("..", fFsFolder));
		assertEquals(fFsProject, ResourceTools.resolve("../", fFsFolder));

		assertEquals(fFsWorkspace, ResourceTools.resolve("..", fFsProject));
		assertEquals(fFsWorkspace, ResourceTools.resolve("../../", fFsFile1));
	}

	@Test
	public void resolveRelativeInFileSystemOnLinux() {
		if (isLinux()) {
			// absolute locations, should still work
			assertEquals(new File("/home/user/file.txt"), ResourceTools.resolve("/home/user/file.txt", null));
			assertEquals(new File("/rootFile.txt"), ResourceTools.resolve("/rootFile.txt", null));

			assertEquals(new File("/home/user/file.txt"), ResourceTools.resolve("file://home/user/file.txt", null));
			assertEquals(new File("/rootFile.txt"), ResourceTools.resolve("file://rootFile.txt", null));

			assertEquals(new File("/home/user/file.txt"), ResourceTools.resolve("file:///home/user/file.txt", null));
			assertEquals(new File("/rootFile.txt"), ResourceTools.resolve("file:///rootFile.txt", null));
		}
	}

	@Test
	public void resolveRelativeInFileSystemOnWindows() {
		if (isWindows()) {
			// absolute locations, should still work
			assertEquals(new File("C:/home/user/file.txt"), ResourceTools.resolve("C:\\home\\user\\file.txt", null));
			assertEquals(new File("C:/rootFile.txt"), ResourceTools.resolve("C:\\rootFile.txt", null));

			assertEquals(new File("C:/home/user/file.txt"), ResourceTools.resolve("C:/home/user/file.txt", null));
			assertEquals(new File("C:/rootFile.txt"), ResourceTools.resolve("C:/rootFile.txt", null));

			assertEquals(new File("C:/home/user/file.txt"), ResourceTools.resolve("file://C:/home/user/file.txt", null));
			assertEquals(new File("C:/rootFile.txt"), ResourceTools.resolve("file://C:/rootFile.txt", null));

			assertEquals(new File("C:/home/user/file.txt"), ResourceTools.resolve("file:///C:/home/user/file.txt", null));
			assertEquals(new File("C:/rootFile.txt"), ResourceTools.resolve("file:///C:/rootFile.txt", null));

			assertEquals(new File("//someServer/home/user/file.txt"), ResourceTools.resolve("file://someServer/home/user/file.txt", null));
			assertEquals(new File("//someServer/rootFile.txt"), ResourceTools.resolve("file://someServer/rootFile.txt", null));

			assertEquals(new File("//someServer/home/user/file.txt"), ResourceTools.resolve("\\\\someServer\\home\\user\\file.txt", null));
			assertEquals(new File("//someServer/rootFile.txt"), ResourceTools.resolve("\\\\someServer\\rootFile.txt", null));
		}
	}

	@Test
	public void resolveRelativeURIs() {
		// absolute locations, should still work
		assertEquals(URI.create("http://eclipse.org/file.js"), ResourceTools.resolve("http://eclipse.org/file.js", null));
		assertEquals(URI.create("https://eclipse.org/file.js"), ResourceTools.resolve("https://eclipse.org/file.js", null));
		assertEquals(URI.create("svn://eclipse.org/file.js"), ResourceTools.resolve("svn://eclipse.org/file.js", null));

		assertEquals(URI.create("http://eclipse.org/file%20with%20spaces.js"), ResourceTools.resolve("http://eclipse.org/file with spaces.js", null));

		// relative URIs
		assertEquals(URI.create("http://eclipse.org/"), ResourceTools.resolve(".", "http://eclipse.org/file.js"));
		assertEquals(URI.create("http://eclipse.org/"), ResourceTools.resolve("..", "http://eclipse.org/folder/file.js"));
		assertEquals(URI.create("http://eclipse.org/anotherFile.js"), ResourceTools.resolve("anotherFile.js", "http://eclipse.org/file.js"));
	}

	@Test
	public void resolvePlatformURI() {
		// needed to locate platform images
		assertEquals(URI.create("platform:/plugin/org.eclipse.ease/icons/myImage.png"),
				ResourceTools.resolve("platform:/plugin/org.eclipse.ease/icons/myImage.png"));
		assertEquals(URI.create("platform:/plugin/org.eclipse.ease/icons/myImage.png"),
				ResourceTools.resolve("platform:/plugin/org.eclipse.ease/icons/myImage.png", null));
	}

	@Test
	public void resolveRelativefromWorkspaceToFilesystem() {
		assertEquals(fFsWorkspace.getParentFile(), ResourceTools.resolve("../../../", fFile1));
	}

	@Test
	public void resolveRelativeWithoutBaseFile() {
		assertEquals(fWorkspace, ResourceTools.resolve(null, fWorkspace));
		assertEquals(fProject, ResourceTools.resolve(null, fProject));
		assertEquals(fFolder, ResourceTools.resolve(null, fFolder));
		assertEquals(fFolder, ResourceTools.resolve(null, fFile1));

		assertEquals(fWorkspace, ResourceTools.resolve(" ", fWorkspace));
		assertEquals(fProject, ResourceTools.resolve(" ", fProject));
		assertEquals(fFolder, ResourceTools.resolve(" ", fFolder));
		assertEquals(fFolder, ResourceTools.resolve(" ", fFile1));

		assertEquals(fFsWorkspace, ResourceTools.resolve(" ", fFsWorkspace));
		assertEquals(fFsProject, ResourceTools.resolve(" ", fFsProject));
		assertEquals(fFsFolder, ResourceTools.resolve(" ", fFsFolder));
		assertEquals(fFsFolder, ResourceTools.resolve(" ", fFsFile1));
	}

	@Test
	public void exists() {
		assertTrue(ResourceTools.exists(fWorkspace));
		assertTrue(ResourceTools.exists(fProject));
		assertTrue(ResourceTools.exists(fFolder));
		assertTrue(ResourceTools.exists(fFile1));

		assertTrue(ResourceTools.exists(fFsWorkspace));
		assertTrue(ResourceTools.exists(fFsProject));
		assertTrue(ResourceTools.exists(fFsFolder));
		assertTrue(ResourceTools.exists(fFsFile1));

		// valid locations, but not resolved
		assertFalse(ResourceTools.exists("workspace://" + PROJECT_NAME));
		assertFalse(ResourceTools.exists(fFsWorkspace.getAbsolutePath()));

		// invalid locations
		assertFalse(ResourceTools.exists(fProject.getFile(DOES_NOT_EXIST)));
		assertFalse(ResourceTools.exists(new File("/linux/" + DOES_NOT_EXIST)));
		assertFalse(ResourceTools.exists(new File("C:\\" + DOES_NOT_EXIST)));
	}

	@Test
	public void isFile() {
		assertFalse(ResourceTools.isFile(fWorkspace));
		assertFalse(ResourceTools.isFile(fProject));
		assertFalse(ResourceTools.isFile(fFolder));
		assertTrue(ResourceTools.isFile(fFile1));

		assertFalse(ResourceTools.isFile(fFsWorkspace));
		assertFalse(ResourceTools.isFile(fFsProject));
		assertFalse(ResourceTools.isFile(fFsFolder));
		assertTrue(ResourceTools.isFile(fFsFile1));

		// valid locations, but not resolved
		assertFalse(ResourceTools.isFile("workspace://" + PROJECT_NAME));
		assertFalse(ResourceTools.isFile(fFsWorkspace.getAbsolutePath()));

		// invalid locations
		assertFalse(ResourceTools.isFile(fProject.getFile(DOES_NOT_EXIST)));
		assertFalse(ResourceTools.isFile(new File("/linux/" + DOES_NOT_EXIST)));
		assertFalse(ResourceTools.isFile(new File("C:\\" + DOES_NOT_EXIST)));
	}

	@Test
	public void isFolder() {
		assertTrue(ResourceTools.isFolder(fWorkspace));
		assertTrue(ResourceTools.isFolder(fProject));
		assertTrue(ResourceTools.isFolder(fFolder));
		assertFalse(ResourceTools.isFolder(fFile1));

		assertTrue(ResourceTools.isFolder(fFsWorkspace));
		assertTrue(ResourceTools.isFolder(fFsProject));
		assertTrue(ResourceTools.isFolder(fFsFolder));
		assertFalse(ResourceTools.isFolder(fFsFile1));

		// valid locations, but not resolved
		assertFalse(ResourceTools.isFolder("workspace://" + PROJECT_NAME));
		assertFalse(ResourceTools.isFolder(fFsWorkspace.getAbsolutePath()));

		// invalid locations
		assertFalse(ResourceTools.isFolder(fProject.getFile(DOES_NOT_EXIST)));
		assertFalse(ResourceTools.isFolder(new File("/linux/" + DOES_NOT_EXIST)));
		assertFalse(ResourceTools.isFolder(new File("C:\\" + DOES_NOT_EXIST)));
	}

	@Test
	public void createURI() throws MalformedURLException, URISyntaxException {
		assertEquals(URI.create("http://eclipse.org/some%20file.js"), ResourceTools.createURI("http://eclipse.org/some file.js"));
	}

	@Test
	public void toProjectRelativeLocation() {
		assertEquals("project://" + FOLDER_NAME, ResourceTools.toProjectRelativeLocation(fFolder));
		assertEquals("project://" + FOLDER_NAME + "/" + FILE1_NAME, ResourceTools.toProjectRelativeLocation(fFile1));
	}

	@Test
	public void workspaceToAbsoluteLocation() {
		assertEquals("workspace://", ResourceTools.toAbsoluteLocation(fWorkspace, null));
		assertEquals("workspace://" + PROJECT_NAME, ResourceTools.toAbsoluteLocation(fProject, null));
		assertEquals("workspace://" + PROJECT_NAME + "/" + FOLDER_NAME, ResourceTools.toAbsoluteLocation(fFolder, null));
		assertEquals("workspace://" + PROJECT_NAME + "/" + FOLDER_NAME + "/" + FILE1_NAME, ResourceTools.toAbsoluteLocation(fFile1, null));
	}

	@Test
	public void fileSystemToAbsoluteLocationOnWindows() {
		if (isWindows()) {
			assertEquals("file:///" + fFsWorkspace.toString().replaceAll("\\\\", "/"), ResourceTools.toAbsoluteLocation(fFsWorkspace, null));
			assertEquals("file:///" + fFsProject.toString().replaceAll("\\\\", "/"), ResourceTools.toAbsoluteLocation(fFsProject, null));
			assertEquals("file:///" + fFsFolder.toString().replaceAll("\\\\", "/"), ResourceTools.toAbsoluteLocation(fFsFolder, null));
			assertEquals("file:///" + fFsFile1.toString().replaceAll("\\\\", "/"), ResourceTools.toAbsoluteLocation(fFsFile1, null));

			// make sure we just have forward slashes available in the filename (windows)
			assertFalse(ResourceTools.toAbsoluteLocation(fFsWorkspace, null).contains("\\"));
		}
	}

	@Test
	public void fileSystemToAbsoluteLocationOnLinux() {
		if (isLinux()) {
			assertEquals("file://" + fFsWorkspace.toString(), ResourceTools.toAbsoluteLocation(fFsWorkspace, null));
			assertEquals("file://" + fFsProject.toString(), ResourceTools.toAbsoluteLocation(fFsProject, null));
			assertEquals("file://" + fFsFolder.toString(), ResourceTools.toAbsoluteLocation(fFsFolder, null));
			assertEquals("file://" + fFsFile1.toString(), ResourceTools.toAbsoluteLocation(fFsFile1, null));

			// make sure we do not start with 4 slashes (linux)
			assertFalse(ResourceTools.toAbsoluteLocation(fFsWorkspace, null).startsWith("file:////"));
		}
	}

	@Test
	public void uriToAbsoluteLocation() {
		assertEquals("http://eclipse.org", ResourceTools.toAbsoluteLocation("http://eclipse.org", null));
		assertEquals("https://eclipse.org/somefile.js", ResourceTools.toAbsoluteLocation("https://eclipse.org/somefile.js", null));
		assertEquals("svn://eclipse.org/file", ResourceTools.toAbsoluteLocation("svn://eclipse.org/file", null));
	}

	@Test
	public void toRelativeLocationInWorkspace() {
		assertEquals(fFile1.getName(), ResourceTools.toRelativeLocation(fFile1, fFile2));
	}

	@Test
	public void getWorkspaceInputStream() throws IOException {
		final InputStream stream = ResourceTools.getInputStream("workspace://" + PROJECT_NAME + "/" + FOLDER_NAME + "/" + FILE1_NAME);
		assertNotNull(stream);
		if (stream != null) {
			assertTrue(stream.available() > 0);
			stream.close();
		}
	}

	@Test
	public void getFilesystemInputStream() throws IOException {
		final InputStream stream = ResourceTools.getInputStream(fFsFile1.toURI());
		assertNotNull(stream);
		if (stream != null) {
			assertTrue(stream.available() > 0);
			stream.close();
		}
	}

	@Test
	public void getInvalidInputStream() {
		assertNull(ResourceTools.getInputStream(null));

		assertNull(ResourceTools.getInputStream("workspace://" + PROJECT_NAME + "/" + FOLDER_NAME + "/" + DOES_NOT_EXIST));
		assertNull(ResourceTools.getInputStream(fFolder));
	}

	@Test
	public void readerToString() throws IOException {
		assertEquals("", ResourceTools.toString(new StringReader("")));
		assertEquals(FILE1_NAME, ResourceTools.toString(new StringReader(FILE1_NAME)));
	}

	@Test
	public void inputStreamToString() throws IOException {
		assertEquals("", ResourceTools.toString(new ByteArrayInputStream(new byte[0])));
		assertEquals(FILE1_NAME, ResourceTools.toString(new ByteArrayInputStream(FILE1_NAME.getBytes())));
	}

	@Test
	public void invalidToString() throws IOException {
		assertNull(ResourceTools.toString((InputStream) null));
		assertNull(ResourceTools.toString((Reader) null));
	}

	@Test
	public void workspaceFileToString() {
		assertEquals(FILE1_CONTENT, ResourceTools.toString("workspace://" + PROJECT_NAME + "/" + FOLDER_NAME + "/" + FILE1_NAME));
	}

	@Test
	public void filesystemFileToString() {
		assertEquals(FILE1_CONTENT, ResourceTools.toString(fFsFile1.toString()));
	}

	@Test
	public void toFile() {
		assertEquals(fFsWorkspace, ResourceTools.toFile(fFsWorkspace));

		assertEquals(fFsWorkspace, ResourceTools.toFile(fWorkspace));
		assertEquals(fFsProject, ResourceTools.toFile(fProject));
		assertEquals(fFsFolder, ResourceTools.toFile(fFolder));
		assertEquals(fFsFile1, ResourceTools.toFile(fFile1));
	}

	@Test
	public void invalidToFile() {
		assertNull(ResourceTools.toFile(null));
		assertNull(ResourceTools.toFile("workspace://"));
	}

	@Test
	public void createFolder() throws CoreException {
		final String folderName = "/some/new/subfolder";

		assertFalse(fProject.getFolder(folderName).exists());
		ResourceTools.createFolder(fProject.getFolder(folderName));
		assertTrue(fProject.getFolder(folderName).exists());
	}

	@Test
	public void resolveScriptLocation() throws MalformedURLException {
		assertEquals(new URL("script://SimpleScript.js"), ResourceTools.resolve("script://SimpleScript.js"));
	}

	@Test
	public void resolveScriptLocationWithSpacesInDomain() throws MalformedURLException {
		assertEquals(new URL("script://Simple Script.js"), ResourceTools.resolve("script://Simple Script.js"));
	}

	@Test
	public void resolveScriptLocationWithSpacesInPath() throws MalformedURLException {
		assertEquals(new URL("script://domain/Simple Script.js"), ResourceTools.resolve("script://domain/Simple Script.js"));
	}

	@Test
	public void resolveScriptLocationWithSpecialCharactersInDomain() throws MalformedURLException {
		assertEquals(new URL("script://Simple_Script.js"), ResourceTools.resolve("script://Simple_Script.js"));
	}

	@Test
	public void resolveScriptLocationWithSpecialCharactersInPath() throws MalformedURLException {
		assertEquals(new URL("script://domain/Simple_Script.js"), ResourceTools.resolve("script://domain/Simple_Script.js"));
	}
}
