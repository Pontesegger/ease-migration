/*******************************************************************************
 * Copyright (c) 2018 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.help.hovers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ease.Logger;
import org.eclipse.ease.ui.Activator;

public class HelpHoverImageProvider {

	private static final Map<String, String> CACHED_IMAGES = new HashMap<>();

	/**
	 * When we need to add images to HTML sites we need to copy them over to the file system. Reason is that images are part of *.jar files and cannot be
	 * directly used in a browser.
	 *
	 * @param bundlePath
	 *            path within org.eclipse.ease.ui plugin
	 * @return file system path
	 */
	public static String getImageLocation(String bundlePath) {

		if (!CACHED_IMAGES.containsKey(bundlePath)) {
			final InputStream input = Activator.getResource(bundlePath);
			if (input != null) {
				try {
					final File tempFile = File.createTempFile("EASE_image", "png");
					tempFile.deleteOnExit();
					final OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFile));

					final InputStream inputStream = new BufferedInputStream(input);
					final byte[] buffer = new byte[1024];

					int bytes = inputStream.read(buffer);
					while (bytes != -1) {
						outputStream.write(buffer, 0, bytes);
						bytes = inputStream.read(buffer);
					}

					inputStream.close();
					outputStream.close();

					CACHED_IMAGES.put(bundlePath, tempFile.toURI().toString());
				} catch (final FileNotFoundException e) {
					Logger.error(Activator.PLUGIN_ID, "Cannot find image file for help hover", e);
					return null;

				} catch (final IOException e) {
					Logger.error(Activator.PLUGIN_ID, "Cannot create image file for help hover", e);
					return null;
				}
			}
		}

		return CACHED_IMAGES.get(bundlePath);
	}
}
