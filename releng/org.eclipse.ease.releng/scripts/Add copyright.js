loadModule('/System/Resources');

const COPYRIGHT_NOTICE = [
"/*******************************************************************************",
" * Copyright (c) 2014 Christian Pontesegger and others.",
" * All rights reserved. This program and the accompanying materials",
" * are made available under the terms of the Eclipse Public License v2.0",
" * which accompanies this distribution, and is available at",
" * https://www.eclipse.org/legal/epl-2.0/",
" *",
" * Contributors:",
" *     Christian Pontesegger - initial API and implementation",
" *******************************************************************************/",
""
].join('\n');

function hasCopyright(file) {
	var handle = openFile(file);
	// ignore first line
	readLine(handle);
	
	var copyright = readLine(handle);
	closeFile(handle);
	
	copyright = copyright.trim();
	if (!copyright.isEmpty())
		copyright = copyright.substring(1).trim();	
	
	return copyright.startsWith("Copyright");
}

function addCopyright(file) {
	var handle = openFile(file, RANDOM_ACCESS);
	writeFile(handle, COPYRIGHT_NOTICE, 0);
	closeFile(handle);	
}

javaFiles = findFiles(".*.java");

for each (var file in javaFiles) {

	if (!hasCopyright(file))
		addCopyright(file);
}
