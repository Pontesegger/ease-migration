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

package org.eclipse.ease.ui.completion;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.ICompletionContext.Type;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

public abstract class AbstractCompletionProvider implements ICompletionProvider {

	public static class DescriptorImageResolver implements IImageResolver {
		private final ImageDescriptor fDescriptor;

		public DescriptorImageResolver() {
			fDescriptor = null;
		}

		public DescriptorImageResolver(ImageDescriptor descriptor) {
			fDescriptor = descriptor;
		}

		@Override
		public Image getImage() {
			return (getDescriptor() != null) ? getDescriptor().createImage() : null;
		}

		protected ImageDescriptor getDescriptor() {
			return fDescriptor;
		}
	}

	private Collection<ScriptCompletionProposal> fProposals = null;
	private ICompletionContext fContext;

	@Override
	public boolean isActive(final ICompletionContext context) {
		return context.getType() != Type.UNKNOWN;
	}

	@Override
	public Collection<? extends ScriptCompletionProposal> getProposals(final ICompletionContext context) {
		fContext = context;
		fProposals = new ArrayList<>();

		prepareProposals(context);

		final Collection<ScriptCompletionProposal> result = fProposals;
		fProposals = null;
		fContext = null;
		return result;
	}

	/**
	 * Get the current context. Only valid during proposal evaluation. Clients may retrieve the content when {@link #prepareProposals(ICompletionContext)} is
	 * called.
	 *
	 * @return the current context or <code>null</code> when proposals are not evaluated
	 */
	public ICompletionContext getContext() {
		return fContext;
	}

	protected void addProposal(final ScriptCompletionProposal proposal) {
		fProposals.add(proposal);
	}

	protected void addProposal(final StyledString displayString, final String replacementString, final IImageResolver imageResolver, final int priority,
			final IHelpResolver helpResolver) {

		fProposals.add(new ScriptCompletionProposal(fContext, displayString, replacementString, imageResolver, priority, helpResolver));
	}

	protected void addProposal(final String displayString, final String replacementString, final IImageResolver imageResolver, final int priority,
			final IHelpResolver helpResolver) {
		
		fProposals.add(new ScriptCompletionProposal(fContext, displayString, replacementString, imageResolver, priority, helpResolver));
	}

	protected boolean matchesFilter(final String proposal) {
		return matches(fContext.getFilter(), proposal);
	}

	protected boolean matchesFilterIgnoreCase(final String proposal) {
		return matchesIgnoreCase(fContext.getFilter(), proposal);
	}

	protected static boolean matches(final String filter, final String proposal) {
		return (filter != null) ? proposal.startsWith(filter) : true;
	}

	protected static boolean matchesIgnoreCase(final String filter, final String proposal) {
		return (filter != null) ? proposal.toLowerCase().startsWith(filter.toLowerCase()) : true;
	}

	protected abstract void prepareProposals(ICompletionContext context);
}
