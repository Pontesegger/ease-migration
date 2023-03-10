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

package org.eclipse.ease.ui.completion;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.Logger;
import org.eclipse.ease.ui.Activator;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension5;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public class ScriptCompletionProposal
		implements ICompletionProposal, ICompletionProposalExtension5, ICompletionProposalExtension6, IContentProposal, Comparable<ScriptCompletionProposal> {

	public static final int ORDER_HISTORY = 0;
	public static final int ORDER_PACKAGE = 60;
	public static final int ORDER_CLASS = 70;
	public static final int ORDER_FIELD = 80;
	public static final int ORDER_METHOD = 90;
	public static final int ORDER_DEFAULT = 100;

	private final String fDisplayString;
	private final String fReplacementString;
	private final IImageResolver fImageResolver;
	private StyledString fStyledString;
	private final int fSortOrder;

	private final IHelpResolver fHelpResolver;
	private final ICompletionContext fContext;

	// needs IImageResolver as workbench images can only be fetched within the UI thread
	public ScriptCompletionProposal(final ICompletionContext context, final String displayString, final String replacementString,
			final IImageResolver imageResolver, final int sortOrder, final IHelpResolver helpResolver) {
		fContext = context;
		fDisplayString = displayString;
		fReplacementString = replacementString;
		fImageResolver = imageResolver;
		fSortOrder = sortOrder;
		fHelpResolver = helpResolver;
	}

	public ScriptCompletionProposal(final ICompletionContext context, final StyledString styledString, final String replacementString,
			final IImageResolver imageResolver, final int sortOrder, final IHelpResolver helpResolver) {
		this(context, styledString.getString(), replacementString, imageResolver, sortOrder, helpResolver);

		fStyledString = styledString;
	}

	@Override
	public int compareTo(final ScriptCompletionProposal o) {
		final int priority = o.fSortOrder - fSortOrder;
		if (priority != 0)
			return priority;

		return getDisplayString().compareToIgnoreCase(o.getDisplayString());
	}
	// ----------------------------------------------------------------------
	// ICompletionProposal interface implementation (for editor replacements)
	// ----------------------------------------------------------------------

	@Override
	public String getDisplayString() {
		return fDisplayString;
	}

	@Override
	public Image getImage() {
		return (fImageResolver != null) ? fImageResolver.getImage() : null;
	}

	@Override
	public StyledString getStyledDisplayString() {
		return (fStyledString != null) ? fStyledString : new StyledString(getDisplayString());
	}

	@Override
	public void apply(final IDocument document) {
		try {
			document.replace(fContext.getReplaceOffset(), fContext.getReplaceLength(), fReplacementString);

		} catch (final BadLocationException e) {
			Logger.error(Activator.PLUGIN_ID, "Could not insert completion proposal into document", e);
		}
	}

	@Override
	public Point getSelection(final IDocument document) {
		return new Point(fContext.getReplaceOffset() + fReplacementString.length(), 0);
	}

	@Override
	public String getAdditionalProposalInfo() {
		if (fHelpResolver != null) {

			try {
				final FutureTask<String> futureTask = new FutureTask<>(() -> {
					final String htmlHelp = fHelpResolver.resolveHTMLHelp();
					return (htmlHelp != null) ? htmlHelp : fHelpResolver.resolveHelp();
				});

				final ExecutorService executor = Executors.newSingleThreadExecutor();
				executor.execute(futureTask);

				return futureTask.get(500, TimeUnit.MILLISECONDS);

			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				// completion did not finish in time, do not provide help
			}
		}

		return null;
	}

	@Override
	public Object getAdditionalProposalInfo(final IProgressMonitor monitor) {
		return getAdditionalProposalInfo();
	}

	@Override
	public IContextInformation getContextInformation() {
		return null;
	}

	// ------------------------------------------------------------------
	// IContentProposal interface implementation (for shell replacements)
	// ------------------------------------------------------------------
	@Override
	public String getContent() {
		final String prefix = fContext.getText().substring(0, fContext.getReplaceOffset() - fContext.getFilter().length());
		final String suffix = fContext.getText().substring(fContext.getReplaceOffset());

		return prefix + fReplacementString + suffix;
	}

	@Override
	public int getCursorPosition() {
		final String prefix = fContext.getText().substring(0, fContext.getReplaceOffset() - fContext.getFilter().length());

		return (prefix + fReplacementString).length();
	}

	@Override
	public String getLabel() {
		return getDisplayString();
	}

	@Override
	public String getDescription() {
		return getAdditionalProposalInfo();
	}
}
