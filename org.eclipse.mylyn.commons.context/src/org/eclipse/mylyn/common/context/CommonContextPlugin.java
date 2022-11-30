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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.monitor.core.InteractionEvent;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class CommonContextPlugin implements BundleActivator {

	public static final String ID_PLUGIN = "org.eclipse.mylyn.common.context"; //$NON-NLS-1$

	private static final String ORG_ECLIPSE_MYLYN_COMMONS_CONTEXT_STARTUP = "org.eclipse.mylyn.commons.context.startup";

	private static final String STARTUP = "startup";

	private static final String ELEMENT_CLASS = "class"; //$NON-NLS-1$

	private static CommonContextPlugin INSTANCE;

	private IContextStarter contextCallBack;

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

	private IContextStarter getContextCallBack() {
		if (contextCallBack == null) {
			IExtensionRegistry registry = Platform.getExtensionRegistry();

			IExtensionPoint extensionPoint = registry.getExtensionPoint(ORG_ECLIPSE_MYLYN_COMMONS_CONTEXT_STARTUP);
			IExtension[] extensions = extensionPoint.getExtensions();
			if (extensions.length == 1) {
				IConfigurationElement[] elements = extensions[0].getConfigurationElements();
				for (IConfigurationElement element : elements) {
					if (element.getName().equals(STARTUP)) {
						Object object;
						try {
							object = element.createExecutableExtension(ELEMENT_CLASS);
							if (object instanceof IContextStarter) {
								contextCallBack = (IContextStarter) object;
								break;
							}
						} catch (CoreException e) {
							StatusHandler.log(new Status(IStatus.ERROR, ID_PLUGIN, e.getMessage(), e));
						}
					}
				}
			} else {
				StatusHandler.log(new Status(IStatus.ERROR, ID_PLUGIN,
						String.format("only exact one %s expected", ORG_ECLIPSE_MYLYN_COMMONS_CONTEXT_STARTUP)));
			}

		}
		return contextCallBack;
	}

	public void processActivityMetaContextEvent(InteractionEvent event) {
		getContextCallBack().processActivityMetaContextEvent(event);
	}

	public String getStructureHandle() {
		return getContextCallBack().getActiveContextHandleIdentifier();
	}
}
