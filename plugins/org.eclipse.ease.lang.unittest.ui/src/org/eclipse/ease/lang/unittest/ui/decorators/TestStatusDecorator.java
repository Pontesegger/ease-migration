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
package org.eclipse.ease.lang.unittest.ui.decorators;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.ease.lang.unittest.runtime.ITestContainer;
import org.eclipse.ease.lang.unittest.runtime.ITestEntity;
import org.eclipse.ease.lang.unittest.runtime.TestStatus;
import org.eclipse.ease.lang.unittest.ui.Activator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

public class TestStatusDecorator implements ILightweightLabelDecorator {

	@Override
	public void addListener(final ILabelProviderListener listener) {
		// nothing to do
	}

	@Override
	public void dispose() {
		// nothing to do
	}

	@Override
	public boolean isLabelProperty(final Object element, final String property) {
		// return UnitTestView.TEST_STATUS_PROPERTY.equals(property);
		return true;
	}

	@Override
	public void removeListener(final ILabelProviderListener listener) {
		// nothing to do
	}

	@Override
	public void decorate(final Object element, final IDecoration decoration) {
		if (element instanceof ITestContainer) {
			final TestStatus status = ((ITestContainer) element).getStatus();
			addOverlay((ITestContainer) element, decoration);

			if (status != TestStatus.PASS) {
				final Collection<ITestEntity> testEntities = new ArrayList<>(((ITestContainer) element).getCopyOfChildren());
				int valid = 0;
				for (final ITestEntity entity : testEntities) {
					if (entity.getStatus() == TestStatus.PASS)
						valid++;
				}

				// if (!element.isActive())
				// decoration.setForegroundColor(new Color(Display.getDefault(), 180, 180, 180));

				if ((testEntities.size() > 0) && (valid != testEntities.size()))
					decoration.addSuffix(" (" + valid + "/" + testEntities.size() + " valid)");
			}
		}
	}

	/**
	 * Get an image descriptor from the overlay folder.
	 *
	 * @param image
	 *            image file name in ovr16 folder
	 * @return image descriptor or <code>null</code>
	 */
	public static ImageDescriptor getImage(final String image) {
		return Activator.getImageDescriptor(Activator.PLUGIN_ID, "/icons/ovr16/" + image);
	}

	private void addOverlay(final ITestContainer element, final IDecoration decoration) {
		switch (element.getStatus()) {
		case PASS:
			decoration.addOverlay(getImage("success.png"), IDecoration.BOTTOM_LEFT);
			break;
		case ERROR:
			decoration.addOverlay(getImage("error.png"), IDecoration.BOTTOM_LEFT);
			break;
		case FAILURE:
			decoration.addOverlay(getImage("failure.png"), IDecoration.BOTTOM_LEFT);
			break;
		case RUNNING:
			decoration.addOverlay(getImage("running.png"), IDecoration.BOTTOM_LEFT);
			break;
		case DISABLED:
			decoration.addOverlay(getImage("ignore.png"), IDecoration.BOTTOM_LEFT);
			break;
		default:
			// nothing to do
			break;
		}

		if (!element.getMetadata().isEmpty())
			decoration.addOverlay(getImage("metadata.png"), IDecoration.TOP_RIGHT);
	}
}
