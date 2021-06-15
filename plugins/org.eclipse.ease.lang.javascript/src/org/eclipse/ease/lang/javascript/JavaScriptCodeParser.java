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
package org.eclipse.ease.lang.javascript;

import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.ui.completion.AbstractCompletionParser;

public class JavaScriptCodeParser extends AbstractCompletionParser {

	private static final String LINE_COMMENT = "//";
	private static final String BLOCK_COMMENT_START = "/*";
	private static final String BLOCK_COMMENT_END = "*/";

	@Override
	protected String getLineCommentToken() {
		return LINE_COMMENT;
	}

	@Override
	protected String getBlockCommentEndToken() {
		return BLOCK_COMMENT_END;
	}

	@Override
	protected String getBlockCommentStartToken() {
		return BLOCK_COMMENT_START;
	}

	@Override
	protected boolean hasBlockComment() {
		return true;
	}

	@Override
	public ICompletionContext getContext(IScriptEngine scriptEngine, Object resource, String contents, int position, int selectionRange) {
		final JavaScriptCompletionContext context = new JavaScriptCompletionContext(scriptEngine);
		context.calculateContext(resource, contents, position, selectionRange);

		return context;
	}
}
