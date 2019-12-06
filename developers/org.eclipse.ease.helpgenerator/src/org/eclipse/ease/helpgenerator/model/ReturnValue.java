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

public class ReturnValue extends Parameter {

	public ReturnValue(String typeName, String comment) {
		super(null, typeName, comment, null);
	}

	public boolean isVoid() {
		return "void".equals(getTypeName());
	}
}
