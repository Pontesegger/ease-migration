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

package org.eclipse.ease.ui.completion.provider;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.ui.completion.IHelpResolver;
import org.eclipse.ease.ui.completion.IImageResolver;
import org.eclipse.ease.ui.completion.ScriptCompletionProposal;
import org.eclipse.ease.ui.completion.tokenizer.InputTokenizer;
import org.eclipse.ease.ui.completion.tokenizer.TokenList;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

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

	public static class WorkbenchDescriptorImageResolver extends DescriptorImageResolver {
		private final String fIdentifier;

		public WorkbenchDescriptorImageResolver(String identifier) {
			super();

			fIdentifier = identifier;
		}

		@Override
		protected ImageDescriptor getDescriptor() {
			return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(fIdentifier);
		}
	}

	protected static ModuleDefinition getModuleDefinition(final String identifier) {
		final IScriptService scriptService = PlatformUI.getWorkbench().getService(IScriptService.class);
		return scriptService.getModuleDefinition(identifier);
	}

	private Collection<ScriptCompletionProposal> fProposals = null;
	private ICompletionContext fContext;

	@Override
	public boolean isActive(final ICompletionContext context) {
		return true;
	}

	@Override
	public Collection<ScriptCompletionProposal> getProposals(final ICompletionContext context) {
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

	protected boolean isMethodParameter(ICompletionContext context, Method calledMethod, int parameterIndex) {
		return isMethodParameter(context, calledMethod.getName(), parameterIndex);
	}

	protected boolean isMethodParameter(ICompletionContext context, String calledMethod, int parameterIndex) {
		return isMethodCall(context) && matchesMethodName(context, calledMethod) && isParameterIndex(context, parameterIndex);
	}

	protected boolean isStringParameter(ICompletionContext context) {
		return isMethodCall(context) && context.isStringLiteral(context.getFilterToken());
	}

	private boolean matchesMethodName(ICompletionContext context, String name) {
		final List<Object> tokens = context.getTokens();
		final TokenList candidates = new TokenList(tokens).getFromLast("(");

		if (!candidates.isEmpty())
			return name.equals(tokens.get(tokens.size() - 1 - candidates.size()));

		return false;
	}

	private boolean isMethodCall(ICompletionContext context) {
		final TokenList candidates = new TokenList(context.getTokens()).getFromLast("(");

		if (!candidates.isEmpty()) {
			candidates.removeIfMatches(0, "(");
			candidates.removeAny(",");

			return candidates.isEmpty() || ((candidates.size() == 1) && (InputTokenizer.isTextFilter(candidates.get(0))));
		}

		return false;
	}

	private boolean isParameterIndex(ICompletionContext context, int parameterIndex) {
		final TokenList candidates = new TokenList(context.getTokens()).getFromLast("(");

		if (!candidates.isEmpty()) {
			candidates.removeIfMatches(0, "(");
			return parameterIndex == candidates.stream().filter(t -> ",".equals(t)).count();
		}

		return false;
	}

	protected static boolean matches(final String filter, final String proposal) {
		return (filter != null) ? proposal.startsWith(filter) : true;
	}

	protected static boolean matchesIgnoreCase(final String filter, final String proposal) {
		return (filter != null) ? proposal.toLowerCase().startsWith(filter.toLowerCase()) : true;
	}

	protected abstract void prepareProposals(ICompletionContext context);
}
