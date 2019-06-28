/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.javascript;

import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.ui.completion.CompletionContext;

public class JavaScriptCompletionContext extends CompletionContext {

	public JavaScriptCompletionContext(final IScriptEngine scriptEngine) {
		super(scriptEngine, JavaScriptHelper.getScriptType());
	}

	@Override
	protected boolean isLiteral(final char candidate) {
		for (final char literal : "'\"".toCharArray()) {
			if (candidate == literal)
				return true;
		}

		return false;
	}

	@Override
	protected String simplifyCode() {
		final String code = super.simplifyCode();
		if (code.startsWith("Packages."))
			return code.substring("Packages.".length());

		return code;
	}

	@Override
	protected Class<? extends Object> getVariableClazz(final String name) {
		final Class<? extends Object> clazz = super.getVariableClazz(name);

		// skip all rhino specific classes
		if ((clazz != null) && (clazz.getName().startsWith("org.mozilla.javascript")))
			return null;

		return clazz;
	}
}
