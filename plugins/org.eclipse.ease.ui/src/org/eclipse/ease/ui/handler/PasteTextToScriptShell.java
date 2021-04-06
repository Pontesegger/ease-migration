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
package org.eclipse.ease.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ease.IScriptEngineProvider;
import org.eclipse.ease.ui.view.ScriptShell;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

public class PasteTextToScriptShell extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof ITextSelection) {
			final IViewReference viewReference = findViewReference(ScriptShell.VIEW_ID);
			if (viewReference != null) {
				final IViewPart view = viewReference.getView(true);

				String selectedText = ((ITextSelection) selection).getText();
				final boolean isEmptySelection = selection.isEmpty() || (selectedText == null) || selectedText.isEmpty();

				selectedText = isEmptySelection ? getCurrentLineFromActiveEditor() : selectedText;
				if ((!selectedText.isEmpty()) && (view instanceof IScriptEngineProvider))
					((IScriptEngineProvider) view).getScriptEngine().execute(selectedText);
			}
		}

		return null;
	}

	private String getCurrentLineFromActiveEditor() {
		final IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

		final Control editorControl = editor.getAdapter(Control.class);
		if (editorControl instanceof StyledText) {
			if (editor instanceof ITextEditor) {
				final IDocument document = ((ITextEditor) editor).getDocumentProvider().getDocument(editor.getEditorInput());
				if (document != null) {
					try {
						final IRegion lineInformation = document.getLineInformationOfOffset(((StyledText) editorControl).getCaretOffset());
						return document.get(lineInformation.getOffset(), lineInformation.getLength()).trim();
					} catch (final BadLocationException e) {
						// ignore gracefully;
					}
				}
			}
		}

		return "";
	}

	public static IViewReference findViewReference(String idOrTitle) {
		for (final IWorkbenchWindow workbenchWindow : PlatformUI.getWorkbench().getWorkbenchWindows()) {
			for (final IWorkbenchPage page : workbenchWindow.getPages()) {
				for (final IViewReference viewReference : page.getViewReferences()) {
					if ((idOrTitle.equals(viewReference.getId())) || (idOrTitle.equals(viewReference.getTitle())))
						return viewReference;
				}
			}
		}

		return null;
	}
}
