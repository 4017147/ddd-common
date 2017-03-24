package cn.mljia.ddd.common.port.adapter.persistence.hibernate;

import java.util.List;

import org.hibernate.Query;

import cn.mljia.ddd.common.event.ConsumedEvent;
import cn.mljia.ddd.common.event.ConsumedEventStore;

public class HibernateConsumedEventStore extends AbstractHibernateSession implements ConsumedEventStore {

	@Override
	public boolean isDealWithEvent(long eventId, String typeName) throws Exception {
		Query query = this.session().createQuery("from ConsumedEvent as _obj_  where _obj_.typeName=? and _obj_.eventId=? ");
		query.setParameter(0, typeName);
		query.setParameter(1, eventId);
		@SuppressWarnings("unchecked")
		List<ConsumedEvent> consumedEvents = query.list();
		return consumedEvents.isEmpty() ? false : true;
	}

	@Override
	public ConsumedEvent append(long eventId, String typeName) throws Exception {
		ConsumedEvent consumedEvent = new ConsumedEvent(eventId, typeName);

		this.session().save(consumedEvent);

		return consumedEvent;
	}

}
