/*******************************************************************************
 * Copyright (c) 2018 Christian Pontesegger and others.
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * A text box with a label at the left hand side.
 */
public class TextWithImage extends Composite {
	private final Text fText;
	private final Label fLabel;
	private Image fImage;

	public TextWithImage(Composite parent, int style) {
		super(parent, SWT.BORDER);

		final Color backgroundColor = Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND);

		setBackground(backgroundColor);
		final GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 1;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 1;
		setLayout(gridLayout);

		fLabel = new Label(this, SWT.NONE);
		fLabel.setBackground(backgroundColor);
		if (fImage != null)
			setImage(fImage);

		fText = new Text(this, SWT.NONE);
		fText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	}

	@Override
	public void dispose() {
		fImage = null;

		super.dispose();
	}

	/**
	 * Get the underlying text widget instance.
	 *
	 * @return text widget
	 */
	public Text getTextElement() {
		return fText;
	}

	/**
	 * Sets the receiver's image to the argument, which may be null indicating that no image should be displayed.
	 *
	 * @param image
	 *            the image to display on the receiver (may be null)
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public void setImage(Image image) {
		fImage = image;

		if ((fLabel != null) && (!fLabel.isDisposed())) {
			fLabel.setImage(fImage);
			fLabel.setVisible(fImage != null);
		}
	}

	/**
	 * Returns the widget text.
	 * <p>
	 * The text for a text widget is the characters in the widget, or an empty string if this has never been set.
	 * </p>
	 *
	 * @return the widget text
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public String getText() {
		return getTextElement().getText();
	}

	/**
	 * Sets the contents of the receiver to the given string. If the receiver has style SINGLE and the argument contains multiple lines of text, the result of
	 * this operation is undefined and may vary from platform to platform.
	 * <p>
	 * Note: If control characters like '\n', '\t' etc. are used in the string, then the behavior is platform dependent.
	 * </p>
	 *
	 * @param string
	 *            the new text
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the string is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *                </ul>
	 */
	public void setText(String string) {
		getTextElement().setText(string);
	}
}
