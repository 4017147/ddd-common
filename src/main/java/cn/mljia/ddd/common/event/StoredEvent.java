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

package cn.mljia.ddd.common.event;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;

import cn.mljia.ddd.common.AssertionConcern;
import cn.mljia.ddd.common.domain.model.DomainEvent;

@Entity
@Table(name = "tb_stored_event")
public class StoredEvent extends AssertionConcern {
	@Column(name = "event_body", nullable = true, length = 5000)
	private String eventBody;
	@Id
	@GeneratedValue(generator = "generator")
	@GenericGenerator(name = "generator", strategy = "identity")
	@Column(name = "event_id", nullable = false, unique = true, length = 20)
	private long eventId;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "occurred_on", nullable = true, length = 10)
	private Date occurredOn;
	@Column(name = "type_name", nullable = true, length = 200)
	private String typeName;
	@Column(name = "tracker_name", nullable = false, length = 50)
	private String trackerName;
	
	public StoredEvent(String trackerName,String aTypeName, Date anOccurredOn, String anEventBody) {
		this();
		
		this.setTrackerName(trackerName);
		this.setEventBody(anEventBody);
		this.setOccurredOn(anOccurredOn);
		this.setTypeName(aTypeName);
	}

	public StoredEvent(String trackerName,String aTypeName, Date anOccurredOn, String anEventBody, long anEventId) {
		this(trackerName,aTypeName, anOccurredOn, anEventBody);

		this.setEventId(anEventId);
	}

	public String eventBody() {
		return this.eventBody;
	}

	public long eventId() {
		return this.eventId;
	}

	public Date occurredOn() {
		return this.occurredOn;
	}

	@SuppressWarnings("unchecked")
	public <T extends DomainEvent> T toDomainEvent() {
		Class<T> domainEventClass = null;

		try {
			domainEventClass = (Class<T>) Class.forName(this.typeName());
		} catch (Exception e) {
			throw new IllegalStateException("Class load error, because: " + e.getMessage());
		}

		T domainEvent = EventSerializer.instance().deserialize(this.eventBody(), domainEventClass);

		return domainEvent;
	}

	public String typeName() {
		return this.typeName;
	}

	@Override
	public boolean equals(Object anObject) {
		boolean equalObjects = false;

		if (anObject != null && this.getClass() == anObject.getClass()) {
			StoredEvent typedObject = (StoredEvent) anObject;
			equalObjects = this.eventId() == typedObject.eventId();
		}

		return equalObjects;
	}

	@Override
	public int hashCode() {
		int hashCodeValue = +(1237 * 233) + (int) this.eventId();

		return hashCodeValue;
	}

	@Override
	public String toString() {
		return "StoredEvent [eventBody=" + eventBody + ", eventId=" + eventId + ", occurredOn=" + occurredOn + ", typeName=" + typeName + "]";
	}

	public StoredEvent() {
		super();

		this.setEventId(-1);
	}

	protected void setEventBody(String anEventBody) {
		this.assertArgumentNotEmpty(anEventBody, "The event body is required.");
		this.assertArgumentLength(anEventBody, 1, 65000, "The event body must be 65000 characters or less.");

		this.eventBody = anEventBody;
	}

	protected void setEventId(long anEventId) {
		this.eventId = anEventId;
	}

	protected void setOccurredOn(Date anOccurredOn) {
		this.occurredOn = anOccurredOn;
	}

	protected void setTypeName(String aTypeName) {
		this.assertArgumentNotEmpty(aTypeName, "The event type name is required.");
		this.assertArgumentLength(aTypeName, 1, 100, "The event type name must be 100 characters or less.");

		this.typeName = aTypeName;
	}

	protected void setTrackerName(String trackerName) {
		this.assertArgumentNotEmpty(trackerName, "The event tracker name is required.");
		this.trackerName = trackerName;
	}
	
	 
}
