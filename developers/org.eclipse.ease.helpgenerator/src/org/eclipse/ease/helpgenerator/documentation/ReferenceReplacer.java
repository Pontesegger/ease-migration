/*******************************************************************************
 * Copyright (c) 2020 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.helpgenerator.documentation;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ease.helpgenerator.documentation.linkcreators.ContainmentLinkCreator;
import org.eclipse.ease.helpgenerator.documentation.linkcreators.StaticTextLinkCreator;

public class ReferenceReplacer extends ContainmentLinkCreator implements IClassNameResolver {
	private static final Pattern REFERENCE = Pattern.compile("\\{@(?:link|module)\\s+.*?\\}");
	private IClassNameResolver fClassNameResolver;

	public ReferenceReplacer() {
		setClassNameResolver(className -> className);
	}

	public String replaceReferences(String content) {
		final StringBuffer buffer = new StringBuffer(); // require StringBuffer as Matcher.appendReplacement() does not support StringBuilder in Java 8

		final Matcher matcher = REFERENCE.matcher(content);
		while (matcher.find())
			matcher.appendReplacement(buffer, getReplacement(matcher));

		matcher.appendTail(buffer);

		return buffer.toString();
	}

	private String getReplacement(final Matcher matcher) {
		try {
			return createLink(matcher.group());

		} catch (final IOException e) {
			return new StaticTextLinkCreator().createLink(matcher.group());
		}
	}

	public final void setClassNameResolver(IClassNameResolver classNameResolver) {
		fClassNameResolver = classNameResolver;
	}

	@Override
	public String resolveClassName(String className) {
		return fClassNameResolver.resolveClassName(className);
	}
}
