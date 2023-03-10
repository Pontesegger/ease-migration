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
 *     Arthur Daussy - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.lang.python.jython;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.python.core.packagecache.SysPackageManager;

public class JythonPackageManager extends SysPackageManager {

	private final Map<String, Boolean> mLookupCache = new HashMap<String, Boolean>();

	public JythonPackageManager(final File cachedir, final Properties registry) {
		super(cachedir, registry);
	}

	@Override
	public boolean packageExists(final String pkg, final String name) {

		// create fully qualified name
		StringBuilder buffer = new StringBuilder();
		if ((pkg != null) && (!pkg.isEmpty()))
			buffer.append(pkg);

		if ((name != null) && (!name.isEmpty())) {
			if (buffer.length() > 0)
				buffer.append('.');

			buffer.append(name);
		}
		String qualifiedName = buffer.toString();

		// first lookup in cache
		if (mLookupCache.containsKey(qualifiedName))
			return mLookupCache.get(qualifiedName);

		if (isPythonLib(qualifiedName)) {
			// this is a python library, not a java package
			mLookupCache.put(qualifiedName, false);
			return false;
		}

		// not from python, might be something from java
		if (qualifiedName.endsWith(".__path__")) {
			qualifiedName = qualifiedName.substring(0, qualifiedName.length() - 9);

			// lookup cache again
			if (mLookupCache.containsKey(qualifiedName))
				return mLookupCache.get(qualifiedName);
		}

		try {
			// try to locate class
			Class.forName(qualifiedName);
			// this is a class, not a package
			mLookupCache.put(qualifiedName, false);
			return false;

		} catch (ClassNotFoundException e) {
			// must be a package
			mLookupCache.put(qualifiedName, true);
			return true;
		}

		// PySystemState.builtin_module_names

		// test with:
		// import copy OK
		// import datetime OK
		// import os OK
		// import re OK
		// import sys OK
		// import threading OK

		// import math ERROR
		// import time ERROR
		//
		// import inspect ERROR / OK w/o PackageManager OK
		// import json ERROR / OK w/o PackageManager OK
		// import Queue ERROR / OK w/o PackageManager OK
		// import select ERROR / OK w/o PackageManager OK
		// import socket ERROR / OK w/o PackageManager OK
		// import sqlite3 NOT FOUND
		// import subprocess ERROR / OK w/o PackageManager OK
		// import unittest ERROR / OK w/o PackageManager OK
		// import xml.dom.minidom ERROR / OK w/o PackageManager
		// import xml.etree.ElementTree ERROR / OK w/o PackageManager
		// import httplib
	}

	private boolean isPythonLib(final String qualifiedName) {

		try {
			if ((qualifiedName != null) && qualifiedName.contains("extlibraryinitHelper"))
				return true;
		} catch (Exception e) {
		}

		// FIXME _ might be part of a java class/package !!!!
		// but "import json" also tries to locate _json, how to deal with that???
		if ((qualifiedName.startsWith("_")) && !(qualifiedName.startsWith("org.jython")))
			return true;
		/*
		 *
		 * Code this with 1.6 library
		 */
		for (File folder : Activator.getLibraryFolders()) {
			String path = folder.getPath();
			File resolvedFolder = new File(path + File.separator + qualifiedName.replaceAll("\\.", "/"));
			File resolvedLib = new File(path + File.separator + qualifiedName.replaceAll("\\.", "/") + ".py");
			if (resolvedFolder.exists())
				return true;

			if (resolvedLib.exists())
				return true;
		}

		return RESERVED_LIBS.contains(qualifiedName);
	}

	private static final List<String> RESERVED_LIBS = Arrays.asList("strop");
}
