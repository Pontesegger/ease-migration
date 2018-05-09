/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.lang.unittest.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ease.lang.unittest.runtime.ITest;
import org.eclipse.ease.lang.unittest.runtime.ITestContainer;
import org.eclipse.ease.lang.unittest.runtime.ITestEntity;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

public class TestSuiteContentProvider extends ArrayContentProvider implements ITreeContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof ITestContainer)
			return ((ITestContainer) inputElement).getCopyOfChildren().toArray();

		return super.getElements(inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ITestContainer) {
			final List<Object> children = new ArrayList<>();

			for (final Object child : ((ITestContainer) parentElement).getCopyOfChildren()) {
				if (!(child instanceof ITest))
					children.add(child);
			}

			return children.toArray();
		}

		return new Object[0];
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof ITestEntity)
			return ((ITestEntity) element).getParent();

		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof ITestContainer) {
			for (final Object child : ((ITestContainer) element).getCopyOfChildren()) {
				if (!(child instanceof ITest))
					return true;
			}
		}

		return false;
	}
}
