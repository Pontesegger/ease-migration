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

package org.eclipse.ease.lang.javascript.ui.completion;

import org.eclipse.ease.ui.completion.ScriptCompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.wst.jsdt.ui.text.java.AbstractProposalSorter;

public class JavaScriptProposalSorter extends AbstractProposalSorter {

	@Override
	public int compare(final ICompletionProposal proposal1, final ICompletionProposal proposal2) {
		if ((proposal1 instanceof ScriptCompletionProposal) && (proposal1 instanceof ScriptCompletionProposal))
			return ((ScriptCompletionProposal) proposal1).compareTo((ScriptCompletionProposal) proposal2);

		return proposal1.getDisplayString().compareToIgnoreCase(proposal2.getDisplayString());
	}
}
