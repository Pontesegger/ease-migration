/*******************************************************************************
 * Copyright (c) 2013 Atos
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License_Identifier: EPL-2.0
 *
 * Contributors:
 *     Arthur Daussy - initial implementation
 *******************************************************************************/
package org.eclipse.ease.modules.modeling.selector;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.notation.View;

/**
 * Selector used to retrieve a view from the selection
 *
 * @author adaussy
 *
 */
public class GMFNotationSelector extends ModelingSelector {

	public static final String SELECTOR_ID = "GMFNotationSelector";

	@Override
	protected EObject getEObject(final Object in) {
		Object result = null;
		if (in instanceof View) {
			result = in;
		} else if (in instanceof IGraphicalEditPart) {
			result = ((IGraphicalEditPart) in).getModel();
		} else {
			result = super.getEObject(in);
		}
		if (result instanceof View) {
			return (View) result;
		}
		return null;
	}

}
