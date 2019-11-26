/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.lang.javascript.rhino.debugger.editor;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.ease.debugging.model.EaseDebugStackFrame;
import org.eclipse.ease.lang.javascript.rhino.debugger.model.RhinoDebugModelPresentation;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.wst.jsdt.debug.internal.ui.eval.ExpressionInformationControlCreator;
import org.eclipse.wst.jsdt.ui.text.java.hover.IJavaEditorTextHover;

/**
 * Slightly adapted version of: org.eclipse.wst.jsdt.debug.internal.ui.eval.JavaScriptDebugHover
 */
public class VariablesTextHover implements IJavaEditorTextHover, ITextHover, ITextHoverExtension, ITextHoverExtension2 {

	/**
	 * Returns HTML text for the given variable
	 */
	private static String getVariableText(IVariable variable) {
		final StringBuilder buffer = new StringBuilder();

		final RhinoDebugModelPresentation modelPresentation = new RhinoDebugModelPresentation();

		buffer.append("<p><pre>"); //$NON-NLS-1$
		final String variableText = modelPresentation.getText(variable);
		buffer.append(replaceHTMLChars(variableText));
		buffer.append("</pre></p>"); //$NON-NLS-1$

		modelPresentation.dispose();

		return (buffer.length() > 0) ? buffer.toString() : null;
	}

	/**
	 * Replaces reserved HTML characters in the given string with their escaped equivalents. This is to ensure that variable values containing reserved
	 * characters are correctly displayed.
	 */
	private static String replaceHTMLChars(String variableText) {
		final StringBuffer buffer = new StringBuffer(variableText.length());
		final char[] characters = variableText.toCharArray();
		for (final char character : characters) {
			switch (character) {
			case '<':
				buffer.append("&lt;"); //$NON-NLS-1$
				break;
			case '>':
				buffer.append("&gt;"); //$NON-NLS-1$
				break;
			case '&':
				buffer.append("&amp;"); //$NON-NLS-1$
				break;
			case '"':
				buffer.append("&quot;"); //$NON-NLS-1$
				break;
			default:
				buffer.append(character);
			}
		}
		return buffer.toString();
	}

	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		final IVariable variable = getHoverInfo2(textViewer, hoverRegion);
		if (variable != null)
			return getVariableText(variable);

		return null;
	}

	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		return JavaScriptWordFinder.findWord(textViewer.getDocument(), offset);
	}

	@Override
	public IVariable getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
		final EaseDebugStackFrame frame = getFrame();
		if (frame != null) {
			final IDocument document = textViewer.getDocument();
			if (document != null) {
				try {
					final String variableName = document.get(hoverRegion.getOffset(), hoverRegion.getLength());
					return findLocalVariable(frame, variableName);

				} catch (final BadLocationException e) {
					return null;
				}
			}
		}

		return null;
	}

	/**
	 * Returns a local variable in the given frame based on the the given name or <code>null</code> if none.
	 *
	 * @return local variable or <code>null</code>
	 */
	private IVariable findLocalVariable(EaseDebugStackFrame frame, String variableName) {
		if (frame != null) {
			for (final IVariable variable : frame.getVariables()) {
				try {
					if (variable.getName().equals(variableName))
						return variable;

				} catch (final DebugException e) {
				}
			}
		}

		return null;
	}

	@Override
	public IInformationControlCreator getHoverControlCreator() {
		return new ExpressionInformationControlCreator();
	}

	@Override
	public void setEditor(IEditorPart editor) {
	}

	/**
	 * Returns the stack frame in which to search for variables, or <code>null</code> if none.
	 *
	 * @return the stack frame in which to search for variables, or <code>null</code> if none
	 */
	private EaseDebugStackFrame getFrame() {
		final IAdaptable context = DebugUITools.getDebugContext();
		if (context instanceof EaseDebugStackFrame)
			return (EaseDebugStackFrame) context;

		return null;
	}
}