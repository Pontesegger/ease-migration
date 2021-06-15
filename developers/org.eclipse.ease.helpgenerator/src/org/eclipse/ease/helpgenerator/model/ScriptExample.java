/*******************************************************************************
 * Copyright (c) 2019 Christian Pontesegger and others.
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

package org.eclipse.ease.helpgenerator.model;

public class ScriptExample {

	private final String fComment;
	private final String fCode;

	public ScriptExample(String content) {
		if (content.contains("...")) {
			fCode = content.substring(0, content.indexOf("...")).trim();
			fComment = content.substring(content.indexOf("...") + 3).trim();

		} else {
			int pos = content.indexOf('(');
			if (pos > 0) {
				int open = 1;
				for (int index = pos + 1; index < content.length(); index++) {
					if (content.charAt(index) == ')')
						open--;
					else if (content.charAt(index) == '(')
						open++;

					if (open == 0) {
						pos = index + 1;
						break;
					}
				}
				fCode = content.substring(0, pos);
				fComment = content.substring(pos).trim();

			} else {
				fCode = content;
				fComment = "";
			}
		}
	}

	public ScriptExample(String comment, String code) {
		fComment = comment;
		fCode = code;
	}

	public String getComment() {
		return fComment;
	}

	public String getCode() {
		return fCode;
	}
}
