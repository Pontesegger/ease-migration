/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
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

package org.eclipse.ease.lang.groovy;

import java.util.regex.Pattern;

import org.eclipse.ease.service.IScriptService;
import org.eclipse.ease.service.ScriptService;
import org.eclipse.ease.service.ScriptType;

public final class GroovyHelper {

	/** Script type identifier for JavaScript. Must match with the script type 'name' from plugin.xml. */
	public static final String SCRIPT_TYPE_GROOVY = "Groovy";

	/**
	 * Get the {@link ScriptType} for java script.
	 *
	 * @return script type definition
	 */
	public static ScriptType getScriptType() {
		final IScriptService scriptService = ScriptService.getInstance();
		return scriptService.getAvailableScriptTypes().get(SCRIPT_TYPE_GROOVY);
	}

	public static boolean isSaveName(final String identifier) {
		return Pattern.matches("[a-zA-Z_$][a-zA-Z0-9_$]*", identifier);
	}

	public static String getSaveName(final String identifier) {
		// check if name is already valid
		if (isSaveName(identifier))
			return identifier;

		// not valid, convert string to valid format
		final StringBuilder buffer = new StringBuilder(identifier.replaceAll("[^a-zA-Z0-9]", "_"));

		// remove '_' at the beginning
		while ((buffer.length() > 0) && (buffer.charAt(0) == '_'))
			buffer.deleteCharAt(0);

		// remove trailing '_'
		while ((buffer.length() > 0) && (buffer.charAt(buffer.length() - 1) == '_'))
			buffer.deleteCharAt(buffer.length() - 1);

		// check for valid first character
		if (buffer.length() > 0) {
			final char start = buffer.charAt(0);
			if ((start < 65) || ((start > 90) && (start < 97)) || (start > 122))
				buffer.insert(0, '_');
		} else
			// buffer is empty
			buffer.insert(0, '_');

		return buffer.toString();
	}
}
