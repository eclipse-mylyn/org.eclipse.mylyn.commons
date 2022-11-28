/*******************************************************************************
 * Copyright (c) 2022 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.common.context;

import org.eclipse.mylyn.monitor.core.InteractionEvent;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class CommonContextPlugin implements BundleActivator {

	public static final String ID_PLUGIN = "org.eclipse.mylyn.common.context"; //$NON-NLS-1$

	private static CommonContextPlugin INSTANCE;

	private ContextCallBack contextCallBack;

	public void setContextCallBack(ContextCallBack contextCallBack) {
		this.contextCallBack = contextCallBack;
	}

	public CommonContextPlugin() {
		INSTANCE = this;
	}

	public static CommonContextPlugin getDefault() {
		return INSTANCE;
	}

	@Override
	public void start(BundleContext context) throws Exception {
	}

	@Override
	public void stop(BundleContext context) throws Exception {
	}

	public void processActivityMetaContextEvent(InteractionEvent event) {
		contextCallBack.processActivityMetaContextEvent(event);
	}

	public String getStructureHandle() {
		return contextCallBack.getActiveContextHandleIdentifier();
	}
}
