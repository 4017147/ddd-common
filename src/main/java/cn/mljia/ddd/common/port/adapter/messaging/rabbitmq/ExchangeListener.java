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

package cn.mljia.ddd.common.port.adapter.messaging.rabbitmq;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import cn.mljia.ddd.common.AssertionConcern;
import cn.mljia.ddd.common.application.configuration.RabbitmqConfiguration;
import cn.mljia.ddd.common.event.ConsumedEventStore;
import cn.mljia.ddd.common.notification.NotificationReader;

/**
 * I am an abstract base class for exchange listeners. I perform the basic set up according to the answers from my concrete subclass.
 *
 * @author Vaughn Vernon
 */
public abstract class ExchangeListener extends AssertionConcern {

	private MessageConsumer messageConsumer;

	private Queue queue;

	private RabbitmqConfiguration rabbitmqConfiguration;

	private HibernateTransactionManager hibernateTransactionManager;

	private ConsumedEventStore consumedEventStore;

	/**
	 * Constructs my default state.
	 */
	public ExchangeListener(RabbitmqConfiguration rabbitmqConfiguration, HibernateTransactionManager hibernateTransactionManager, ConsumedEventStore consumedEventStore) {
		super();

		this.setRabbitmqConfiguration(rabbitmqConfiguration);

		this.setHibernateTransactionManager(hibernateTransactionManager);

		this.setConsumedEventStore(consumedEventStore);
		
		this.attachToQueue();

		this.registerConsumer();

	}

	/**
	 * Closes my queue.
	 */
	public void close() {
		this.queue().close();
	}

	/**
	 * Answers the String name of the exchange I listen to.
	 * 
	 * @return String
	 */
	protected abstract String exchangeName();

	/**
	 * Filters out unwanted events and dispatches ones of interest.
	 * 
	 * @param aType
	 *            the String message type
	 * @param aTextMessage
	 *            the String raw text message being handled
	 */
	protected abstract void filteredDispatch(String aType, String aTextMessage) throws Exception;

	/**
	 * Answers the kinds of messages I listen to.
	 * 
	 * @return String[]
	 */
	protected abstract String[] listensTo();

	/**
	 * Answers the String name of the queue I listen to. By default it is the simple name of my concrete class. May be overridden to change the name.
	 * 
	 * @return String
	 */
	protected String queueName() {
		return this.getClass().getSimpleName();
	}

	/**
	 * Attaches to the queues I listen to for messages.
	 */
	private void attachToQueue() {
		// creates my exchange if non-existing
		Exchange exchange = Exchange.fanOutInstance(ConnectionSettings.instance(rabbitmqConfiguration().getAddress(),
        		rabbitmqConfiguration().getVirtualHost(),
        		rabbitmqConfiguration().getUsername(), rabbitmqConfiguration().getPassword()), this.exchangeName(), true);

		this.queue = Queue.individualExchangeSubscriberInstance(exchange, this.exchangeName() + "." + this.queueName());
	}

	/**
	 * Answers my queue.
	 * 
	 * @return Queue
	 */
	private Queue queue() {
		return this.queue;
	}

	/**
	 * Registers my listener for queue messages and dispatching.
	 */
	private void registerConsumer() {
		this.messageConsumer = MessageConsumer.instance(this.queue(), false);

		this.messageConsumer.receiveOnly(this.listensTo(), new MessageListener(MessageListener.Type.TEXT) {

			@Override
			public void handleMessage(String aType, String aMessageId, Date aTimestamp, String aTextMessage, long aDeliveryTag, boolean isRedelivery) throws Exception {
				if (StringUtils.isNotEmpty(aTextMessage) && StringUtils.isNotEmpty(aType)) {
					NotificationReader notificationReader = new NotificationReader(aTextMessage);
					long notificationId = notificationReader.notificationId();
					TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
					TransactionStatus status = hibernateTransactionManager().getTransaction(transactionDefinition);
					try {
						boolean isDealWith = consumedEventStore().isDealWithEvent(notificationId, aType);
						if (!isDealWith) {
							filteredDispatch(aType, aTextMessage);
							consumedEventStore().append(notificationId, aType);
							hibernateTransactionManager().commit(status);
						} else {
							hibernateTransactionManager().rollback(status);
						}
					} catch (Exception e) {
						hibernateTransactionManager().rollback(status);
						throw e;
					}
				}
			}
		});
	}

	private RabbitmqConfiguration rabbitmqConfiguration() {
		return rabbitmqConfiguration;
	}

	private void setRabbitmqConfiguration(RabbitmqConfiguration rabbitmqConfiguration) {
		assertArgumentNotNull(rabbitmqConfiguration, "rabbitmq configuration is must be requird.");
		this.rabbitmqConfiguration = rabbitmqConfiguration;
	}

	private HibernateTransactionManager hibernateTransactionManager() {
		return hibernateTransactionManager;
	}

	private void setHibernateTransactionManager(HibernateTransactionManager hibernateTransactionManager) {
		assertArgumentNotNull(hibernateTransactionManager, "rabbitmq hibernateTransaction is must be requird.");
		this.hibernateTransactionManager = hibernateTransactionManager;
	}

	private ConsumedEventStore consumedEventStore() {
		return consumedEventStore;
	}

	private void setConsumedEventStore(ConsumedEventStore consumedEventStore) {
		assertArgumentNotNull(consumedEventStore, "rabbitmq consumedEventStore is must be requird.");
		this.consumedEventStore = consumedEventStore;
	}

}
