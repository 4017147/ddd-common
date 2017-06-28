package cn.mljia.ddd.common.port.adapter.persistence.springJdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import cn.mljia.ddd.common.domain.model.DomainEvent;
import cn.mljia.ddd.common.event.EventSerializer;
import cn.mljia.ddd.common.event.EventStore;
import cn.mljia.ddd.common.event.StoredEvent;

public class SpringJdbcEventStore implements EventStore
{
    
    private JdbcTemplate jdbcTemplate;
    
    private String trackerName;
    
    public SpringJdbcEventStore(JdbcTemplate jdbcTemplate, String trackerName)
    {
        super();
        this.setJdbcTemplate(jdbcTemplate);
        this.setTrackerName(trackerName);
    }
    
    private void setJdbcTemplate(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Override
    public List<StoredEvent> allStoredEventsBetween(long aLowStoredEventId, long aHighStoredEventId)
    {
        
        return jdbcTemplate.query("select * from tb_stored_event where tracker_name=? and event_id between ? and ? order by  event_id ",
            new Object[] {this.trackerName(), aLowStoredEventId, aHighStoredEventId},
            new int[] {java.sql.Types.VARCHAR, java.sql.Types.INTEGER, java.sql.Types.INTEGER},
            new RowMapper<StoredEvent>()
            {
                @Override
                public StoredEvent mapRow(ResultSet rs, int rowNum)
                    throws SQLException
                {
                    StoredEvent storedEvent =
                        new StoredEvent(rs.getString("tracker_name"), rs.getString("type_name"),
                            rs.getDate("occurred_on"), rs.getString("event_body"), rs.getInt("event_id"),rs.getInt("send_status"));
                    return storedEvent;
                }
            });
        
    }
    
    @Override
    public List<StoredEvent> allStoredEventsSince(long aStoredEventId)
    { 
        
        return jdbcTemplate.query("select * from tb_stored_event where tracker_name=? and  event_id > ? order by  event_id ",
        new Object[] {this.trackerName(), aStoredEventId},
        new int[] {java.sql.Types.VARCHAR, java.sql.Types.INTEGER},
        new RowMapper<StoredEvent>()
        {
            @Override
            public StoredEvent mapRow(ResultSet rs, int rowNum)
                throws SQLException
            {
                StoredEvent storedEvent =
                    new StoredEvent(rs.getString("tracker_name"), rs.getString("type_name"),
                        rs.getDate("occurred_on"), rs.getString("event_body"), rs.getInt("event_id"),rs.getInt("send_status"));
                return storedEvent;
            }
        });
    }
    
    @Override
    public List<StoredEvent> allStoredEventsSince(long aStoredEventId, String trackerName,Integer limit)
    {
        
        return jdbcTemplate.query("select * from tb_stored_event where tracker_name=? and  event_id > ? order by  event_id ",
            new Object[] {trackerName, aStoredEventId},
            new int[] {java.sql.Types.VARCHAR, java.sql.Types.INTEGER},
            new RowMapper<StoredEvent>()
            {
                @Override
                public StoredEvent mapRow(ResultSet rs, int rowNum)
                    throws SQLException
                {
                    StoredEvent storedEvent =
                        new StoredEvent(rs.getString("tracker_name"), rs.getString("type_name"),
                            rs.getDate("occurred_on"), rs.getString("event_body"), rs.getInt("event_id"),rs.getInt("send_status"));
                    return storedEvent;
                }
            });
        
    }
    
    @Override
    public StoredEvent append(DomainEvent aDomainEvent)
    {
        String eventSerialization = EventSerializer.instance().serialize(aDomainEvent);
        final StoredEvent storedEvent =
            new StoredEvent(this.trackerName(), aDomainEvent.getClass().getName(), aDomainEvent.occurredOn(),
                eventSerialization);
        String sql_stored_event = "insert into tb_stored_event (event_body,occurred_on,type_name) values(?,?,?)";
        jdbcTemplate.update(sql_stored_event, new Object[] {storedEvent.eventBody(), storedEvent.occurredOn(),
            storedEvent.typeName()});
        return storedEvent;
    }
    
    @Override
    public void close()
    {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public long countStoredEvents()
    {
        return jdbcTemplate.queryForObject("select count(1) as num from tb_stored_event ", new RowMapper<Long>()
        {
            @Override
            public Long mapRow(ResultSet rs, int rowNum)
                throws SQLException
            {
                return rs.getLong("num");
            }
            
        });
    }
    
    @Override
    public String trackerName()
    {
        return trackerName;
    }
    
    private void setTrackerName(String trackerName)
    {
        this.trackerName = trackerName;
    }

	@Override
	public Integer complete(Long[] eventIds) {
		// TODO Auto-generated method stub
		return jdbcTemplate.update("update tb_stored_event SET send_status =1  WHERE event_id IN(?)", eventIds);
	}

	@Override
	public List<StoredEvent> compensationStoredEvents(long aStoredEventId,
			String trackerName, Integer limit) {
		 return jdbcTemplate.query("select * from tb_stored_event where tracker_name=? and  event_id < ? and send_status = 0 order by  event_id ",
		            new Object[] {trackerName, aStoredEventId},
		            new int[] {java.sql.Types.VARCHAR, java.sql.Types.INTEGER},
		            new RowMapper<StoredEvent>()
		            {
		                @Override
		                public StoredEvent mapRow(ResultSet rs, int rowNum)
		                    throws SQLException
		                {
		                    StoredEvent storedEvent =
		                        new StoredEvent(rs.getString("tracker_name"), rs.getString("type_name"),
		                            rs.getDate("occurred_on"), rs.getString("event_body"), rs.getInt("event_id"),rs.getInt("send_status"));
		                    return storedEvent;
		                }
		            });
	}
    
}