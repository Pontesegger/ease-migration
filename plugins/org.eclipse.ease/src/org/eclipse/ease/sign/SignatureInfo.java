/*******************************************************************************
 * Copyright (c) 2016 Varun Raval and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License_Identifier: EPL-2.0
 *
 * Contributors:
 *     Varun Raval - initial API and implementation
 *******************************************************************************/

package org.eclipse.ease.sign;

public class SignatureInfo {

	private final String fSignature, fProvider, fMessageDigestAlgo, fCertificates[];
	private String fContentOnly;

	/**
	 * Public constructor to store variables related to signature of script. It is preferable to provide the required values.
	 *
	 * @param signature
	 *            provide signature in String format
	 * @param provider
	 *            name provider of signature
	 * @param messageDigestAlgo
	 *            name message-digest i.e. hash algorithm used for signature
	 * @param certificates
	 *            provide certificates in String array
	 * @param contentOnly
	 *            provide contents excluding signature
	 */
	public SignatureInfo(String signature, String provider, String messageDigestAlgo, String[] certificates, String contentOnly) {

		fSignature = signature;
		fProvider = provider;
		fMessageDigestAlgo = messageDigestAlgo;
		fCertificates = certificates;
		fContentOnly = contentOnly;
	}

	/**
	 * Public constructor to store variables related to signature of script. It is preferable to provide the required values.
	 *
	 * @param signature
	 *            provide signature in String format
	 * @param provider
	 *            name provider of signature
	 * @param messageDigestAlgo
	 *            name message-digest i.e. hash algorithm used for signature
	 * @param certificates
	 *            provide certificates in String array
	 */
	public SignatureInfo(String signature, String provider, String messageDigestAlgo, String[] certificates) {
		this(signature, provider, messageDigestAlgo, certificates, null);
	}

	/**
	 * Public constructor to store variables related to signature of script. It is preferable to provide the required values.
	 *
	 * @param signature
	 *            provide signature in String format
	 * @param provider
	 *            name provider of signature
	 * @param messageDigestAlgo
	 *            name message-digest i.e. hash algorithm used for signature
	 */
	public SignatureInfo(String signature, String provider, String messageDigestAlgo) {
		this(signature, provider, messageDigestAlgo, null, null);
	}

	/**
	 * @return the signature
	 */
	public String getSignature() {
		return fSignature;
	}

	/**
	 * @return the provider
	 */
	public String getProvider() {
		return fProvider;
	}

	/**
	 * @return the messageDigestAlgo
	 */
	public String getMessageDigestAlgo() {
		return fMessageDigestAlgo;
	}

	/**
	 * @return the certificate chain in Base64 String array
	 */
	public String[] getCertificateChain() {
		return fCertificates;
	}

	/**
	 * @return the contentOnly
	 */
	public String getContentOnly() {
		return fContentOnly;
	}

	/**
	 * Set content corresponding to signature explicitly. Helpful when content and signature are not all in single file.
	 *
	 * @param fileContent
	 *            provide file content corresponding to the stored signature
	 */
	public void setContentOnly(String fileContent) {
		fContentOnly = fileContent;
	}
}
