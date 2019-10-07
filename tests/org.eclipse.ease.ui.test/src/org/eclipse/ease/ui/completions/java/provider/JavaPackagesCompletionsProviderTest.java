package org.eclipse.ease.ui.completions.java.provider;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.ease.ICodeParser;
import org.eclipse.ease.ICompletionContext;
import org.eclipse.ease.ICompletionContext.Type;
import org.eclipse.ease.service.ScriptType;
import org.eclipse.ease.ui.completion.CodeCompletionAggregator;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class JavaPackagesCompletionsProviderTest {
	private static final int REPETITIONS = 10;

	protected ICompletionContext fMockedContext;
	protected CodeCompletionAggregator fAggregator;

	@BeforeClass
	public static void classSetup() throws InterruptedException {
		final ICodeParser mockedParser = mock(ICodeParser.class);
		final ICompletionContext mockedContext = mock(ICompletionContext.class);
		final ScriptType mockedScriptType = mock(ScriptType.class);
		when(mockedScriptType.getName()).thenReturn("JavaScript");
		when(mockedScriptType.getCodeParser()).thenReturn(mockedParser);

		when(mockedParser.getContext(any(), any(), any(), anyInt(), anyInt())).thenReturn(mockedContext);

		final CodeCompletionAggregator aggregator = new CodeCompletionAggregator();
		aggregator.setScriptType(mockedScriptType);

		when(mockedContext.getType()).thenReturn(Type.NONE);
		when(mockedContext.getOriginalCode()).thenReturn("");
		when(mockedContext.getFilter()).thenReturn("");

		// allow to lazy load class definitions
		aggregator.getProposals("", 0);
		Thread.sleep(5000);
	}

	@Before
	public void setup() {
		final ICodeParser mockedParser = mock(ICodeParser.class);
		fMockedContext = mock(ICompletionContext.class);
		final ScriptType mockedScriptType = mock(ScriptType.class);
		when(mockedScriptType.getName()).thenReturn("JavaScript");
		when(mockedScriptType.getCodeParser()).thenReturn(mockedParser);

		when(mockedParser.getContext(any(), any(), any(), anyInt(), anyInt())).thenReturn(fMockedContext);

		fAggregator = new CodeCompletionAggregator();
		fAggregator.setScriptType(mockedScriptType);
	}

	@Test
	public void containsJavaRootPackageProposal() {
		when(fMockedContext.getType()).thenReturn(Type.NONE);
		when(fMockedContext.getOriginalCode()).thenReturn("");
		when(fMockedContext.getFilter()).thenReturn("");

		IContentProposal javaPackageProposal = null;

		final IContentProposal[] proposals = getProposals();
		for (final IContentProposal proposal : proposals) {
			if ("java.".equals(proposal.getContent()))
				javaPackageProposal = proposal;
		}

		assertNotNull(javaPackageProposal);
	}

	@Test
	public void containsOrgRootPackageProposal() {
		when(fMockedContext.getType()).thenReturn(Type.NONE);
		when(fMockedContext.getOriginalCode()).thenReturn("");
		when(fMockedContext.getFilter()).thenReturn("");

		IContentProposal orgPackageProposal = null;

		final IContentProposal[] proposals = getProposals();
		for (final IContentProposal proposal : proposals) {
			if ("org.".equals(proposal.getContent()))
				orgPackageProposal = proposal;
		}

		assertNotNull(orgPackageProposal);
	}

	@Test
	public void containsJavaLangPackageProposal() {
		when(fMockedContext.getType()).thenReturn(Type.PACKAGE);
		when(fMockedContext.getPackage()).thenReturn("java");
		when(fMockedContext.getOriginalCode()).thenReturn("java.");
		when(fMockedContext.getFilter()).thenReturn("");

		IContentProposal javaLangPackageProposal = null;

		final IContentProposal[] proposals = getProposals();
		for (final IContentProposal proposal : proposals) {
			if ("java.lang.".equals(proposal.getContent()))
				javaLangPackageProposal = proposal;
		}

		assertNotNull(javaLangPackageProposal);
	}

	@Test
	public void containsOrgEclipsePackageProposal() {
		when(fMockedContext.getType()).thenReturn(Type.PACKAGE);
		when(fMockedContext.getPackage()).thenReturn("org");
		when(fMockedContext.getOriginalCode()).thenReturn("org.");
		when(fMockedContext.getFilter()).thenReturn("");

		IContentProposal orgEclipsePackageProposal = null;

		final IContentProposal[] proposals = getProposals();
		for (final IContentProposal proposal : proposals) {
			if ("org.eclipse.".equals(proposal.getContent()))
				orgEclipsePackageProposal = proposal;
		}

		assertNotNull(orgEclipsePackageProposal);
	}

	protected IContentProposal[] getProposals() {
		for (int repetition = 0; repetition < REPETITIONS; repetition++) {
			final IContentProposal[] proposals = fAggregator.getProposals("", 0);
			if (proposals.length > 0)
				return proposals;
		}

		throw new RuntimeException("No proposals found");
	}
}
