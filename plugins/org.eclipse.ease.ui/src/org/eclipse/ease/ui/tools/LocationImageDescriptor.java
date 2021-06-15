/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
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
package org.eclipse.ease.ui.tools;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Policy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.ImageData;

public class LocationImageDescriptor extends ImageDescriptor {

	private final String fLocation;

	public static ImageDescriptor createFromLocation(final String location) {
		if (location != null)
			return new LocationImageDescriptor(location);

		return null;
	}

	private LocationImageDescriptor(final String location) {
		fLocation = location;
	}

	@Override
	public ImageData getImageData() {
		ImageData result = null;
		final InputStream in = ResourceTools.getInputStream(fLocation);

		// implementation copied from org.eclipse.jface.URLImageDescriptor
		if (in != null) {
			try {
				result = new ImageData(in);
			} catch (final SWTException e) {
				if (e.code != SWT.ERROR_INVALID_IMAGE) {
					throw e;
					// fall through otherwise
				}
			} finally {
				try {
					in.close();
				} catch (final IOException e) {
					Policy.getLog().log(new Status(IStatus.ERROR, Policy.JFACE, e.getLocalizedMessage(), e));
				}
			}
		}

		return (result != null) ? result : ImageDescriptor.getMissingImageDescriptor().getImageData();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((fLocation == null) ? 0 : fLocation.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final LocationImageDescriptor other = (LocationImageDescriptor) obj;
		if (fLocation == null) {
			if (other.fLocation != null)
				return false;
		} else if (!fLocation.equals(other.fLocation))
			return false;
		return true;
	}
}
