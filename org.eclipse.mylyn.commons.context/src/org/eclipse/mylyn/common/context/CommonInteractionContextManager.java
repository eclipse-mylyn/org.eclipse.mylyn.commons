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

public interface CommonInteractionContextManager {

	public static final String ACTIVITY_ORIGINID_WORKBENCH = "org.eclipse.ui.workbench"; //$NON-NLS-1$

	public static final String ACTIVITY_STRUCTUREKIND_WORKINGSET = "workingset"; //$NON-NLS-1$

	public static final String ACTIVITY_HANDLE_NONE = "none"; //$NON-NLS-1$

	public static final String ACTIVITY_STRUCTUREKIND_TIMING = "timing"; //$NON-NLS-1$

	public static final String ACTIVITY_DELTA_ADDED = "added"; //$NON-NLS-1$

	public static final String ACTIVITY_ORIGINID_USER = "user"; //$NON-NLS-1$

	public static final String ACTIVITY_DELTA_REMOVED = "removed"; //$NON-NLS-1$

}
