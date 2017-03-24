<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:context="http://www.springframework.org/schema/context"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:tx="http://www.springframework.org/schema/tx"
	xmlns:mvc="http://www.springframework.org/schema/mvc" xmlns:util="http://www.springframework.org/schema/util"
	xmlns:aop="http://www.springframework.org/schema/aop" xmlns:task="http://www.springframework.org/schema/task"
	xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans-4.2.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context-4.2.xsd
        http://www.springframework.org/schema/aop
        http://www.springframework.org/schema/aop/spring-aop-4.2.xsd
        http://www.springframework.org/schema/tx
        http://www.springframework.org/schema/tx/spring-tx-4.2.xsd
         http://www.springframework.org/schema/task
    	http://www.springframework.org/schema/task/spring-task-4.2.xsd
        http://www.springframework.org/schema/mvc
        http://www.springframework.org/schema/mvc/spring-mvc-4.2.xsd ">

	<!-- 连接池配置. -->
	<bean id="jedisPoolConfig" class="redis.clients.jedis.JedisPoolConfig">
		<!-- 连接池中最大连接数。高版本：maxTotal，低版本：maxActive -->
		<property name="maxTotal" value="8" />
		<!-- 连接池中最大空闲的连接数. -->
		<property name="maxIdle" value="4" />
		<!-- 连接池中最少空闲的连接数. -->
		<property name="minIdle" value="1" />
		<!-- 当连接池资源耗尽时，调用者最大阻塞的时间，超时将跑出异常。单位，毫秒数;默认为-1.表示永不超时。高版本：maxWaitMillis，低版本：maxWait -->
		<property name="maxWaitMillis" value="5000" />
		<!-- 连接空闲的最小时间，达到此值后空闲连接将可能会被移除。负值(-1)表示不移除. -->
		<property name="minEvictableIdleTimeMillis" value="300000" />
		<!-- 对于“空闲链接”检测线程而言，每次检测的链接资源的个数。默认为3 -->
		<property name="numTestsPerEvictionRun" value="3" />
		 <!-- “空闲链接”检测线程，检测的周期，毫秒数。如果为负值，表示不运行“检测线程”。默认为-1. -->
		<property name="timeBetweenEvictionRunsMillis" value="60000" />
		<!-- testOnBorrow:向调用者输出“链接”资源时，是否检测是有有效，如果无效则从连接池中移除，并尝试获取继续获取。默认为false。建议保持默认值. -->
		<!-- testOnReturn:向连接池“归还”链接时，是否检测“链接”对象的有效性。默认为false。建议保持默认值.-->
		<!-- testWhileIdle:向调用者输出“链接”对象时，是否检测它的空闲超时；默认为false。如果“链接”空闲超时，将会被移除。建议保持默认值. -->
		<!-- whenExhaustedAction:当“连接池”中active数量达到阀值时，即“链接”资源耗尽时，连接池需要采取的手段, 默认为1(0:抛出异常。1:阻塞，直到有可用链接资源。2:强制创建新的链接资源) -->
	</bean>
	
	<!-- Spring提供的Redis连接工厂 -->
	<bean id="jedisConnectionFactory" class="org.springframework.data.redis.connection.jedis.JedisConnectionFactory" destroy-method="destroy">
		<!-- 连接池配置. -->
		<property name="poolConfig" ref="jedisPoolConfig" />
		<!-- Redis服务主机. -->
		<property name="hostName" value="${redis.group.client1.host}" />
		<!-- Redis服务端口号. -->
		<property name="port" value="${redis.group.client1.port}" />
		<!-- Redis服务连接密码. -->
		<property name="password" value="${redis.group.client1.password}" />
		<!-- 连超时设置. -->
		<property name="timeout" value="${redis.group.client1.timeout}" />
		<!-- 是否使用连接池. -->
		<property name="usePool" value="true" />
	</bean>
	
	
</beans>

