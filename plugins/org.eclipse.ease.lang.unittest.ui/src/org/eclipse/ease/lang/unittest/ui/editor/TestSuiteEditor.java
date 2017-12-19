/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.unittest.ui.editor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ease.Logger;
import org.eclipse.ease.lang.unittest.UnitTestHelper;
import org.eclipse.ease.lang.unittest.definition.ITestSuiteDefinition;
import org.eclipse.ease.lang.unittest.definition.provider.DefinitionItemProviderAdapterFactory;
import org.eclipse.ease.lang.unittest.ui.Activator;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.part.FileEditorInput;

public class TestSuiteEditor extends FormEditor implements IEditingDomainProvider {

	public static final String EDITOR_ID = "org.eclipse.ease.editor.suiteEditor";

	public static final String OVERVIEW_PAGE = "Overview";

	public static final String TEST_SELECTION_PAGE = "Test Selection";

	public static final String VARIABLES_PAGE = "Variables";

	public static final String CUSTOM_CODE_PAGE = "Custom Code";

	private boolean fDirty = false;

	/** EMF editing domain for testsuite model. */
	private AdapterFactoryEditingDomain fEditingDomain;

	/** Loaded testsuite model from file. */
	private ITestSuiteDefinition fTestSuite = null;

	private LocalResourceManager fResourceManager;

	private ComposedAdapterFactory fAdapterFactory;

	public TestSuiteEditor() {
		initializeEditingDomain();
		addChangeListener();
	}

	private void initializeEditingDomain() {
		// Create an adapter factory that yields item providers.
		fAdapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);

		fAdapterFactory.addAdapterFactory(new ResourceItemProviderAdapterFactory());
		fAdapterFactory.addAdapterFactory(new DefinitionItemProviderAdapterFactory());
		fAdapterFactory.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());

		// Create the command stack that will notify this editor as commands are executed.
		final BasicCommandStack commandStack = new BasicCommandStack();

		// Create the editing domain with a special command stack.
		fEditingDomain = new AdapterFactoryEditingDomain(fAdapterFactory, commandStack, new HashMap<Resource, Boolean>());
	}

	private void addChangeListener() {
		fEditingDomain.getCommandStack().addCommandStackListener(event -> {
			fDirty = true;
			firePropertyChange(IEditorPart.PROP_DIRTY);
		});
	}

	@Override
	public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
		super.init(site, input);

		fResourceManager = new LocalResourceManager(JFaceResources.getResources());

		try {
			final InputStream content = getInputContent();
			fTestSuite = UnitTestHelper.loadTestSuite(content);
			content.close();

			fTestSuite.setResource(getFile());

		} catch (final IOException e) {
			// TODO handle this exception (but for now, at least know it happened)
			throw new RuntimeException(e);

		} catch (final CoreException e) {
			// TODO handle this exception (but for now, at least know it happened)
			throw new RuntimeException(e);
		}

		setPartName(getEditorInput().getName());
		firePropertyChange(PROP_TITLE);

		// TODO add resource change listener
		// ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener, IResourceChangeEvent.POST_CHANGE);
	}

	@Override
	protected void addPages() {

		try {
			addPage(new OverviewPage(this, OVERVIEW_PAGE, OVERVIEW_PAGE));
			addPage(new FileSelectionPage(this, TEST_SELECTION_PAGE, TEST_SELECTION_PAGE));
			addPage(new VariablesPage(this, VARIABLES_PAGE, VARIABLES_PAGE));
			addPage(new CustomCodePage(this, CUSTOM_CODE_PAGE, CUSTOM_CODE_PAGE));

		} catch (final PartInitException e) {
			Logger.error(Activator.PLUGIN_ID, "Could not create all pages for TestSuiteEditor", e);
		}
	}

	@Override
	public void doSaveAs() {
		// not allowed
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void doSave(final IProgressMonitor monitor) {
		try {
			final byte[] xmlContent = UnitTestHelper.serializeTestSuite(getTestSuite());
			getFile().setContents(new ByteArrayInputStream(xmlContent), true, true, null);

		} catch (final IOException e) {
			// TODO handle this exception (but for now, at least know it happened)
			throw new RuntimeException(e);

		} catch (final CoreException e) {
			// TODO handle this exception (but for now, at least know it happened)
			throw new RuntimeException(e);
		}

		fDirty = false;
		editorDirtyStateChanged();
	}

	@Override
	protected void pageChange(final int newPageIndex) {
		// switch page
		super.pageChange(newPageIndex);

		// update page if needed
		final IFormPage page = getActivePageInstance();
		if (page != null)
			page.setFocus();
	}

	public ITestSuiteDefinition getTestSuite() {
		return fTestSuite;
	}

	@Override
	public boolean isDirty() {
		return (super.isDirty() | fDirty) && (getFile() != null);
	}

	private IFile getFile() {
		final IEditorInput input = getEditorInput();
		if (input instanceof FileEditorInput)
			return ((FileEditorInput) input).getFile();

		return null;
	}

	private InputStream getInputContent() throws CoreException {
		final IEditorInput editorInput = getEditorInput();
		if (editorInput instanceof FileEditorInput)
			return ((FileEditorInput) editorInput).getFile().getContents();

		if (editorInput instanceof IStorageEditorInput)
			return ((IStorageEditorInput) editorInput).getStorage().getContents();

		return null;
	}

	@Override
	public AdapterFactoryEditingDomain getEditingDomain() {
		return fEditingDomain;
	}

	@Override
	public void dispose() {
		fResourceManager.dispose();

		super.dispose();
	}

	public LocalResourceManager getResourceManager() {
		return fResourceManager;
	}

	public void executeCommand(Command command) {
		getEditingDomain().getCommandStack().execute(command);
	}
}
