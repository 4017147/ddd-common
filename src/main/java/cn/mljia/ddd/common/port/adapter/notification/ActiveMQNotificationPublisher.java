//   Copyright 2012,2013 Vaughn Vernon
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package cn.mljia.ddd.common.port.adapter.notification;

import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.commons.lang.StringUtils;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import cn.mljia.ddd.common.domain.model.DomainEvent;
import cn.mljia.ddd.common.event.EventStore;
import cn.mljia.ddd.common.event.StoredEvent;
import cn.mljia.ddd.common.notification.Notification;
import cn.mljia.ddd.common.notification.NotificationPublisher;
import cn.mljia.ddd.common.notification.NotificationSerializer;
import cn.mljia.ddd.common.notification.PublishedNotificationTracker;
import cn.mljia.ddd.common.notification.PublishedNotificationTrackerStore;
import cn.mljia.ddd.common.port.adapter.messaging.rabbitmq.MessageProducer;

public class ActiveMQNotificationPublisher implements NotificationPublisher {

	private EventStore eventStore;

	private JmsTemplate jmsTemplate;

	private PublishedNotificationTrackerStore publishedNotificationTrackerStore;

	public ActiveMQNotificationPublisher() {
		super();
	}

	public ActiveMQNotificationPublisher(EventStore eventStore,
			JmsTemplate jmsTemplate,
			PublishedNotificationTrackerStore publishedNotificationTrackerStore) {
		super();
		this.setJmsTemplate(jmsTemplate);
		this.setEventStore(eventStore);
		this.setPublishedNotificationTrackerStore(publishedNotificationTrackerStore);
	}

	public void publishNotifications() {

		PublishedNotificationTracker publishedNotificationTracker = this
				.publishedNotificationTrackerStore()
				.publishedNotificationTracker();

		List<Notification> notifications = this
				.listUnpublishedNotifications(publishedNotificationTracker
						.mostRecentPublishedNotificationId());
		try {
			for (Notification notification : notifications) {
				this.publish(notification);
			}

			this.publishedNotificationTrackerStore()
					.trackMostRecentPublishedNotification(
							publishedNotificationTracker, notifications);
		} catch (Exception e) {// 异常是否需要处理
			e.printStackTrace();
		}
	}

	public boolean internalOnlyTestConfirmation() {
		throw new UnsupportedOperationException(
				"Not supported by production implementation.");
	}

	private EventStore eventStore() {
		return this.eventStore;
	}

	private void setEventStore(EventStore anEventStore) {
		this.eventStore = anEventStore;
	}

	private List<Notification> listUnpublishedNotifications(
			long aMostRecentPublishedMessageId) {
		List<StoredEvent> storedEvents = this.eventStore()
				.allStoredEventsSince(aMostRecentPublishedMessageId,"",null);

		List<Notification> notifications = this.notificationsFrom(storedEvents);

		return notifications;
	}

	private List<Notification> notificationsFrom(List<StoredEvent> aStoredEvents) {
		List<Notification> notifications = new ArrayList<Notification>(
				aStoredEvents.size());

		for (StoredEvent storedEvent : aStoredEvents) {
			DomainEvent domainEvent = storedEvent.toDomainEvent();

			Notification notification = new Notification(storedEvent.eventId(),
					domainEvent);

			notifications.add(notification);
		}

		return notifications;
	}

	public void sendDestinationMessage(String destinationName,
			final String message) throws Exception {
		jmsTemplate.setPubSubDomain(true);
		if (StringUtils.isNotEmpty(destinationName)
				&& StringUtils.isNotBlank(destinationName)) {
			jmsTemplate.send(destinationName, new MessageCreator() {
				public Message createMessage(Session session)
						throws JMSException {
					return session.createTextMessage(message);
				}
			});
		}
	}

	private void publish(Notification aNotification) throws Exception {
		String notification = NotificationSerializer.instance().serialize(
				aNotification);
		sendDestinationMessage(aNotification.typeName(), notification);
	}

	private PublishedNotificationTrackerStore publishedNotificationTrackerStore() {
		return publishedNotificationTrackerStore;
	}

	private void setPublishedNotificationTrackerStore(
			PublishedNotificationTrackerStore publishedNotificationTrackerStore) {
		this.publishedNotificationTrackerStore = publishedNotificationTrackerStore;
	}

	private void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

    @Override
    public void publishNotifications(MessageProducer messageProducer, String typeName)
        throws Exception
    {
        // TODO Auto-generated method stub
        
    }

 

}
