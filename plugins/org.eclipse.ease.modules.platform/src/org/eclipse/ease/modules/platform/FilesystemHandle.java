/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.modules.platform;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;

public class FilesystemHandle implements IFileHandle {

	private final File fFile;
	private int fMode;
	protected BufferedReader fReader = null;
	private PrintWriter fWriter = null;
	private OutputStream fOutput = null;

	public FilesystemHandle(final File file, final int mode) {
		fFile = file;
		fMode = mode;
	}

	protected BufferedReader createReader() throws Exception {
		return new BufferedReader(new InputStreamReader(new FileInputStream(fFile)));
	}

	private BufferedReader getReader() {
		try {
			if (fReader == null)
				fReader = createReader();

		} catch (final Exception e) {
		}

		return fReader;
	}

	@Override
	public String read(final int characters) throws IOException {
		final BufferedReader reader = getReader();
		if (reader != null) {
			final StringBuilder result = new StringBuilder();
			final char[] buffer = new char[1024 * 4];
			while (characters != 0) {
				final int length = reader.read(buffer, 0, (characters < 0) ? buffer.length : Math.min(buffer.length, characters));
				if (length == -1) {
					// EOF reached
					reader.close();
					break;
				}

				result.append(new String(buffer, 0, length));
			}

			return result.toString();
		}

		return null;
	}

	@Override
	public String readLine() throws IOException {
		final BufferedReader reader = getReader();
		if (reader != null)
			return reader.readLine();

		return null;
	}

	@Override
	public boolean write(final String data) {
		try {
			// replace file content or append content
			if (fWriter == null) {
				if (fOutput == null)
					fOutput = new BufferedOutputStream(new FileOutputStream(fFile, (fMode & APPEND) == APPEND));

				fWriter = new PrintWriter(new OutputStreamWriter(fOutput));
			}

			fWriter.print(data);
			fWriter.flush();
			return true;

		} catch (final Exception e) {
		}

		return false;
	}

	@Override
	public boolean write(final byte[] data) {
		try {
			// replace file content or append content
			if (fOutput == null)
				fOutput = new BufferedOutputStream(new FileOutputStream(fFile, (fMode & APPEND) == APPEND));

			fOutput.write(data);
			fOutput.flush();
			return true;

		} catch (final Exception e) {
		}

		return false;
	}

	protected static StringBuilder read(final Reader reader) throws IOException {
		// consume reader
		final StringBuilder builder = new StringBuilder();
		final char[] buffer = new char[1024 * 4];
		int bytes = 0;
		do {
			bytes = reader.read(buffer);
			builder.append(buffer, 0, Math.max(bytes, 0));
		} while (bytes != -1);

		if (builder.length() > 0)
			return builder;

		return null;
	}

	@Override
	public boolean exists() {
		return fFile.exists();
	}

	@Override
	public boolean createFile(final boolean createHierarchy) throws Exception {
		if (createHierarchy) {
			final File folder = fFile.getParentFile();
			if (!folder.exists())
				folder.mkdirs();
		}

		return fFile.createNewFile();
	}

	public void setMode(final int mode) {
		fMode = mode;
	}

	protected int getMode() {
		return fMode;
	}

	@Override
	protected void finalize() throws Throwable {
		close();

		super.finalize();
	}

	@Override
	public void close() {
		try {
			if (fReader != null)
				fReader.close();
		} catch (final IOException e) {
		}

		try {
			if (fWriter != null)
				fWriter.close();
		} catch (final Exception e) {
		}

		try {
			if (fOutput != null)
				fOutput.close();
		} catch (final Exception e) {
		}
	}

	@Override
	public Object getFile() {
		return fFile;
	}
}
