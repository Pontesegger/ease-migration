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
package org.eclipse.ease.ui.dnd;

import java.io.File;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ease.IScriptEngineProvider;
import org.eclipse.ease.Logger;
import org.eclipse.ease.tools.ResourceTools;
import org.eclipse.ease.ui.Activator;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.ResourceTransfer;

/**
 * DND support for JavaScript shell. DND of plain text, files, resources and IDevices is supported.
 */
public final class ShellDropTarget extends DropTargetAdapter {

	private static final String EXTENSION_DROP_HANDLER_ID = "org.eclipse.ease.ui.shell";
	private static final String DROP_HANDLER = "dropHandler";
	private static final String PARAMETER_CLASS = "class";
	private static final String ATTRIBUTE_PRIORITY = "priority";

	private static Collection<IShellDropHandler> getDropTargetListeners() {

		final List<AbstractMap.SimpleEntry<Integer, IShellDropHandler>> candidates = new ArrayList<>();

		final IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_DROP_HANDLER_ID);
		for (final IConfigurationElement e : config) {
			if (DROP_HANDLER.equals(e.getName())) {
				// drop handler
				// candidates.add(e);
				try {
					final Object executable = e.createExecutableExtension(PARAMETER_CLASS);
					if (executable instanceof IShellDropHandler) {
						int priority = 0;
						try {
							priority = Integer.parseInt(e.getAttribute(ATTRIBUTE_PRIORITY));
						} catch (final NumberFormatException e1) {
						} catch (final NullPointerException e1) {
						}

						candidates.add(new AbstractMap.SimpleEntry<>(priority, (IShellDropHandler) executable));
					}

				} catch (final CoreException e1) {

					Logger.error(Activator.PLUGIN_ID, "Invalid drop target listener detected in plugin \"" + e.getContributor().getName() + "\"", e1);
				}
			}
		}

		// sort handler by priority
		Collections.sort(candidates, (e1, e2) -> e2.getKey() - e1.getKey());

		final Collection<IShellDropHandler> handler = new ArrayList<>(candidates.size());
		for (final AbstractMap.SimpleEntry<Integer, IShellDropHandler> candidate : candidates)
			handler.add(candidate.getValue());

		return handler;
	}

	/**
	 * JavaScript shell for DND execution.
	 */
	private final IScriptEngineProvider fScriptEngineProvider;

	/**
	 * Add drop support for various objects. A drop will always be interpreted as <i>copy</i>, even if <i>move</i> was requested.
	 *
	 * @param parent
	 *            control accepting drops
	 * @param engineProvider
	 *            container providing a script engine
	 */
	public static void addDropSupport(final Control parent, final IScriptEngineProvider engineProvider) {
		final DropTarget target = new DropTarget(parent, DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK);
		target.setTransfer(new Transfer[] { FileTransfer.getInstance(), ResourceTransfer.getInstance(), LocalSelectionTransfer.getTransfer(),
				TextTransfer.getInstance() });
		target.addDropListener(new ShellDropTarget(engineProvider));
	}

	private ShellDropTarget(final IScriptEngineProvider provider) {
		fScriptEngineProvider = provider;
	}

	@Override
	public void drop(final DropTargetEvent event) {
		Object element = unpackElement(event.data);

		// first ask registered drop handlers
		final Collection<IShellDropHandler> listeners = getDropTargetListeners();
		if (!listeners.isEmpty()) {

			for (final IShellDropHandler listener : listeners) {
				// 1st pass: try to drop unpacked dropped element and pass it to drop handler
				if (listener.accepts(fScriptEngineProvider.getScriptEngine(), element)) {
					listener.performDrop(fScriptEngineProvider.getScriptEngine(), element, event.detail);
					return;
				}

				if (!event.data.equals(element)) {
					// 2nd pass: no listener found for unwrapped object, try with original object
					if (listener.accepts(fScriptEngineProvider.getScriptEngine(), event.data)) {
						listener.performDrop(fScriptEngineProvider.getScriptEngine(), event.data, event.detail);
						return;
					}
				}
			}
		}

		// no drop processor found, try generic approaches

		// resolve nice filename for workspace resources
		if (element instanceof IResource)
			element = ResourceTools.toAbsoluteLocation(element, null);

		fScriptEngineProvider.getScriptEngine().execute(element.toString());
	}

	private Object unpackElement(Object element) {
		// look for file system files
		if (element instanceof String[]) {
			// drop of external files (eg. from explorer)
			final File[] files = new File[((String[]) element).length];
			for (int i = 0; i < files.length; i++)
				files[i] = new File(((String[]) element)[i]);

			element = files;
		}

		// unpack selections
		if (element instanceof IStructuredSelection)
			element = ((IStructuredSelection) element).toArray();

		// unpack arrays with a single element
		if ((element instanceof Object[]) && (((Object[]) element).length == 1))
			element = ((Object[]) element)[0];

		// unpack collections with a single element
		if ((element instanceof Collection<?>) && (((Collection<?>) element).size() == 1))
			element = ((Collection<?>) element).iterator().next();

		return element;
	}

	@Override
	public void dragEnter(DropTargetEvent event) {
		event.detail = DND.DROP_COPY;
	}
}
