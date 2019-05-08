/*******************************************************************************
 * Copyright (c) 2013 Atos
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Arthur Daussy - initial implementation
 *******************************************************************************/
package org.eclipse.ease.modules.modeling;

import org.eclipse.ease.modules.WrapToScript;
import org.eclipse.ease.tools.RunnableWithResult;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * Helper class to display dialogs.
 */
public class DialogHelper {

	/**
	 * Return the active {@link Shell}
	 *
	 * @return
	 */
	@WrapToScript
	public static Shell getActiveShell() {
		return Display.getDefault().getActiveShell();
	}

	/**
	 * Open a dialog
	 *
	 * @param window
	 *            A Window to open
	 * @return window.open().
	 */
	@WrapToScript
	public static int openDialog(final Window window) {
		final RunnableWithResult<Integer> run = new RunnableWithResult<Integer>() {

			@Override
			public Integer runWithTry() throws Throwable {
				return window.open();
			}
		};
		Display.getDefault().syncExec(run);
		return run.getResult();
	}

	/**
	 * Open a dialog and ask to the user to select from a list of element
	 *
	 * @param selectionOption
	 *            The list element from which the user shall choose
	 * @param labelProvider
	 *            The label provider used to display the elements
	 * @return An array of the selected objects
	 */
	@WrapToScript
	public static Object[] selectFromList(final Object[] selectionOption, final ILabelProvider labelProvider) {
		final RunnableWithResult<Object[]> runnable = new RunnableWithResult<Object[]>() {

			@Override
			public Object[] runWithTry() throws Throwable {
				final ElementListSelectionDialog dialog = new ElementListSelectionDialog(Display.getDefault().getActiveShell(), labelProvider);
				dialog.setElements(selectionOption);
				if (dialog.open() == Window.OK)
					return dialog.getResult();

				return null;
			}
		};
		Display.getDefault().syncExec(runnable);
		return runnable.getResult();

	}

	public static void openWindow(final Window window) {
		final Runnable runnable = () -> window.open();
		Display.getDefault().syncExec(runnable);
	}

}
