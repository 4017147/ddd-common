package cn.mljia.ddd.common.event;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tb_consumed_event_store")
public class ConsumedEvent {
	@Id
	@Column(name = "event_id", nullable = false, unique = true, length = 20)
	private long eventId;

	@Column(name = "type_name", nullable = true, length = 200)
	private String typeName;

	public ConsumedEvent() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ConsumedEvent(long eventId, String typeName) {
		super();
		this.eventId = eventId;
		this.typeName = typeName;
	}

	public long getEventId() {
		return eventId;
	}

	public void setEventId(long eventId) {
		this.eventId = eventId;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	@Override
	public String toString() {
		return "ConsumedEventStore [eventId=" + eventId + ", typeName=" + typeName + "]";
	}

}
