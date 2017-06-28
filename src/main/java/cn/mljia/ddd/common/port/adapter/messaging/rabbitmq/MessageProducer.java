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

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.mljia.ddd.common.notification.Notification;
import cn.mljia.ddd.common.notification.NotificationSerializer;
import cn.mljia.ddd.common.port.adapter.messaging.MessageException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.MessageProperties;

/**
 * I am a message producer, which facilitates sending messages to a BrokerChannel.
 * A BrokerChannel may be either an Exchange or a Queue.
 *
 * @author Vaughn Vernon
 */
public class MessageProducer {

	private static Logger logger = LoggerFactory.getLogger(MessageProducer.class);
	
    /** My brokerChannel, which is where I send messages. */
    private BrokerChannel brokerChannel;

    /**
     * Answers a new instance of a MessageProducer.
     * @param aBrokerChannel the BrokerChannel where messages are to be sent
     * @return MessageProducer
     */
    public static MessageProducer instance(BrokerChannel aBrokerChannel) {
        return new MessageProducer(aBrokerChannel);
    }

    /**
     * Closes me, which closes my broker channel.
     */
    public void close() {
        this.brokerChannel().close();
    }

    /**
     * Answers the receiver after sending aTextMessage to my channel.
     * This is a producer ignorance way to use either an exchange or
     * a queue channel without requiring it to pass specific parameters.
     * By answering myself I allow for sending message bursts.
     * @param aTextMessage the String text message to send
     * @return MessageProducer
     */
    public MessageProducer send(String aTextMessage) {
        try {
        	this.confirmSelect();
            this.brokerChannel().channel().basicPublish(
                    this.brokerChannel().exchangeName(),
                    this.brokerChannel().queueName(),
                    this.textDurability(),
                    aTextMessage.getBytes());
            this.waitForConfirmsOrDie();
        } catch (IOException e) {
            throw new MessageException("Failed to send message to channel.", e);
        }
        return this;
    }

    /**
     * Answers the receiver after sending aTextMessage to my channel
     * with aMessageParameters as the message basic properties.
     * This is a producer ignorance way to use either an exchange or
     * a queue channel without requiring it to pass specific parameters.
     * By answering myself I allow for sending message bursts.
     * @param aTextMessage the String text message to send
     * @param aMessageParameters the MessageParameters
     * @return MessageProducer
     */
    public MessageProducer send(List<Notification> notifications) throws MessageException {
        try {
//        	this.confirmSelect();
        	String notificationIds="";
         	for(Notification aNotification :notifications){
       				MessageParameters aMessageParameters =
    		        MessageParameters.durableTextParameters(
    		                    aNotification.typeName(),
    		                    Long.toString(aNotification.notificationId()),
    		                    aNotification.occurredOn());
    		        String notification =
    		            NotificationSerializer
    		                .instance()
    		                .serialize(aNotification);
	       		 	this.check(aMessageParameters);
	       		 	this.brokerChannel().channel().basicPublish(
	                     this.brokerChannel().exchangeName(),
	                     this.brokerChannel().queueName(),
	                     aMessageParameters.properties(),
	                     notification.getBytes());
	       		 notificationIds+=aNotification.notificationId()+",";
         	}
         	if(notifications!=null&&!notifications.isEmpty()){
	         	logger.warn("send notifications size------>>>[{}]------typeName----->>>[{}]------notificationIds----->>>[{}]"
	         			,new Object[]{notifications.size(),notifications.get(0).typeName(),notificationIds.substring(0, notificationIds.length()-1)});
         	}
            this.waitForConfirmsOrDie();
        } catch (IOException e) {
            throw new MessageException("Failed to send message to channel.", e);
        } catch (IllegalArgumentException e) {
        	throw new MessageException("Failed to send message to channel.", e);
		}
        return this;
    }
    
    
    /**
     * Answers the receiver after sending aTextMessage to my channel
     * with aMessageParameters as the message basic properties.
     * This is a producer ignorance way to use either an exchange or
     * a queue channel without requiring it to pass specific parameters.
     * By answering myself I allow for sending message bursts.
     * @param aTextMessage the String text message to send
     * @param aMessageParameters the MessageParameters
     * @return MessageProducer
     */
    public MessageProducer send(
            String aTextMessage,
            MessageParameters aMessageParameters) {

        this.check(aMessageParameters);

        try {
        	this.confirmSelect();
            this.brokerChannel().channel().basicPublish(
                    this.brokerChannel().exchangeName(),
                    this.brokerChannel().queueName(),
                    aMessageParameters.properties(),
                    aTextMessage.getBytes());
            this.waitForConfirmsOrDie();
        } catch (IOException e) {
            throw new MessageException("Failed to send message to channel.", e);
        }
        return this;
    }

