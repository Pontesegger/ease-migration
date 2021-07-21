/*******************************************************************************
 * Copyright (c) 2021 Christian Pontesegger and others.
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

package org.eclipse.ease.ui.completion.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ease.ICompletionContext;

public abstract class AbstractPathCompletionProvider extends AbstractCompletionProvider {

	protected <T> Collection<T> filter(Collection<T> elements, ICompletionContext context) {
		final Collection<T> filteredElements = new ArrayList<>();

		final String filter = getFilter(context);
		for (final T element : elements) {
			final IPath path = convertToPath(element).makeAbsolute();
			if (path.toString().startsWith(filter)) {
				if ((new Path(filter).segmentCount() + (filter.endsWith("/") ? 1 : 0)) == path.segmentCount())
					filteredElements.add(element);
			}
		}

		return filteredElements;
	}

	private String getFilter(ICompletionContext context) {
		return (context.getFilter().startsWith("/")) ? context.getFilter() : "/" + context.getFilter();
	}

	protected Collection<IPath> getPathsFromElements(Collection<?> elements) {
		final Collection<IPath> paths = new LinkedHashSet<>();

		final List<IPath> pathsFromElements = elements.stream().map(e -> convertToPath(e)).filter(p -> p.segmentCount() > 0)
				.map(p -> p.removeLastSegments(1).makeAbsolute()).collect(Collectors.toList());
		for (final IPath path : pathsFromElements)
			addPath(path, paths);

		return paths;
	}

	private void addPath(IPath path, Collection<IPath> paths) {
		if (!Path.ROOT.equals(path)) {
			addPath(path.removeLastSegments(1), paths);
			paths.add(path);
		}
	}

	private IPath convertToPath(Object element) {
		if (element instanceof IPath)
			return (IPath) element;

		return toPath(element);
	}

	protected abstract IPath toPath(Object element);

}
