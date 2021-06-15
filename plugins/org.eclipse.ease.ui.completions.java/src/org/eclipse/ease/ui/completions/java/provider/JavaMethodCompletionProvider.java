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
import org.eclipse.ease.ICompletionContext.Type;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.completion.AbstractCompletionProvider;
import org.eclipse.ease.ui.completion.IHelpResolver;
import org.eclipse.ease.ui.completion.IImageResolver;
import org.eclipse.ease.ui.completion.ScriptCompletionProposal;
import org.eclipse.ease.ui.completions.java.help.handlers.JavaFieldHelpResolver;
import org.eclipse.ease.ui.completions.java.help.handlers.JavaMethodHelpResolver;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;

public class JavaMethodCompletionProvider extends AbstractCompletionProvider {

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

	@Override
	public boolean isActive(final ICompletionContext context) {
		return context.getReferredClazz() != null;
	}

	@Override
	protected void prepareProposals(final ICompletionContext context) {
		final Class<? extends Object> clazz = context.getReferredClazz();

		for (final Method method : clazz.getMethods()) {
			if ((context.getType() == Type.STATIC_CLASS) && (!Modifier.isStatic(method.getModifiers())))
				continue;

			if (matchesFilterIgnoreCase(method.getName())) {

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
		}

		for (final Field field : clazz.getFields()) {
			if ((context.getType() == Type.STATIC_CLASS) && (!Modifier.isStatic(field.getModifiers())))
				continue;

			if (matchesFilterIgnoreCase(field.getName())) {
				final IHelpResolver helpResolver = new JavaFieldHelpResolver(field);

				final StyledString styledString = new StyledString(field.getName() + " : " + field.getType().getSimpleName());
				styledString.append(" - " + field.getDeclaringClass().getSimpleName(), StyledString.QUALIFIER_STYLER);

				final IImageResolver imageResolver = Modifier.isStatic(field.getModifiers())
						? new DescriptorImageResolver(Activator.getImageDescriptor(Activator.PLUGIN_ID, "/icons/eobj16/static_field.png"))
						: new JDTImageResolver(ISharedImages.IMG_FIELD_PUBLIC);

				addProposal(styledString, field.getName(), imageResolver, ScriptCompletionProposal.ORDER_FIELD, helpResolver);
			}
		}
	}

	public static String getMethodReturnType(final Method method) {
		final Class<?> returnType = method.getReturnType();
		return (returnType != null) ? returnType.getSimpleName() : "void";
	}

	public static String getMethodSignature(final Method method) {
		final StringBuilder result = new StringBuilder();

		for (final Parameter parameter : method.getParameters()) {
			if (result.length() > 0)
				result.append(", ");

			result.append(parameter.getType().getSimpleName());
			result.append(' ').append(parameter.getName());
		}

		return result.toString();
	}
}
