/*******************************************************************************
 * Copyright (c) 2014 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.helpgenerator.sunapi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.ease.helpgenerator.model.AbstractClassModel;
import org.eclipse.ease.helpgenerator.model.Description;
import org.eclipse.ease.helpgenerator.model.ExceptionValue;
import org.eclipse.ease.helpgenerator.model.Field;
import org.eclipse.ease.helpgenerator.model.Method;
import org.eclipse.ease.helpgenerator.model.Parameter;
import org.eclipse.ease.helpgenerator.model.ReturnValue;
import org.eclipse.ease.helpgenerator.model.ScriptExample;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MemberDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.Tag;
import com.sun.javadoc.ThrowsTag;
import com.sun.javadoc.Type;

public class Java5ClassModel extends AbstractClassModel {

	private static final String WRAP_TO_SCRIPT = "WrapToScript";
	private static final String QUALIFIED_WRAP_TO_SCRIPT = "org.eclipse.ease.modules." + WRAP_TO_SCRIPT;

	private static final Object SCRIPT_PARAMETER = "ScriptParameter";
	private static final Object QUALIFIED_SCRIPT_PARAMETER = "org.eclipse.ease.modules." + SCRIPT_PARAMETER;

	private static String getParameterComment(final MethodDoc method, final String name) {
		final String comment = extractComment(method, method1 -> {
			for (final ParamTag paramTags : method1.paramTags()) {
				if (name.equals(paramTags.parameterName()))
					return paramTags.parameterComment();
			}

			return "";
		});

		return comment;
	}

	private static String getDefaultValue(com.sun.javadoc.Parameter param) {
		final AnnotationDesc parameterAnnotation = getScriptParameterAnnotation(param);

		if (parameterAnnotation != null) {
			for (final ElementValuePair pair : parameterAnnotation.elementValues()) {
				if ("org.eclipse.ease.modules.ScriptParameter.defaultValue()".equals(pair.element().toString()))
					return pair.value().toString();
			}
		}

		return null;
	}

	private static AnnotationDesc getScriptParameterAnnotation(final com.sun.javadoc.Parameter parameter) {
		for (final AnnotationDesc annotation : parameter.annotations()) {
			if (isScriptParameterAnnotation(annotation))
				return annotation;
		}

		return null;
	}

	private static boolean isScriptParameterAnnotation(final AnnotationDesc annotation) {
		return (QUALIFIED_SCRIPT_PARAMETER.equals(annotation.annotationType().qualifiedName()))
				|| (SCRIPT_PARAMETER.equals(annotation.annotationType().qualifiedName()));
	}

	private static String extractComment(MethodDoc method, CommentExtractor extractor) {
		String comment = extractor.extract(method);
		if ((comment != null) && (!comment.isEmpty()))
			return comment;

		// try to look up interfaces
		for (final ClassDoc iface : method.containingClass().interfaces()) {
			for (final MethodDoc ifaceMethod : iface.methods()) {
				if (method.overrides(ifaceMethod)) {
					comment = extractComment(ifaceMethod, extractor);
					if ((comment != null) && (!comment.isEmpty()))
						return comment;
				}
			}
		}

		// not found, retry with super class
		final ClassDoc parent = method.containingClass().superclass();
		if (parent != null) {
			for (final MethodDoc superMethod : parent.methods()) {
				if (method.overrides(superMethod))
					return (extractComment(superMethod, extractor));
			}
		}

		return "";
	}

	private static AnnotationDesc getWrapAnnotation(final ProgramElementDoc method) {
		for (final AnnotationDesc annotation : method.annotations()) {
			if (isWrapToScriptAnnotation(annotation))
				return annotation;
		}

		return null;
	}

	private static boolean isWrapToScriptAnnotation(final AnnotationDesc annotation) {
		return (QUALIFIED_WRAP_TO_SCRIPT.equals(annotation.annotationType().qualifiedName()))
				|| (WRAP_TO_SCRIPT.equals(annotation.annotationType().qualifiedName()));
	}

	private static boolean isDeprecated(final MemberDoc field) {
		final Tag[] tags = field.tags("deprecated");
		return (tags != null) && (tags.length > 0);
	}

	private static Collection<String> getFunctionAliases(final MethodDoc method) {
		final Collection<String> aliases = new HashSet<>();
		final AnnotationDesc annotation = getWrapAnnotation(method);
		if (annotation != null) {
			for (final ElementValuePair pair : annotation.elementValues()) {
				if ("alias".equals(pair.element().name())) {
					String candidates = pair.value().toString();
					candidates = candidates.substring(1, candidates.length() - 1);
					for (final String token : candidates.split("[,;]")) {
						if (!token.trim().isEmpty())
							aliases.add(token.trim());
					}
				}
			}
		}

		return aliases;
	}

	private final ClassDoc fClassDoc;

	public Java5ClassModel(ClassDoc classDoc) {
		fClassDoc = classDoc;
	}

	@Override
	public void populateModel() {
		setClassName(fClassDoc.name());

		setClassDocumentation(new Description(fClassDoc.commentText()));

		setExportedFields(fetchExportedFields());
		setExportedMethods(fetchExportedMethods());

		setImportedClasses(fetchImportedClasses());
	};

	private List<String> fetchImportedClasses() {
		try {
			return Arrays.asList(fClassDoc.importedClasses()).stream().map(f -> f.toString()).collect(Collectors.toList());
		} catch (final NullPointerException e) {
			// we might get an NPE @ com.sun.tools.javadoc.ClassDocImpl.importedClasses(ClassDocImpl.java:1111)
			// nothing we can do about it, but skipping imports for this file
			return Collections.emptyList();
		}
	}

	private List<Field> fetchExportedFields() {
		final List<FieldDoc> fields = new ArrayList<>();

		final boolean hasAnnotation = hasWrapToScriptAnnotation();

		final ArrayList<ClassDoc> candidates = new ArrayList<>();
		candidates.add(fClassDoc);
		while (!candidates.isEmpty()) {
			final ClassDoc clazzDoc = candidates.remove(0);

			for (final FieldDoc field : clazzDoc.fields()) {
				if ((!hasAnnotation) || (getWrapAnnotation(field) != null))
					fields.add(field);
			}

			// add interfaces
			candidates.addAll(Arrays.asList(clazzDoc.interfaces()));

			// add super class/interface
			final ClassDoc nextCandidate = clazzDoc.superclass();
			if ((nextCandidate != null) && (!Object.class.getName().equals(nextCandidate.qualifiedName())))
				candidates.add(nextCandidate);
		}

		// sort fields alphabetically
		return fields.stream().map(doc -> {
			final String deprecationMessage = isDeprecated(doc) ? doc.tags("deprecated")[0].text() : null;
			return new Field(doc.name(), doc.commentText(), deprecationMessage);
		}).collect(Collectors.toList());
	}

	private boolean hasWrapToScriptAnnotation() {
		ClassDoc classDoc = fClassDoc;
		while (classDoc != null) {
			for (final MethodDoc method : classDoc.methods()) {
				if (getWrapAnnotation(method) != null)
					return true;
			}

			for (final FieldDoc field : classDoc.fields()) {
				if (getWrapAnnotation(field) != null)
					return true;
			}

			classDoc = classDoc.superclass();
		}

		return false;
	}

	private List<Method> fetchExportedMethods() {
		return new MethodExtractor().getMethods(fClassDoc, hasWrapToScriptAnnotation());
	}

	private String getExceptionComment(MethodDoc method, Type exceptionType) {
		final String comment = extractComment(method, method1 -> {

			for (final ThrowsTag tag : method1.throwsTags()) {
				if ((exceptionType.simpleTypeName().equals(tag.exceptionName())) || (exceptionType.typeName().equals(tag.exceptionName())))
					return tag.exceptionComment();
			}

			return "";
		});

		return comment;
	}

	private static interface CommentExtractor {
		String extract(MethodDoc method);
	}

	private class MethodExtractor {
		private final List<Method> fMethods = new ArrayList<>();

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

		public List<Method> getMethods(ClassDoc classDoc, boolean hasWrapToScriptAnnotation) {

			for (final MethodDoc doc : classDoc.methods()) {
				final Method registeredMethod = findRegisteredMethod(doc.name());
				final AnnotationDesc wrapAnnotation = getWrapAnnotation(doc);
				if (!hasWrapToScriptAnnotation || (wrapAnnotation != null) || (registeredMethod != null)) {
					if (doc.isPublic()) {
						final String deprecationMessage = isDeprecated(doc) ? doc.tags("deprecated")[0].text() : null;

						final String returnComment = (doc.tags("return").length > 0) ? doc.tags("return")[0].text() : null;
						final ReturnValue returnValue = new ReturnValue(doc.returnType().qualifiedTypeName(), returnComment);

						final List<Parameter> parameters = Arrays.asList(doc.parameters()).stream().map(param -> {
							final String defaultValue = getDefaultValue(param);
							return new Parameter(param.name(), param.typeName(), getParameterComment(doc, param.name()), defaultValue);
						}).collect(Collectors.toList());

						final List<ExceptionValue> exceptions = Arrays.asList(doc.thrownExceptionTypes()).stream()
								.map(e -> new ExceptionValue(e.qualifiedTypeName(), getExceptionComment(doc, e))).collect(Collectors.toList());

						final List<ScriptExample> examples = Arrays.asList(doc.tags("scriptExample")).stream().map(example -> new ScriptExample(example.text()))
								.collect(Collectors.toList());

						addMethod(new Method(doc.name(), extractComment(doc, d -> d.commentText()), deprecationMessage, getFunctionAliases(doc), returnValue,
								parameters, exceptions, examples));
					}
				}
			}

			for (final ClassDoc interfaceDoc : classDoc.interfaces())
				getMethods(interfaceDoc, hasWrapToScriptAnnotation);

			final ClassDoc superclassDoc = classDoc.superclass();
			if ((superclassDoc != null) && (!Object.class.getName().equals(superclassDoc.qualifiedName())))
				getMethods(superclassDoc, hasWrapToScriptAnnotation);

			return fMethods;
		}
	}
}
