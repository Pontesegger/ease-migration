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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.completion.IHelpResolver;
import org.eclipse.ease.ui.completion.IImageResolver;
import org.eclipse.ease.ui.completion.ScriptCompletionProposal;
import org.eclipse.ease.ui.completion.provider.AbstractCompletionProvider;
import org.eclipse.ease.ui.completion.tokenizer.InputTokenizer;
import org.eclipse.ease.ui.completion.tokenizer.TokenList;
import org.eclipse.ease.ui.completions.java.help.handlers.JavaFieldHelpResolver;
import org.eclipse.ease.ui.completions.java.help.handlers.JavaMethodHelpResolver;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;

public class JavaMethodCompletionProvider extends AbstractCompletionProvider {

	@Override
	public boolean isActive(final ICompletionContext context) {
		return super.isActive(context) && isJavaClassContext(context);
	}

	@Override
	protected void prepareProposals(final ICompletionContext context) {
		final boolean isStatic = isStaticClassContext(context);

		final Class<? extends Object> clazz = getJavaClass(context);
		final String filter = context.getFilter();

		for (final Method method : clazz.getMethods()) {
			if ((isStatic) != (Modifier.isStatic(method.getModifiers())))
				continue;

			if (method.getName().startsWith(filter))
				addMethodProposal(method);
		}

		for (final Field field : clazz.getFields()) {
			if ((isStatic) != (Modifier.isStatic(field.getModifiers())))
				continue;

			if (field.getName().startsWith(filter))
				addFieldProposal(field);
		}
	}

	private void addFieldProposal(Field field) {
		final IHelpResolver helpResolver = new JavaFieldHelpResolver(field);

		final StyledString styledString = new StyledString(field.getName() + " : " + field.getType().getSimpleName());
		styledString.append(" - " + field.getDeclaringClass().getSimpleName(), StyledString.QUALIFIER_STYLER);

		final IImageResolver imageResolver = Modifier.isStatic(field.getModifiers())
				? new DescriptorImageResolver(Activator.getImageDescriptor(Activator.PLUGIN_ID, "/icons/eobj16/static_field.png"))
				: new JDTImageResolver(ISharedImages.IMG_FIELD_PUBLIC);

		addProposal(styledString, field.getName(), imageResolver, ScriptCompletionProposal.ORDER_FIELD, helpResolver);
	}

	private void addMethodProposal(Method method) {
		final IHelpResolver helpResolver = new JavaMethodHelpResolver(method);

		final StyledString styledString = new StyledString(method.getName() + "(" + getMethodSignature(method) + ") : " + getMethodReturnType(method));
		styledString.append(" - " + method.getDeclaringClass().getSimpleName(), StyledString.QUALIFIER_STYLER);

		final IImageResolver imageResolver = Modifier.isStatic(method.getModifiers())
				? new DescriptorImageResolver(Activator.getImageDescriptor(Activator.PLUGIN_ID, "/icons/eobj16/static_function.png"))
				: new JDTImageResolver(ISharedImages.IMG_OBJS_PUBLIC);

		if (method.getParameterTypes().length > 0)
			addProposal(styledString, method.getName() + "(", imageResolver, ScriptCompletionProposal.ORDER_METHOD, helpResolver);
		else
			addProposal(styledString, method.getName() + "()", imageResolver, ScriptCompletionProposal.ORDER_METHOD, helpResolver);
	}

	private boolean isJavaClassContext(ICompletionContext context) {
		final TokenList tokens = new TokenList(context.getTokens()).getFromLast(Class.class);
		if (!tokens.isEmpty()) {
			tokens.remove(0);
			tokens.removeIfMatches(0, "()");
			tokens.removeIfMatches(0, ".");

			return (tokens.isEmpty()) || ((tokens.size() == 1) && (!InputTokenizer.isDelimiter(tokens.get(0))));
		}

		return false;
	}

	private boolean isStaticClassContext(ICompletionContext context) {
		final TokenList tokens = new TokenList(context.getTokens()).getFromLast(Class.class);
		return (tokens.size() == 1) || (!"()".equals(tokens.get(1)));
	}

	private Class<?> getJavaClass(ICompletionContext context) {
		return (Class<?>) new TokenList(context.getTokens()).getFromLast(Class.class).get(0);
	}

	private String getMethodReturnType(final Method method) {
		final Class<?> returnType = method.getReturnType();
		return (returnType == null) ? "void" : returnType.getSimpleName();
	}

	private String getMethodSignature(final Method method) {
		final StringBuilder result = new StringBuilder();

		for (final Parameter parameter : method.getParameters()) {
			if (result.length() > 0)
				result.append(", ");

			result.append(parameter.getType().getSimpleName());
			result.append(' ').append(parameter.getName());
		}

		return result.toString();
	}

	public static class JDTImageResolver extends DescriptorImageResolver {

		private final String fImageIdentifier;

		public JDTImageResolver(String imageIdentifier) {
			super();

			fImageIdentifier = imageIdentifier;
		}

		@Override
		protected ImageDescriptor getDescriptor() {
			return getDescriptor(fImageIdentifier);
		}

		public static ImageDescriptor getDescriptor(String imageIdentifier) {
			return JavaUI.getSharedImages().getImageDescriptor(imageIdentifier);
		}
	}
}
