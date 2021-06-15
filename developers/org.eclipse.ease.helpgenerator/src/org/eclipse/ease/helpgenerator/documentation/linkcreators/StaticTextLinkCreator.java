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

import org.eclipse.ease.helpgenerator.documentation.ReferenceTokenizer;

public class StaticTextLinkCreator implements ILinkCreator {

	@Override
	public String createLink(String reference) {
		try {
			final ReferenceTokenizer tokenizer = new ReferenceTokenizer(reference, className -> className);

			if (tokenizer.isConstructor())
				return String.format("new %s(%s)", tokenizer.getQualifiedName(), tokenizer.getParameters());

		} catch (final IOException e) {
			// ignore, raw replacement below will do
		}

		return reference.replace("{@module ", "").replace("{@link ", "").replace("}", "").replace('#', '.');
	}
}
