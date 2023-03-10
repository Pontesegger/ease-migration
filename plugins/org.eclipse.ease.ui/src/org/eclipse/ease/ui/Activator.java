/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
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
package org.eclipse.ease.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ease.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.eclipse.ease.ui";

	public static final String ICON_FIELD = "/icons/eobj16/field.png";

	public static final String ICON_METHOD = "/icons/eobj16/function.png";

	public static final int JAVA_CLASSES_MAX_VERSION = 12;

	private static Activator fInstance;

	private BundleContext fContext;

	public static Activator getDefault() {
		return fInstance;
	}

	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);

		fContext = context;
		fInstance = this;
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		fInstance = null;
		fContext = null;

		super.stop(context);
	}

	public BundleContext getContext() {
		return fContext;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path
	 *
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(final String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public static ImageDescriptor getImageDescriptor(final String bundleID, final String path) {
		assert (bundleID != null) : "No bundle defined";
		assert (path != null) : "No path defined";

		// if the bundle is not ready then there is no image
		final Bundle bundle = Platform.getBundle(bundleID);
		final int bundleState = bundle.getState();
		if ((bundleState != Bundle.ACTIVE) && (bundleState != Bundle.STARTING) && (bundleState != Bundle.RESOLVED))
			return null;

		// look for the image (this will check both the plugin and fragment
		// folders
		final URL imagePath = FileLocator.find(bundle, new Path(path), null);

		if (imagePath != null)
			return ImageDescriptor.createFromURL(imagePath);

		return null;
	}

	public static Image getImage(final String bundleID, final String path, final boolean storeToImageRegistry) {
		assert (bundleID != null) : "No bundle defined";
		assert (path != null) : "No path defined";

		Image image = getDefault().getImageRegistry().get(bundleID + path);
		if (image == null) {
			final ImageDescriptor descriptor = getImageDescriptor(bundleID, path);
			if (descriptor != null) {
				image = descriptor.createImage();

				if (storeToImageRegistry)
					getDefault().getImageRegistry().put(bundleID + path, image);
			}
		}

		return image;
	}

	protected final File getConfigurationFile(final String name) {
		return getStateLocation().append(name).toFile();
	}

	// FIXME seems to be obsolete
	/**
	 *
	 * This method returns an <code>org.eclipse.swt.graphics.Image</code> identified by its pluginId and iconPath.<BR>
	 */
	public static Image getPluginIconImage(final String pluginId, final String iconPath) {
		final String key = pluginId + iconPath;
		final ImageRegistry registry = getDefault().getImageRegistry();
		Image image = registry.get(key);
		if (image == null) {
			final ImageDescriptor desc = getImageDescriptor(pluginId, iconPath);
			registry.put(key, desc);
			image = registry.get(key);
		}
		return image;
	}

	public static Image getLocalPluginIconImage(final String iconPath) {
		return getPluginIconImage(PLUGIN_ID, iconPath);
	}

	public static ImageDescriptor getLocalImageDescriptor(final String iconPath) {
		return getImageDescriptor(PLUGIN_ID, iconPath);
	}

	public static InputStream getResource(String path) {
		try {
			final URL url = new URL("platform:/plugin/org.eclipse.ease.ui/" + path);
			return url.openConnection().getInputStream();

		} catch (final IOException e) {
			Logger.error(Activator.PLUGIN_ID, "Cannot read style sheet for hover presentation", e);
		}

		return null;
	}
}
