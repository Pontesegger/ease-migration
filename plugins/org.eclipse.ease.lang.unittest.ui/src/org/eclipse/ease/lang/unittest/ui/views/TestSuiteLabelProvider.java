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
package org.eclipse.ease.lang.unittest.ui.views;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.ease.lang.unittest.runtime.ITestClass;
import org.eclipse.ease.lang.unittest.runtime.ITestContainer;
import org.eclipse.ease.lang.unittest.runtime.ITestEntity;
import org.eclipse.ease.lang.unittest.runtime.ITestFile;
import org.eclipse.ease.lang.unittest.runtime.ITestFolder;
import org.eclipse.ease.lang.unittest.runtime.ITestSuite;
import org.eclipse.ease.lang.unittest.ui.Activator;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE.SharedImages;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class TestSuiteLabelProvider extends LabelProvider {
	private final LocalResourceManager fResourceManager;
	private final WorkbenchLabelProvider fWorkbenchLabelProvider = new WorkbenchLabelProvider();

	public TestSuiteLabelProvider(final LocalResourceManager resourceManager) {
		fResourceManager = resourceManager;
	}

	@Override
	public String getText(final Object element) {
		if (element instanceof ITestEntity)
			return ((ITestEntity) element).getName();

		return super.getText(element);
	}

	@Override
	public Image getImage(final Object element) {
		if (isRootElement(element)) {
			if ("workspace".equals(((ITestEntity) element).getName()))
				return Activator.getImage(Activator.PLUGIN_ID, "/icons/eobj16/workspace.png", true);
			else
				return Activator.getImage(Activator.PLUGIN_ID, "/icons/eobj16/file_system.png", true);

		} else if (element instanceof ITestFolder) {
			if ((isRootElement(((ITestFolder) element).getParent())) && ("workspace".equals(((ITestFolder) element).getParent().getName())))
				// workspace project
				return PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJ_PROJECT);

			else
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);

		} else if (element instanceof ITestFile) {
			final Object file = ((ITestFile) element).getResource();
			if (file instanceof IFile)
				return fWorkbenchLabelProvider.getImage(file);
			else if (file instanceof File)
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);

		} else if (element instanceof ITestSuite) {
			final Object file = ((ITestSuite) element).getResource();
			if (file instanceof IFile)
				return fWorkbenchLabelProvider.getImage(file);
			else if (file instanceof File)
				return fResourceManager.createImage(Activator.getImageDescriptor(Activator.PLUGIN_ID, "/icons/eobj16/testsuite.png"));
			;

		} else if (element instanceof ITestClass) {
			return fResourceManager.createImage(Activator.getImageDescriptor(Activator.PLUGIN_ID, "/icons/eobj16/testclass.png"));
		}

		return super.getImage(element);
	}

	protected boolean isRootElement(Object element) {
		if (element instanceof ITestContainer)
			return ((ITestContainer) element).getParent() == null;

		return false;
	}

	@Override
	public void dispose() {
		fWorkbenchLabelProvider.dispose();

		// resourceManager is provided by UnitTest view and needs not to be disposed here

		super.dispose();
	}
}
