/*******************************************************************************
 * Copyright (c) 2020 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.view;

import org.eclipse.ease.debugging.model.EaseDebugLastExecutionResult;
import org.eclipse.ease.debugging.model.EaseDebugVariable;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;

public class VariablesDragListener implements DragSourceListener {

	private final TreeViewer fTreeViewer;

	public VariablesDragListener(final TreeViewer viewer) {
		fTreeViewer = viewer;
	}

	@Override
	public void dragStart(final DragSourceEvent event) {
		final Object element = getSelection().getFirstElement();

		event.doit = (element instanceof EaseDebugVariable) && !(element instanceof EaseDebugLastExecutionResult);
	}

	@Override
	public void dragSetData(final DragSourceEvent event) {

		if (LocalSelectionTransfer.getTransfer().isSupportedType(event.dataType)) {
			LocalSelectionTransfer.getTransfer().setSelection(getSelection());

		} else if (TextTransfer.getInstance().isSupportedType(event.dataType)) {

			final Object firstElement = getSelection().getFirstElement();
			if (firstElement instanceof EaseDebugVariable)
				event.data = ((EaseDebugVariable) firstElement).getName();
		}
	}

	protected IStructuredSelection getSelection() {
		return fTreeViewer.getStructuredSelection();
	}

	@Override
	public void dragFinished(final DragSourceEvent event) {
		// nothing to do
	}
}
