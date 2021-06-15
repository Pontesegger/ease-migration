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

		int pos = getComment().indexOf(".");
		while (pos > 0) {
			final String sentence = getComment().substring(0, pos + 1);

			if (sentence.split("\\{@").length == sentence.split("\\}").length)
				return sentence;

			pos = getComment().indexOf(".", pos + 1);
		}

		return getComment();
	}

	@Override
	public String toString() {
		return getComment();
	}

	public boolean isEmpty() {
		return (getComment() == null) || getComment().isEmpty();
	}
}
