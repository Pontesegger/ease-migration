/*******************************************************************************
 * Copyright (c) 2015 Martin Kloesch and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License_Identifier: EPL-2.0
 *
 * Contributors:
 *     Martin Kloesch - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.completion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Logger;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.tools.PlatformExtension;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.completion.provider.ICompletionProvider;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

public class CodeCompletionAggregator implements IContentProposalProvider {

	private static final String SCRIPT_COMPLETION_EXTENSION_POINT = "org.eclipse.ease.ui.codeCompletionProvider";

	private final IScriptEngine fScriptEngine;
	private ScriptType fScriptType;
	private final Object fResource;

	private final List<ICompletionProvider> fStaticCompletionProviders = new ArrayList<>();

	public CodeCompletionAggregator(IScriptEngine scriptEngine) {
		if (scriptEngine == null)
			throw new IllegalArgumentException("scriptEngine cannot be null");

		fScriptEngine = scriptEngine;
		setScriptType(fScriptEngine.getDescription().getSupportedScriptTypes().stream().findAny().orElse(null));
		fResource = null;
	}

	public CodeCompletionAggregator(Object resource, ScriptType scriptType) {
		fScriptEngine = null;
		setScriptType(scriptType);
		fResource = resource;
	}

	private void setScriptType(ScriptType scriptType) {
		if (scriptType == null)
			throw new IllegalArgumentException("scriptType cannot be detected");

		fScriptType = scriptType;
	}

	@Override
	public IContentProposal[] getProposals(String contents, int position) {
		return getProposals(contents, position, new NullProgressMonitor()).toArray(new IContentProposal[0]);
	}

	public List<ScriptCompletionProposal> getProposals(String content, int cursorPosition, IProgressMonitor monitor) {
		final List<ScriptCompletionProposal> proposals = new LinkedList<>();

		final ICompletionContext context = createContext(content, cursorPosition);

		for (final ICompletionProvider provider : getProposalProviders()) {
			try {
				if (provider.isActive(context))
					proposals.addAll(provider.getProposals(context));

			} catch (final Throwable e) {
				Logger.error(Activator.PLUGIN_ID, "Code completion provider failed", e);
			}
		}

		Collections.sort(proposals);

		return proposals;
	}

	private ICompletionContext createContext(String content, int cursorPosition) {
		return fScriptType.getCodeParser().getContext(fScriptEngine, null, content, cursorPosition, 0);
	}

	private List<ICompletionProvider> getProposalProviders() {
		final List<ICompletionProvider> providers = new ArrayList<>();
		providers.addAll(getExtensionProposalProviders());
		providers.addAll(getLocalProposalProviders());
		return providers;
	}

	private List<ICompletionProvider> getLocalProposalProviders() {
		return fStaticCompletionProviders;
	}

	private List<ICompletionProvider> getExtensionProposalProviders() {
		final Collection<PlatformExtension> extensions = PlatformExtension.createForName(SCRIPT_COMPLETION_EXTENSION_POINT, "codeCompletionProvider");

		return extensions.stream().filter(e -> matchesScriptType(e.getAttribute("scriptType"))).map(e -> {
			try {
				return e.createInstance("class", ICompletionProvider.class);
			} catch (CoreException | ClassCastException ex) {
				return null;
			}
		}).filter(p -> p != null).collect(Collectors.toList());
	}

	private boolean matchesScriptType(String extensionScriptType) {
		return (extensionScriptType == null) || extensionScriptType.isEmpty() || extensionScriptType.equals(fScriptType.getName());
	}

	public void addCompletionProvider(ICompletionProvider completionProvider) {
		fStaticCompletionProviders.add(completionProvider);
	}
}
