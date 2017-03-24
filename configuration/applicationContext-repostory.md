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

<!-- 	<context:component-scan base-package="cn.mljia.*" /> -->
<!-- 	<context:annotation-config /> -->

	<!-- 配置数据源，使用的是alibaba的Druid(德鲁伊)数据源 -->
	<bean name="dataSource" class="com.alibaba.druid.pool.DruidDataSource"
		init-method="init" destroy-method="close">
		<!-- 基本属性 url、user、password -->
		<property name="driverClassName" value="${jdbc.driverClassName}"></property>
		<property name="url" value="${jdbc.url}"></property>
		<property name="username" value="${jdbc.username}"></property>
		<property name="password" value="${jdbc.password}"></property>
		<!-- 初始化连接大小 -->
		<property name="initialSize" value="${pool.initialSize}" />
		<!-- 连接池最大使用连接数量 -->
		<property name="maxActive" value="${pool.maxActive}" />
		<!-- 连接池最大空闲 -->
		<property name="maxIdle" value="${pool.maxIdle}" />
		<!-- 连接池最小空闲 -->
		<property name="minIdle" value="${pool.minIdle}" />
		<!-- 获取连接最大等待时间 -->
		<property name="maxWait" value="${pool.maxWait}" />
		<!-- <property name="poolPreparedStatements" value="true" /> <property 
			name="maxPoolPreparedStatementPerConnectionSize" value="33" /> -->
		<property name="validationQuery" value="${pool.validationQuery}" />
		<property name="testOnBorrow" value="false" />
		<property name="testOnReturn" value="false" />
		<property name="testWhileIdle" value="true" />
		<!-- 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒 -->
		<property name="timeBetweenEvictionRunsMillis" value="60000" />
		<!-- 配置一个连接在池中最小生存的时间，单位是毫秒 -->
		<property name="minEvictableIdleTimeMillis" value="300000" />
		<!-- 打开removeAbandoned功能 -->
		<property name="removeAbandoned" value="true" />
		<!-- 1800秒，也就是30分钟 -->
		<property name="removeAbandonedTimeout" value="1800" />
		<!-- 关闭abanded连接时输出错误日志 -->
		<property name="logAbandoned" value="true" />
		<!-- 监控数据库 -->
		<!-- <property name="filters" value="stat" /> -->
		<property name="filters" value="mergeStat" />
		<!-- <property name="defaultAutoCommit" value="false"></property> -->
	</bean>


	<!-- 配置session factory，hibernate4 直接改为localsession，也是从上面的配置文件读取 -->
	<bean id="sessionFactory"
		class="org.springframework.orm.hibernate4.LocalSessionFactoryBean">
		<property name="dataSource" ref="dataSource" />
		<!-- hibernate的相关属性配置 -->
		<property name="hibernateProperties">
			<value>
				<!-- 设置数据库方言 -->
				hibernate.dialect=org.hibernate.dialect.MySQL5Dialect
				<!-- 设置自动创建|更新|验证数据库表结构 -->
				hibernate.hbm2ddl.auto=none
				<!-- 是否在控制台显示sql -->
				hibernate.show_sql=false
				<!-- 是否格式化sql，优化显示 -->
				hibernate.format_sql=false
				<!-- 是否开启二级缓存 -->
				hibernate.cache.use_second_level_cache=false
				<!-- 是否开启查询缓存 -->
				hibernate.cache.use_query_cache=false
				<!-- 数据库批量查询最大数 -->
				hibernate.jdbc.fetch_size=50
				<!-- 数据库批量更新、添加、删除操作最大数 -->
				hibernate.jdbc.batch_size=50
				<!-- 是否自动提交事务 -->
				hibernate.connection.autocommit=false
				<!-- 指定hibernate在何时释放JDBC连接 -->
				hibernate.connection.release_mode=auto
				<!-- 创建session方式 hibernate4.x 的方式 -->
				hibernate.current_session_context_class=org.springframework.orm.hibernate4.SpringSessionContext
				<!-- javax.persistence.validation.mode默认情况下是auto的，就是说如果不设置的话它是会自动去你的classpath下面找一个bean-validation**包 
					所以把它设置为none即可 -->
				javax.persistence.validation.mode=none
			</value>
		</property>
		<!-- 自动扫描注解方式配置的hibernate类文件 -->
		<property name="packagesToScan">
			<list>
				<value>cn.mljia.ddd.common.event</value>
				<value>cn.mljia.ddd.common.notification</value>
				<!-- 以上是固定不变的写法,要扩展自己扩展到下面 -->
				<value>cn.mljia.common.user.domain.model</value>
			</list>
		</property>
	</bean>

	<!-- 配置Hibernate事务管理器 -->
	<bean id="transactionManager" class="org.springframework.orm.hibernate4.HibernateTransactionManager">
		<property name="sessionFactory" ref="sessionFactory" />
	</bean>

	<!-- 基于注释的事务，当注释中发现@Transactional时，使用id为“transactionManager”的事务管理器 -->
	<!-- 如果没有设置transaction-manager的值，则spring以缺省默认的事务管理器来处理事务，默认事务管理器为第一个加载的事务管理器 -->
	<tx:annotation-driven proxy-target-class="true" transaction-manager="transactionManager" />

	<bean id="abstractTransactionalServiceProxy" abstract="true" class="org.springframework.transaction.interceptor.TransactionProxyFactoryBean">
		<property name="transactionManager" ref="transactionManager"/>
		<property name="transactionAttributes">
			<props>
				<prop key="*">PROPAGATION_REQUIRED</prop>
			</props>
		</property>
	</bean>
	
	<!--hibernate操作模版 -->
	<bean id="hibernateTemplate" class="org.springframework.orm.hibernate4.HibernateTemplate">
		<property name="sessionFactory" ref="sessionFactory"></property>
	</bean>
	
</beans>