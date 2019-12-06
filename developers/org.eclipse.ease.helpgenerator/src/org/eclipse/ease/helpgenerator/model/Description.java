/*******************************************************************************
 * Copyright (c) 2019 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.helpgenerator.model;

public class Description {

	private final String fComment;

	public Description(String comment) {
		fComment = comment;
	}

	public String getComment() {
		return fComment;
	}

	public String getFirstSentence() {
		if (isEmpty())
			return null;

		final int pos = getComment().indexOf('.');
		return (pos > 0) ? getComment().substring(0, pos + 1) : getComment();
	}

	@Override
	public String toString() {
		return getComment();
	}

	public boolean isEmpty() {
		return (getComment() == null) || getComment().isEmpty();
	}
}
