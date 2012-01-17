/*******************************************************************************
 * Copyright (c) 2011 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.commons.repositories.http.core;

import java.io.IOException;

import javax.net.ssl.TrustManager;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.SyncBasicHttpContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.commons.core.net.SslSupport;
import org.eclipse.mylyn.commons.core.net.TrustAllTrustManager;
import org.eclipse.mylyn.commons.core.operations.IOperationMonitor;
import org.eclipse.mylyn.commons.repositories.core.RepositoryLocation;
import org.eclipse.mylyn.commons.repositories.core.auth.AuthenticationCredentials;
import org.eclipse.mylyn.commons.repositories.core.auth.AuthenticationException;
import org.eclipse.mylyn.commons.repositories.core.auth.AuthenticationRequest;
import org.eclipse.mylyn.commons.repositories.core.auth.AuthenticationType;
import org.eclipse.mylyn.commons.repositories.core.auth.CertificateCredentials;
import org.eclipse.mylyn.commons.repositories.core.auth.UserCredentials;

/**
 * Provides an abstraction for connecting to a {@link RepositoryLocation} through HTTP.
 * 
 * @author Steffen Pingel
 */
public class CommonHttpClient {

	private boolean preemptiveAuthenticationEnabled;

	private boolean authenticated;

	private final SyncBasicHttpContext context;

	private AuthenticationType<UserCredentials> httpAuthenticationType;

	private AbstractHttpClient httpClient;

	private final RepositoryLocation location;

	public CommonHttpClient(RepositoryLocation location) {
		this.location = location;
		this.context = new SyncBasicHttpContext(null);
		this.httpAuthenticationType = AuthenticationType.HTTP;
	}

	public <T> T executeGet(String requestPath, IOperationMonitor monitor, HttpRequestProcessor<T> processor)
			throws IOException {
		HttpGet request = new HttpGet(location.getUrl() + requestPath);
		DefaultHttpOperation<T> op = new DefaultHttpOperation<T>(this, request, processor);
		return op.run(monitor);
	}

	public HttpResponse execute(HttpRequestBase request, IOperationMonitor monitor) throws IOException {
		prepareRequest(request, monitor);
		return HttpUtil.execute(getHttpClient(), HttpUtil.createHost(request), context, request, monitor);
	}

	public HttpContext getContext() {
		return context;
	}

	public AuthenticationType<UserCredentials> getHttpAuthenticationType() {
		return httpAuthenticationType;
	}

	public synchronized AbstractHttpClient getHttpClient() {
		if (httpClient == null) {
			httpClient = createHttpClient(null);
		}
		return httpClient;
	}

	public RepositoryLocation getLocation() {
		return location;
	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	public boolean isPreemptiveAuthenticationEnabled() {
		return preemptiveAuthenticationEnabled;
	}

	public boolean needsAuthentication() {
		return !isAuthenticated() && getLocation().getCredentials(AuthenticationType.REPOSITORY, false) != null;
	}

	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}

	public void setHttpAuthenticationType(AuthenticationType<UserCredentials> httpAuthenticationType) {
		this.httpAuthenticationType = httpAuthenticationType;
	}

	public void setPreemptiveAuthenticationEnabled(boolean preemptiveAuthenticationEnabled) {
		this.preemptiveAuthenticationEnabled = preemptiveAuthenticationEnabled;
	}

	private void prepareRequest(HttpRequestBase request, IOperationMonitor monitor) {
		UserCredentials httpCredentials = location.getCredentials(httpAuthenticationType);
		if (httpCredentials != null) {
			HttpUtil.configureAuthentication(getHttpClient(), location, httpCredentials);

			if (isPreemptiveAuthenticationEnabled()) {
				// create or pre-populate auth cache 
				HttpHost host = HttpUtil.createHost(request);
				Object authCache = context.getAttribute(ClientContext.AUTH_CACHE);
				if (authCache == null) {
					authCache = new BasicAuthCache();
					context.setAttribute(ClientContext.AUTH_CACHE, authCache);
				}
				if (authCache instanceof BasicAuthCache) {
					if (((BasicAuthCache) authCache).get(host) == null) {
						((BasicAuthCache) authCache).put(host, new BasicScheme());
					}
				}
			}
		}
		HttpUtil.configureProxy(getHttpClient(), location);

		CertificateCredentials socketCredentials = location.getCredentials(AuthenticationType.CERTIFICATE);
		if (socketCredentials != null) {
			SslSupport support = new SslSupport(new TrustManager[] { new TrustAllTrustManager() },
					socketCredentials.getKeyStoreFileName(), socketCredentials.getPassword(),
					socketCredentials.getKeyStoreType());
			request.getParams().setParameter(SslSupport.class.getName(), support);
		} else {
			// remove the token that associates certificate credentials with the connection
			context.removeAttribute(ClientContext.USER_TOKEN);
		}
	}

	protected void authenticate(IOperationMonitor monitor) throws IOException {
	}

	protected AbstractHttpClient createHttpClient(String userAgent) {
		// disabled due to https://issues.apache.org/jira/browse/HTTPCORE-257: new ContentEncodingHttpClient() {
		AbstractHttpClient client = new DefaultHttpClient() {
			@Override
			protected ClientConnectionManager createClientConnectionManager() {
				return CommonHttpClient.this.createHttpClientConnectionManager();
			}
		};
//		client.setTargetAuthenticationHandler(new DefaultTargetAuthenticationHandler() {
//			@Override
//			public boolean isAuthenticationRequested(HttpResponse response, HttpContext context) {
//				int statusCode = response.getStatusLine().getStatusCode();
//				return statusCode == HttpStatus.SC_UNAUTHORIZED || statusCode == HttpStatus.SC_FORBIDDEN;
//			}
//		});
		HttpUtil.configureClient(client, userAgent);
		return client;
	}

	protected ClientConnectionManager createHttpClientConnectionManager() {
		return HttpUtil.getConnectionManager();
	}

	protected <T extends AuthenticationCredentials> T requestCredentials(
			AuthenticationRequest<AuthenticationType<T>> request, IProgressMonitor monitor) {
		return location.requestCredentials(request, monitor);
	}

	protected void validate(HttpResponse response, IProgressMonitor monitor) throws AuthenticationException {
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
			AuthenticationRequest<AuthenticationType<UserCredentials>> request = new AuthenticationRequest<AuthenticationType<UserCredentials>>(
					getLocation(), httpAuthenticationType);
			throw new AuthenticationException(HttpUtil.getStatusText(statusCode), request);
		} else if (statusCode == HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED) {
			AuthenticationRequest<AuthenticationType<UserCredentials>> request = new AuthenticationRequest<AuthenticationType<UserCredentials>>(
					getLocation(), AuthenticationType.PROXY);
			throw new AuthenticationException(HttpUtil.getStatusText(statusCode), request);
		}
	}

}
