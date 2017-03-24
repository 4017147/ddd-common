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

package cn.mljia.ddd.common.port.adapter.persistence.hibernate;

import java.util.List;

import org.hibernate.Query;

import cn.mljia.ddd.common.domain.model.DomainEvent;
import cn.mljia.ddd.common.event.EventSerializer;
import cn.mljia.ddd.common.event.EventStore;
import cn.mljia.ddd.common.event.StoredEvent;
import cn.mljia.ddd.common.persistence.PersistenceManagerProvider;

public class HibernateEventStore extends AbstractHibernateSession implements EventStore
{
    
    private static final Integer LIMIT = 100;
    
    private String trackerName;
    
    public HibernateEventStore(PersistenceManagerProvider aPersistenceManagerProvider)
    {
        this();
        
        if (!aPersistenceManagerProvider.hasHibernateSession())
        {
            throw new IllegalArgumentException("The PersistenceManagerProvider must have a Hibernate Session.");
        }
        
        this.setSession(aPersistenceManagerProvider.hibernateSession());
        
    }
    
    public HibernateEventStore()
    {
        super();
    }
 
    
    @Override
    @SuppressWarnings("unchecked")
    public List<StoredEvent> allStoredEventsBetween(long aLowStoredEventId, long aHighStoredEventId)
    {
        Query query =
            this.session().createQuery("from StoredEvent as _obj_ "
                + "where _obj_.trackerName=? and _obj_.eventId between ? and ? " + "order by _obj_.eventId");
        
        query.setParameter(0, this.trackerName());
        query.setParameter(1, aLowStoredEventId);
        query.setParameter(2, aHighStoredEventId);
        
        List<StoredEvent> storedEvents = query.list();
        
        return storedEvents;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<StoredEvent> allStoredEventsSince(long aStoredEventId)
    {
        Query query =
            this.session().createQuery("from StoredEvent as _obj_ "
                + "where _obj_.trackerName=? and _obj_.eventId > ? " + "order by _obj_.eventId");
        
        query.setParameter(0, this.trackerName());
        query.setParameter(1, aStoredEventId);
        query.setMaxResults(LIMIT); // 相当于sql语句中的查询数量
        query.setFirstResult(0); // 相当于sql语句中的开始索引
        List<StoredEvent> storedEvents = query.list();
        
        return storedEvents;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<StoredEvent> allStoredEventsSince(long aStoredEventId, String trackerName)
    {
        Query query =
            this.session().createQuery("from StoredEvent as _obj_ "
                + "where _obj_.trackerName=? and _obj_.eventId > ? " + "order by _obj_.eventId");
        
        query.setParameter(0, trackerName);
        query.setParameter(1, aStoredEventId);
        query.setMaxResults(LIMIT); // 相当于sql语句中的查询数量
        query.setFirstResult(0); // 相当于sql语句中的开始索引
        List<StoredEvent> storedEvents = query.list();
        
        return storedEvents;
    }
    
    @Override
    public StoredEvent append(DomainEvent aDomainEvent)
    {
        String eventSerialization = EventSerializer.instance().serialize(aDomainEvent);
        
        StoredEvent storedEvent =
            new StoredEvent(aDomainEvent.getClass().getName(), aDomainEvent.getClass().getName(),
                aDomainEvent.occurredOn(), eventSerialization);
        
        this.session().save(storedEvent);
        
        return storedEvent;
    }
    
    @Override
    public void close()
    {
        // no-op
    }
    
    @Override
    public long countStoredEvents()
    {
        Query query = this.session().createQuery("select count(1) from StoredEvent");
        
        long count = ((Long)query.uniqueResult()).longValue();
        
        return count;
    }
    
    @Override
    public String trackerName()
    {
        return trackerName;
    }
    
   
    
}
