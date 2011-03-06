/*******************************************************************************
 * Copyright (c) 2010 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Itema AS - bug 330064 notification filtering and model persistence
 *     Itema AS - bug 331424 handle default event-sink action associations
 *******************************************************************************/

package org.eclipse.mylyn.internal.commons.ui.notifications;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ui.IMemento;

/**
 * @author Steffen Pingel
 * @author Torkild U. Resheim
 */
public class NotificationModel {

	private boolean dirty;

	private Map<String, NotificationHandler> handlerByEventId;

	public NotificationModel(IMemento memento) {
		initialize(memento);
	}

	void initialize(IMemento memento) {
		this.handlerByEventId = new HashMap<String, NotificationHandler>();
		// We need the handlerByEventId map to be populated early
		for (NotificationCategory category : getCategories()) {
			for (NotificationEvent event : category.getEvents()) {
				getOrCreateNotificationHandler(event);
			}
		}
		if (memento != null) {
			load(memento);
		}
	}

	public Collection<NotificationCategory> getCategories() {
		return NotificationsExtensionReader.getCategories();
	}

	public NotificationHandler getNotificationHandler(String eventId) {
		return handlerByEventId.get(eventId);
	}

	public NotificationHandler getOrCreateNotificationHandler(NotificationEvent event) {
		NotificationHandler handler = getNotificationHandler(event.getId());
		if (handler == null) {
			handler = new NotificationHandler(event, getActions(event));
			handlerByEventId.put(event.getId(), handler);
		}
		return handler;
	}

	private List<NotificationAction> getActions(NotificationEvent event) {
		List<NotificationSinkDescriptor> descriptors = NotificationsExtensionReader.getSinks();
		List<NotificationAction> actions = new ArrayList<NotificationAction>(descriptors.size());
		for (NotificationSinkDescriptor descriptor : descriptors) {
			NotificationAction action = new NotificationAction(descriptor);
			if (event.defaultHandledBySink(descriptor.getId())) {
				action.setSelected(true);
			}
			actions.add(action);
		}
		return actions;
	}

	public boolean isDirty() {
		return dirty;
	}

	/**
	 * Stores the selected state of events and sinks.
	 * 
	 * @param memento
	 *            the memento to store in.
	 */
	public void save(IMemento memento) {
		for (Entry<String, NotificationHandler> entry : handlerByEventId.entrySet()) {
			IMemento event = memento.createChild("event"); //$NON-NLS-1$
			event.putString("id", entry.getKey()); //$NON-NLS-1$
			List<NotificationAction> actions = entry.getValue().getActions();
			for (NotificationAction notificationAction : actions) {
				IMemento action = event.createChild("action"); //$NON-NLS-1$
				action.putBoolean("selected", notificationAction.isSelected()); //$NON-NLS-1$
				action.putString("sink", notificationAction.getSinkDescriptor().getId()); //$NON-NLS-1$
			}
		}
		setDirty(false);
	}

	/**
	 * Updates the notification model with selected states from the memento instance.
	 * 
	 * @param memento
	 */
	private void load(IMemento memento) {
		Assert.isNotNull(memento);
		for (IMemento mEvent : memento.getChildren("event")) { //$NON-NLS-1$
			for (NotificationCategory category : getCategories()) {
				for (NotificationEvent event : category.getEvents()) {
					if (event.getId().equals(mEvent.getString("id"))) { //$NON-NLS-1$
						// The selected state will be controlled by the child 
						// "action" items. If any of these are active, the event
						// will also be active.
						event.setSelected(false);
						NotificationHandler handler = getOrCreateNotificationHandler(event);
						List<NotificationAction> actions = handler.getActions();
						for (NotificationAction notificationAction : actions) {
							IMemento[] mActions = mEvent.getChildren("action"); //$NON-NLS-1$
							for (IMemento mAction : mActions) {
								if (notificationAction.getSinkDescriptor().getId().equals(mAction.getString("sink"))) { //$NON-NLS-1$
									notificationAction.setSelected(mAction.getBoolean("selected")); //$NON-NLS-1$
								}
							}
							if (notificationAction.isSelected()) {
								event.setSelected(true);
							}
						}
					}
				}
			}
		}
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	/**
	 * Updates the state of the notification handlers depending on their child notification action states.
	 */
	public void updateStates() {
		Collection<NotificationHandler> handlers = handlerByEventId.values();
		for (NotificationHandler notificationHandler : handlers) {
			List<NotificationAction> actions = notificationHandler.getActions();
			boolean selected = false;
			for (NotificationAction notificationAction : actions) {
				if (notificationAction.isSelected()) {
					selected = true;
				}
			}
			notificationHandler.getEvent().setSelected(selected);
		}
	}

	public void setNotificationHandler(String eventId, NotificationHandler handler) {
		handlerByEventId.put(eventId, handler);
		setDirty(true);
	}

}