    /**
     * Answers the receiver after sending aTextMessage to my channel with
     * aRoutingKey and aMessageParameters. This is a producer ignorance way
     * to use an exchange without requiring it to pass the exchange name.
     * By answering myself I allow for sending message bursts.
     * @param aRoutingKey the String routing key
     * @param aTextMessage the String text message to send
     * @param aMessageParameters the MessageParameters
     * @return MessageProducer
     */
    public MessageProducer send(
            String aRoutingKey,
            String aTextMessage,
            MessageParameters aMessageParameters) {

        this.check(aMessageParameters);

        try {
        	this.confirmSelect();
            this.brokerChannel().channel().basicPublish(
                    this.brokerChannel().exchangeName(),
                    aRoutingKey,
                    aMessageParameters.properties(),
                    aTextMessage.getBytes());
            this.waitForConfirmsOrDie();
        } catch (IOException e) {
            throw new MessageException("Failed to send message to channel.", e);
        }
        return this;
    }

    /**
     * Answers the receiver after sending aTextMessage to my channel
     * with anExchange and aRoutingKey. By answering myself I allow
     * for sending message bursts.
     * @param anExchange the String name of the exchange
     * @param aRoutingKey the String routing key
     * @param aTextMessage the String text message to send
     * @param aMessageParameters the MessageParameters
     * @return MessageProducer
     */
    public MessageProducer send(
            String anExchange,
            String aRoutingKey,
            String aTextMessage,
            MessageParameters aMessageParameters) {

        this.check(aMessageParameters);

        try {
        	this.confirmSelect();
            this.brokerChannel().channel().basicPublish(
                    anExchange,
                    aRoutingKey,
                    aMessageParameters.properties(),
                    aTextMessage.getBytes());
            this.waitForConfirmsOrDie();
        } catch (IOException e) {
            throw new MessageException("Failed to send message to channel.", e);
        }
        return this;
    }

    /**
     * Answers the receiver after sending aBinaryMessage to my channel.
     * This is a producer ignorance way to use either an exchange or
     * a queue channel without requiring it to pass specific parameters.
     * By answering myself I allow for sending message bursts.
     * @param aBinaryMessage the byte[] binary message to send
     * @return MessageProducer
     */
    public MessageProducer send(byte[] aBinaryMessage) {
        try {
        	this.confirmSelect();
            this.brokerChannel().channel().basicPublish(
                    this.brokerChannel().exchangeName(),
                    this.brokerChannel().queueName(),
                    this.binaryDurability(),
                    aBinaryMessage);
            this.waitForConfirmsOrDie();
        } catch (IOException e) {
            throw new MessageException("Failed to send message to channel.", e);
        }
        return this;
    }

    /**
     * Answers the receiver after sending aBinaryMessage to my channel.
     * This is a producer ignorance way to use either an exchange or
     * a queue channel without requiring it to pass specific parameters.
     * By answering myself I allow for sending message bursts.
     * @param aBinaryMessage the byte[] binary message to send
     * @param aMessageParameters the MessageParameters
     * @return MessageProducer
     */
    public MessageProducer send(
            byte[] aBinaryMessage,
            MessageParameters aMessageParameters) {

        this.check(aMessageParameters);

        try {
        	this.confirmSelect();
            this.brokerChannel().channel().basicPublish(
                    this.brokerChannel().exchangeName(),
                    this.brokerChannel().queueName(),
                    this.binaryDurability(),
                    aBinaryMessage);
            this.waitForConfirmsOrDie();
        } catch (IOException e) {
            throw new MessageException("Failed to send message to channel.", e);
        }
        return this;
    }

