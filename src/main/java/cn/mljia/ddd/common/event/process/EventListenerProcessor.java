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

package cn.mljia.ddd.common.event.process;

import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cn.mljia.ddd.common.domain.model.DomainEvent;
import cn.mljia.ddd.common.domain.model.DomainEventPublisher;
import cn.mljia.ddd.common.domain.model.DomainEventSubscriber;
import cn.mljia.ddd.common.event.EventStore;
import cn.mljia.ddd.common.event.annotation.EventListener;

@Aspect
public class EventListenerProcessor {

	private static Logger LOGGER = LoggerFactory.getLogger(EventListenerProcessor.class);

	@Autowired
	private EventStore eventStore;

	/**
	 * Registers a IdentityAccessEventProcessor to listen and forward all domain events to external subscribers. This factory method is provided in the case where Spring AOP wiring is not desired.
	 */
	public static void register() {
		 (new EventListenerProcessor()).tolisten();
	}

	/**
	 * Constructs my default state.
	 */
	public EventListenerProcessor() {
		super();
	}

	@Pointcut("@annotation(cn.mljia.ddd.common.event.annotation.EventListener)")
	public void listener() {

	}

	/**
	 * Listens for all domain events and stores them.
	 */
	@Before("listener()")
	public void listen(JoinPoint joinPoint) {
		LOGGER.debug("event listener is start ...");
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();
		EventListener eventListener = method.getAnnotation(EventListener.class);
		if (eventListener != null) {
			if (eventListener.isListening()) {// 监听开启
				tolisten();
			} else {
				LOGGER.debug("event listener is setting false ...");
			}
		}
	}
	
	
	public void tolisten(){
		DomainEventPublisher.instance().reset();

		DomainEventPublisher.instance().subscribe(new DomainEventSubscriber<DomainEvent>() {

			public void handleEvent(DomainEvent aDomainEvent) {
				store(aDomainEvent);
			}

			public Class<DomainEvent> subscribedToEventType() {
				return DomainEvent.class; // all domain events
			}
		});
	}
	

	/**
	 * Stores aDomainEvent to the event store.
	 * 
	 * @param aDomainEvent
	 *            the DomainEvent to store
	 */
	private void store(DomainEvent aDomainEvent) {
		this.eventStore().append(aDomainEvent);
	}

	/**
	 * Answers my EventStore.
	 * 
	 * @return EventStore
	 */
	private EventStore eventStore() {
		return this.eventStore;
	}

}
