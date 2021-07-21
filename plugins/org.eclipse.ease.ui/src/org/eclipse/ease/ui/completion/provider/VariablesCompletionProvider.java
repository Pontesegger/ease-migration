/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
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

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.completion.ScriptCompletionProposal;
import org.eclipse.ease.ui.completion.tokenizer.InputTokenizer;
import org.eclipse.ease.ui.completion.tokenizer.TokenList;
import org.eclipse.jface.viewers.StyledString;

/**
 * Provides completion proposals for variables stored in a script engine.
 */
public class VariablesCompletionProvider extends AbstractCompletionProvider {

	@Override
	public boolean isActive(final ICompletionContext context) {
		return super.isActive(context) && (context.getScriptEngine() != null) && ((context.getTokens().size() <= 1) || (isParameter(context)));
	}

	private boolean isParameter(ICompletionContext context) {
		final TokenList candidates = new TokenList(context.getTokens()).getFromLast("(");

		if (!candidates.isEmpty()) {
			candidates.removeIfMatches(0, "(");
			candidates.removeAny(",");

			return candidates.isEmpty() || ((candidates.size() == 1) && (InputTokenizer.isTextFilter(candidates.get(0))));
		}

		return false;
	}

	@Override
	protected void prepareProposals(final ICompletionContext context) {
		final Collection<Entry<String, Object>> elements = context.getScriptEngine().getVariables().entrySet();

		final List<Entry<String, Object>> filteredEntries = elements.stream().filter(e -> !e.getKey().startsWith(IEnvironment.MODULE_PREFIX))
				.filter(e -> matchesFilterIgnoreCase(e.getKey())).collect(Collectors.toList());

		for (final Entry<String, Object> entry : filteredEntries) {
			final StyledString styledString = new StyledString(entry.getKey());
			styledString.append(String.format(" : %s", getClassName(entry.getValue())), StyledString.DECORATIONS_STYLER);
			styledString.append(" - Variable", StyledString.QUALIFIER_STYLER);

			addProposal(styledString, entry.getKey(), new DescriptorImageResolver(Activator.getLocalImageDescriptor("/icons/eobj16/debug_local_variable.png")),
					ScriptCompletionProposal.ORDER_FIELD, null);
		}
	}

	private String getClassName(Object entry) {
		return (entry == null) ? "null" : entry.getClass().getSimpleName();
	}
}
