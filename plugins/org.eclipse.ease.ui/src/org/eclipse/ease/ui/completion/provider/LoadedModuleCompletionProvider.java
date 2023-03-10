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

import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.completion.ScriptCompletionProposal;
import org.eclipse.ease.ui.completion.tokenizer.TokenList;
import org.eclipse.ease.ui.modules.ui.ModulesTools;
import org.eclipse.jface.viewers.StyledString;

public class LoadedModuleCompletionProvider extends AbstractCompletionProvider {

	@Override
	public boolean isActive(final ICompletionContext context) {
		return super.isActive(context) && isModuleMethodFilter(context);
	}

	private boolean isModuleMethodFilter(ICompletionContext context) {

		TokenList tokensToInvestigate = new TokenList(context.getTokens());
		final TokenList methodCall = tokensToInvestigate.getFromLast("(");

		if (!methodCall.isEmpty()) {
			methodCall.remove(0);
			while (methodCall.removeIfMatches(0, ",")) {
				// repeat
			}

			tokensToInvestigate = methodCall;
		}

		if (tokensToInvestigate.isEmpty())
			return true;

		return (tokensToInvestigate.size() == 1) && (tokensToInvestigate.get(0) instanceof String)
				&& (!context.isStringLiteral(tokensToInvestigate.get(0).toString()));
	}

	@Override
	protected void prepareProposals(final ICompletionContext context) {
		for (final ModuleDefinition definition : context.getLoadedModules()) {
			// field proposals
			definition.getFields().stream().filter(f -> f.getName().startsWith(context.getFilter())).forEach(field -> {
				final StyledString styledString = new StyledString(field.getName());
				styledString.append(" : " + field.getType().getSimpleName(), StyledString.DECORATIONS_STYLER);
				styledString.append(" - " + definition.getName(), StyledString.QUALIFIER_STYLER);

				addProposal(styledString, field.getName(), new DescriptorImageResolver(Activator.getLocalImageDescriptor("/icons/eobj16/field_public_obj.png")),
						ScriptCompletionProposal.ORDER_FIELD, null);
			});

			// method proposals
			definition.getMethods().stream().filter(m -> m.getName().startsWith(context.getFilter())).forEach(method -> {
				final StyledString styledString = ModulesTools.getSignature(method, true);
				styledString.append(" - " + definition.getName(), StyledString.QUALIFIER_STYLER);

				if ((method.getParameterTypes().length - ModulesTools.getOptionalParameterCount(method)) > 0) {
					addProposal(styledString, method.getName() + "(",
							new DescriptorImageResolver(Activator.getLocalImageDescriptor("/icons/eobj16/field_public_obj.png")),
							ScriptCompletionProposal.ORDER_METHOD, null);
				} else {
					addProposal(styledString, method.getName() + "()",
							new DescriptorImageResolver(Activator.getLocalImageDescriptor("/icons/eobj16/field_public_obj.png")),
							ScriptCompletionProposal.ORDER_METHOD, null);
				}
			});
		}
	}
}
