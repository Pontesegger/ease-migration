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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.NoType;
import javax.lang.model.util.ElementScanner9;

import org.eclipse.ease.helpgenerator.model.AbstractClassModel;
import org.eclipse.ease.helpgenerator.model.Description;
import org.eclipse.ease.helpgenerator.model.ExceptionValue;
import org.eclipse.ease.helpgenerator.model.Field;
import org.eclipse.ease.helpgenerator.model.Method;
import org.eclipse.ease.helpgenerator.model.Parameter;
import org.eclipse.ease.helpgenerator.model.ReturnValue;
import org.eclipse.ease.helpgenerator.model.ScriptExample;
import org.eclipse.ease.modules.ScriptParameter;
import org.eclipse.ease.modules.WrapToScript;

import com.sun.source.doctree.DeprecatedTree;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.ReturnTree;
import com.sun.source.doctree.ThrowsTree;
import com.sun.source.doctree.UnknownBlockTagTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.util.SimpleDocTreeVisitor;
import com.sun.source.util.SimpleTreeVisitor;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Type;

import jdk.javadoc.doclet.DocletEnvironment;

public class Jep221ClassModel extends AbstractClassModel {

	private final ClassSymbol fClassSymbol;
	private final DocletEnvironment fEnvironment;

	public Jep221ClassModel(DocletEnvironment environment, ClassSymbol classSymbol) {
		fEnvironment = environment;
		fClassSymbol = classSymbol;
	}

	@Override
	public void populateModel() {
		setClassName(fClassSymbol.className());

		setClassDocumentation(new Description(getCommentFor(fClassSymbol)));

		final DocCommentTree tree = fEnvironment.getDocTrees().getDocCommentTree(fClassSymbol);
		String deprecationText = new DeprecationMessageScanner().visit(tree, null);
		if ((deprecationText == null) && (fClassSymbol.getAnnotation(Deprecated.class) != null))
			deprecationText = "Module deprecated";

		setDeprecationMessage(deprecationText);

		final boolean hasAnnotation = hasWrapToScriptAnnotation();

		setExportedFields(new FieldScanner().scan(fClassSymbol, hasAnnotation));
		setExportedMethods(new MethodScanner().scan(fClassSymbol, hasAnnotation));

		setImportedClasses(Collections.emptyList());
	}

	private String getCommentFor(Element element) {
		final DocCommentTree elementComment = fEnvironment.getDocTrees().getDocCommentTree(element);
		if (elementComment != null) {
			final List<? extends DocTree> body = elementComment.getFullBody();
			return body.stream().map(e -> e.toString()).collect(Collectors.joining());
		}

		return null;
	}

	private boolean hasWrapToScriptAnnotation() {
		return new WrapToScriptScanner().scan(fClassSymbol);
	}

	private class ImportScanner extends SimpleTreeVisitor<Void, Void> {

		@Override
		public Void visitImport(ImportTree node, Void p) {
			// TODO Auto-generated method stub
			return super.visitImport(node, p);
		}
	}

	private abstract class RecursiveScanner<R, P> extends ElementScanner9<R, P> {

		private final List<Object> fScannedElementsCache = new ArrayList<>();

		@Override
		public R scan(Element e, P p) {
			super.scan(e, p);

			if (needsScanning(e)) {
				addToScanCache((ClassSymbol) e);

				for (final Type iface : ((ClassSymbol) e).getInterfaces())
					scan(iface.asElement(), p);

				final Type superclass = ((ClassSymbol) e).getSuperclass();
				if (!(superclass instanceof NoType))
					scan(superclass.asElement(), p);
			}

			return getResult();
		}

		private boolean needsScanning(Element e) {
			return (e instanceof ClassSymbol) && (!isContainedInScanCache((ClassSymbol) e));
		}

		private void addToScanCache(ClassSymbol classSymbol) {
			fScannedElementsCache.add(classSymbol.getQualifiedName());
		}

		private boolean isContainedInScanCache(ClassSymbol classSymbol) {
			return fScannedElementsCache.contains(classSymbol.getQualifiedName());
		}

		protected abstract R getResult();
	}

	private class WrapToScriptScanner extends RecursiveScanner<Boolean, Void> {

		private boolean fHasAnnotation = false;

		@Override
		public Boolean visitExecutable(ExecutableElement e, Void p) {
			final WrapToScript annotation = e.getAnnotation(WrapToScript.class);
			fHasAnnotation |= (annotation != null);

			return getResult();
		}

		@Override
		public Boolean visitVariable(VariableElement e, Void p) {
			final WrapToScript annotation = e.getAnnotation(WrapToScript.class);
			fHasAnnotation |= (annotation != null);

			return getResult();
		}

		@Override
		protected Boolean getResult() {
			return fHasAnnotation;
		}
	}

	private class FieldScanner extends RecursiveScanner<List<Field>, Boolean> {

		private final List<Field> fFields = new ArrayList<>();

		@Override
		public List<Field> visitVariable(VariableElement e, Boolean p) {

			if (!p || (e.getAnnotation(WrapToScript.class) != null)) {
				if ((e.getModifiers().contains(Modifier.STATIC)) && (e.getModifiers().contains(Modifier.PUBLIC))
						&& (e.getModifiers().contains(Modifier.FINAL))) {

					final DocCommentTree tree = fEnvironment.getDocTrees().getDocCommentTree(e);
					String deprecationText = new DeprecationMessageScanner().visit(tree, getDeprecationMessage());
					if ((deprecationText == null) && (e.getAnnotation(Deprecated.class) != null))
						deprecationText = "";

					fFields.add(new Field(e.getSimpleName().toString(), getCommentFor(e), deprecationText));
				}
			}

			return super.visitVariable(e, p);
		}

