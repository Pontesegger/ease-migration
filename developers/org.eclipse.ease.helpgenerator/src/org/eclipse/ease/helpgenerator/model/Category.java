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

import org.eclipse.ease.helpgenerator.IMemento;

public class Category {

	private final IMemento fDefinition;

	public Category(IMemento definition) {
		fDefinition = definition;
	}

	public String getName() {
		return fDefinition.getString("name");
	}

	public String getHelpLink() {
		return createCategoryLink(fDefinition.getString("parent"));
	}

	public String getFileName() {
		return createCategoryFileName(fDefinition.getString("id"));
	}

	public static String createCategoryLink(final String categoryId) {
		String pluginID = "org.eclipse.ease.help";
		if (categoryId != null) {
			final int index = categoryId.indexOf(".category.");
			if (index != -1)
				pluginID = categoryId.substring(0, index);
		}

		return "../" + pluginID + "/help/" + createCategoryFileName(categoryId) + "#modules_anchor";
	}

	public static String createCategoryFileName(final String categoryId) {
		final String category = extractCategoryName(categoryId);
		return (category != null) ? "category_" + category + ".xml" : "reference.xml";
	}

	private static String extractCategoryName(final String categoryId) {
		if (categoryId != null) {
			final int index = categoryId.indexOf(".category.");
			if (index != -1)
				return categoryId.substring(index + ".category.".length());
		}

		return null;
	}
}
