package cn.mljia.ddd.common.port.adapter.persistence.springJdbc;

import org.springframework.jdbc.core.JdbcTemplate;

public class SpringJdbcCreateTable {
	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public SpringJdbcCreateTable(JdbcTemplate jdbcTemplate) {
		super();
		setJdbcTemplate(jdbcTemplate);
	}

	public void createTable() {
		StringBuilder builder = new StringBuilder();
		builder.append(" CREATE TABLE IF NOT EXISTS `tb_published_notification_tracker` ( ");
		builder.append(" `published_notification_tracker_id` bigint(20) NOT NULL auto_increment, ");
		builder.append(" `most_recent_published_notification_id` bigint(20) NOT NULL, ");
		builder.append(" `type_name` varchar(100) NOT NULL, ");
		builder.append(" `concurrency_version` int(11) NOT NULL DEFAULT '0', ");
		builder.append(" PRIMARY KEY (`published_notification_tracker_id`) ");
		builder.append(" ) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8 COMMENT='店铺在线统计表' ");
		jdbcTemplate.update(builder.toString());
		builder.delete(0, builder.length());
		builder.append(" CREATE TABLE IF NOT EXISTS `tb_stored_event` ( ");
		builder.append(" `event_id` bigint(20) NOT NULL auto_increment, ");
		builder.append(" `event_body` varchar(20000) NOT NULL, ");
		builder.append(" `occurred_on` datetime NOT NULL, ");
		builder.append(" `type_name` varchar(200) NOT NULL, ");
		builder.append(" PRIMARY KEY (`event_id`) ");
		builder.append(" ) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8 COMMENT='店铺在线统计表' ");
		jdbcTemplate.update(builder.toString());
	}

}
