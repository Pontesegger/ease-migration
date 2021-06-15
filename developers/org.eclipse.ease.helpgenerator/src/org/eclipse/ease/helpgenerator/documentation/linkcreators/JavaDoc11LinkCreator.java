/*******************************************************************************
 * Copyright (c) 2020 Christian Pontesegger and others.
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

package org.eclipse.ease.helpgenerator.documentation.linkcreators;

import java.util.List;

import org.eclipse.ease.helpgenerator.documentation.IClassNameResolver;

public class JavaDoc11LinkCreator extends JavaDoc8LinkCreator {

	public static boolean isModuleEntry(String candidate) {
		return candidate.startsWith("module:");
	}

	public JavaDoc11LinkCreator(String onlineReference, List<String> siteContent, IClassNameResolver classNameResolver) {
		super(onlineReference, siteContent, classNameResolver);
	}

	@Override
	protected String getModule(String packageName) {
		final int packageIndex = getSiteContent().indexOf(packageName);
		for (int index = packageIndex - 1; index >= 0; index--) {
			if (isModuleEntry(getSiteContent().get(index)))
				return getSiteContent().get(index).substring("module:".length());
		}

		return "";
	}

	@Override
	protected String getParametersString(String parameters) {
		final StringBuilder builder = new StringBuilder();

		builder.append('(');
		builder.append(parameters.replaceAll("\\[\\]", "%5B%5D").replaceAll("\\s", ""));
		builder.append(')');

		return builder.toString();
	}

	@Override
	protected String getConstructorString(String qualifiedClassName) {
		return "%3Cinit%3E";
	}
}
