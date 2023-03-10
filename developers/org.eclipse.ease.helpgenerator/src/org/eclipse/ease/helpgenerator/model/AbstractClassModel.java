/*******************************************************************************
 * Copyright (c) 2019 Christian Pontesegger and others.
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

package org.eclipse.ease.helpgenerator.model;

import java.util.Collection;
import java.util.List;

public abstract class AbstractClassModel {

	private String fClassName;
	private Description fClassDocumentation;
	private List<Field> fExportedFields;
	private List<Method> fExportedMethods;
	private Collection<String> fImportedClasses;
	private String fDeprecationMessage = null;

	public String getClassName() {
		return fClassName;
	}

	public Description getClassDocumentation() {
		return fClassDocumentation;
	}

	public List<Field> getExportedFields() {
		return fExportedFields;
	}

	public List<Method> getExportedMethods() {
		return fExportedMethods;
	}

	public Collection<String> getImportedClasses() {
		return fImportedClasses;
	}

	public String getDeprecationMessage() {
		return fDeprecationMessage;
	}

	public boolean isDeprecated() {
		return fDeprecationMessage != null;
	}

	protected void setClassName(String className) {
		fClassName = className;
	}

	protected void setClassDocumentation(Description classDocumentation) {
		fClassDocumentation = classDocumentation;
	}

	protected void setExportedFields(List<Field> exportedFields) {
		fExportedFields = exportedFields;
		fExportedFields.sort((a, b) -> a.getName().compareTo(b.getName()));
	}

	protected void setExportedMethods(List<Method> exportedMethods) {
		fExportedMethods = exportedMethods;
		fExportedMethods.sort((a, b) -> a.getName().compareTo(b.getName()));
	}

	public void setImportedClasses(Collection<String> importedClasses) {
		fImportedClasses = importedClasses;
	}

	public void setDeprecationMessage(String deprecationMessage) {
		fDeprecationMessage = deprecationMessage;
	}

	public abstract void populateModel();
}
