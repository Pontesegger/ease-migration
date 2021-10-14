/*******************************************************************************
 * Copyright (c) 2021 Christian Pontesegger and others.
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

package org.eclipse.ease.ui.completion.tokenizer;

import java.util.ArrayList;
import java.util.List;

public class TokenList extends ArrayList<Object> {

	private static final long serialVersionUID = -6713581488474706366L;

	public TokenList() {
		super();
	}

	public TokenList(List<Object> baseList) {
		super(baseList);
	}

	public Object getLastToken() {
		return getFromTail(0);
	}

	public Object getFromTail(int index) {
		if (size() > index)
			return get(size() - index - 1);

		return null;
	}

	public TokenList getFromLast(Class<?> clazz) {
		int startIndex = size();
		for (int index = 0; index < size(); index++) {
			if (clazz.isAssignableFrom(get(index).getClass()))
				startIndex = index;
		}

		return new TokenList(subList(startIndex, size()));
	}

	public TokenList getFromLast(String needle) {
		int startIndex = size();
		for (int index = 0; index < size(); index++) {
			if (needle.equals(get(index)))
				startIndex = index;
		}

		return new TokenList(subList(startIndex, size()));
	}

	public <T> T getLast(Class<T> clazz) {
		final TokenList remainingTokens = getFromLast(clazz);
		return remainingTokens.isEmpty() ? null : (T) getFromLast(clazz).get(0);
	}

	public boolean removeIfMatches(int index, String expected) {
		if ((size() > index) && (expected.equals(get(index)))) {
			remove(index);
			return true;
		}

		return false;
	}

	public void removeAny(String needle) {
		for (final Object element : new ArrayList<>(this)) {
			if (needle.equals(element))
				remove(element);
		}
	}
}
