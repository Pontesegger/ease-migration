/*******************************************************************************
 * Copyright (c) 2019 Christian Pontesegger and others.
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

package org.eclipse.ease.helpgenerator.model;

public class Parameter {

	private Description fComment;
	private final String fName;

	private final String fTypeName;

	private final String fDefaultValue;

	public Parameter(String name, String typeName, String comment, String defaultValue) {
		fName = name;
		fTypeName = typeName;
		fDefaultValue = defaultValue;
		fComment = new Description(comment);
	}

	public String getName() {
		return fName;
	}

	public Description getComment() {
		return fComment;
	}

	public String getTypeName() {
		return fTypeName;
	}

	public String getDefaultValue() {
		return fDefaultValue;
	}

	public void setComment(Description comment) {
		fComment = comment;
	}

	public boolean isOptional() {
		return getDefaultValue() != null;
	}
}
