/*******************************************************************************
 * Copyright (c) 2013 Atos
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Arthur Daussy - initial implementation
 *******************************************************************************/
package org.eclipse.ease.modules.modeling.selector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.ease.modules.modeling.ISelector;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;

/**
 * This selector is used to return object from a modeling editor (GMF or Generated from GenModel)
 *
 * @author adaussy
 *
 */
public class ModelingSelector implements ISelector {

	public ModelingSelector() {
		super();
	}

	@Override
	public Object getCustomSelection(final Object selection) {
		if (selection instanceof ISelection) {
			if (selection instanceof IStructuredSelection) {
				List<EObject> result = new ArrayList<EObject>();
				Iterator<?> ite = ((IStructuredSelection) selection).iterator();
				while (ite.hasNext()) {
					Object next = ite.next();
					EObject eObject = getEObject(next);
					if (eObject != null) {
						result.add(eObject);
					}
				}
				if (result.isEmpty()) {
					return null;
				} else if (result.size() == 1) {
					return result.get(0);
				}
				return result;
			}
		}
		return null;
	}

	protected EObject getEObject(final Object in) {
		if (in instanceof EObject) {
			return (EObject) in;
		} else if (in instanceof IAdaptable) {
			return (EObject) ((IAdaptable) in).getAdapter(EObject.class);
		} else {
			IAdapterManager adapterService = (IAdapterManager) PlatformUI.getWorkbench().getService(IAdapterManager.class);
			return (EObject) adapterService.getAdapter(in, EObject.class);
		}
	}

}
