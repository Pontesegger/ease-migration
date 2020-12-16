/*******************************************************************************
 * Copyright (c) 2020 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.helpgenerator.documentation.linkcreators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ContainmentLinkCreatorTest {

	@Test
	@DisplayName("createLink() throws when no child element is available")
	public void createLinkThrowsWithoutChildElement() {
		assertThrows(IOException.class, () -> new ContainmentLinkCreator().createLink(""));
	}

	@Test
	@DisplayName("createLink() uses child ILinkCreator")
	public void createLinkUsesChildLinkCreator() throws IOException {
		final ContainmentLinkCreator linkCreator = new ContainmentLinkCreator();

		linkCreator.addLinkCreator(reference -> reference);
		assertEquals("test", linkCreator.createLink("test"));
	}

	@Test
	@DisplayName("createLink() succeeds when 1 child creator succeeds")
	public void createLinkSucceedsWhen1ChildSucceeds() throws IOException {
		final ContainmentLinkCreator linkCreator = new ContainmentLinkCreator();

		linkCreator.addLinkCreator(reference -> {
			throw new IOException("not handled");
		});
		linkCreator.addLinkCreator(reference -> reference);
		assertEquals("test", linkCreator.createLink("test"));
	}
}
