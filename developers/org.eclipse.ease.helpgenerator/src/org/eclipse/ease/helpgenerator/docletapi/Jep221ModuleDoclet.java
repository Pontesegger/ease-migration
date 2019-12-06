/*******************************************************************************
 * Copyright (c) 2019 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.helpgenerator.docletapi;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.tools.Diagnostic.Kind;

import org.eclipse.ease.helpgenerator.AbstractModuleDoclet;
import org.eclipse.ease.helpgenerator.IReporter;
import org.eclipse.ease.helpgenerator.model.AbstractClassModel;
import org.eclipse.ease.helpgenerator.model.ModuleDefinition;

import com.sun.tools.javac.code.Symbol.ClassSymbol;

import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

public class Jep221ModuleDoclet extends AbstractModuleDoclet {

	private DocletEnvironment fEnvironment;
	private final Reporter fReporter;

	public Jep221ModuleDoclet(Reporter reporter) {
		fReporter = reporter;
	}

	@Override
	protected IReporter createReporter() {
		return new Java11DocletReporter(fReporter);
	}

	public void setDocletEnvironment(DocletEnvironment environment) {
		fEnvironment = environment;
	}

	@Override
	protected AbstractClassModel getClassModel(ModuleDefinition module) {
		for (final Element element : fEnvironment.getIncludedElements()) {
			if (ElementKind.CLASS.equals(element.getKind())) {
				final ClassSymbol classSymbol = ((ClassSymbol) element);

				if (classSymbol.className().equals(module.getClassName())) {
					final AbstractClassModel classModel = new Jep221ClassModel(fEnvironment, classSymbol);
					classModel.populateModel();
					return classModel;
				}
			}
		}

		return null;
	}

	private class Java11DocletReporter implements IReporter {

		private final Reporter fReporter;
		private boolean fHasErrors = false;

		public Java11DocletReporter(Reporter reporter) {
			fReporter = reporter;
		}

		@Override
		public void report(int status, String message) {
			switch (status) {
			case INFO:
				fReporter.print(Kind.NOTE, message);
				break;
			case WARNING:
				fReporter.print(Kind.WARNING, message);
				break;
			case ERROR:
				// fall through
			default:
				fReporter.print(Kind.ERROR, message);
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
