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

import java.io.IOException;

import org.eclipse.ease.helpgenerator.AbstractModuleDoclet;
import org.eclipse.ease.helpgenerator.documentation.ReferenceTokenizer;

public class ModuleLinkCreator implements ILinkCreator {

	@Override
	public String createLink(String reference) throws IOException {
		final ReferenceTokenizer tokenizer = new ReferenceTokenizer(reference);
		if (tokenizer.isModule()) {
			if (tokenizer.isMethod())
				return createMethodLink(tokenizer.getQualifiedName(), tokenizer.getMethod(), tokenizer.getParameters());

			if (tokenizer.isField())
				return createFieldLink(tokenizer.getQualifiedName(), tokenizer.getMethod());

			if (tokenizer.isClass())
				return createClassLink(tokenizer.getQualifiedName());
		}

		throw new IOException("Unknown reference: '" + reference + "'");
	}

	private String createFieldLink(String qualifiedName, String field) {
		final StringBuilder builder = new StringBuilder();

		builder.append("<a href='");
		if (!qualifiedName.isEmpty())
			builder.append(createModuleString(qualifiedName));

		builder.append('#');
		builder.append(field);
		builder.append("'>");

		if (!qualifiedName.isEmpty()) {
			builder.append(ReferenceTokenizer.getSimpleClassName(qualifiedName));
			builder.append('.');
		}

		builder.append(field);
		builder.append("</a>");

		return builder.toString();
	}

	private String createMethodLink(String qualifiedName, String method, String parameters) {
		final StringBuilder builder = new StringBuilder();

		builder.append("<a href='");
		if (!qualifiedName.isEmpty())
			builder.append(createModuleString(qualifiedName));

		builder.append('#');
		builder.append(method);
		builder.append("'>");

		if (!qualifiedName.isEmpty()) {
			builder.append(ReferenceTokenizer.getSimpleClassName(qualifiedName));
			builder.append('.');
		}

		builder.append(method);
		builder.append("()");
		builder.append("</a>");

		return builder.toString();
	}

	private String createClassLink(String qualifiedName) {
		final StringBuilder builder = new StringBuilder();

		builder.append("<a href='");
		builder.append(createModuleString(qualifiedName));
		builder.append("'>");
		builder.append(ReferenceTokenizer.getSimpleClassName(qualifiedName));
		builder.append("</a>");

		return builder.toString();
	}

	private String createModuleString(String qualifiedName) {
		final StringBuilder builder = new StringBuilder();

		builder.append("../../");
		builder.append(ReferenceTokenizer.getPackageName(qualifiedName));
		builder.append("/help/");
		builder.append(AbstractModuleDoclet.createHTMLFileName(qualifiedName));

		return builder.toString();
	}
}
