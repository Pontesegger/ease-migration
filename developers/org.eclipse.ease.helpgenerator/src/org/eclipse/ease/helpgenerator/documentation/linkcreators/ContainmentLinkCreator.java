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
import java.util.ArrayList;
import java.util.List;

public class ContainmentLinkCreator implements ILinkCreator {

	private final List<ILinkCreator> fLinkCreators = new ArrayList<>();

	@Override
	public String createLink(String reference) throws IOException {
		for (final ILinkCreator linkCreator : fLinkCreators) {
			try {
				return linkCreator.createLink(reference);
			} catch (final IOException e) {
				// current linkCreator cannot handle reference, try next one
			}
		}

		throw new IOException("No link creator available to handle '" + reference + "'");
	}

	public void addLinkCreator(ILinkCreator linkCreator) {
		fLinkCreators.add(linkCreator);
	}
}
