<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:context="http://www.springframework.org/schema/context"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:tx="http://www.springframework.org/schema/tx"
	xmlns:mvc="http://www.springframework.org/schema/mvc" xmlns:util="http://www.springframework.org/schema/util"
	xmlns:aop="http://www.springframework.org/schema/aop" xmlns:task="http://www.springframework.org/schema/task"
	xmlns:tool="http://www.springframework.org/schema/tool" xmlns:jms="http://www.springframework.org/schema/jms"
	xsi:schemaLocation="
        http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.2.xsd
        http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-4.2.xsd
        http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-4.2.xsd
        http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx-4.2.xsd
        http://www.springframework.org/schema/task http://www.springframework.org/schema/task/spring-task-4.2.xsd
        http://www.springframework.org/schema/mvc http://www.springframework.org/schema/mvc/spring-mvc-4.2.xsd
        http://www.springframework.org/schema/tool http://www.springframework.org/schema/tool/spring-tool-4.2.xsd
     	http://www.springframework.org/schema/jms http://www.springframework.org/schema/jms/spring-jms-4.2.xsd">

	<!-- 使用AspectJ方式配置AOP -->
	<aop:aspectj-autoproxy proxy-target-class="true" />

	<!-- 启动CGLIB动态代理 -->
	<aop:config proxy-target-class="true" />

	<!-- 激活Spring解驱动 -->
	<context:annotation-config />

	<!-- 事件监听 -->
	<bean class="cn.mljia.common.user.application.listener.IMessageListener">
		<constructor-arg index="0" ref="rabbitmqConfiguration" />
		<constructor-arg index="1" ref="transactionManager" />
		<constructor-arg index="2" ref="consumedEventStore" />
	</bean>

</beans>