/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
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

package org.eclipse.ease.ui.completion;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

public class CompletionLabelProvider extends LabelProvider implements ILabelProvider, IStyledLabelProvider {

	@Override
	public String getText(final Object element) {
		if (element instanceof ScriptCompletionProposal)
			return ((ScriptCompletionProposal) element).getDisplayString();

		return super.getText(element);
	}

	@Override
	public Image getImage(final Object element) {
		if (element instanceof ScriptCompletionProposal)
			return ((ScriptCompletionProposal) element).getImage();

		return super.getImage(element);
	}

	@Override
	public StyledString getStyledText(final Object element) {
		if (element instanceof ScriptCompletionProposal)
			return ((ScriptCompletionProposal) element).getStyledDisplayString();

		return null;
	}
}
