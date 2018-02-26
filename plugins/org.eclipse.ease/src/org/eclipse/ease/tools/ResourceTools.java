/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.tools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ease.Activator;
import org.eclipse.ease.Logger;
import org.eclipse.ease.urlhandler.WorkspaceURLConnection;

public final class ResourceTools {

	/**
	 * InputStream delegating all tasks to a base stream. The only method not forwarded is close().
	 */
	public static class NonClosingInputStream extends InputStream {

		private final InputStream fBaseStream;

		public NonClosingInputStream(InputStream baseStream) {
			fBaseStream = baseStream;
		}

		@Override
		public int read() throws IOException {
			return fBaseStream.read();
		}

		@Override
		public int available() throws IOException {
			return fBaseStream.available();
		}

		@Override
		public long skip(long n) throws IOException {
			return fBaseStream.skip(n);
		}

		@Override
		public boolean markSupported() {
			return fBaseStream.markSupported();
		}

		@Override
		public synchronized void mark(int readlimit) {
			fBaseStream.mark(readlimit);
		}

		@Override
		public synchronized void reset() throws IOException {
			fBaseStream.reset();
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			return fBaseStream.read(b, off, len);
		}

		@Override
		public int read(byte[] b) throws IOException {
			return fBaseStream.read(b);
		}
	}

	private static final String PROJECT_SCHEME = "project";
	private static final String PROJECT_PREFIX = PROJECT_SCHEME + "://";

	private static final String WORKSPACE_SCHEME = "workspace";
	private static final String WORKSPACE_PREFIX = WORKSPACE_SCHEME + "://";

	private static final String FILE_SCHEME = "file";
	private static final String FILE_PREFIX = FILE_SCHEME + "://";

	private static final String SCRIPT_SCHEME = "script";

	private static final Pattern WINDOWS_LOCAL_FILE_PATTERN = Pattern.compile("(?:file:///?)?([A-Z]:[/\\\\].*)");
	private static final Pattern WINDOWS_NETWORK_FILE_PATTERN = Pattern.compile("(?:file\\:)?((?://|\\\\\\\\)[^:/][^:]*)");

	/** Virtual file indicating a file system root on windows. Needed as windows does not have a real root file object. */
	public static final Object VIRTUAL_WINDOWS_ROOT = new File("/");

	/**
	 * @deprecated
	 */
	@Deprecated
	private ResourceTools() {
	}

	/**
	 * Resolve to an existing File/IResource/URI/URL. For relative locations the parent object location is taken into account.
	 *
	 * @param location
	 *            location to resolve
	 * @param parent
	 *            parent location to resolve from (needs to be absolute)
	 * @return resource or <code>null</code>
	 */
	public static Object resolve(Object location, Object parent) {
		if ((location instanceof File) || (location instanceof IResource) || (location instanceof URI) || (location instanceof URL))
			return location;

		if ((location == null) || (location.toString().trim().isEmpty())) {
			final Object resolvedParent = resolve(parent);
			if (resolvedParent instanceof IFile)
				return ((IFile) resolvedParent).getParent();

			if ((resolvedParent instanceof File) && (((File) resolvedParent).isFile()))
				return ((File) resolvedParent).getParentFile();

			return resolvedParent;
		}

		final String locationStr = location.toString();
		if (isAbsolute(locationStr))
			return resolve(location);

		// simple checks done, now we need the parent
		parent = resolve(parent);
		if (!exists(parent))
			return null;

		if (parent instanceof IResource) {
			// resolve within workspace

			if (locationStr.startsWith(PROJECT_PREFIX)) {
				// "project://..." location
				final String targetPath = WORKSPACE_PREFIX + ((IResource) parent).getProject().getName() + "/" + locationStr.substring(PROJECT_PREFIX.length());
				return resolve(targetPath);
			}

			// pure relative location
			final IPath parentPath = (parent instanceof IFile) ? ((IResource) parent).getParent().getFullPath() : ((IResource) parent).getFullPath();
			final IPath locationPath = new Path(locationStr);

			IPath targetPath;
			if (locationPath.segmentCount() > parentPath.segmentCount()) {
				// check for special case when relative path steps out of workspace hierarchy
				targetPath = appendPath(parentPath, locationPath);
			} else
				targetPath = parentPath.append(locationPath);

			if (targetPath != null) {
				final Object resolvedTarget = resolve(WORKSPACE_PREFIX + targetPath.makeRelative().toPortableString());
				if (resolvedTarget != null)
					return resolvedTarget;
			}

			// could not resolve within the workspace, try within the file system
			parent = toFile(parent);
		}

		if (parent instanceof File) {
			// resolve within file system
			final Path parentPath = new Path(
					(((File) parent).isFile()) ? ((File) parent).getParentFile().getAbsolutePath() : ((File) parent).getAbsolutePath());

			final IPath targetPath = parentPath.append(new Path(locationStr));
			return targetPath.toFile();
		}

		if (parent instanceof URL) {
			try {
				parent = ((URL) parent).toURI();
			} catch (final URISyntaxException e) {
				// TODO handle this exception (but for now, at least know it happened)
				throw new RuntimeException(e);
			}
		}

		if (parent instanceof URI) {
			return ((URI) parent).resolve(locationStr);
		}

		return null;
	}

