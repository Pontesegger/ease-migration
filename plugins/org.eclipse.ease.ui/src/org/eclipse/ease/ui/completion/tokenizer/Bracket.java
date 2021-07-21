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

import java.util.Objects;

public class Bracket {
	private final int fStart;
	private int fEnd;

	public Bracket(final int start, final int end) {
		fStart = start;
		fEnd = end;
	}

	public int getStart() {
		return fStart;
	}

	public int getEnd() {
		return fEnd;
	}

	public void setEnd(int end) {
		fEnd = end;
	}

	@Override
	public int hashCode() {
		return Objects.hash(fEnd, fStart);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Bracket other = (Bracket) obj;
		return (fEnd == other.fEnd) && (fStart == other.fStart);
	}

	@Override
	public String toString() {
		return String.format("start: %d, end: %d", getStart(), getEnd());
	}
}
