/*******************************************************************************
 * Copyright (c) 2015 CNES and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     JF Rolland (Atos) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.modules.modeling.ui.exceptions;

/**
 * Exception occuring during matching operation
 * 
 * @author a185132
 *
 */
public class MatcherException extends Exception {

	public MatcherException(String message) {
		super(message);
	}

	private static final long serialVersionUID = 1L;

}
