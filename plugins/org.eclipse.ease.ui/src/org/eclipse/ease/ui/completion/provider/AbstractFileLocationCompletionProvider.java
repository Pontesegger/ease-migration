/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.completion.provider;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.ICompletionContext.Type;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.ease.ui.completion.AbstractCompletionProvider;
import org.eclipse.ease.ui.completion.IImageResolver;
import org.eclipse.ease.ui.completion.ScriptCompletionProposal;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public abstract class AbstractFileLocationCompletionProvider extends AbstractCompletionProvider {

	private static class ResourceImageResolver implements IImageResolver {

		private final Object fFile;

		public ResourceImageResolver(Object file) {
			fFile = file;
		}

		@Override
		public Image getImage() {
			if (fFile instanceof IResource) {
				final IWorkbenchAdapter adapter = Platform.getAdapterManager().getAdapter(fFile, IWorkbenchAdapter.class);
				return (adapter != null) ? adapter.getImageDescriptor(fFile).createImage() : null;

			} else if (fFile instanceof File) {

				if (isRootFile((File) fFile))
					return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER).createImage();

				if (((File) fFile).isFile())
					return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE).createImage();

				if (((File) fFile).isDirectory())
					return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER).createImage();

			} else if (fFile instanceof ImageDescriptor)
				return ((ImageDescriptor) fFile).createImage();

			return null;
		}
	}

	private static final int ORDER_URI_SCHEME = ScriptCompletionProposal.ORDER_DEFAULT;
	private static final int ORDER_PROJECT = ScriptCompletionProposal.ORDER_DEFAULT + 1;
	private static final int ORDER_FOLDER = ScriptCompletionProposal.ORDER_DEFAULT + 2;
	private static final int ORDER_FILE = ScriptCompletionProposal.ORDER_DEFAULT + 3;

	private final ILabelProvider fLabelProvider = new WorkbenchLabelProvider();

	public AbstractFileLocationCompletionProvider() {
		Display.getDefault().syncExec(() -> fLabelProvider.getImage(ResourcesPlugin.getWorkspace().getRoot()));
	}

	@Override
	public boolean isActive(final ICompletionContext context) {
		return (context.getType() == Type.STRING_LITERAL);
	}

	@Override
	protected void prepareProposals(final ICompletionContext context) {
		final LocationResolver resolver = new LocationResolver(context.getFilter(), context.getResource());

		// add URI scheme proposals
		if ((matches(context.getFilter(), "workspace:/")) && (showCandidate("workspace://")))
			addProposal("workspace://", "workspace://", null, ORDER_URI_SCHEME, null);

		if ((matches(context.getFilter(), "project:/")) && (getContext().getResource() instanceof IResource) && (showCandidate("project://")))
			addProposal("project://", "project://", null, ORDER_URI_SCHEME, null);

		if ((matches(context.getFilter(), "file://")) && (showCandidate("file:///")))
			addProposal("file:///", "file:///", null, ORDER_URI_SCHEME, null);

		// display proposals
		for (final Object child : resolver.getChildren()) {
			if (child instanceof File) {
				String name = ((File) child).getName();
				final String suffix = (((File) child).isDirectory()) ? "/" : "";
				String parentFolder = resolver.getParentString();

				if ((name.isEmpty()) && (ResourceTools.isWindows())) {
					// this is a windows root folder
					name = ((File) child).getPath().substring(0, 2);
					parentFolder = "file:///";
				}

				final String replacement = parentFolder + ((parentFolder.endsWith("/") ? "" : "/")) + name + suffix;

				if ((matchesIgnoreCase(resolver.getFilterPart(), name)) && (showCandidate(child)))
					addProposal(name, replacement, new ResourceImageResolver(child), ORDER_FILE, null);

			} else if (child instanceof IResource) {
				if ((matchesIgnoreCase(resolver.getFilterPart(), ((IResource) child).getName())) && (showCandidate(child))) {

					if (child instanceof IProject) {
						addProposal(((IProject) child).getName(), resolver.getParentString() + ((IProject) child).getName() + '/',
								new ResourceImageResolver(child), ORDER_PROJECT, null);

					} else if (child instanceof IContainer) {
						addProposal(((IContainer) child).getName(), resolver.getParentString() + '/' + ((IContainer) child).getName() + '/',
								new ResourceImageResolver(child), ORDER_FOLDER, null);

					} else {
						addProposal(((IResource) child).getName(), resolver.getParentString() + '/' + ((IResource) child).getName(),
								new ResourceImageResolver(child), ORDER_FILE, null);
					}
				}
			}
		}

		// add '..' proposal if we are not located in a root folder
		if ((matches(resolver.getFilterPart(), "..")) && (showCandidate(".."))) {
			final Object parentFolder = resolver.getParentFolder();

			if ((parentFolder instanceof IResource) && !(parentFolder instanceof IWorkspaceRoot)) {
				addProposal("..", resolver.getParentString() + "../",
						new ResourceImageResolver(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER)), ORDER_FOLDER,
						null);

			} else if ((parentFolder instanceof File) && !(isRootFile((File) parentFolder)) && !(ResourceTools.VIRTUAL_WINDOWS_ROOT.equals(parentFolder))) {
				addProposal("..", resolver.getParentString() + "/../",
						new ResourceImageResolver(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER)), ORDER_FOLDER,
						null);
			}
		}
	}

	protected boolean showCandidate(final Object candidate) {
		// do not show closed projects
		if ((candidate instanceof IProject) && (!((IProject) candidate).isOpen()))
			return false;

		return true;
	}

	/**
	 * Checks if a given file is a root file of the local file system.
	 *
	 * @param file
	 *            file to check
	 * @return <code>true</code> for root files
	 */
	private static boolean isRootFile(File file) {
		try {
			file = file.getCanonicalFile();
		} catch (final IOException e) {
			// could not resolve canonical file, continue with provided file instance
		}

		for (final File rootFile : File.listRoots()) {
			if (rootFile.equals(file))
				return true;
		}

		return false;
	}

	protected static boolean hasFileExtension(final Object candidate, final String extension) {
		if (candidate instanceof File)
			return ((File) candidate).getName().toLowerCase().endsWith("." + extension.toLowerCase());

		else if (candidate instanceof IFile)
			return ((IFile) candidate).getName().toLowerCase().endsWith("." + extension.toLowerCase());

		return false;
	}

	protected static boolean isFileSystemResource(final Object candidate) {
		return ("file:///".equals(candidate)) || (candidate instanceof File);
	}

	protected static boolean isWorkspaceResource(final Object candidate) {
		return ("workspace:///".equals(candidate)) || ("project:///".equals(candidate)) || (candidate instanceof IResource);
	}

	protected static boolean isFile(final Object candidate) {
		return ((candidate instanceof File) && (((File) candidate).isFile())) || (candidate instanceof IFile);
	}

	protected static boolean isFolder(final Object candidate) {
		return ((candidate instanceof File) && (((File) candidate).isDirectory())) || (candidate instanceof IContainer);
	}
}