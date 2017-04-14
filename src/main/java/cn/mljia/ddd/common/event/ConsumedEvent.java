package cn.mljia.ddd.common.event;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tb_consumed_event_store")
public class ConsumedEvent
{
    @Id
    @Column(name = "event_id", nullable = false, length = 11)
    private long eventId;
    
    @Column(name = "type_name", nullable = true, length = 200)
    private String typeName;
    
    @Column(name = "receive_name", nullable = true, length = 255)
    private String receiveName;
    
    public ConsumedEvent()
    {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public ConsumedEvent(long eventId, String typeName, String receiveName)
    {
        super();
        this.eventId = eventId;
        this.typeName = typeName;
        this.receiveName = receiveName;
    }
    
    public long getEventId()
    {
        return eventId;
    }
    
    public void setEventId(long eventId)
    {
        this.eventId = eventId;
    }
    
    public String getTypeName()
    {
        return typeName;
    }
    
    public void setTypeName(String typeName)
    {
        this.typeName = typeName;
    }
    
    public String getReceiveName()
    {
        return receiveName;
    }
    
    public void setReceiveName(String receiveName)
    {
        this.receiveName = receiveName;
    }
    
    @Override
    public String toString()
    {
        return "ConsumedEvent [eventId=" + eventId + ", typeName=" + typeName + ", receiveName=" + receiveName + "]";
    }
    
}
