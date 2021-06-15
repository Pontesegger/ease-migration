/*******************************************************************************
 * Copyright (c) 2018 Bachmann electronic GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License_Identifier: EPL-2.0
 *
 * Contributors:
 *     Bachmann electronic GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.sign;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;

import org.eclipse.ease.service.ScriptType;
import org.junit.jupiter.api.Test;

public class VerifySignatureTest {

	@Test
	public void testWithNonCodeParserScriptType() throws ScriptSignatureException {
		final ScriptType scriptType = mock(ScriptType.class);
		final VerifySignature verifySignature = VerifySignature.getInstance(scriptType, new ByteArrayInputStream(new byte[0]));
		assertNull(verifySignature);
	}

}
