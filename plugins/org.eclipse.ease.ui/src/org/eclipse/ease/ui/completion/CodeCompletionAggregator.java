/*******************************************************************************
 * Copyright (c) 2015 Martin Kloesch and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Martin Kloesch - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.completion;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ease.ICodeParser;
import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Logger;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.ui.Activator;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * Dispatcher class create code completion proposals.
 *
 * First checks all registered {@link ICompletionProvider} objects to get the {@link ICompletionContext} for the desired line.
 *
 * Then uses all registered {@link ICompletionProvider} objects to calculate the {@link IContentProposal} array for {@link #getProposals(String, int)}.
 *
 * TODO: Refactor to use multi-threading.
 *
 * @author Martin Kloesch
 *
 */
public class CodeCompletionAggregator implements IContentProposalProvider {

	/**
	 * String constant for codeCompletionProvider extension point.
	 */
	public static final String COMPLETION_PROCESSOR = "org.eclipse.ease.ui.codeCompletionProvider";

	/**
	 * String constant for script type attribute of codeCompletionProvider extension.
	 */
	public static final String ATTRIBUTE_SCRIPT_TYPE = "scriptType";

	/**
	 * String constant for class attribute of codeCompletionProvider extension.
	 */
	public static final String ATTRIBUTE_CLASS = "class";

	/** Timeout for completion processor. */
	private static final long COMPLETION_TIMEOUT = 500;

	/**
	 * Retrieve all {@link ICompletionProvider}s matching a given script type.
	 *
	 * @param scriptType
	 *            script type filter for code completion proposal providers. May be <code>null</code> to get all providers.
	 *
	 * @return list of all matching {@link ICompletionProvider}s.
	 */
	private static Collection<ICompletionProvider> getProviders(final String scriptType) {
		final Collection<ICompletionProvider> providers = new HashSet<>();

		final IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(COMPLETION_PROCESSOR);
		for (final IConfigurationElement element : elements) {

			if (scriptType != null) {
				final String registeredType = element.getAttribute(ATTRIBUTE_SCRIPT_TYPE);
				if ((registeredType != null) && (!registeredType.isEmpty()) && (!scriptType.equals(registeredType)))
					// ignore as script type does not match
					continue;
			}

			try {
				final Object candidate = element.createExecutableExtension(ATTRIBUTE_CLASS);
				if (candidate instanceof ICompletionProvider)
					// register provider
					providers.add((ICompletionProvider) candidate);

			} catch (final CoreException e) {
				Logger.error(Activator.PLUGIN_ID, "Invalid completion provider detected in " + element.getContributor().getName(), e);
			}
		}

		return providers;
	}

	/**
	 * Static code analyzer to split given line of code to base {@link ICompletionContext} for {@link ICompletionProvider#refineContext(ICompletionContext)}.
	 */
	private ICodeParser fCodeParser;

	/** All registered {@link ICompletionProvider}s. */
	private Collection<ICompletionProvider> fCompletionProviders = Collections.emptySet();

	/** Manually added {@link ICompletionProvider}s. */
	private final Collection<ICompletionProvider> fAddedCompletionProviders = new HashSet<>();

	/** Registered script engine. */
	private IScriptEngine fScriptEngine;

	/**
	 * Setter method for ICompletionAnalyzer.
	 *
	 * @param codeParser
	 *            {@link ICodeParser} for completion calculation.
	 */
	public void setCodeParser(final ICodeParser codeParser) {
		fCodeParser = codeParser;
	}

	public char[] getActivationChars() {
		return new char[] { '.' };
	}

	/**
	 * Sets the given script engine for all registered completion providers.
	 *
	 * @param scriptEngine
	 *            {@link IScriptEngine} to be set.
	 */
	public void setScriptEngine(final IScriptEngine scriptEngine) {
		fScriptEngine = scriptEngine;

		if (fScriptEngine != null) {
			final List<ScriptType> supportedScriptTypes = fScriptEngine.getDescription().getSupportedScriptTypes();
			if (supportedScriptTypes.size() > 0) {
				setScriptType(supportedScriptTypes.get(0));
			}
		}
	}

	public void setScriptType(final ScriptType scriptType) {
		setCodeParser(scriptType.getCodeParser());
		fCompletionProviders = getProviders(scriptType.getName());
		fCompletionProviders.addAll(fAddedCompletionProviders);
	}

	public void addCompletionProvider(ICompletionProvider completionProvider) {
		fAddedCompletionProviders.add(completionProvider);
	}

	/**
	 * Calculate relevant completion proposals.
	 *
	 * @param resource
	 *            resource that contains relevantText. May be <code>null</code>
	 * @param relevantText
	 *            text that is relevant for completion calculation
	 * @param insertOffset
	 *            cursor position within relevatText
	 * @param selectionRange
	 * @param monitor
	 *            job monitor for calculation termination
	 * @return
	 */
	public List<ICompletionProposal> getCompletionProposals(final Object resource, final String relevantText, final int insertOffset, final int selectionRange,
			final IProgressMonitor monitor) {

		final LinkedList<ICompletionProposal> proposals = new LinkedList<>();
		final Job completionProcessorJob = new Job("Calculate EASE code completions") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;

					final ICompletionContext context = createContext(resource, relevantText, insertOffset, selectionRange);

					if (context != null) {
						for (final ICompletionProvider provider : fCompletionProviders) {
							try {
								if (monitor.isCanceled())
									return Status.CANCEL_STATUS;

								if (provider.isActive(context))
									proposals.addAll(provider.getProposals(context));

							} catch (final Throwable ex) {
								Logger.error(Activator.PLUGIN_ID, "Could not get proposals from ICompletionProvider <" + provider.getClass().getName() + ">",
										ex);
							}
						}
					}

				} finally {
					synchronized (this) {
						notifyAll();
					}
				}

				return Status.OK_STATUS;
			}
		};

		completionProcessorJob.schedule();

		synchronized (completionProcessorJob) {
			try {
				completionProcessorJob.wait(COMPLETION_TIMEOUT);
			} catch (final InterruptedException e) {
			}
		}

		return proposals;
	}

	private ICompletionContext createContext(final Object resource, final String relevantText, final int insertOffset, final int selectionRange) {
		if (fCodeParser != null)
			return fCodeParser.getContext(fScriptEngine, resource, relevantText, insertOffset, selectionRange);

		return null;
	}

	@Override
	public IContentProposal[] getProposals(final String contents, final int position) {
		final List<ICompletionProposal> proposals = getCompletionProposals(null, contents, position, 0, null);
		Collections.sort(proposals, (o1, o2) -> {
			if ((o1 instanceof ScriptCompletionProposal) && (o2 instanceof ScriptCompletionProposal))
				return ((ScriptCompletionProposal) o1).compareTo((ScriptCompletionProposal) o2);

			return o1.getDisplayString().compareTo(o2.getDisplayString());
		});

		return proposals.toArray(new IContentProposal[proposals.size()]);
	}
}
