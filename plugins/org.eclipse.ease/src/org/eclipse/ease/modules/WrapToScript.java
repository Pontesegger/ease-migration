/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
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
package org.eclipse.ease.modules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation indicating that the annotated method should be wrapped to the target script language. Wrappers add script code that automatically calls the
 * annotated Java method.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD })
public @interface WrapToScript {

	/** Delimiter for alias names. */
	String DELIMITER = ";";

	/**
	 * Defines alias names for the same command. Names are delimited by ";"
	 */
	String alias() default "";

	/**
	 * Provide a list of supported script languages. Language names shall match the <i>name</i> field of the <i>scriptType</i> extension point. Multiple
	 * languages may be provided via a comma separated list. Explicitly excluding a language is supported via <i>!scriptType</i>. Mixing include and exclude
	 * patterns is not supported. By default all languages will be supported.
	 */
	String supportedLanguages() default "";
}
