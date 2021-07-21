/*******************************************************************************
 * Copyright (c) 2021 Christian Pontesegger and others.
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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.ui.completion.provider.ICompletionProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

public class CodeCompletionAggregatorTest {

	private static List<ScriptCompletionProposal> createProposal(String displayString) {
		return Arrays.asList(new ScriptCompletionProposal(null, displayString, null, null, 0, null));
	}

	private ScriptType fScriptType;
	private CodeCompletionAggregator fAggregator;

	@BeforeEach
	public void beforeEach() {
		fScriptType = mock(ScriptType.class);
		when(fScriptType.getName()).thenReturn("TestEngine");

		fAggregator = new CodeCompletionAggregator(null, fScriptType);
	}

	@Test
	@DisplayName("getProposals() contains proposal from local provider")
	public void getProposals_contains_proposal_from_local_provider() {
		final ICompletionProvider localProvider = mock(ICompletionProvider.class);
		when(localProvider.getProposals(ArgumentMatchers.any())).thenReturn(createProposal("test1"));
		when(localProvider.isActive(ArgumentMatchers.any())).thenReturn(true);

		fAggregator.addCompletionProvider(localProvider);

		final List<ScriptCompletionProposal> proposals = fAggregator.getProposals("foo", 0, null);
		assertTrue(proposals.stream().filter(p -> "test1".equals(p.getDisplayString())).findAny().isPresent());
	}

	@Test
	@DisplayName("context for '|'")
	public void context_for_empty_input() {
		final ICompletionContext context = captureContext("");

		assertTrue(context.getTokens().isEmpty());
	}

	@Test
	@DisplayName("context for 'java.|'")
	public void context_for_root_package() {
		final ICompletionContext context = captureContext("java.");

		assertArrayEquals(new Object[] { "java", "." }, context.getTokens().toArray());
	}

	@Test
	@DisplayName("context for 'java.|another'")
	public void context_for_root_package_with_suffix() {
		final ICompletionContext context = captureContext("java.another", 5);

		assertArrayEquals(new Object[] { "java", "." }, context.getTokens().toArray());
	}

	private ICompletionContext captureContext(String input) {
		return captureContext(input, input.length());
	}

	private ICompletionContext captureContext(String input, int offset) {
		final ICompletionProvider localProvider = mock(ICompletionProvider.class);

		final ArgumentCaptor<ICompletionContext> contextCaptor = ArgumentCaptor.forClass(ICompletionContext.class);
		fAggregator.addCompletionProvider(localProvider);

		fAggregator.getProposals(input, offset, null);

		verify(localProvider).isActive(contextCaptor.capture());

		return contextCaptor.getValue();
	}
}