    /**
     * Answers the receiver after sending aBinaryMessage to my channel with
     * aRoutingKey. This is a producer ignorance way to use an exchange
     * without requiring it to pass the exchange name. By answering
     * myself I allow for sending message bursts.
     * @param aRoutingKey the String routing key
     * @param aBinaryMessage the byte[] binary message to send
     * @param aMessageParameters the MessageParameters
     * @return MessageProducer
     */
    public MessageProducer send(
            String aRoutingKey,
            byte[] aBinaryMessage,
            MessageParameters aMessageParameters) {

        this.check(aMessageParameters);

        try {
        	this.confirmSelect();
            this.brokerChannel().channel().basicPublish(
                    this.brokerChannel().exchangeName(),
                    aRoutingKey,
                    this.binaryDurability(),
                    aBinaryMessage);
            this.waitForConfirmsOrDie();
        } catch (IOException e) {
            throw new MessageException("Failed to send message to channel.", e);
        }
        return this;
    }

    /**
     * Answers the receiver after sending aBinaryMessage to my channel
     * with anExchange and aRoutingKey. By answering myself I allow
     * for sending message bursts.
     * @param anExchange the String name of the exchange
     * @param aRoutingKey the String routing key
     * @param aBinaryMessage the byte[] binary message to send
     * @param aMessageParameters the MessageParameters
     * @return MessageProducer
     */
    public MessageProducer send(
            String anExchange,
            String aRoutingKey,
            byte[] aBinaryMessage,
            MessageParameters aMessageParameters) {

        this.check(aMessageParameters);

        try {
        	this.confirmSelect();
            this.brokerChannel().channel().basicPublish(
                    anExchange,
                    aRoutingKey,
                    this.binaryDurability(),
                    aBinaryMessage);
            this.waitForConfirmsOrDie();
        } catch (IOException e) {
            throw new MessageException("Failed to send message to channel.", e);
        }
        return this;
    }

    /**
     * Constructs my default state.
     * @param aBrokerChannel the BrokerChannel to which I send messages
     */
    protected MessageProducer(BrokerChannel aBrokerChannel) {
        super();
        this.setBrokerChannel(aBrokerChannel);
    }

    /**
     * Answers my brokerChannel.
     * @return BrokerChannel
     */
    protected BrokerChannel brokerChannel() {
        return this.brokerChannel;
    }

    /**
     * Sets my brokerChannel.
     * @param aBrokerChannel the BrokerChannel to set as my brokerChannel
     */
    private void setBrokerChannel(BrokerChannel aBrokerChannel) {
        this.brokerChannel = aBrokerChannel;
    }

    /**
     * Checks aMessageParameters for validity.
     * @param aMessageParameters the MessageParameters to check
     */
    private void check(MessageParameters aMessageParameters) {
        if (this.brokerChannel().isDurable()) {
            if (!aMessageParameters.isDurable()) {
                throw new IllegalArgumentException("MessageParameters must be durable.");
            }
        } else {
            if (aMessageParameters.isDurable()) {
                throw new IllegalArgumentException("MessageParameters must not be durable.");
            }
        }
    }

    /**
     * Answers the binary durability BasicProperties according
     * to the brokerChannel's durability.
     * @return BasicProperties
     */
    private BasicProperties binaryDurability() {
        BasicProperties durability = null;
        if (this.brokerChannel().isDurable()) {
            durability = MessageProperties.PERSISTENT_BASIC;
        }
        return durability;
    }

    /**
     * Answers the text durability BasicProperties according
     * to the brokerChannel's durability.
     * @return BasicProperties
     */
    private BasicProperties textDurability() {
        BasicProperties durability = null;
        if (this.brokerChannel().isDurable()) {
            durability = MessageProperties.PERSISTENT_TEXT_PLAIN;
        }
        return durability;
    }
    
    /**
     * 
     * @Title: confirmSelect 
     * @Description: TODO 开启通道上的 publisher acknowledgements
     * @return: void
     */
    private void confirmSelect(){
    	try {
			this.brokerChannel().channel().confirmSelect();
		} catch (IOException e) {
			throw new IllegalArgumentException("this brokerChannel confirmSelect is IOException.");
		}
    }
    
    /**
     * 
     * @Title: waitForConfirmsOrDie 
     * @Description: TODO 等待所有消息都被ack或者nack，如果某个消息被nack，则抛出IOException
     * @return: void
     */
    private void waitForConfirmsOrDie(){  
    	try {
			this.brokerChannel().channel().waitForConfirmsOrDie();
		} catch (IOException e) {
			throw new IllegalArgumentException("this brokerChannel waitForConfirmsOrDie is IOException.");
		} catch (InterruptedException e) {
			throw new IllegalArgumentException("this brokerChannel waitForConfirmsOrDie is InterruptedException.");
		}
    }			
    
}
