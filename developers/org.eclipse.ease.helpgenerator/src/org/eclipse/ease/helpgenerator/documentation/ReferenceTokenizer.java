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
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReferenceTokenizer {

	private static final Pattern PATTERN_REFERENCE = Pattern.compile("\\{@(link|module)\\s+(.*?)(?:#([\\p{Alpha}-_]*)(?:\\((.*?)\\))?)?}");

	public static String getSimpleClassName(String qualifiedClassName) {
		return (qualifiedClassName.contains(".")) ? qualifiedClassName.substring(qualifiedClassName.lastIndexOf('.') + 1) : qualifiedClassName;
	}

	public static String getPackageName(String qualifiedName) {
		return (qualifiedName.contains(".")) ? qualifiedName.substring(0, qualifiedName.lastIndexOf('.')) : "";
	}

	private String fType;

	private String fQualifiedName;

	private String fMethod;

	private String fParameters;

	public ReferenceTokenizer(String reference, IClassNameResolver classNameResolver) throws IOException {
		final Matcher matcher = PATTERN_REFERENCE.matcher(reference);
		if (matcher.matches()) {
			fType = matcher.group(1);
			fQualifiedName = classNameResolver.resolveClassName(matcher.group(2));
			fMethod = matcher.group(3);
			fParameters = matcher.group(4);

		} else
			throw new IOException("invalid reference detected: \"" + reference + "\"");
	}

	public ReferenceTokenizer(String reference) throws IOException {
		this(reference, name -> name);
	}

	public boolean isLink() {
		return "link".equals(fType);
	}

	public boolean isModule() {
		return "module".equals(fType);
	}

	public String getQualifiedName() {
		return fQualifiedName;
	}

	public String getMethod() {
		return fMethod;
	}

	public String getParameters() {
		return fParameters;
	}

	public boolean isConstructor() {
		return isLink() && (getMethod() != null) && Objects.equals(getMethod(), getSimpleClassName(getQualifiedName()));
	}

	public boolean isMethod() {
		return (getMethod() != null) && (getParameters() != null) && !isConstructor();
	}

	public boolean isField() {
		return (getMethod() != null) && (getParameters() == null);
	}

	public boolean isClass() {
		return (getMethod() == null);
	}
}
