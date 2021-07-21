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

package org.eclipse.ease.ui.completion.provider;

import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.modules.EnvironmentModule;

public class EnvironmentLocationCompletionProvider extends AbstractFileLocationCompletionProvider {

	@Override
	public boolean isActive(final ICompletionContext context) {
		return super.isActive(context)
				&& (isMethodParameter(context, EnvironmentModule.INCLUDE_METHOD, 0) || isMethodParameter(context, EnvironmentModule.LOAD_JAR_METHOD, 0));
	}

	@Override
	protected boolean showCandidate(final Object candidate) {
		if (isFile(candidate)) {
			if (isMethodParameter(getContext(), EnvironmentModule.INCLUDE_METHOD, 0))
				return hasFileExtension(candidate, getContext().getScriptType().getDefaultExtension());

			else if (isMethodParameter(getContext(), EnvironmentModule.LOAD_JAR_METHOD, 0))
				return hasFileExtension(candidate, "jar");
		}

		return super.showCandidate(candidate);
	}
}
