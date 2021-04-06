/*******************************************************************************
 * Copyright (c) 2021 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease;

import java.util.concurrent.ExecutionException;

public class ScriptEngineInterruptedException extends ExecutionException {

	private static final long serialVersionUID = 5509227470725409993L;

	public ScriptEngineInterruptedException(InterruptedException e) {
		super(e);
	}
}
