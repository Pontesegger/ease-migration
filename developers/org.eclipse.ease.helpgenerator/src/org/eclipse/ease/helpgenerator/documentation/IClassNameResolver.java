/*******************************************************************************
 * Copyright (c) 2020 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.helpgenerator.documentation;

@FunctionalInterface
public interface IClassNameResolver {

	/**
	 * Resolve a given class name to a full qualified name. <i>className</i> might be the short name of an import or the name of a class in java.lang, which
	 * does not require a FQN in javadocs.
	 *
	 * @param className
	 *            given class name
	 * @return full qualified class name
	 */
	String resolveClassName(String className);
}
