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

public interface IContextStarter {

	public void processActivityMetaContextEvent(InteractionEvent event);

	public String getActiveContextHandleIdentifier();

}
