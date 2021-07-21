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

package org.eclipse.ease.ui.completions.java.provider;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class JavaResourcesTest {

	@BeforeAll
	public static void beforeAll() {
		// preload java packages

		while (!JavaResources.getInstance().getPackages().contains("java.io")) {
			try {
				Thread.sleep(300);
			} catch (final InterruptedException e) {
			}
		}
	}

	@Test
	@DisplayName("singleton exists")
	public void singleton_exists() {
		assertNotNull(JavaResources.getInstance());
	}

	@Test
	@DisplayName("getClasses() is populated")
	public void getClasses_is_populated() {
		assertFalse(JavaResources.getInstance().getClasses().isEmpty());
	}

	@Test
	@DisplayName("getPackages() is populated")
	public void getPackages_is_populated() {
		assertFalse(JavaResources.getInstance().getPackages().isEmpty());
	}

	@Test
	@DisplayName("getClasses(<filter>) is populated")
	public void getClasses_with_filter_is_populated() {
		assertFalse(JavaResources.getInstance().getClasses("java.io").isEmpty());
	}

	@Test
	@DisplayName("getClasses(java.io) contains File class")
	public void getClasses_with_filter_contains_File_class() {
		assertTrue(JavaResources.getInstance().getClasses("java.io").contains("File"));
	}
}
