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
 
	<!-- 消息通知服务 -->
	<bean id="notificationApplicationService" class="cn.mljia.ddd.common.application.NotificationApplicationService" >
		 <property name="notificationPublisher" ref="notificationPublisher"></property>
	</bean>

	<!-- 事件监听处理器-用于事件存储 -->
	<bean id="eventProcessor" class="cn.mljia.ddd.common.event.process.EventListenerProcessor" scope="prototype" autowire="byName" />
	
	<!-- 事件消费仓库 -->
	<bean id="consumedEventStore" class="cn.mljia.ddd.common.port.adapter.persistence.hibernate.HibernateConsumedEventStore" autowire="byName"></bean>

	<!-- hibernate session 提供者 -->
	<bean id="sessionProvider" class="cn.mljia.ddd.common.spring.SpringHibernateSessionProvider" autowire="byName" />
	
	<!-- 事件存储仓库 -->
	<bean id="eventStore" class="cn.mljia.ddd.common.port.adapter.persistence.hibernate.HibernateEventStore" autowire="byName" />

	<!-- Rabbitmq消息中间件配置 -->
	<bean id="rabbitmqConfiguration" class="cn.mljia.ddd.common.application.configuration.RabbitmqConfiguration">
		<!-- rabbitmq connecting address -->
		<property name="address" value="${rabbitmq.address}"></property>
		<!-- rabbitmq connecting user -->
		<property name="username" value="${rabbitmq.username}"></property>
		<!-- rabbitmq connecting password -->
		<property name="password" value="${rabbitmq.password}"></property>
		<!-- rabbitmq virtual host -->
		<property name="virtualHost" value="${rabbitmq.virtualHost}"></property>
	</bean>

	<!--消息事件发布者 -->
	<bean id="notificationPublisher" class="cn.mljia.ddd.common.port.adapter.notification.RabbitMQNotificationPublisher">
		<constructor-arg index="0" ref="eventStore" />
		<constructor-arg index="1" ref="publishedNotificationTrackerStore" />
	</bean>

	<!-- 事件跟踪器仓库 -->
	<bean id="publishedNotificationTrackerStore" class="cn.mljia.ddd.common.port.adapter.persistence.hibernate.HibernatePublishedNotificationTrackerStore"
		autowire="byName"/>

	<!-- 消息通知分布式锁 -->
	<bean id="jedisLock" class="cn.mljia.ddd.common.port.adapter.lock.redis.JedisLock">
		<property name="jedisConnectionFactory" ref="jedisConnectionFactory"></property>
		<!-- 锁的超时时间:默认5s -->
		<property name="waitTimeOut" value="${ddd-cfg.waitTimeOut}"></property>
		<!-- 锁的过期时间：默认60秒钟 -->
		<property name="lockExpireTime" value="${ddd-cfg.lockExpireTime}"></property>
	</bean>
 
	
	<!--事件通知调度服务 -->
	<bean class="cn.mljia.ddd.common.port.adapter.instantiation.InstantiationTracingProcessor">
		<property name="threadPoolTaskExecutor" ref="threadPoolTaskExecutor"></property>
		<property name="notificationApplicationService" ref="notificationApplicationService"></property>
		<property name="lock" ref="jedisLock"></property>
		<property name="scanPackage" value="cn.mljia"></property>
		<property name="commonConfiguration" ref="commonConfiguration"></property>
		<property name="rabbitmqConfiguration" ref="rabbitmqConfiguration"></property>
	</bean>

	<!-- RabbitMQ 连接工厂 -->    
    <bean id="connectionFactory" class="com.rabbitmq.client.ConnectionFactory" scope="singleton"/>
    
    <!-- NotificationTrackerExecutor -->
    <bean id="threadPoolTaskExecutor"   class="org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor">
	        <!-- 核心线程数，默认为1 -->
	        <property name="corePoolSize" value="10" />
	        <!-- 最大线程数，默认为Integer.MAX_VALUE -->
	        <property name="maxPoolSize" value="50" />
	        <!-- 队列最大长度，一般需要设置值>=notifyScheduledMainExecutor.maxNum；默认为Integer.MAX_VALUE
	            <property name="queueCapacity" value="1000" /> -->
	        <!-- 线程池维护线程所允许的空闲时间，默认为60s -->
	        <property name="keepAliveSeconds" value="300" />
	        <!-- 线程池对拒绝任务（无线程可用）的处理策略，目前只支持AbortPolicy、CallerRunsPolicy；默认为后者 -->
	        <property name="rejectedExecutionHandler">
	            <!-- AbortPolicy:直接抛出java.util.concurrent.RejectedExecutionException异常 -->
	            <!-- CallerRunsPolicy:主线程直接执行该任务，执行完之后尝试添加下一个任务到线程池中，可以有效降低向线程池内添加任务的速度 -->
	            <!-- DiscardOldestPolicy:抛弃旧的任务、暂不支持；会导致被丢弃的任务无法再次被执行 -->
	            <!-- DiscardPolicy:抛弃当前任务、暂不支持；会导致被丢弃的任务无法再次被执行 -->
	            <bean class="java.util.concurrent.ThreadPoolExecutor$CallerRunsPolicy" />
	        </property>
	        <property name="threadNamePrefix" value="domain_event_tracker_"></property>
	</bean>


	<bean id="commonConfiguration" class="cn.mljia.ddd.common.application.configuration.CommonConfiguration">
		<property name="domainName" value="${ddd-cfg.domainName}"></property>
	</bean>


</beans>