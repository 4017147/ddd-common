/*
Navicat MySQL Data Transfer

Source Server         : 192.168.20.113_mysql
Source Server Version : 50628
Source Host           : 192.168.20.113:3306
Source Database       : customer

Target Server Type    : MYSQL
Target Server Version : 50628
File Encoding         : 65001

Date: 2016-09-12 10:09:58
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for tb_published_notification_tracker
-- ----------------------------
DROP TABLE IF EXISTS `tb_published_notification_tracker`;
CREATE TABLE `tb_published_notification_tracker` (
  `published_notification_tracker_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `most_recent_published_notification_id` bigint(20) NOT NULL,
  `type_name` varchar(100) DEFAULT NULL,
  `concurrency_version` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`published_notification_tracker_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='领域事件跟踪表';

-- ----------------------------
-- Table structure for tb_stored_event
-- ----------------------------
DROP TABLE IF EXISTS `tb_stored_event`;
CREATE TABLE `tb_stored_event` (
  `event_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '事件ID',
  `event_body` varchar(5000) DEFAULT NULL COMMENT ' 事件内容',
  `occurred_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '事件创建时间',
  `type_name` varchar(200) DEFAULT NULL COMMENT ' 事件类型',
  `tracker_name` varchar(70) NOT NULL COMMENT ' 跟踪器名称',
  `send_status` int(1) DEFAULT '0' COMMENT '事件发送状态(0:待发送,1:已发送)',
  PRIMARY KEY (`event_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='领域事件存储表';
-- ----------------------------
-- Table structure for tb_consumed_event_store
-- ----------------------------
DROP TABLE IF EXISTS `tb_consumed_event_store`;
CREATE TABLE `tb_consumed_event_store` (
  `event_id` int(11) NOT NULL,
  `type_name` varchar(200) NOT NULL,
  `receive_name` varchar(255) NOT NULL,
  UNIQUE KEY `event_type_name_index_unique` (`event_id`,`type_name`,`receive_name`),
  KEY `event_type_name_index` (`event_id`,`type_name`,`receive_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

