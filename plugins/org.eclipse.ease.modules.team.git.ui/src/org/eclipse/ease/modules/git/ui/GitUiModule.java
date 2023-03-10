/*******************************************************************************
 * Copyright (C) 2016, Max Hohenegger <eclipse@hohenegger.eu>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License_Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ease.modules.git.ui;

import static org.eclipse.ease.modules.ScriptParameter.NULL;
import static org.eclipse.ease.modules.git.ui.PluginConstants.PLUGIN_ID;
import static org.eclipse.jgit.api.ResetCommand.ResetType.HARD;
import static org.eclipse.jgit.lib.Constants.HEAD;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ease.Logger;
import org.eclipse.ease.modules.AbstractScriptModule;
import org.eclipse.ease.modules.ScriptParameter;
import org.eclipse.ease.modules.WrapToScript;
import org.eclipse.egit.core.op.MergeOperation;
import org.eclipse.egit.core.op.RebaseOperation;
import org.eclipse.egit.core.op.ResetOperation;
import org.eclipse.egit.ui.internal.UIRepositoryUtils;
import org.eclipse.egit.ui.internal.branch.BranchOperationUI;
import org.eclipse.egit.ui.internal.repository.tree.RepositoryNode;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jgit.api.MergeCommand.FastForwardMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.swt.widgets.Display;

@SuppressWarnings("restriction") // EGit does not expose RepositoryNode as API
public class GitUiModule extends AbstractScriptModule {

	/**
	 * Checkout the branch with the given name.
	 *
	 * @param repository
	 *            The JGit repository to work with.
	 * @param branchName
	 *            The (possibly abbreviated) name of the branch to checkout.
	 */
	@WrapToScript
	public void checkoutBranch(@ScriptParameter(defaultValue = NULL) Object repository, @ScriptParameter(defaultValue = NULL) final String branchName) {
		try {
			final Ref branchRef = ((Repository) repository).findRef(branchName);
			BranchOperationUI.checkout((Repository) repository, branchRef.getName()).start();
		} catch (final IOException e) {
			Logger.error(PLUGIN_ID, "Failed to checkout branch " + branchName, e);
		}
	}

	/**
	 * Opens a dialog, allowing to create a new branch in the repository.
	 *
	 * @param repository
	 *            The JGit repository to work with.
	 */
	// FIXME currently disabled due to API breakage in egit
	// @WrapToScript
	// public void createBranch(@ScriptParameter(defaultValue = NULL) Object repository) {
	// BranchOperationUI.create((Repository) repository).start();
	// }

	/**
	 * Opens a dialog, allowing to select branches that ought to be removed.
	 *
	 * @param repository
	 *            The JGit repository to work with.
	 */
	// FIXME currently disabled due to API breakage in egit
	// @WrapToScript
	// public void deleteBranch(@ScriptParameter(defaultValue = NULL) Object repository) {
	// BranchOperationUI.delete((Repository) repository).start();
	// }

	/**
	 * Opens a dialog, allowing to select a branch that ought to be renamed.
	 *
	 * @param repository
	 *            The JGit repository to work with.
	 */
	// FIXME currently disabled due to API breakage in egit
	// @WrapToScript
	// public void renameBranch(@ScriptParameter(defaultValue = NULL) Object repository) {
	// BranchOperationUI.rename((Repository) repository).start();
	// }

	/**
	 * Get the JGit repository for the first selected repository node.
	 *
	 * @param selection
	 *            <code>IStructuredSelection</code> containing a <code>RepositoryNode</code> as its first element.
	 * @return The JGit repository.
	 */
	@WrapToScript
	public static Repository getRepository(@ScriptParameter(defaultValue = NULL) ISelection selection) {
		final RepositoryNode repositoryNode = (RepositoryNode) ((IStructuredSelection) selection).getFirstElement();
		return repositoryNode.getObject();
	}

	/**
	 * Opens a dialog, allowing to commit, stash, or reset uncommitted changes.
	 *
	 * @param repository
	 *            The JGit repository to work with.
	 */
	@WrapToScript
	public void handleUncommittedFiles(@ScriptParameter(defaultValue = NULL) final Object repository) {
		Display.getDefault().asyncExec(() -> {
			try {
				UIRepositoryUtils.handleUncommittedFiles((Repository) repository, Display.getDefault().getActiveShell());
			} catch (final GitAPIException e) {
				throw new RuntimeException(e);
			}
		});
	}

	/**
	 * Rebase the current branch onto the <code>HEAD</code> of the specified branch.
	 *
	 * @param repository
	 *            The JGit repository to work with.
	 * @param branchName
	 *            The (possibly abbreviated) name of the branch to rebase on.
	 */
	@WrapToScript
	public void rebaseOn(@ScriptParameter(defaultValue = NULL) final Object repository, @ScriptParameter(defaultValue = NULL) final String branchName) {
		final Job job = new Job("Rebasing on " + branchName) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					final Ref branchRef = ((Repository) repository).findRef(branchName);
					final RebaseOperation op = new RebaseOperation((Repository) repository, branchRef);
					op.execute(monitor);
					if (op.getResult().getStatus().isSuccessful()) {
						return Status.OK_STATUS;
					}
				} catch (final IOException e) {
					return createErrorStatus(e);
				} catch (final CoreException e) {
					return createErrorStatus(e);
				}
				return createErrorStatus("Rebase failed");
			}

		};
		job.setUser(true);
		job.schedule();
	}

	/**
	 * Merges the given branch into the current branch.
	 *
	 * @param repository
	 *            The git repository to work with.
	 * @param branchName
	 *            The (possibly abbreviated) name of the branch to merge.
	 *
	 */
	@WrapToScript
	public void mergeFrom(@ScriptParameter(defaultValue = NULL) final Object repository, @ScriptParameter(defaultValue = NULL) final String branchName) {
		final Job job = new Job("Merging " + branchName) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					final Ref branchRef = ((Repository) repository).findRef(branchName);
					final MergeOperation op = new MergeOperation((Repository) repository, branchRef.getName());
					op.setSquash(false);
					op.setFastForwardMode(FastForwardMode.FF);
					op.execute(monitor);
				} catch (final CoreException e) {
					return e.getStatus();
				} catch (final IOException e) {
					return createErrorStatus(e);
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}

	/**
	 * Resets the current branch hard by the given number of commits.
	 *
	 * @param repository
	 *            The git repository to work with.
	 * @param commits
	 *            The number of commits to go back from HEAD.
	 */
	@WrapToScript
	public void resetCommits(@ScriptParameter(defaultValue = NULL) final Object repository, @ScriptParameter(defaultValue = NULL) final Integer commits) {
		final Job job = new Job("Rebasing " + commits + " commits") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					final ResetOperation resetOperation = new ResetOperation((Repository) repository, HEAD + "~" + commits.toString(), HARD);
					resetOperation.execute(monitor);
				} catch (final CoreException e) {
					return createErrorStatus(e);
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}

	private IStatus createErrorStatus(final Exception e) {
		return new Status(IStatus.ERROR, PLUGIN_ID, e.getMessage(), e);
	}

	private IStatus createErrorStatus(String message) {
		return new Status(IStatus.ERROR, PLUGIN_ID, message);
	}
}
