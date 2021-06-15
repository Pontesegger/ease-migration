/*******************************************************************************
 * Copyright (c) 2019 Christian Pontesegger and others.
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
package org.eclipse.ease.tools;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Version;

/**
 * This utility class provides backwards compatibility for older target platforms. Using this implementation allows to benefit from the new iterable interface.
 * Still it can be used on old platforms where this is not provided.
 */
public class ListenerList<E> extends org.eclipse.core.runtime.ListenerList<E> implements Iterable<E> {

	@Override
	public Iterator<E> iterator() {
		final Version equinoxBundleVersion = Platform.getBundle("org.eclipse.equinox.common").getVersion();
		if (equinoxBundleVersion.compareTo(Version.valueOf("3.8.0")) < 0) {
			// old ListenerList does not support iterators
			return new ListenerListIterator<>(getListeners());
		}

		return super.iterator();
	}

	/**
	 * Copied from org.eclipse.core.runtime.ListenerList
	 */
	private static class ListenerListIterator<E> implements Iterator<E> {
		private final Object[] fListeners;
		private int fIndex;

		public ListenerListIterator(Object[] listeners) {
			fListeners = listeners;
		}

		@Override
		public boolean hasNext() {
			return fIndex < fListeners.length;
		}

		@Override
		public E next() {
			if (fIndex >= fListeners.length) {
				throw new NoSuchElementException();
			}
			@SuppressWarnings("unchecked") // (E) is safe, because #add(E) only accepts Es
			final E next = (E) fListeners[fIndex++];
			return next;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
