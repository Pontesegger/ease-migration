/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
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
package org.eclipse.ease.helpgenerator.sunapi;

import org.eclipse.ease.helpgenerator.AbstractModuleDoclet;
import org.eclipse.ease.helpgenerator.IReporter;
import org.eclipse.ease.helpgenerator.model.AbstractClassModel;
import org.eclipse.ease.helpgenerator.model.ModuleDefinition;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.RootDoc;

public class Java5ModuleDoclet extends AbstractModuleDoclet {

	private RootDoc fRootDoc;

	@Override
	protected IReporter createReporter() {
		return new OutputStreamReporter();
	}

	public void setRootDoc(RootDoc rootDoc) {
		fRootDoc = rootDoc;
	}

	@Override
	protected AbstractClassModel getClassModel(ModuleDefinition module) {
		for (final ClassDoc classDoc : fRootDoc.classes()) {
			if (classDoc.qualifiedName().equals(module.getClassName())) {
				final AbstractClassModel classModel = new Java5ClassModel(classDoc);
				classModel.populateModel();
				return classModel;
			}
		}

		return null;
	}

	private class OutputStreamReporter implements IReporter {

		private boolean fHasErrors = false;

		@Override
		public void report(int status, String message) {
			switch (status) {
			case INFO:
				System.out.println(message);
				break;
			case WARNING:
				System.out.println("WARNING: " + message);
				break;
			case ERROR:
				// fall through
			default:
				System.err.println("ERROR: " + message);
				fHasErrors = true;
				break;
			}
		}

		@Override
		public boolean hasErrors() {
			return fHasErrors;
		}

		@Override
		public void reportMissingDocs(String message) {
			report(failOnMissingDocs() ? ERROR : WARNING, message);
		}

		@Override
		public void reportInvalidHtml(String message) {
			report(failOnHtmlErrors() ? ERROR : WARNING, message);
		}
	}
}
