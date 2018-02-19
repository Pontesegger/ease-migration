/*******************************************************************************
 * Copyright (c) 2015 Atos
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Guillaume Renier - initial implementation
 *******************************************************************************/
package org.eclipse.ease.modules.modeling;

import org.eclipse.ease.modules.AbstractScriptModule;
import org.eclipse.ease.modules.WrapToScript;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.UMLFactory;

/**
 * This module help to handle UML models.
 */
public class SYSMLModule extends AbstractScriptModule {

	/**
	 * Get the UML model from the current active editor
	 *
	 * @param qualifiedName
	 *            qualified name of the model
	 *
	 * @return UML model
	 */
	@WrapToScript
	public EObject createSysML(final String qualifiedName) {
		final Class clazz = ((UMLFactory) getEnvironment().getModule(EcoreModule.class).getFactory()).createClass();
		final EList<Stereotype> stereotypes = clazz.getApplicableStereotypes();
		for (final Stereotype s : stereotypes) {
			if (s.getQualifiedName().equals(qualifiedName)) {
				final EObject sysml = clazz.applyStereotype(s);
				return sysml;
			}
		}
		return null;
	}
}
