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
     	http://www.springframework.org/schema/jms http://www.springframework.org/schema/jms/spring-jms-4.2.xsd
     	http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util-4.2.xsd">

	<context:annotation-config />

	<!-- 使用AspectJ方式配置AOP -->
	<aop:aspectj-autoproxy proxy-target-class="true" />

	<aop:config proxy-target-class="true" />

	<mvc:annotation-driven />

	<!--默认访问 controller -->
	<mvc:view-controller path="/"
		view-name="redirect:/college/route/index.html" />

	<!-- 组件自动扫描 -->
	<context:component-scan base-package="cn.mljia">
		<context:include-filter type="annotation"
			expression="org.springframework.stereotype.Controller" />
		<context:include-filter type="annotation"
			expression="org.springframework.web.bind.annotation.RestController" />
		<context:include-filter type="annotation"
			expression="org.springframework.web.bind.annotation.ControllerAdvice" />
	</context:component-scan>

	<!-- 取消对静态资源的解释,这样可以直接访问静态资源,这里判断访问资源是否被映射过 -->
	<!-- 这样不会出现找不到匹配资源的情况 -->
	<mvc:default-servlet-handler />

	<!-- 静态资源的访问 -->
	<mvc:resources mapping="/resources/**" location="/resources/" />


	<!--json处理 -->
	<bean id="mappingJacksonHttpMessageConverter"
		class="org.springframework.http.converter.json.MappingJackson2HttpMessageConverter">
		<property name="supportedMediaTypes">
			<list>
				<value>application/json;charset=UTF-8</value>
				<value>text/html;charset=UTF-8</value>
				<value>text/plain;charset=UTF-8</value>
				<value>application/x-www-form-urlencoded; charset=UTF-8</value>
			</list>
		</property>
		<property name="prettyPrint" value="true" />
		<property name="objectMapper">
			<bean class="com.fasterxml.jackson.databind.ObjectMapper">
				<property name="dateFormat">
					<bean class="java.text.SimpleDateFormat">
						<constructor-arg type="java.lang.String" value="yyyy-MM-dd HH:mm:ss" />
					</bean>
				</property>
			</bean>
		</property>
	</bean>

	<!--String处理 -->
	<!-- <bean id="stringHttpMessageConverter" class="org.springframework.http.converter.StringHttpMessageConverter"> 
		<constructor-arg value="UTF-8" index="0"></constructor-arg> 避免出现乱码 <property 
		name="supportedMediaTypes"> <list> <value>text/plain;charset=UTF-8</value> 
		<value>text/html;charset=UTF-8</value> </list> </property> </bean> -->

	<mvc:annotation-driven
		content-negotiation-manager="contentNegotiationManager">
		<mvc:message-converters register-defaults="true">
			<!-- 将StringHttpMessageConverter的默认编码设为UTF-8 -->
			<bean class="org.springframework.http.converter.StringHttpMessageConverter">
				<constructor-arg value="UTF-8" />
			</bean>
			<!-- 将FastJsonHttpMessageConverter的默认格式化输出设为true -->
			<bean
				class="com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter">
				<property name="features">
					<util:list>
						<value>WriteMapNullValue</value>
						<value>WriteNullListAsEmpty</value>
						<value>WriteNullBooleanAsFalse</value>
						<value>WriteEnumUsingToString</value>
						<value>DisableCircularReferenceDetect</value>
					</util:list>
				</property>
			</bean>
		</mvc:message-converters>
	</mvc:annotation-driven>

	<!-- REST中根据URL后缀自动判定Content-Type及相应的View -->
	<bean id="contentNegotiationManager"
		class="org.springframework.web.accept.ContentNegotiationManagerFactoryBean">
		<property name="ignoreAcceptHeader" value="true" />
		<property name="defaultContentType" value="application/json" />
		<property name="mediaTypes">
			<value>
				json=application/json
				xml=application/xml
				atom=application/atom+xml
				html=text/html
				json=application/json
				*=*/*
			</value>
		</property>
	</bean>


	<!-- 控制日期格式的 -->
	<bean id="conversionService"
		class="org.springframework.format.support.FormattingConversionServiceFactoryBean" />


	<!-- 配置JSP视图 -->
	<bean id="internalResourceViewResolver"
		class="org.springframework.web.servlet.view.InternalResourceViewResolver">
		<property name="viewClass"
			value="org.springframework.web.servlet.view.JstlView" />
		<property name="prefix" value="/WEB-INF/template/" />
		<property name="suffix" value=".jsp" />
		<property name="contentType" value="text/html;charset=UTF-8" />
		<property name="order" value="1" />
	</bean>



	<!-- 配置FreeMark视图 -->
	<bean id="freeMarkerViewResolver"
		class="org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver">
		<property name="contentType" value="text/html;charset=UTF-8" />
		<property name="viewClass"
			value="org.springframework.web.servlet.view.freemarker.FreeMarkerView" />
		<property name="suffix" value=".html" />
		<property name="cache" value="true" />
		<property name="exposeSessionAttributes" value="true" />
		<property name="exposeRequestAttributes" value="true" />
		<property name="exposeSpringMacroHelpers" value="true" />
		<!-- 在页面中使用${rc.contextPath}就可获得contextPath -->
		<property name="requestContextAttribute" value="rc" />
		<property name="order" value="0" />
	</bean>

	<bean id="fmXmlEscape" class="freemarker.template.utility.XmlEscape" />

	<bean id="freemarkConfig"
		class="org.springframework.beans.factory.config.PropertiesFactoryBean">
		<property name="location" value="classpath:freemarker.properties" />
	</bean>

	<bean id="FreeMarkerConfigurer"
		class="org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer">
		<property name="templateLoaderPath" value="/WEB-INF/template/" />
		<property name="defaultEncoding" value="UTF-8" />
		<property name="freemarkerSettings" ref="freemarkConfig" />
		<property name="freemarkerVariables">
			<map>
				<entry key="xml_escape" value-ref="fmXmlEscape" />
			</map>
		</property>
	</bean>



	<!-- 根据客户端的不同的请求决定不同的view进行响应, 如 /blog/1.json /blog/1.xml -->
	<bean
		class="org.springframework.web.servlet.view.ContentNegotiatingViewResolver">
		<property name="viewResolvers">
			<list>
				<ref bean="internalResourceViewResolver" />
				<ref bean="freeMarkerViewResolver" />
			</list>
		</property>
		<property name="defaultViews">
			<list>
				<bean
					class="org.springframework.web.servlet.view.json.MappingJackson2JsonView">
					<property name="jsonpParameterNames">
						<set>
							<value>jsonp</value>
							<value>callback</value>
						</set>
					</property>
				</bean>
			</list>
		</property>
	</bean>



</beans>  