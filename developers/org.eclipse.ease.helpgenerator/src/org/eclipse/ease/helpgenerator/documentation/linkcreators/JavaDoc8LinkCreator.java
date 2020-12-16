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

package org.eclipse.ease.helpgenerator.documentation.linkcreators;

import java.io.IOException;
import java.util.List;

import org.eclipse.ease.helpgenerator.documentation.IClassNameResolver;
import org.eclipse.ease.helpgenerator.documentation.ReferenceTokenizer;

public class JavaDoc8LinkCreator implements ILinkCreator {

	private final String fOnlineReference;
	private final List<String> fSiteContent;
	private final IClassNameResolver fClassNameResolver;

	public JavaDoc8LinkCreator(String onlineReference, List<String> siteContent, IClassNameResolver classNameResolver) {
		fOnlineReference = onlineReference;
		fSiteContent = siteContent;
		fClassNameResolver = classNameResolver;
	}

	@Override
	public String createLink(String reference) throws IOException {
		final ReferenceTokenizer tokenizer = new ReferenceTokenizer(reference, fClassNameResolver);
		if (tokenizer.isLink()) {
			if (containsPackage(ReferenceTokenizer.getPackageName(tokenizer.getQualifiedName()))) {
				if (tokenizer.isMethod())
					return createMethodLink(tokenizer.getQualifiedName(), tokenizer.getMethod(), tokenizer.getParameters());

				if (tokenizer.isField())
					return createFieldLink(tokenizer.getQualifiedName(), tokenizer.getMethod());

				if (tokenizer.isConstructor())
					return createConstructorLink(tokenizer.getQualifiedName(), tokenizer.getParameters());

				if (tokenizer.isClass())
					return createClassLink(tokenizer.getQualifiedName());

			} else
				throw new IOException("Unknown package: '" + reference + "'");
		}

		throw new IOException("Unknown reference: '" + reference + "'");
	}

	private boolean containsPackage(String packageName) {
		return fSiteContent.contains(packageName);
	}

	private String createMethodLink(String qualifiedClassName, String method, String parameters) {
		final StringBuilder builder = createAnchorAndClassString(qualifiedClassName);

		builder.append('#');
		builder.append(method);
		builder.append(getParametersString(parameters));
		builder.append("'>");

		builder.append(ReferenceTokenizer.getSimpleClassName(qualifiedClassName));
		builder.append('.');
		builder.append(method);

		builder.append('(');
		builder.append(parameters);
		builder.append(")</a>");

		return builder.toString();
	}

	private String createConstructorLink(String qualifiedClassName, String parameters) {
		final StringBuilder builder = createAnchorAndClassString(qualifiedClassName);
		builder.append('#');
		builder.append(getConstructorString(qualifiedClassName));
		builder.append(getParametersString(parameters));
		builder.append("'>");

		builder.append("new ");
		builder.append(ReferenceTokenizer.getSimpleClassName(qualifiedClassName));

		builder.append('(');
		builder.append(parameters);
		builder.append(')');
		builder.append("</a>");

		return builder.toString();
	}

	public String createFieldLink(String qualifiedClassName, String field) {
		final StringBuilder builder = createAnchorAndClassString(qualifiedClassName);
		builder.append('#');
		builder.append(field);
		builder.append("'>");
		builder.append(ReferenceTokenizer.getSimpleClassName(qualifiedClassName));
		builder.append('.');
		builder.append(field);
		builder.append("</a>");

		return builder.toString();
	}

	public String createClassLink(String qualifiedClassName) {

		final StringBuilder builder = createAnchorAndClassString(qualifiedClassName);
		builder.append("'>");
		builder.append(ReferenceTokenizer.getSimpleClassName(qualifiedClassName));
		builder.append("</a>");

		return builder.toString();
	}

	protected StringBuilder createAnchorAndClassString(String qualifiedClassName) {
		final String module = getModule(ReferenceTokenizer.getPackageName(qualifiedClassName));

		final StringBuilder builder = new StringBuilder();

		builder.append("<a href='");
		builder.append(fOnlineReference);
		builder.append('/');

		if (!module.isEmpty())
			builder.append(module).append('/');

		builder.append(qualifiedClassName.replaceAll("\\.", "/"));
		builder.append(".html");

		return builder;
	}

	protected String getParametersString(String parameters) {
		final StringBuilder builder = new StringBuilder();

		builder.append('-');
		builder.append(parameters.replaceAll("\\[\\]", ":A").replaceAll(",", "-").replaceAll("\\s", ""));
		builder.append('-');

		return builder.toString();
	}

	protected String getConstructorString(String qualifiedClassName) {
		return ReferenceTokenizer.getSimpleClassName(qualifiedClassName);
	}

	protected String getModule(String packageName) {
		return "";
	}

	protected List<String> getSiteContent() {
		return fSiteContent;
	}
}
