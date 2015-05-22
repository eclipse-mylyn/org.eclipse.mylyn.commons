/*******************************************************************************
 * Copyright (c) 2004, 2012 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.commons.repositories.core.auth;

import org.eclipse.core.runtime.Assert;

/**
 * An enum of supported authentication types.
 * 
 * @author Steffen Pingel
 */
public class AuthenticationType<T extends AuthenticationCredentials> {

	/**
	 * Certificate authentication.
	 */
	public static final AuthenticationType<CertificateCredentials> CERTIFICATE = new AuthenticationType<CertificateCredentials>(
			CertificateCredentials.class, "org.eclipse.mylyn.commons.repositories.core.auth.certficate"); //$NON-NLS-1$

	/**
	 * HTTP authentication, this is often basic authentication but other methods such as digest or NTLM are used as
	 * well.
	 */
	public static final AuthenticationType<UserCredentials> HTTP = new AuthenticationType<UserCredentials>(
			UserCredentials.class, "org.eclipse.mylyn.tasklist.repositories.httpauth"); //$NON-NLS-1$

	public static final AuthenticationType<OpenIdCredentials> OPENID = new AuthenticationType<OpenIdCredentials>(
			OpenIdCredentials.class, "org.eclipse.mylyn.commons.repositories.core.auth.openid"); //$NON-NLS-1$

	/** Proxy authentication. */
	public static final AuthenticationType<UserCredentials> PROXY = new AuthenticationType<UserCredentials>(
			UserCredentials.class, "org.eclipse.mylyn.tasklist.repositories.proxy"); //$NON-NLS-1$

	/** Repository authentication. */
	public static final AuthenticationType<UserCredentials> REPOSITORY = new AuthenticationType<UserCredentials>(
			UserCredentials.class, "org.eclipse.mylyn.tasklist.repositories"); //$NON-NLS-1$

	private final Class<T> credentialsType;

	private final String key;

	public AuthenticationType(Class<T> credentialsType, String key) {
		Assert.isNotNull(credentialsType);
		Assert.isNotNull(key);
		this.credentialsType = credentialsType;
		this.key = key;
	}

	public Class<T> getCredentialsType() {
		return credentialsType;
	}

	public String getKey() {
		return key;
	}

}