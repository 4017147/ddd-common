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

import cn.mljia.ddd.common.AssertionConcern;
import cn.mljia.ddd.common.domain.model.DomainEvent;
import cn.mljia.ddd.common.event.EventStore;
import cn.mljia.ddd.common.event.StoredEvent;
import cn.mljia.ddd.common.notification.Notification;
import cn.mljia.ddd.common.notification.NotificationPublisher;
import cn.mljia.ddd.common.notification.PublishedNotificationTracker;
import cn.mljia.ddd.common.notification.PublishedNotificationTrackerStore;
import cn.mljia.ddd.common.port.adapter.messaging.rabbitmq.MessageProducer;

public class RabbitMQNotificationPublisher extends AssertionConcern implements NotificationPublisher
{
    
    private EventStore eventStore;
    
    private PublishedNotificationTrackerStore publishedNotificationTrackerStore;
    
    
    public RabbitMQNotificationPublisher(EventStore anEventStore,
        PublishedNotificationTrackerStore aPublishedNotificationTrackerStore)
    {
        super();
        this.setEventStore(anEventStore);
        this.setPublishedNotificationTrackerStore(aPublishedNotificationTrackerStore);
    }
    
    @Override
    public void publishNotifications(MessageProducer messageProducer,String aTypeName)
        throws Exception
    {
        PublishedNotificationTracker publishedNotificationTracker =
            this.publishedNotificationTrackerStore().publishedNotificationTracker(aTypeName);
        
        List<Notification> notifications =
            this.listUnpublishedNotifications(publishedNotificationTracker.mostRecentPublishedNotificationId(),
                aTypeName);
        
        try
        {
            this.bachPublish(notifications, messageProducer);// bach confirm publish
            
        }
        catch (Exception e)
        {
            throw e;
        }
        
        this.publishedNotificationTrackerStore().trackMostRecentPublishedNotification(publishedNotificationTracker,
            notifications);
    }
    
    @Override
    public boolean internalOnlyTestConfirmation()
    {
        throw new UnsupportedOperationException("Not supported by production implementation.");
    }
    
    private EventStore eventStore()
    {
        return this.eventStore;
    }
    
    private void setEventStore(EventStore anEventStore)
    {
        assertArgumentNotNull(anEventStore, "eventStore is not be null.");
        this.eventStore = anEventStore;
    }
    
    private List<Notification> listUnpublishedNotifications(long aMostRecentPublishedMessageId, String trackerName)
    {
        List<StoredEvent> storedEvents =
            this.eventStore().allStoredEventsSince(aMostRecentPublishedMessageId, trackerName);
        
        List<Notification> notifications = this.notificationsFrom(storedEvents);
        
        return notifications;
    }
    
//    private MessageProducer messageProducer(String exchangeName)
//    {
//        
//        // creates my exchange if non-existing
//        Exchange exchange =
//            Exchange.directInstance(ConnectionSettings.instance(rabbitmqConfiguration().getAddress(),
//                rabbitmqConfiguration().getVirtualHost(),
//                rabbitmqConfiguration().getUsername(),
//                rabbitmqConfiguration().getPassword()), exchangeName, true);
//        // create a message producer used to forward events
//        MessageProducer messageProducer = MessageProducer.instance(exchange);
//        
//        return messageProducer;
//    }
    
    private List<Notification> notificationsFrom(List<StoredEvent> aStoredEvents)
    {
        List<Notification> notifications = new ArrayList<Notification>(aStoredEvents.size());
        
        for (StoredEvent storedEvent : aStoredEvents)
        {
            DomainEvent domainEvent = storedEvent.toDomainEvent();
            
            Notification notification = new Notification(storedEvent.eventId(), domainEvent);
            
            notifications.add(notification);
        }
        
        return notifications;
    }
    
    private void bachPublish(List<Notification> notifications, MessageProducer aMessageProducer)
    {
        aMessageProducer.send(notifications);
    }
    
    private PublishedNotificationTrackerStore publishedNotificationTrackerStore()
    {
        return publishedNotificationTrackerStore;
    }
    
    private void setPublishedNotificationTrackerStore(
        PublishedNotificationTrackerStore publishedNotificationTrackerStore)
    {
        assertArgumentNotNull(publishedNotificationTrackerStore, "rabbitmqConfiguration is not be null.");
        this.publishedNotificationTrackerStore = publishedNotificationTrackerStore;
    }
    
}
