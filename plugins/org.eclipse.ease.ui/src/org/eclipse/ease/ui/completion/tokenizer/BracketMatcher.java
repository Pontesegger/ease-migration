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
import java.util.stream.Collectors;

public class BracketMatcher {

	private static final char OPEN = '(';
	private static final char CLOSE = ')';

	private final List<Bracket> fBrackets;

	public BracketMatcher(String input) {
		fBrackets = new ArrayList<>();

		for (int pos = 0; pos < input.length(); pos++) {
			final char c = input.charAt(pos);
			if (c == OPEN) {
				fBrackets.add(0, new Bracket(pos, -1));

			} else if (c == CLOSE) {
				for (final Bracket bracket : fBrackets) {
					if (bracket.getEnd() == -1) {
						bracket.setEnd(pos);
						break;
					}
				}
			}
		}
	}

	public List<Bracket> getBrackets() {
		return fBrackets;
	}

	public boolean hasOpenBrackets() {
		return getBrackets().stream().anyMatch(b -> b.getEnd() == -1);
	}

	public List<Bracket> getOpenBrackets() {
		return getBrackets().stream().filter(b -> b.getEnd() == -1).collect(Collectors.toList());
	}
}
