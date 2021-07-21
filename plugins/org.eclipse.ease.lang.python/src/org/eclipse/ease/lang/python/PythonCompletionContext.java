/*******************************************************************************
 * Copyright (c) 2016 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License_Identifier: EPL-2.0
 *
 * Contributors:
 *     Jonah Graham (Kichwa Coders) - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.python;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.ui.completion.BasicContext;
import org.eclipse.ease.ui.completion.tokenizer.IVariablesResolver;
import org.eclipse.ease.ui.completion.tokenizer.InputTokenizer;

public class PythonCompletionContext extends BasicContext {

	public PythonCompletionContext(IScriptEngine scriptEngine, String contents, int position) {
		super(scriptEngine, contents, position);
	}

	public PythonCompletionContext(Object resource, String contents, int position) {
		super(PythonHelper.getScriptType(), resource, contents, position);
	}

	// @Override
	// protected String simplifyCode() {
	// final String code = super.simplifyCode();
	//
	// // XXX: API needs a review here, these simplifications are Py4J specific, not
	// // all Python code.
	// if (code.startsWith("jvm.gateway."))
	// return code.substring("jvm.gateway.".length());
	// if (code.startsWith("gateway."))
	// return code.substring("gateway.".length());
	//
	// return code;
	// }

	@Override
	protected InputTokenizer getInputTokenizer() {
		if (getScriptEngine() != null)
			return new PythonInputTokenizer(v -> {
				final Object variable = getScriptEngine().getVariable(v);
				return variable == null ? null : variable.getClass();
			});

		return new PythonInputTokenizer();
	}

	private final class PythonInputTokenizer extends InputTokenizer {

		private PythonInputTokenizer() {
			super();
		}

		private PythonInputTokenizer(IVariablesResolver variablesResolver) {
			super(variablesResolver);
		}

		@Override
		protected boolean isLiteral(final char candidate) {
			return ('"' == candidate) || ('\'' == candidate);
		}
	}
}
