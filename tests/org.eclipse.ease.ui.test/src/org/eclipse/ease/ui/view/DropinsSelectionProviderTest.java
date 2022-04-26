/*******************************************************************************
 * Copyright (c) 2022 Christian Pontesegger and others.
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

package org.eclipse.ease.ui.view;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.TabFolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DropinsSelectionProviderTest {

	private DropinsSelectionProvider fProvider;

	@BeforeEach
	public void beforeEach() {
		final TabFolder tabFolder = mock(TabFolder.class);
		fProvider = new DropinsSelectionProvider(tabFolder, Collections.emptySet());
	}

	@Test
	@DisplayName("addSelectionChangedListener() registers listener")
	public void addSelectionChangedListener_registers_listener() {
		final ISelectionChangedListener listener = mock(ISelectionChangedListener.class);
		fProvider.addSelectionChangedListener(listener);
		fProvider.selectionChanged(new SelectionChangedEvent(mock(ISelectionProvider.class), mock(ISelection.class)));

		verify(listener).selectionChanged(any());
	}

	@Test
	@DisplayName("removeSelectionChangedListener() unregisters listener")
	public void removeSelectionChangedListener_unregisters_listener() {
		final ISelectionChangedListener listener = mock(ISelectionChangedListener.class);
		fProvider.addSelectionChangedListener(listener);
		fProvider.removeSelectionChangedListener(listener);
		fProvider.selectionChanged(new SelectionChangedEvent(mock(ISelectionProvider.class), mock(ISelection.class)));

		verify(listener, never()).selectionChanged(any());
	}

	@Test
	@DisplayName("getSelection() != null")
	public void getSelection_is_not_null() {
		assertNotNull(fProvider.getSelection());
	}

}
