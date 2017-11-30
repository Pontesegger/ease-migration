/*******************************************************************************
 * Copyright (c) 2017 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.ui.debugging.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.ease.Script;
import org.eclipse.ease.debugging.DynamicContentEditorInput;
import org.eclipse.ease.debugging.model.EaseDebugStackFrame;
import org.eclipse.ease.debugging.model.EaseDebugVariable;
import org.eclipse.ease.debugging.model.EaseJavaFieldVariable;
import org.eclipse.ease.ui.Activator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;

public abstract class AbstractEaseDebugModelPresentation implements ILabelProvider {

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof EaseDebugVariable) {
			switch (((EaseDebugVariable) element).getType()) {
			case JAVA_OBJECT:
				if (element instanceof EaseJavaFieldVariable) {
					String imageName = "/icons/eobj16/debug_";

					if (((EaseJavaFieldVariable) element).isPublic())
						imageName += "public";
					else if (((EaseJavaFieldVariable) element).isPrivate())
						imageName += "private";
					else
						imageName += "protected";

					if (((EaseJavaFieldVariable) element).isFinal())
						imageName += "_final";

					imageName += "_field.png";
					return Activator.getImage(Activator.PLUGIN_ID, imageName, true);

				} else
					return Activator.getImage(Activator.PLUGIN_ID, "/icons/eobj16/debug_java_class.png", true);

			case NATIVE:
				// fall through
			case PRIMITIVE:
				return Activator.getImage(Activator.PLUGIN_ID, "/icons/eobj16/debug_local_variable.png", true);

			case NATIVE_ARRAY:
				return Activator.getImage(Activator.PLUGIN_ID, "/icons/eobj16/debug_local_array.png", true);

			case NATIVE_OBJECT:
				return Activator.getImage(Activator.PLUGIN_ID, "/icons/eobj16/debug_local_object.png", true);

			default:
				return null;
			}
		}

		return null;
	}

	@Override
	public String getText(final Object element) {
		if (element instanceof EaseDebugStackFrame) {
			// FIXME EaseDebugStackFrame.getName needs refactoring
			return ((EaseDebugStackFrame) element).getDebugFrame().getName();
		}

		return null;
	}

	public void setAttribute(final String attribute, final Object value) {
	}

	public IEditorInput getEditorInput(final Object element) {
		if (element instanceof Script) {
			final Object file = ((Script) element).getFile();
			if (file instanceof IFile)
				return new FileEditorInput((IFile) file);

			else
				return new DynamicContentEditorInput((Script) element);
		}

		return null;
	}
}
