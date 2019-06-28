/*******************************************************************************
 * Copyright (c) 2016 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.modules.git;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.eclipse.ease.modules.AbstractScriptModule;
import org.eclipse.ease.modules.ScriptParameter;
import org.eclipse.ease.modules.WrapToScript;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.RawParseUtils;

/**
 * Provides functions to access and operate on git repositories through JGIT.
 */
public class GitModule extends AbstractScriptModule {

	/**
	 * Clone a git repository.
	 *
	 * @param remoteLocation
	 *            location to fetch repository from
	 * @param localLocation
	 *            local path to be used
	 * @param user
	 *            username for the remote repository
	 * @param pass
	 *            password for the remote repository
	 * @param branch
	 *            branch to checkout (<code>null</code> for all branches)
	 * @return GIT API instance
	 * @throws InvalidRemoteException
	 *             when command was called with an invalid remote
	 * @throws TransportException
	 *             when transport operation failed
	 * @throws GitAPIException
	 *             on a general error during git execution
	 */
	@WrapToScript
	public Git clone(final String remoteLocation, final Object localLocation, @ScriptParameter(defaultValue = ScriptParameter.NULL) final String user,
			@ScriptParameter(defaultValue = ScriptParameter.NULL) final String pass, @ScriptParameter(defaultValue = ScriptParameter.NULL) final String branch)
			throws InvalidRemoteException, TransportException, GitAPIException {

		final Object resource = ResourceTools.resolve(localLocation, getScriptEngine().getExecutedFile());
		final File folder = ResourceTools.toFile(resource);

		if (ResourceTools.isFolder(folder)) {
			final CloneCommand cloneCommand = Git.cloneRepository();
			cloneCommand.setURI(remoteLocation);
			cloneCommand.setDirectory(folder);

			if ((user != null) && (pass != null))
				cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(user, pass));

			if (branch != null)
				cloneCommand.setBranchesToClone(Arrays.asList(branch));

			final Git result = cloneCommand.call();
			addToEGit(result.getRepository().getDirectory());
			return result;

		} else
			throw new RuntimeException("invalid local folder detected: " + localLocation);
	}

	/**
	 * Open a local repository.
	 *
	 * @param location
	 *            local repository root folder
	 * @return GIT API instance
	 * @throws IOException
	 *             when resource cannot be accessed
	 */
	@WrapToScript
	public Git openRepository(final Object location) throws IOException {
		if (location instanceof Git)
			return (Git) location;

		final Object resource = ResourceTools.resolve(location);
		if (resource != null) {
			final File folder = ResourceTools.toFile(resource);

			if (folder != null)
				return Git.open(folder);
		}

		throw new RuntimeException("Invalid folder location: " + location);
	}

	/**
	 * Initialize a fresh repository.
	 *
	 * @param location
	 *            repository location
	 * @param bare
	 *            <code>true</code> for bare repositories
	 * @return GIT API instance
	 * @throws GitAPIException
	 *             on a general error during git execution
	 */
	@WrapToScript
	public Git initRepository(final Object location, @ScriptParameter(defaultValue = "false") final boolean bare) throws GitAPIException {
		final Object resource = ResourceTools.resolve(location, getScriptEngine().getExecutedFile());
		final File folder = ResourceTools.toFile(resource);

		if (folder != null) {
			if (!folder.exists())
				folder.mkdirs();

			final Git result = Git.init().setDirectory(folder).setBare(bare).call();
			addToEGit(result.getRepository().getDirectory());
			return result;

		} else
			throw new RuntimeException("Invalid folder location: " + location);
	}

	/**
	 * Add git repository to EGit repositories view, if available.
	 *
	 * @param directory
	 *            .git folder of a local git repository
	 */
	private static void addToEGit(final File directory) {
		// FIXME needs EGit master branch (4.4) as previously there was no public API available
		// try {
		// org.eclipse.egit.core.Activator.getDefault().getRepositoryUtil().addConfiguredRepository(directory);
		// } catch (NoClassDefFoundError e) {
		// // seems EGit is not available, ignore
		// }
	}

	/**
	 * Commit to a repository.
	 *
	 * @param repository
	 *            repository instance or location (local) to pull
	 * @param message
	 *            commit message
	 * @param author
	 *            author to be used for the commit. Use format 'Real Name &lt;email@address&gt;'
	 * @param amend
	 *            whether to amend the previous commit
	 * @return commit result
	 * @throws IOException
	 *             the repository could not be accessed
	 * @throws GitAPIException
	 *             on a general error during git execution
	 */
	@WrapToScript
	public RevCommit commit(final Object repository, final String message, @ScriptParameter(defaultValue = ScriptParameter.NULL) String author,
			@ScriptParameter(defaultValue = "false") final boolean amend) throws IOException, GitAPIException {

		final Git repo = openRepository(repository);
		if (repo != null)
			return repo.commit().setMessage(message).setAuthor(RawParseUtils.parsePersonIdent((author != null) ? author : "")).setAmend(amend).call();

		else
			throw new RuntimeException("No repository found at: " + repository);
	}

	/**
	 * Get repository status.
	 *
	 * @param repository
	 *            repository instance or location (local) to get status from
	 * @param filepattern
	 *            repository-relative path of file/directory to add (with <code>/</code> as separator)
	 * @return add result
	 * @throws IOException
	 *             when resource cannot be accessed
	 * @throws NoFilepatternException
	 *             when <i>filepattern</i> is empty
	 * @throws GitAPIException
	 *             on a general error during git execution
	 */
	@WrapToScript
	public DirCache add(final Object repository, final String filepattern) throws IOException, NoFilepatternException, GitAPIException {
		final Git repo = openRepository(repository);
		if (repo != null) {
			return repo.add().addFilepattern(filepattern).call();

		} else
			throw new RuntimeException("No repository found at: " + repository);
	}

	/**
	 * Get repository status.
	 *
	 * @param repository
	 *            repository instance or location (local) to get status from
	 * @return repository status
	 * @throws IOException
	 *             when resource cannot be accessed
	 * @throws GitAPIException
	 *             on a general error during git execution
	 */
	@WrapToScript
	public Status getStatus(final Object repository) throws IOException, GitAPIException {
		final Git repo = openRepository(repository);
		if (repo != null) {
			return repo.status().call();

		} else
			throw new RuntimeException("No repository found at: " + repository);
	}

	/**
	 * Push a repository.
	 *
	 * @param repository
	 *            repository instance or location (local) to pull
	 * @return push result
	 * @throws IOException
	 *             when resource cannot be accessed
	 * @throws GitAPIException
	 *             on a general error during git execution
	 */
	@WrapToScript
	public Iterable<PushResult> push(final Object repository) throws IOException, GitAPIException {
		final Git repo = openRepository(repository);
		if (repo != null) {
			return repo.push().call();

		} else
			throw new RuntimeException("No repository found at: " + repository);
	}

	/**
	 * Pull a repository.
	 *
	 * @param repository
	 *            repository instance or location (local) to pull
	 * @return pull result
	 * @throws IOException
	 *             when resource cannot be accessed
	 * @throws GitAPIException
	 *             on a general error during git execution
	 */
	@WrapToScript
	public PullResult pull(final Object repository) throws IOException, GitAPIException {
		final Git repo = openRepository(repository);
		if (repo != null) {
			return repo.pull().call();

		} else
			throw new RuntimeException("No repository found at: " + repository);
	}
}
