/*******************************************************************************
 * Copyright (c) 2014 Bernhard Wedl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Bernhard Wedl - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.ui.modules.ui;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ease.modules.ModuleDefinition;
import org.eclipse.ease.ui.Activator;
import org.eclipse.ease.ui.modules.ui.ModulesTools.ModuleEntry;
import org.eclipse.ease.ui.tools.DecoratedLabelProvider;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;;

public class ModulesComposite extends Composite implements BundleListener {
	private final TreeViewer treeViewer;

	/**
	 * Create the composite.
	 *
	 * @param parent
	 * @param style
	 * @param modulesOnly
	 *            if true only the modules are shown in the tree. if false also the fields and functions are shown.
	 */
	public ModulesComposite(final Composite parent, final int style, final boolean modulesOnly) {
		super(parent, style);

		treeViewer = new TreeViewer(this, SWT.NONE);

		setLayout(new FillLayout(SWT.HORIZONTAL));
		treeViewer.getTree().setLayout(new FillLayout(SWT.HORIZONTAL));

		final DecoratedLabelProvider labelProvider = new DecoratedLabelProvider(new ModulesLabelProvider());
		treeViewer.setLabelProvider(labelProvider);

		treeViewer.setContentProvider(new ModulesContentProvider(modulesOnly));

		treeViewer.setComparator(new ViewerComparator() {
			@Override
			public int category(Object element) {

				// unpack field/method
				if (element instanceof ModuleEntry)
					element = ((ModuleEntry<?>) element).getEntry();

				if ((element instanceof IPath))
					return 1;

				if ((element instanceof ModuleDefinition))
					return 2;

				if ((element instanceof Field))
					return 3;

				if ((element instanceof Method))
					return 4;

				return super.category(element);
			}
		});

		treeViewer.addDragSupport(DND.DROP_MOVE | DND.DROP_COPY, new Transfer[] { LocalSelectionTransfer.getTransfer(), TextTransfer.getInstance() },
				new ModulesDragListener(treeViewer));

		// input is handled by the content provider directly
		treeViewer.setInput("dummy");

		Activator.getDefault().getContext().addBundleListener(this);
	}

	@Override
	public void dispose() {
		Activator.getDefault().getContext().removeBundleListener(this);
		super.dispose();
	}

	public void setInput(final Object input) {
		treeViewer.setInput(input);
	}

	public void refresh() {
		treeViewer.refresh();
	}

	public void addFilter(final ViewerFilter filter) {
		treeViewer.addFilter(filter);
	}

	public TreeViewer getTreeViewer() {
		return treeViewer;
	}

	@Override
	public void bundleChanged(BundleEvent event) {
		final int type = event.getType();
		if ((type == BundleEvent.RESOLVED) || (type == BundleEvent.STOPPED) || (type == BundleEvent.UPDATED)) {
			Display.getDefault().asyncExec(() -> {
				if (!treeViewer.getTree().isDisposed())
					treeViewer.refresh();
			});
		}
	}
}
