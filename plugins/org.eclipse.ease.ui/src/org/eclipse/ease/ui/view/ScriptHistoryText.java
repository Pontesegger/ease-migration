/*******************************************************************************
 * Copyright (c) 2015 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.view;

import java.util.concurrent.ExecutionException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ease.IExecutionListener;
import org.eclipse.ease.IReplEngine;
import org.eclipse.ease.IScriptEngine;
import org.eclipse.ease.Script;
import org.eclipse.ease.ScriptObjectType;
import org.eclipse.ease.ScriptResult;
import org.eclipse.ease.ui.Activator;
import org.eclipse.jface.resource.ColorDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;

public class ScriptHistoryText extends StyledText implements IExecutionListener {

	public static final int STYLE_ERROR = 1;

	public static final int STYLE_RESULT = 3;

	public static final int STYLE_COMMAND = 4;

	private static Image getImage(ScriptObjectType type) {
		switch (type) {
		case JAVA_OBJECT:
			return Activator.getImage(Activator.PLUGIN_ID, "/icons/eobj16/debug_java_class.png", true);

		case NATIVE_ARRAY:
			return Activator.getImage(Activator.PLUGIN_ID, "/icons/eobj16/debug_local_array.png", true);

		case NATIVE_OBJECT:
			return Activator.getImage(Activator.PLUGIN_ID, "/icons/eobj16/debug_local_object.png", true);

		case VOID:
			return Activator.getImage(Activator.PLUGIN_ID, "/icons/eobj16/void_type.png", true);

		default:
			return Activator.getImage(Activator.PLUGIN_ID, "/icons/eobj16/debug_local_variable.png", true);
		}
	}

	private class BlendBackgroundJob extends UIJob {

		private boolean fRun;
		private volatile boolean fStarted;

		public BlendBackgroundJob() {
			super("Darken Script Shell background");
			setSystem(true);
		}

		public void arm() {
			fRun = true;
			fStarted = false;
			schedule(300);
		}

		public void disarm() {
			fRun = false;
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			fStarted = true;
			if (fRun)
				setBackground(fResourceManager.createColor(fColorShadedBackground));

			return Status.OK_STATUS;
		}
	}

	private BlendBackgroundJob fBlendBackgroundJob = null;

	private final LocalResourceManager fResourceManager = new LocalResourceManager(JFaceResources.getResources(), getParent());

	private final ColorDescriptor fColorDescriptorResult = ColorDescriptor.createFrom(getShell().getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
	private final ColorDescriptor fColorDescriptorCommand = ColorDescriptor.createFrom(getShell().getDisplay().getSystemColor(SWT.COLOR_BLUE));
	private final ColorDescriptor fColorDescriptorError = ColorDescriptor.createFrom(getShell().getDisplay().getSystemColor(SWT.COLOR_RED));
	private ColorDescriptor fColorDefaultBackground = null;
	private ColorDescriptor fColorShadedBackground = null;

	private IReplEngine fCurrentEngine;

	public ScriptHistoryText(final Composite parent, final int style) {
		super(parent, style);

		initialize();
	}

	public void addScriptEngine(final IReplEngine engine) {
		if (engine != null) {
			engine.addExecutionListener(this);
			fCurrentEngine = engine;
		}
	}

	public void removeScriptEngine(final IReplEngine engine) {
		if (engine != null) {
			engine.removeExecutionListener(this);
			Display.getDefault().asyncExec(() -> setBackground(fResourceManager.createColor(fColorDefaultBackground)));
		}
	}

	private ColorDescriptor createShadedBackground(Color defaultColor) {
		final int defaultRgb = defaultColor.getRed() + defaultColor.getGreen() + defaultColor.getBlue();
		if (defaultRgb >= 0x20)
			return ColorDescriptor.createFrom(new RGB(Math.max(defaultColor.getRed() - 0x10, 0), Math.max(defaultColor.getGreen() - 0x10, 0),
					Math.max(defaultColor.getBlue() - 0x10, 0)));

		// original color is too dark, use a lighter color for shading
		return ColorDescriptor.createFrom(new RGB(Math.min(defaultColor.getRed() + 0x10, 0xFF), Math.min(defaultColor.getGreen() + 0x10, 0xFF),
				Math.min(defaultColor.getBlue() + 0x10, 0xFF)));
	}

	private void initialize() {
		// set monospaced font
		setFont(JFaceResources.getFont(IDebugUIConstants.PREF_CONSOLE_FONT));
		fColorDefaultBackground = ColorDescriptor.createFrom(DebugUIPlugin.getPreferenceColor(IDebugPreferenceConstants.CONSOLE_BAKGROUND_COLOR));
		fColorShadedBackground = createShadedBackground(DebugUIPlugin.getPreferenceColor(IDebugPreferenceConstants.CONSOLE_BAKGROUND_COLOR));

		JFaceResources.getFontRegistry().addListener(event -> {
			if (IDebugUIConstants.PREF_CONSOLE_FONT.equals(event.getProperty()))
				setFont(JFaceResources.getFont(IDebugUIConstants.PREF_CONSOLE_FONT));
		});

		setEditable(false);

		fBlendBackgroundJob = new BlendBackgroundJob();

		// add support for response type images
		addPaintObjectListener(event -> {
			final StyleRange style = event.style;
			if (style.data instanceof Image) {
				final int lineHeight = event.ascent + event.descent;
				final int yOffset = Math.max(0, (lineHeight - ((Image) style.data).getBounds().height) / 2);
				final int x = event.x - 5;
				final int y = event.y + yOffset + 2; // not sure why we need +2 here. Icon is not centered on linux without

				event.gc.drawImage((Image) style.data, x, y);
			}
		});
	}

	@Override
	public void dispose() {
		fResourceManager.dispose();

		if (fCurrentEngine != null) {
			fCurrentEngine.removeExecutionListener(this);
			fCurrentEngine = null;
		}

		super.dispose();
	}

	public void clear() {
		setText("");
		setStyleRanges(new StyleRange[0]);
	}

	@Override
	public void notify(final IScriptEngine engine, final Script script, final int status) {

		try {
			switch (status) {
			case SCRIPT_START:
				fBlendBackgroundJob.arm();
				printCommand(script.getCode());
				break;

			case SCRIPT_END:
				fBlendBackgroundJob.disarm();
				if (fBlendBackgroundJob.fStarted) {
					// we need to reset the background color
					Display.getDefault().asyncExec(() -> setBackground(fResourceManager.createColor(fColorDefaultBackground)));
				}

				printResult(script.getResult());
				break;

			default:
				// do nothing
				break;
			}

		} catch (final Exception e) {
			// script.getCode() failed, ignore
		}
	}

	/**
	 * Print the script command.
	 *
	 * @param message
	 *            message to print
	 */
	public void printCommand(final String message) {
		if (message != null) {
			// print to output pane
			Display.getDefault().asyncExec(() -> {
				if (!isDisposed()) {
					if (!getText().isEmpty())
						append("\n");

					// create new style range
					final StyleRange styleRange = getStyle(STYLE_COMMAND, getText().length(), message.trim().length());

					append(message.trim());
					setStyleRange(styleRange);

					// scroll to end of window
					scrollToEnd();
				}
			});
		}
	}

	/**
	 * Print the script result.
	 *
	 * @param result
	 *            script execution result
	 */
	public void printResult(ScriptResult result) {
		final String message = getResultMessage(result);
		final Image image = getResultImage(result);

		// // print to output pane
		Display.getDefault().asyncExec(() -> {
			// indent message
			final String out = message.replaceAll("\\r?\\n", "\n\t");

			if (!isDisposed()) {

				// append image
				if (image != null) {
					append("\n   "); // dummy character to be replaced by image

					final StyleRange styleRange = new StyleRange();
					styleRange.start = getText().length() - 1;
					styleRange.length = 1;
					styleRange.data = image;
					final Rectangle rect = image.getBounds();
					styleRange.metrics = new GlyphMetrics(rect.height, 0, rect.width);

					setStyleRange(styleRange);
				} else
					append("\n\t");

				// append message
				append(out);

				try {
					result.get();
					setStyleRange(getStyle(STYLE_RESULT, getText().length(), out.length()));

				} catch (ExecutionException | InterruptedException e) {
					setStyleRange(getStyle(STYLE_ERROR, getText().length(), out.length()));
				}

				// scroll to end of window
				scrollToEnd();
			}
		});
	}

	/**
	 * Vertically scroll to the end of the history text.
	 */
	private void scrollToEnd() {
		setHorizontalPixel(0);
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=538973
		// seems the StyledText widget has a strange bug when trying to scroll correctly. We therefore add a factor to the calculated pixel offset to make sure
		// we correctly scroll to the end.
		setTopPixel((int) (getLineHeight() * getLineCount() * 1.1));
	}

	/**
	 * Get a text representation for the script execution result.
	 *
	 * @param result
	 *            script result
	 * @return text string
	 */
	private String getResultMessage(ScriptResult result) {
		try {
			final Object executionResult = result.get();
			if (fCurrentEngine != null)
				return fCurrentEngine.toString(executionResult);
			else
				return (executionResult != null) ? executionResult.toString() : "null";

		} catch (ExecutionException | InterruptedException e) {
			final String message = e.getLocalizedMessage();
			return (message != null) ? message : e.getClass().getName();
		}
	}

	/**
	 * Get an image representation for the script execution result.
	 *
	 * @param result
	 *            script result
	 * @return image or <code>null</code>
	 */
	private Image getResultImage(ScriptResult result) {
		try {
			result.get(); // fetch result to provoke exception check

			if (fCurrentEngine != null) {
				final ScriptObjectType type = fCurrentEngine.getType(result.getResult());
				return getImage(type);
			}

		} catch (ExecutionException | InterruptedException e) {
			return Activator.getImage(Activator.PLUGIN_ID, "/icons/eobj16/script_exception.png", true);
		}

		return null;
	}

	/**
	 * Create a new style range for a given offset/length.
	 *
	 * @param style
	 *            style to use (see JavaScriptShell.STYLE_* constants)
	 * @param start
	 *            start of text to be styled
	 * @param length
	 *            length of text to be styled
	 * @return StyleRange for text
	 */
	private StyleRange getStyle(final int style, final int start, final int length) {

		final StyleRange styleRange = new StyleRange();
		styleRange.start = start;
		styleRange.length = length;

		switch (style) {
		case STYLE_RESULT:
			styleRange.foreground = fResourceManager.createColor(fColorDescriptorResult);
			break;

		case STYLE_COMMAND:
			styleRange.foreground = fResourceManager.createColor(fColorDescriptorCommand);
			styleRange.fontStyle = SWT.BOLD;
			break;

		case STYLE_ERROR:
			styleRange.foreground = fResourceManager.createColor(fColorDescriptorError);
			styleRange.fontStyle = SWT.ITALIC;
			break;

		default:
			break;
		}

		return styleRange;
	}
}
