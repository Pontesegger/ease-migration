package org.eclipse.ease.ui.completions.java.provider;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import org.eclipse.ease.ICompletionContext.Type;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.junit.Test;

public class JavaClassCompletionsProviderTest extends JavaPackagesCompletionsProviderTest {

	@Test
	public void containsGlobalFileCompletion() {
		when(fMockedContext.getType()).thenReturn(Type.NONE);
		when(fMockedContext.getOriginalCode()).thenReturn("Fil");
		when(fMockedContext.getFilter()).thenReturn("Fil");

		IContentProposal javaPackageProposal = null;

		final IContentProposal[] proposals = getProposals();
		for (final IContentProposal proposal : proposals) {
			if ("java.io.File".equals(proposal.getContent()))
				javaPackageProposal = proposal;
		}

		assertNotNull(javaPackageProposal);
	}

	@Test
	public void containsPackageFileCompletion() {
		when(fMockedContext.getType()).thenReturn(Type.PACKAGE);
		when(fMockedContext.getOriginalCode()).thenReturn("java.io.Fil");
		when(fMockedContext.getFilter()).thenReturn("Fil");
		when(fMockedContext.getPackage()).thenReturn("java.io");

		IContentProposal javaPackageProposal = null;

		final IContentProposal[] proposals = getProposals();
		for (final IContentProposal proposal : proposals) {
			if ("java.io.File".equals(proposal.getContent()))
				javaPackageProposal = proposal;
		}

		assertNotNull(javaPackageProposal);
	}
}
