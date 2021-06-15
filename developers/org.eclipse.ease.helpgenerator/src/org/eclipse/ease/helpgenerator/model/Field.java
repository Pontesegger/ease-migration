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

public class Field {

	private Description fComment;
	private final String fName;
	private String fDeprecationMessage;

	public Field(String name, String comment, String deprecationMessage) {
		fName = name;
		fComment = new Description(comment);
		fDeprecationMessage = deprecationMessage;
	}

	public String getName() {
		return fName;
	}

	public Description getComment() {
		return fComment;
	}

	public String getDeprecationMessage() {
		return fDeprecationMessage;
	}

	public boolean isDeprecated() {
		return fDeprecationMessage != null;
	}

	public void setComment(Description comment) {
		fComment = comment;
	}

	public void setDeprecationMessage(String deprecationMessage) {
		fDeprecationMessage = deprecationMessage;
	}
}
