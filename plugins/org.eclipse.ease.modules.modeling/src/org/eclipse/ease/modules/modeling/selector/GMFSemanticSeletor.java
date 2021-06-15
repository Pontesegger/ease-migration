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

/**
 * This selector is used to retreive an EObject from the selection
 *
 * @author adaussy
 *
 */
public class GMFSemanticSeletor extends ModelingSelector {

	public static final String SELECTOR_ID = "GMFSemanticSelector";

	public GMFSemanticSeletor() {
	}

	@Override
	protected EObject getEObject(final Object in) {
		if (in instanceof IGraphicalEditPart) {
			return ((IGraphicalEditPart) in).resolveSemanticElement();
		}
		return super.getEObject(in);
	}

}