	/**
	 * Manually add segment by segment to a path to make sure we do not step back outside of the given parent path. Tries to detect cases like this one:
	 * '/some/path/../../..' which do not get resolved correctly by path.append().
	 *
	 * @param parentPath
	 *            parent path
	 * @param locationPath
	 *            relative path to append
	 * @return appended path or <code>null</code> in case we leave the context
	 */
	private static IPath appendPath(IPath parentPath, IPath locationPath) {
		if (locationPath.segmentCount() > 0) {
			final IPath newParent = parentPath.append(locationPath.segment(0));
			if (parentPath.equals(newParent))
				return null;

			return appendPath(newParent, locationPath.removeFirstSegments(1));

		} else
			return parentPath;
	}

	/**
	 * Check whether a resource exists. Works only for IResource, File, URI, URL objects. Does not try to resolve the given location.
	 *
	 * @param resource
	 *            resource to query for
	 * @return <code>true</code> if resource exists or is of type URI or URL
	 */
	public static boolean exists(Object resource) {
		if (resource instanceof IResource)
			return ((IResource) resource).exists();

		if (resource instanceof File)
			return ((File) resource).exists();

		if ((resource instanceof URI) || (resource instanceof URL))
			return true;

		return false;
	}

	/**
	 * Check if a resource exists and is of type file. Does not resolve the resource.
	 *
	 * @param resource
	 *            {@link File} or {@link IResource} object
	 * @return <code>true</code> when resource is a file
	 */
	public static boolean isFile(Object resource) {
		if (exists(resource)) {
			if (resource instanceof IFile)
				return true;

			if ((resource instanceof File) && (((File) resource).isFile()))
				return true;
		}

		return false;
	}

	/**
	 * Check if a resource exists and is of type folder. For the eclipse workspace this check is also true for the workspace root and project locations. Does
	 * not resolve the resource.
	 *
	 * @param resource
	 *            {@link File} or {@link IResource} object
	 * @return <code>true</code> when resource is a folder
	 */
	public static boolean isFolder(Object resource) {
		if (exists(resource)) {
			if (resource instanceof IContainer)
				return true;

			if ((resource instanceof File) && (((File) resource).isDirectory()))
				return true;
		}

		return false;
	}

	/**
	 * Resolve to an existing File/IResource/URI/URL.
	 *
	 * @param location
	 *            location to resolve
	 * @return resource or <code>null</code>
	 */
	public static Object resolve(Object location) {
		if ((location instanceof File) || (location instanceof IResource) || (location instanceof URI) || (location instanceof URL))
			return location;

		if ((location == null) || (location.toString().trim().isEmpty()))
			return null;

		final String locationStr = location.toString();
		if (isAbsolute(locationStr)) {
			if (locationStr.startsWith(WORKSPACE_PREFIX)) {
				if (WORKSPACE_PREFIX.equals(locationStr))
					return getWorkspace();

				// workspace file
				final IPath path = new Path(locationStr.substring(WORKSPACE_PREFIX.length()));

				if (path.isEmpty())
					// user stepped back into workspace root
					return getWorkspace();

				final IProject project = getWorkspace().getProject(path.segment(0));
				if (project != null) {
					if (path.segmentCount() == 1)
						return project;

					final IFolder folder = project.getFolder(path.removeFirstSegments(1));
					if (folder.exists())
						return folder;

					return project.getFile(path.removeFirstSegments(1));
				}

			} else if (locationStr.startsWith(FILE_PREFIX)) {
				if (isWindows()) {
					Matcher matcher = WINDOWS_LOCAL_FILE_PATTERN.matcher(locationStr);
					if (matcher.matches())
						return new File(matcher.group(1));

					matcher = WINDOWS_NETWORK_FILE_PATTERN.matcher(locationStr);
					if (matcher.matches())
						return new File(matcher.group(1));

					else if (("file:///".equals(locationStr)) || ("file://".equals(locationStr)))
						return VIRTUAL_WINDOWS_ROOT;

					// giving up
					return null;

				} else {
					if (locationStr.startsWith("file:///"))
						return new File(locationStr.substring(7));

					else if (locationStr.startsWith("file://"))
						return new File(locationStr.substring(6));

					else
						return new File(locationStr);
				}

			} else if (locationStr.startsWith(SCRIPT_SCHEME)) {
				// script scheme needs to resolve to URLs to be usable
				try {
					return new URL(locationStr);
				} catch (final MalformedURLException e1) {
					// TODO handle this exception (but for now, at least know it happened)
					throw new RuntimeException(e1);
				}

			} else if (locationStr.contains(":/")) {
				// simple check for URI schemes
				try {
					return createURI(locationStr);

				} catch (final MalformedURLException e) {
					// could be thrown for unknown protocols like svn://

					// fallback, no more escaping of spaces
					return URI.create(locationStr);

				} catch (final URISyntaxException e) {
					// TODO handle this exception (but for now, at least know it happened)
					throw new RuntimeException(e);
				}

			} else {
				// quite likely an absolute path into the file system
				return new File(locationStr);
			}
		}

		return null;
	}

