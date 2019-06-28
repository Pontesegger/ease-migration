/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.unittest.ui.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ease.lang.unittest.runtime.ITestEntity;
import org.eclipse.ease.lang.unittest.runtime.TestStatus;
import org.eclipse.ease.lang.unittest.ui.views.UnitTestView;
import org.eclipse.ease.ui.tools.ToggleHandler;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class ToggleTestFilter extends ToggleHandler {

	private class Filter extends ViewerFilter {
		@Override
		public boolean select(final Viewer viewer, final Object parentElement, final Object element) {

			if (element instanceof ITestEntity) {
				final TestStatus status = ((ITestEntity) element).getStatus();
				return (status != TestStatus.PASS);
			}

			return true;
		}

		@Override
		public boolean isFilterProperty(final Object element, final String property) {
			return UnitTestView.TEST_STATUS_PROPERTY.equals(property);
		}
	}

	private Filter fFilter = null;

	@Override
	protected void executeToggle(final ExecutionEvent event, final boolean checked) {
		final UnitTestView view = AbstractViewToolbarHandler.getView(event, UnitTestView.class);
		if (view != null) {

			if (checked) {
				if (fFilter == null)
					fFilter = new Filter();

				view.getFileTreeViewer().addFilter(fFilter);
				view.getTableViewer().addFilter(fFilter);

			} else if (fFilter != null) {
				view.getFileTreeViewer().removeFilter(fFilter);
				view.getTableViewer().removeFilter(fFilter);

				fFilter = null;
			}
		}
	}
}