		@Override
		protected List<Field> getResult() {
			return fFields;
		}
	}

	private class MethodScanner extends RecursiveScanner<List<Method>, Boolean> {

		private final List<Method> fMethods = new ArrayList<>();

		@Override
		public List<Method> visitExecutable(ExecutableElement e, Boolean hasWrapToScriptAnnotation) {
			final Method registeredMethod = findRegisteredMethod(e.getSimpleName().toString());

			final WrapToScript wrapAnnotation = e.getAnnotation(WrapToScript.class);
			if (!hasWrapToScriptAnnotation || (wrapAnnotation != null) || (registeredMethod != null)) {
				if (e.getModifiers().contains(Modifier.PUBLIC)) {

					final DocCommentTree tree = fEnvironment.getDocTrees().getDocCommentTree(e);
					String deprecationText = new DeprecationMessageScanner().visit(tree, getDeprecationMessage());
					if ((deprecationText == null) && (e.getAnnotation(Deprecated.class) != null))
						deprecationText = "";

					final Collection<String> aliases = new HashSet<>();
					if (wrapAnnotation != null) {
						final String candidates = wrapAnnotation.alias();
						for (final String token : candidates.split("[,;]")) {
							if (!token.trim().isEmpty())
								aliases.add(token.trim());
						}
					}

					final ReturnValue returnValue = new ReturnValue(e.getReturnType().toString(), new ReturnTypeMessageScanner().visit(tree, null));

					final List<Parameter> parameters = e.getParameters().stream().map(param -> {
						final ScriptParameter scriptParameter = param.getAnnotation(ScriptParameter.class);
						final String defaultValue = (scriptParameter != null) ? scriptParameter.defaultValue() : null;

						final String comment = new ParameterCommentScanner().visit(tree, param.getSimpleName().toString());

						return new Parameter(param.getSimpleName().toString(), param.asType().toString(), comment, defaultValue);
					}).collect(Collectors.toList());

					final List<ExceptionValue> exceptions = e.getThrownTypes().stream().map(type -> {
						return new ExceptionValue(type.toString(), new ExceptionMessageScanner().visit(tree, type.toString()));
					}).collect(Collectors.toList());

					final List<ScriptExample> examples = new ScriptExampleScanner().visit(tree, null);

					addMethod(new Method(e.getSimpleName().toString(), getCommentFor(e), deprecationText, aliases, returnValue, parameters, exceptions,
							examples));
				}
			}

			return getResult();
		}

		private Method findRegisteredMethod(String methodName) {
			return fMethods.stream().filter(m -> methodName.equals(m.getName())).findFirst().orElse(null);
		}

		private void addMethod(Method method) {
			final Method existingMethod = findRegisteredMethod(method.getName());
			if (existingMethod != null)
				existingMethod.fetchDetailsFrom(method);
			else
				fMethods.add(method);
		}

		@Override
		protected List<Method> getResult() {
			return fMethods;
		}
	}

	private class ScriptExampleScanner extends SimpleDocTreeVisitor<List<ScriptExample>, Void> {

		private final List<ScriptExample> fScriptExamples = new ArrayList<>();

		@Override
		public List<ScriptExample> visitDocComment(DocCommentTree node, Void p) {
			visit(node.getBlockTags(), p);

			return fScriptExamples;
		}

		@Override
		public List<ScriptExample> visitUnknownBlockTag(UnknownBlockTagTree node, Void p) {
			final String content = node.getContent().stream().map(e -> e.toString()).collect(Collectors.joining());
			fScriptExamples.add(new ScriptExample(content));

			return fScriptExamples;
		}
	}

	private class ParameterCommentScanner extends SimpleDocTreeVisitor<String, String> {

		private String fComment;

		@Override
		public String visitDocComment(DocCommentTree node, String p) {
			visit(node.getBlockTags(), p);

			return fComment;
		}

		@Override
		public String visitParam(ParamTree node, String p) {
			final List<? extends DocTree> body = node.getDescription();
			if (p.equals(node.getName().toString()))
				fComment = body.stream().map(e -> e.toString()).collect(Collectors.joining());

			return super.visitParam(node, p);
		}
	}

	private class ExceptionMessageScanner extends SimpleDocTreeVisitor<String, String> {
		private String fComment;

		@Override
		public String visitDocComment(DocCommentTree node, String p) {
			visit(node.getBlockTags(), p);

			return fComment;
		}

		@Override
		public String visitThrows(ThrowsTree node, String p) {
			final List<? extends DocTree> body = node.getDescription();
			if (p.endsWith(node.getExceptionName().toString()))
				fComment = body.stream().map(e -> e.toString()).collect(Collectors.joining());

			return super.visitThrows(node, p);
		}
	}

	private class ReturnTypeMessageScanner extends SimpleDocTreeVisitor<String, Void> {

		String fDocumentation;

		@Override
		public String visitDocComment(DocCommentTree node, Void p) {
			visit(node.getBlockTags(), p);

			return fDocumentation;
		}

		@Override
		public String visitReturn(ReturnTree node, Void p) {
			final List<? extends DocTree> body = node.getDescription();
			fDocumentation = body.stream().map(e -> e.toString()).collect(Collectors.joining());

			return fDocumentation;
		}
	}

	private class DeprecationMessageScanner extends SimpleDocTreeVisitor<String, String> {
		String fDocumentation;

		@Override
		public String visitDocComment(DocCommentTree node, String p) {
			visit(node.getBlockTags(), p);

			return (fDocumentation != null) ? fDocumentation : p;
		}

		@Override
		public String visitDeprecated(DeprecatedTree node, String p) {
			final List<? extends DocTree> body = node.getBody();
			fDocumentation = body.stream().map(e -> e.toString()).collect(Collectors.joining());

			return fDocumentation;
		}
	}
}
