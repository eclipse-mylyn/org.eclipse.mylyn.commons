/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.monitor.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin;
import org.eclipse.mylyn.monitor.core.InteractionEvent;
import org.eclipse.mylyn.monitor.core.InteractionEvent.Kind;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Self-registering on construction. Encapsulates users' interaction with the context model.
 *
 * @author Mik Kersten
 * @author Shawn Minto
 * @since 2.0
 */
public abstract class AbstractUserInteractionMonitor implements ISelectionListener {

	protected Object lastSelectedElement = null;

	/**
	 * Requires workbench to be active.
	 */
	public AbstractUserInteractionMonitor() {
		try {
			MonitorUiPlugin.getDefault().addWindowPostSelectionListener(this);
		} catch (NullPointerException e) {
			StatusHandler.log(new Status(IStatus.WARNING, MonitorUiPlugin.ID_PLUGIN,
					"Monitors can not be instantiated until the workbench is active", e)); //$NON-NLS-1$
		}
	}

	public void dispose() {
		try {
			MonitorUiPlugin.getDefault().removeWindowPostSelectionListener(this);
		} catch (NullPointerException e) {
			StatusHandler.log(new Status(IStatus.WARNING, MonitorUiPlugin.ID_PLUGIN, "Could not dispose monitor", e)); //$NON-NLS-1$
		}
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		throw new UnsupportedOperationException("should be handeled in AbstractContextInteractionMonitor");
	}

	protected abstract void handleWorkbenchPartSelection(IWorkbenchPart part, ISelection selection,
			boolean contributeToContext);

	/**
	 * Intended to be called back by subclasses.
	 */
	protected InteractionEvent handleElementSelection(IWorkbenchPart part, Object selectedElement,
			boolean contributeToContext) {
		return handleElementSelection(part.getSite().getId(), selectedElement, contributeToContext);
	}

	/**
	 * Intended to be called back by subclasses.
	 */
	protected void handleElementEdit(IWorkbenchPart part, Object selectedElement, boolean contributeToContext) {
		handleElementEdit(part.getSite().getId(), selectedElement, contributeToContext);
	}

	/**
	 * Intended to be called back by subclasses.
	 */
	protected void handleNavigation(IWorkbenchPart part, Object targetElement, String kind,
			boolean contributeToContext) {
		handleNavigation(part.getSite().getId(), targetElement, kind, contributeToContext);
	}

	/**
	 * Intended to be called back by subclasses. *
	 *
	 * @since 3.1
	 */
	protected void handleNavigation(String partId, Object targetElement, String kind, boolean contributeToContext) {
		throw new UnsupportedOperationException("should be handeled in AbstractContextInteractionMonitor");
	}

	/**
	 * Intended to be called back by subclasses.
	 *
	 * @since 3.1
	 */
	protected void handleElementEdit(String partId, Object selectedElement, boolean contributeToContext) {
		throw new UnsupportedOperationException("should be handeled in AbstractContextInteractionMonitor");
	}

	/**
	 * Intended to be called back by subclasses. *
	 *
	 * @since 3.1
	 */
	protected InteractionEvent handleElementSelection(String partId, Object selectedElement,
			boolean contributeToContext) {
		throw new UnsupportedOperationException("should be handeled in AbstractContextInteractionMonitor");
	}

	public Kind getEventKind() {
		return InteractionEvent.Kind.SELECTION;
	}
}