	public static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().startsWith("windows");
	}

	/**
	 * Correctly escapes spaces in URIs.
	 *
	 * @param address
	 *            address to create URI for
	 * @return URI
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public static URI createURI(String address) throws MalformedURLException, URISyntaxException {
		final URL url = new URL(address);
		return new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
	}

	/**
	 * Get the workspace root.
	 *
	 * @return workspace root
	 */
	private static IWorkspaceRoot getWorkspace() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	/**
	 * Verifies if a location is provided in absolute form.
	 *
	 * @param location
	 *            location string
	 * @return <code>true</code> for absolute locations
	 */
	public static boolean isAbsolute(String location) {
		if (location.startsWith(PROJECT_PREFIX))
			return false;

		// simple check for URI style
		if (location.contains(":/"))
			return true;

		// check for windows files
		if (WINDOWS_LOCAL_FILE_PATTERN.matcher(location).matches())
			return true;

		if (WINDOWS_NETWORK_FILE_PATTERN.matcher(location).matches())
			return true;

		return new Path(location).isAbsolute();
	}

	/**
	 * Provides the project relative URI for a given workspace resource. The provided location will be of type 'project://...'.
	 *
	 * @param resource
	 *            resource to get relative URI for.
	 * @return project relative location
	 */
	public static String toProjectRelativeLocation(final IResource resource) {
		return PROJECT_SCHEME + "://" + resource.getProjectRelativePath().toPortableString();
	}

	/**
	 * Resolve a given location to an absolute location URI. When <i>location</i> is not a string, we create an absolute string representation of the given
	 * location.
	 *
	 * @param location
	 *            (relative) location
	 * @param parent
	 *            parent object to resolve from
	 * @return resolved location string or <code>null</code>
	 */
	public static String toAbsoluteLocation(final Object location, final Object parent) {

		// try to resolve file
		final Object file = resolve(location, parent);
		if (file instanceof IResource)
			return WorkspaceURLConnection.SCHEME + ":/" + ((IResource) file).getFullPath().toPortableString();

		else if (file instanceof File) {
			if (isWindows())
				return "file:///" + ((File) file).toString().replaceAll("\\\\", "/");
			else
				return "file://" + ((File) file).toString();
		}

		else if (file != null)
			return file.toString();

		// nothing to resolve, return null
		return null;
	}

	/**
	 * Create a relative path from one resource to another.
	 *
	 * @param resource
	 *            resource to create a relative path for
	 * @param parent
	 *            resource to create relative path from
	 * @return relative path for <i>resource</i>
	 */
	public static String toRelativeLocation(Object resource, Object parent) {
		resource = resolve(resource);
		if (resource == null)
			return null;

		parent = resolve(parent);
		if (parent == null)
			return null;

		if (isFile(parent)) {
			if (parent instanceof IFile)
				parent = ((IFile) parent).getParent();
			else if (parent instanceof File)
				parent = ((File) parent).getParentFile();
		}

		IPath parentPath;
		IPath resourcePath;

		if ((resource instanceof IResource) && (parent instanceof IResource)) {
			// within workspace
			parentPath = ((IResource) parent).getFullPath();
			resourcePath = ((IResource) resource).getFullPath();

		} else {
			// within file system
			resource = toFile(resource);
			parent = toFile(parent);

			try {
				parentPath = new Path(((File) parent).getCanonicalPath());
				resourcePath = new Path(((File) resource).getCanonicalPath());
				if (!parentPath.getDevice().equals(resourcePath.getDevice()))
					// different devices, no relative path possible
					return null;
			} catch (final IOException e) {
				// could not get canonical path, giving up
				return null;
			}
		}

		// concatenate paths
		final int matchingSegments = parentPath.matchingFirstSegments(resourcePath);
		parentPath = parentPath.removeFirstSegments(matchingSegments);
		resourcePath = resourcePath.removeFirstSegments(matchingSegments);

		for (int index = 0; index < parentPath.segmentCount(); index++)
			resourcePath = new Path("..").append(resourcePath);

		return resourcePath.toPortableString();
	}

	/**
	 * Get the content of a resource location as {@link InputStream}.
	 *
	 * @param location
	 *            location to read from
	 * @return {@link InputStream} or <code>null</code>
	 */
	public static InputStream getInputStream(final Object location) {
		if (location instanceof InputStream)
			return (InputStream) location;

		try {
			final Object resource = resolve(location);
			if (resource instanceof IFile)
				return ((IFile) resource).getContents();

			if (resource instanceof File)
				return new FileInputStream((File) resource);

			if (resource instanceof URI)
				return ((URI) resource).toURL().openStream();

			// last resort: try to create a URL directly
			if (location != null)
				return new URL(location.toString()).openStream();

		} catch (final Exception e) {
			// cannot open stream
			Logger.error(Activator.PLUGIN_ID, "Cannot open stream for \"" + location + "\"", e);
		}

		return null;
	}

	/**
	 * Reads from a resource into a string. Does not throw any exceptions, instead returns <code>null</code> in case of errors and logs the error to the system
	 * logger.
	 *
	 * @param location
	 *            location to look up
	 * @return content or <code>null</code> in case of error
	 */
	public static String toString(final Object location) {
		try {
			final InputStream inputStream = new BufferedInputStream(getInputStream(location));
			return StringTools.toString(inputStream);
		} catch (final IOException e) {
			Logger.error(Activator.PLUGIN_ID, "Cannot read from resource \"" + location + "\"", e);
		}

		return null;
	}

	/**
	 * Convert an input stream to a string.
	 *
	 * @param stream
	 *            input string to read from
	 * @return string containing stream data
	 * @throws IOException
	 *             thrown on problems with input stream
	 */
	public static String toString(final InputStream stream) throws IOException {
		if (stream == null)
			return null;

		return toString(new InputStreamReader(stream));
	}

	/**
	 * Read characters from a {@link Reader} and return its string representation. Can be used to convert an {@link InputStream} to a string.
	 *
	 * @param reader
	 *            reader to read from
	 * @return string content of reader
	 * @throws IOException
	 *             when reader is not accessible
	 */
	public static String toString(final Reader reader) throws IOException {
		if (reader == null)
			return null;

		final StringBuffer out = new StringBuffer();

		final char[] buffer = new char[1024];
		int bytes = 0;
		do {
			bytes = reader.read(buffer);
			if (bytes > 0)
				out.append(buffer, 0, bytes);
		} while (bytes != -1);

		return out.toString();
	}

	/**
	 * Get the filesystem representation of a given resource. Does not try to resolve the resource.
	 *
	 * @param resource
	 *            resource to convert. Either a {@link File} or an {@link IResource}
	 * @return file or folder in the local file system
	 */
	public static File toFile(Object resource) {
		if (resource instanceof File)
			return (File) resource;

		if (resource instanceof IResource) {
			// on some projects getRawLocation() returns null
			if (((IResource) resource).getRawLocation() != null)
				return ((IResource) resource).getRawLocation().toFile();

			return ((IResource) resource).getLocation().toFile();
		}

		return null;
	}

	/**
	 * Creates a folder in the workspace if it does not exists already. Also creates any parent folder needed. Requires the project the folder resides to to
	 * exist already.
	 *
	 * @param folder
	 *            folder to be created
	 * @throws CoreException
	 *             when folder could not be created
	 */
	public static void createFolder(IContainer folder) throws CoreException {
		if (!folder.exists()) {
			if (!folder.getParent().exists())
				createFolder(folder.getParent());

			if (folder instanceof IFolder)
				((IFolder) folder).create(true, true, new NullProgressMonitor());
		}
	}
}
