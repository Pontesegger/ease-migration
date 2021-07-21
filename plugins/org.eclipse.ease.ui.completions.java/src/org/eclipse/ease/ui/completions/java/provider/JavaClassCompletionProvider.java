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

import java.util.Collection;
import java.util.Map.Entry;

import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.ui.completion.IHelpResolver;
import org.eclipse.ease.ui.completion.IImageResolver;
import org.eclipse.ease.ui.completion.ScriptCompletionProposal;
import org.eclipse.ease.ui.completion.provider.AbstractCompletionProvider;
import org.eclipse.ease.ui.completion.tokenizer.InputTokenizer;
import org.eclipse.ease.ui.completion.tokenizer.TokenList;
import org.eclipse.ease.ui.completions.java.help.handlers.JavaClassHelpResolver;
import org.eclipse.ease.ui.completions.java.provider.JavaMethodCompletionProvider.JDTImageResolver;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;

public class JavaClassCompletionProvider extends AbstractCompletionProvider {

	@Override
	public boolean isActive(final ICompletionContext context) {
		return (super.isActive(context) && (isPackage(context))) || (isClass(context))
		// also activate when no package is provided and the first letter is capitalized and we see at least 3 characters
				|| ((context.getFilter().length() >= 3) && (Character.isUpperCase(context.getFilter().charAt(0))));
	}

	private boolean isClass(ICompletionContext context) {
		return new TokenList(context.getTokens()).getFromLast(Class.class).size() == 1;
	}

	private boolean isPackage(ICompletionContext context) {
		final TokenList tokens = new TokenList(context.getTokens()).getFromLast(Package.class);
		if (!tokens.isEmpty()) {
			tokens.remove(0);
			tokens.removeIfMatches(0, ".");

			return (tokens.isEmpty()) || InputTokenizer.isTextFilter(tokens.get(0));
		}

		return false;
	}

	@Override
	protected void prepareProposals(final ICompletionContext context) {

		final String filter = context.getFilter();

		if (isPackage(context)) {
			final Collection<String> candidates = JavaResources.getInstance().getClasses(getPackageName(context));

			for (final String candidate : candidates) {
				if (candidate.startsWith(filter)) {
					final IHelpResolver helpResolver = new JavaClassHelpResolver(getPackageName(context), candidate);
					final IImageResolver imageResolver = new JavaClassImageResolver(getPackageName(context), candidate);

					addProposal(candidate, candidate, imageResolver, ScriptCompletionProposal.ORDER_CLASS, helpResolver);
				}
			}

		} else {
			// no package provided, look in all packages for matching class

			for (final Entry<String, Collection<String>> packageEntry : JavaResources.getInstance().getClasses().entrySet()) {
				for (final String candidate : packageEntry.getValue()) {
					if (candidate.startsWith(filter)) {
						final IHelpResolver helpResolver = new JavaClassHelpResolver(packageEntry.getKey(), candidate);
						final IImageResolver imageResolver = new JavaClassImageResolver(packageEntry.getKey(), candidate);

						final StyledString styledString = new StyledString(candidate);
						styledString.append(" - " + packageEntry.getKey(), StyledString.QUALIFIER_STYLER);

						addProposal(styledString, packageEntry.getKey() + "." + candidate, imageResolver, ScriptCompletionProposal.ORDER_CLASS, helpResolver);
					}
				}
			}
		}
	}

	private String getPackageName(ICompletionContext context) {
		return ((Package) (new TokenList(context.getTokens()).getFromLast(Package.class).get(0))).getName();
	}

	private static final class JavaClassImageResolver extends DescriptorImageResolver {
		private final String fPackageName;
		private final String fClassName;

		private JavaClassImageResolver(final String packageName, final String className) {
			fPackageName = packageName;
			fClassName = className;
		}

		@Override
		protected ImageDescriptor getDescriptor() {
			try {
				final Class<?> clazz = JavaClassCompletionProvider.class.getClassLoader().loadClass(fPackageName + "." + fClassName.replace('.', '$'));
				if (clazz.isEnum())
					return JDTImageResolver.getDescriptor(ISharedImages.IMG_OBJS_ENUM);

				else if (clazz.isInterface())
					return JDTImageResolver.getDescriptor(ISharedImages.IMG_OBJS_INTERFACE);

			} catch (final ClassNotFoundException e) {
				// if we cannot find the class, use the default image
			}

			return JDTImageResolver.getDescriptor(ISharedImages.IMG_OBJS_CLASS);
		}
	}
}
