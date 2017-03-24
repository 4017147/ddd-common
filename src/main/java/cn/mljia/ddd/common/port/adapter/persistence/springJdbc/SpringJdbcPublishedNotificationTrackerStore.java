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

package cn.mljia.ddd.common.port.adapter.persistence.springJdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

import cn.mljia.ddd.common.notification.Notification;
import cn.mljia.ddd.common.notification.PublishedNotificationTracker;
import cn.mljia.ddd.common.notification.PublishedNotificationTrackerStore;

public class SpringJdbcPublishedNotificationTrackerStore implements PublishedNotificationTrackerStore {

	private String typeName;

	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public SpringJdbcPublishedNotificationTrackerStore() {
	}

	public SpringJdbcPublishedNotificationTrackerStore(String typeName, JdbcTemplate jdbcTemplate) {
		super();
		this.typeName = typeName;
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public PublishedNotificationTracker publishedNotificationTracker() {
		return this.publishedNotificationTracker(this.typeName());
	}

	@Override
	public PublishedNotificationTracker publishedNotificationTracker(final String aTypeName) {

		PublishedNotificationTracker publishedNotificationTracker = null;
		try {
			String sql = "select * from tb_published_notification_tracker as pnt where pnt.type_name = ?";
			publishedNotificationTracker=jdbcTemplate.queryForObject(sql, new Object[] { aTypeName }, new int[] { java.sql.Types.VARCHAR }, new RowMapper<PublishedNotificationTracker>() {
				public PublishedNotificationTracker mapRow(ResultSet rs, int num) throws SQLException {
					Integer concurrencyVersion = rs.getInt("concurrency_version");
					long mostRecentPublishedNotificationId = rs.getLong("most_recent_published_notification_id");
					long publishedNotificationTrackerId = rs.getLong("published_notification_tracker_id");
					String typeName = rs.getString("type_name");
					PublishedNotificationTracker publishedNotificationTracker = new PublishedNotificationTracker(concurrencyVersion,
							mostRecentPublishedNotificationId,
							publishedNotificationTrackerId,
							typeName);
					return publishedNotificationTracker;
				}
			});
		} catch (Exception e) {
			// fall through
		}

		if (publishedNotificationTracker == null) {
			publishedNotificationTracker = new PublishedNotificationTracker(this.typeName());
		}

		return publishedNotificationTracker;
	}

	@Override
	public void trackMostRecentPublishedNotification(final PublishedNotificationTracker aPublishedNotificationTracker, List<Notification> aNotifications) {
		int lastIndex = aNotifications.size() - 1;
		if (lastIndex >= 0) {
			long trackerId = aPublishedNotificationTracker.publishedNotificationTrackerId();
			final long mostRecentId = aNotifications.get(lastIndex).notificationId();
			if (trackerId == 0) {// insert
				jdbcTemplate.update("insert into tb_published_notification_tracker(most_recent_published_notification_id,type_name) values(?,?)",
						new Object[] { aPublishedNotificationTracker.publishedNotificationTrackerId(), aPublishedNotificationTracker.typeName() });
			} else {// update
				jdbcTemplate.update("update tb_published_notification_tracker set most_recent_published_notification_id=?, concurrency_version=concurrency_version+1 where concurrency_version=? and published_notification_tracker_id = ?",
						new PreparedStatementSetter() {
							@Override
							public void setValues(PreparedStatement ps) throws SQLException {
								ps.setLong(1, mostRecentId);
								ps.setInt(2, aPublishedNotificationTracker.concurrencyVersion());
								ps.setLong(3, aPublishedNotificationTracker.publishedNotificationTrackerId());
							}
						});
			}
		}
	}

	@Override
	public String typeName() {
		return typeName;
	}

	public void setTypeName(String aTypeName) {
		this.typeName = aTypeName;
	}
}
