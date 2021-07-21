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
import org.eclipse.ease.ui.completion.tokenizer.IVariablesResolver;
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
		if (getScriptEngine() != null)
			return new JavaScriptInputTokenizer(v -> {
				final Object variable = getScriptEngine().getVariable(v);
				return (variable == null) ? null : variable.getClass();
			});

		return new JavaScriptInputTokenizer();
	}

	private final class JavaScriptInputTokenizer extends InputTokenizer {

		private static final String PACKAGES_PREFIX = "Packages.";

		private JavaScriptInputTokenizer() {
			super();
		}

		private JavaScriptInputTokenizer(IVariablesResolver variablesResolver) {
			super(variablesResolver);
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

		@Override
		protected boolean isLiteral(final char candidate) {
			return ('"' == candidate) || ('\'' == candidate);
		}
	}
}
