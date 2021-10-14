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

package org.eclipse.ease.lang.javascript;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.ui.completion.BasicContext;
import org.eclipse.ease.ui.completion.tokenizer.IClassResolver;
import org.eclipse.ease.ui.completion.tokenizer.IMethodResolver;
import org.eclipse.ease.ui.completion.tokenizer.InputTokenizer;

public class JavaScriptCompletionContext extends BasicContext {

	public JavaScriptCompletionContext(IScriptEngine scriptEngine, String contents, int position) {
		super(scriptEngine, contents, position);
	}

	public JavaScriptCompletionContext(Object resource, String contents, int position) {
		super(JavaScriptHelper.getScriptType(), resource, contents, position);
	}

	@Override
	protected InputTokenizer getInputTokenizer() {
		return new JavaScriptInputTokenizer(getModuleMethodResolver(), getVariablesResolver());
	}

	@Override
	protected IClassResolver getVariablesResolver() {
		if (getScriptEngine() != null) {
			return v -> {
				if (getScriptEngine().hasVariable(v)) {
					final Object variable = getScriptEngine().getVariable(v);
					if (variable != null)
						return (variable.getClass().getName().startsWith("org.mozilla.javascript")) ? null : variable.getClass();

					return null;
				}

				return null;
			};
		}

		return v -> null;
	}

	private static final class JavaScriptInputTokenizer extends InputTokenizer {

		private static final String PACKAGES_PREFIX = "Packages.";

		private JavaScriptInputTokenizer(IMethodResolver moduleMethodResolver, IClassResolver variablesResolver) {
			super(moduleMethodResolver, variablesResolver);
		}

		@Override
		protected Class<?> getClass(String input) {
			if (input.startsWith(PACKAGES_PREFIX))
				return filterRhinoClasses(super.getClass(input.substring(PACKAGES_PREFIX.length())));

			return filterRhinoClasses(super.getClass(input));
		}

		private Class<?> filterRhinoClasses(Class<?> clazz) {
			if ((clazz != null) && (clazz.getName().startsWith("org.mozilla.javascript")))
				return null;

			return clazz;
		}

		@Override
		protected Package getPackage(String input) {
			if (input.startsWith(PACKAGES_PREFIX))
				return super.getPackage(input.substring(PACKAGES_PREFIX.length()));

			return super.getPackage(input);
		}
	}
}
