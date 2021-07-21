/*******************************************************************************
 * Copyright (c) 2015, 2016 Christian Pontesegger and others.
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

package org.eclipse.ease.ui.completions.java.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.ui.completion.ScriptCompletionProposal;
import org.eclipse.ease.ui.completion.provider.AbstractCompletionProvider;
import org.eclipse.ease.ui.completion.tokenizer.InputTokenizer;
import org.eclipse.ease.ui.completion.tokenizer.TokenList;
import org.eclipse.ease.ui.completions.java.help.handlers.JavaPackageHelpResolver;
import org.eclipse.jdt.ui.ISharedImages;

public class JavaPackagesCompletionProvider extends AbstractCompletionProvider {

	@Override
	public boolean isActive(final ICompletionContext context) {
		return super.isActive(context) && !isCallChain(context);
	}

	private boolean isCallChain(ICompletionContext context) {
		final TokenList tokens = new TokenList(context.getTokens()).getFromLast("()");

		return (!tokens.isEmpty()) && tokens.getFromLast("(").isEmpty();
	}

	@Override
	protected void prepareProposals(final ICompletionContext context) {

		final String packageFilter = getFilter(context);

		for (final String candidate : JavaResources.getInstance().getPackages()) {
			if (isValidCandidate(candidate, packageFilter)) {
				addProposal(candidate, candidate.substring(packageFilter.length() - context.getFilter().length()) + ".",
						new JavaMethodCompletionProvider.JDTImageResolver(ISharedImages.IMG_OBJS_PACKAGE), ScriptCompletionProposal.ORDER_PACKAGE,
						new JavaPackageHelpResolver(candidate));
			}
		}
	}

	private boolean isValidCandidate(String candidate, String packageFilter) {
		if (candidate.startsWith(packageFilter))
			return !candidate.substring(packageFilter.length()).contains(".");

		return false;
	}

	private String getFilter(final ICompletionContext context) {
		final StringBuilder filter = new StringBuilder();

		final List<Object> reversedTokens = new ArrayList<>(context.getTokens());
		Collections.reverse(reversedTokens);

		for (final Object token : reversedTokens) {
			if (token instanceof Package) {
				filter.insert(0, ((Package) token).getName());
				break;

			} else if ((InputTokenizer.isDelimiter(token)) && (!".".equals(token))) {
				break;
			} else if (token instanceof String) {
				filter.insert(0, token);
			} else
				break;
		}

		return filter.toString();
	}
}
