package cn.mljia.ddd.common.port.adapter.persistence.hibernate;

import java.math.BigInteger;

import org.hibernate.SQLQuery;

import cn.mljia.ddd.common.event.ConsumedEvent;
import cn.mljia.ddd.common.event.ConsumedEventStore;

public class HibernateConsumedEventStore extends AbstractHibernateSession implements ConsumedEventStore
{
    
    @Override
    public boolean isDealWithEvent(long eventId, String typeName,String receiveName)
        throws Exception
    {
        String sql = "select count(1) from tb_consumed_event_store c where c.type_name = ? and c.event_id = ? and receive_name = ?";
        SQLQuery query = this.session().createSQLQuery(sql);
        query.setParameter(0, typeName);
        query.setParameter(1, eventId);
        query.setParameter(2, receiveName);
        BigInteger count = (BigInteger)query.uniqueResult();
        return count.intValue() > 0 ? true : false;
    }
    
    @Override
    public ConsumedEvent append(long eventId, String typeName,String receiveName)
        throws Exception
    {
        ConsumedEvent consumedEvent = new ConsumedEvent(eventId, typeName,receiveName);
        
        this.session().save(consumedEvent);
        
        return consumedEvent;
    }
    
}
