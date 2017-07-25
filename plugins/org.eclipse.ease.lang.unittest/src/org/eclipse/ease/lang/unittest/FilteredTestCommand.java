/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.lang.unittest;

import java.util.Collection;

import org.eclipse.ease.lang.unittest.runtime.ITestContainer;
import org.eclipse.ease.lang.unittest.runtime.ITestEntity;

public class FilteredTestCommand {

	private final ITestContainer fRoot;
	private final Collection<ITestEntity> fActiveTests;

	public FilteredTestCommand(ITestContainer root, Collection<ITestEntity> activeTests) {
		fRoot = root;
		fActiveTests = activeTests;
	}

	public ITestContainer getTestRoot() {
		return fRoot;
	}

	public Collection<ITestEntity> getActiveTests() {
		return fActiveTests;
	}
}
