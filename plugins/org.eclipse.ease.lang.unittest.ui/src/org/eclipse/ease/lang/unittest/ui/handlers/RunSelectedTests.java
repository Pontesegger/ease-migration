/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
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

package org.eclipse.ease.lang.unittest.ui.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ease.lang.unittest.FilteredTestCommand;
import org.eclipse.ease.lang.unittest.runtime.ITestContainer;
import org.eclipse.ease.lang.unittest.runtime.ITestEntity;
import org.eclipse.ease.lang.unittest.runtime.ITestSuite;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class RunSelectedTests extends RunAllTests {

	@Override
	protected Object getTestRoot(ITestSuite testSuite, ExecutionEvent event) {
		final ISelection menuSelection = HandlerUtil.getActiveMenuSelection(event);
		if (menuSelection instanceof IStructuredSelection) {
			final List<ITestEntity> selectedTests = new ArrayList<>();
			for (final Object element : ((IStructuredSelection) menuSelection).toList()) {
				if (element instanceof ITestContainer) {
					selectedTests.add((ITestEntity) element);
					ITestContainer parent = ((ITestContainer) element).getParent();
					while (parent != null) {
						selectedTests.add(parent);
						parent = parent.getParent();
					}
				}
			}

			return new FilteredTestCommand(testSuite, selectedTests);

		} else
			return new FilteredTestCommand(testSuite, Collections.emptySet());
	}
}
