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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.ui.completion.ScriptCompletionProposal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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

		fFile1 = fFolder.getFile(FILE1_NAME);
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

	@Disabled
	@Test
	public void checkOperatingSystem() {
		assertTrue(isLinux() || isWindows(), "Operating system is: " + System.getProperty("os.name"));
	}

	@Disabled
	@Test
	public void absoluteRootWorkspaceProposals() {

		final ICompletionContext context = mock(ICompletionContext.class);
		when(context.getOriginalCode()).thenReturn("");
		when(context.getFilter()).thenReturn("");
		when(context.getResource()).thenReturn(null);

		final Collection<? extends ScriptCompletionProposal> proposals = fProvider.getProposals(context);
		final Collection<String> content = extractDisplayedContent(proposals);

		assertTrue(content.contains("workspace://"), "<workspace://> proposal missing");
		assertTrue(content.contains("file:///"), "<file:///> proposal missing");
		assertEquals(2, proposals.size());
	}

	@Disabled
	@Test
	public void workspaceProposals() {

		final ICompletionContext context = mock(ICompletionContext.class);
		when(context.getOriginalCode()).thenReturn("workspace://");
		when(context.getFilter()).thenReturn("workspace://");
		when(context.getResource()).thenReturn(null);

		final Collection<? extends ScriptCompletionProposal> proposals = fProvider.getProposals(context);
		final Collection<String> content = extractDisplayedContent(proposals);

		assertTrue(content.contains(PROJECT_NAME), "<" + PROJECT_NAME + "> proposal missing");
		assertEquals(1, proposals.size());
	}

	@Disabled
	@Test
	public void workspaceProjectProposals() {

		final ICompletionContext context = mock(ICompletionContext.class);
		when(context.getOriginalCode()).thenReturn("workspace://" + PROJECT_NAME + "/");
		when(context.getFilter()).thenReturn("workspace://" + PROJECT_NAME + "/");
		when(context.getResource()).thenReturn(null);

		final Collection<? extends ScriptCompletionProposal> proposals = fProvider.getProposals(context);
		final Collection<String> content = extractDisplayedContent(proposals);

		assertTrue(content.contains(".."), "<..> proposal missing");
		assertTrue(content.contains(FOLDER_NAME), "<" + FOLDER_NAME + "> proposal missing");
		assertTrue(content.contains(".project"), "<.project> proposal missing");
		assertEquals(3, proposals.size());
	}

	@Disabled
	@Test
	public void workspaceFolderProposals() {

		final ICompletionContext context = mock(ICompletionContext.class);
		when(context.getOriginalCode()).thenReturn("workspace://" + PROJECT_NAME + "/" + FOLDER_NAME + "/");
		when(context.getFilter()).thenReturn("workspace://" + PROJECT_NAME + "/" + FOLDER_NAME + "/");
		when(context.getResource()).thenReturn(null);

		final Collection<? extends ScriptCompletionProposal> proposals = fProvider.getProposals(context);
		final Collection<String> content = extractDisplayedContent(proposals);

		assertTrue(content.contains(".."), "<..> proposal missing");
		assertTrue(content.contains(FILE1_NAME), "<" + FILE1_NAME + "> proposal missing");
		assertTrue(content.contains(FILE2_NAME), "<" + FILE2_NAME + "> proposal missing");
		assertEquals(3, proposals.size());
	}

	@Disabled
	@Test
	public void relativeRootWorkspaceProposals() {

		final ICompletionContext context = mock(ICompletionContext.class);
		when(context.getOriginalCode()).thenReturn("");
		when(context.getFilter()).thenReturn("");
		when(context.getResource()).thenReturn(fFile1);

		final Collection<? extends ScriptCompletionProposal> proposals = fProvider.getProposals(context);
		final Collection<String> content = extractDisplayedContent(proposals);

		assertTrue(content.contains("workspace://"), "<workspace://> proposal missing");
		assertTrue(content.contains("file:///"), "<file:///> proposal missing");
		assertTrue(content.contains("project://"), "<project://> proposal missing");
		assertTrue(content.contains(".."), "<..> proposal missing");
		assertTrue(content.contains(FILE1_NAME), "<" + FILE1_NAME + "> proposal missing");
		assertTrue(content.contains(FILE2_NAME), "<" + FILE2_NAME + "> proposal missing");
		assertEquals(6, proposals.size());
	}

	@Disabled
	@Test
	public void fileSystemRootProposals() {
		final ICompletionContext context = mock(ICompletionContext.class);
		when(context.getOriginalCode()).thenReturn("file:///");
		when(context.getFilter()).thenReturn("file:///");
		when(context.getResource()).thenReturn(null);

		final Collection<? extends ScriptCompletionProposal> proposals = fProvider.getProposals(context);
		final Collection<String> content = extractDisplayedContent(proposals);

		assertFalse(content.contains(".."), "<..> proposal exists");
		assertFalse(proposals.isEmpty());
	}

	@Disabled
	@Test
	public void fileSystemFolderProposals() {
		final ICompletionContext context = mock(ICompletionContext.class);
		when(context.getOriginalCode()).thenReturn(fFsFolder.getAbsolutePath());
		when(context.getFilter()).thenReturn(fFsFolder.getAbsolutePath());
		when(context.getResource()).thenReturn(null);

		final Collection<? extends ScriptCompletionProposal> proposals = fProvider.getProposals(context);
		final Collection<String> content = extractDisplayedContent(proposals);

		assertTrue(content.contains(".."), "<..> proposal missing");
		assertTrue(content.contains(FILE1_NAME), "<" + FILE1_NAME + "> proposal missing");
		assertTrue(content.contains(FILE2_NAME), "<" + FILE2_NAME + "> proposal missing");
		assertEquals(3, proposals.size());
	}

	private static Collection<String> extractDisplayedContent(Collection<? extends ScriptCompletionProposal> proposals) {
		final Collection<String> content = new HashSet<>();

		for (final ScriptCompletionProposal proposal : proposals)
			content.add(proposal.getDisplayString());

		return content;
	}
}
