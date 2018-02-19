/*******************************************************************************
 * Copyright (c) 2018 Bachmann electronic GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bachmann electronic GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.sign;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;

import org.eclipse.ease.service.ScriptType;
import org.junit.Test;

public class VerifySignatureTest {

	@Test
	public void testWithNonCodeParserScriptType() throws ScriptSignatureException {
		final ScriptType scriptType = mock(ScriptType.class);
		final VerifySignature verifySignature = VerifySignature.getInstance(scriptType, new ByteArrayInputStream(new byte[0]));
		assertNull(verifySignature);
	}

}