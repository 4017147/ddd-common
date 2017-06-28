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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.mljia.ddd.common.AssertionConcern;
import cn.mljia.ddd.common.domain.model.DomainEvent;
import cn.mljia.ddd.common.event.EventStore;
import cn.mljia.ddd.common.event.StoredEvent;
import cn.mljia.ddd.common.media.SqlConfig;
import cn.mljia.ddd.common.notification.Notification;
import cn.mljia.ddd.common.notification.NotificationPublisher;
import cn.mljia.ddd.common.notification.PublishedNotificationTracker;
import cn.mljia.ddd.common.notification.PublishedNotificationTrackerStore;
import cn.mljia.ddd.common.port.adapter.messaging.MessageException;
import cn.mljia.ddd.common.port.adapter.messaging.rabbitmq.MessageProducer;

public class RabbitMQNotificationPublisher extends AssertionConcern implements NotificationPublisher
{
	private static Logger logger = LoggerFactory.getLogger(RabbitMQNotificationPublisher.class);
	    
    private EventStore eventStore;
    
    private PublishedNotificationTrackerStore publishedNotificationTrackerStore;
    
    private static final Integer LIMIT = 100;
    
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
        
        try
        {
        	Integer limit =null;
        	
        	PublishedNotificationTracker publishedNotificationTracker =
        			this.publishedNotificationTrackerStore().publishedNotificationTracker(aTypeName);
        	
        	List<Notification> compensationNotifications =//补偿事件通知
        			this.listCompensationNotifications(publishedNotificationTracker.mostRecentPublishedNotificationId(),
        					aTypeName,SqlConfig.LIMIT);
        	
        	if(compensationNotifications!=null&&!compensationNotifications.isEmpty()){
        		limit=SqlConfig.LIMIT-compensationNotifications.size();
        	}
        	
        	List<Notification> notifications = //正常事件通知
        			this.listUnpublishedNotifications(publishedNotificationTracker.mostRecentPublishedNotificationId(),
        					aTypeName,limit);
        	
        	if(compensationNotifications!=null&&!compensationNotifications.isEmpty()){
        		compensationNotifications.addAll(notifications);
        		this.bachPublish(compensationNotifications, messageProducer);// bach confirm publish
        		this.complete(compensationNotifications);//发多少更新多少
        	}else{
        		this.bachPublish(notifications, messageProducer);// bach confirm publish
        		this.complete(notifications);//发多少更新多少
        	}
        	
            this.publishedNotificationTrackerStore().trackMostRecentPublishedNotification(publishedNotificationTracker,
            		notifications);//更新tracker按最新通知为准
            
        }
        catch (Exception e)
        {
            throw e;
        }
        
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
    
    private List<Notification> listUnpublishedNotifications(long aMostRecentPublishedMessageId, String trackerName,Integer limit)
    {
        List<StoredEvent> storedEvents =
            this.eventStore().allStoredEventsSince(aMostRecentPublishedMessageId, trackerName,limit);
        
        List<Notification> notifications = this.notificationsFrom(storedEvents);
        
        return notifications;
    }
    
    private List<Notification> listCompensationNotifications(long aMostRecentPublishedMessageId, String trackerName,Integer limit)
    {
        List<StoredEvent> storedEvents =
            this.eventStore().compensationStoredEvents(aMostRecentPublishedMessageId, trackerName,limit);
        
        List<Notification> notifications = this.notificationsFrom(storedEvents);
        
        return notifications;
    }
    
    private Integer complete(List<Notification> notifications) throws Exception{
    	if(notifications!=null&&!notifications.isEmpty()){
    		List<Long> eventIds=new ArrayList<Long>(notifications.size());
        	for(Notification notification: notifications){
        		eventIds.add(notification.notificationId());
        	}
        	Long[] arr = (Long[])eventIds.toArray(new Long[eventIds.size()]);
        	Integer sum=this.eventStore().complete(arr);
        	return sum;
    	}
		return null;
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
        try {
			aMessageProducer.send(notifications);
		} catch (MessageException e) {
			logger.error("bachPublish notifications MessageException e:"+e.getMessage(),e);
			throw e;
		}
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
