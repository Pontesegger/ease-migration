/*******************************************************************************
 * Copyright (c) 2013 Atos
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Arthur Daussy - initial implementation
 *******************************************************************************/
package org.eclipse.ease.modules.modeling.editors;

import org.eclipse.jface.text.ITextSelection;

public class SelectionRange {

	private final ITextSelection ts;

	public SelectionRange(final ITextSelection ts) {
		super();
		this.ts = ts;
	}

	public Integer getStartingOffset() {
		return new Integer(ts.getOffset());
	}

	public Integer getEndingOffset() {
		return new Integer(ts.getOffset() + ts.getLength());
	}

}
