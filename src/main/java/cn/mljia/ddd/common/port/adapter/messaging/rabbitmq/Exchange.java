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
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.StringUtils;

import cn.mljia.ddd.common.port.adapter.messaging.MessageException;

/**
 * I am an exchange that simplifies RabbitMQ exchanges.
 *
 * @author Vaughn Vernon
 */
public class Exchange extends BrokerChannel {

    /** My type, which is the type of exchange. */
    private String type;

    /**
     * Answers a new instance of a direct Exchange with the name aName. The
     * underlying exchange has the isDurable quality, and is not auto-deleted.
     * @param aConnectionSettings the ConnectionSettings
     * @param aName the String name of the exchange
     * @param isDurable the boolean indicating whether or not I am durable
     * @return Exchange
     */
    public static Exchange directInstance(
            ConnectionSettings aConnectionSettings,
            String aName,
            boolean isDurable) {

        return new Exchange(aConnectionSettings, aName, "direct", isDurable);
    }

    /**
     * Answers a new instance of a fan-out Exchange with the name aName. The
     * underlying exchange has the isDurable quality, and is not auto-deleted.
     * @param aConnectionSettings the ConnectionSettings
     * @param aName the String name of the exchange
     * @param isDurable the boolean indicating whether or not I am durable
     * @return Exchange
     */
    public static Exchange fanOutInstance(
            ConnectionSettings aConnectionSettings,
            String aName,
            boolean isDurable) {

        return new Exchange(aConnectionSettings, aName, "fanout", isDurable);
    }

    /**
     * Answers a new instance of a fan-out Exchange with the name aName. The
     * underlying exchange has the isDurable quality, and is not auto-deleted.
     * @param aConnectionSettings the ConnectionSettings
     * @param aName the String name of the exchange
     * @param isDurable the boolean indicating whether or not I am durable
     * @return Exchange
     */
    public static Exchange fanOutInstance(
            ConnectionSettings aConnectionSettings,
            String aName,
            boolean isDurable,boolean isConfirmSelect) {

        return new Exchange(aConnectionSettings, aName, "fanout", isDurable,isConfirmSelect);
    }

    
    
    /**
     * Answers a new instance of a headers Exchange with the name aName. The
     * underlying exchange has the isDurable quality, and is not auto-deleted.
     * @param aConnectionSettings the ConnectionSettings
     * @param aName the String name of the exchange
     * @param isDurable the boolean indicating whether or not I am durable
     * @return Exchange
     */
    public static Exchange headersInstance(
            ConnectionSettings aConnectionSettings,
            String aName,
            boolean isDurable) {

        return new Exchange(aConnectionSettings, aName, "headers", isDurable);
    }

    /**
     * Answers a new instance of a topic Exchange with the name aName. The
     * underlying exchange has the isDurable quality, and is not auto-deleted.
     * @param aConnectionSettings the ConnectionSettings
     * @param aName the String name of the exchange
     * @param isDurable the boolean indicating whether or not I am durable
     * @return Exchange
     */
    public static Exchange topicInstance(
            ConnectionSettings aConnectionSettings,
            String aName,
            boolean isDurable) {

        return new Exchange(aConnectionSettings, aName, "topic", isDurable);
    }

    /**
     * Constructs my default state.
     * @param aConnectionSettings the ConnectionSettings
     * @param aName the String name of the exchange
     * @param aType the String type of the exchange
     * @param isDurable the boolean indicating whether or not I am durable
     */
    protected Exchange(
            ConnectionSettings aConnectionSettings,
            String aName,
            String aType,
            boolean isDurable) {

        super(aConnectionSettings, aName);

        this.setDurable(isDurable);

        this.setType(aType);

        try {
            this.channel().exchangeDeclare(aName, aType, isDurable);
        } catch (IOException e) {
            throw new MessageException("Failed to create/open the exchange.", e);
        }
    }
    
    
    /**
     * Constructs my default state.
     * @param aConnectionSettings the ConnectionSettings
     * @param aName the String name of the exchange
     * @param aType the String type of the exchange
     * @param isDurable the boolean indicating whether or not I am durable
     */
    protected Exchange(
            ConnectionSettings aConnectionSettings,
            String aName,
            String aType,
            boolean isDurable,
            boolean isConfirmSelect) {
        this(aConnectionSettings, aName, aType, isDurable);
        if(isConfirmSelect){
            confirmSelect();
        }   
    }

    /**
     * 
     * @Title: confirmSelect 
     * @Description: TODO 开启通道上的 publisher acknowledgements
     * @return: void
     */
    private void confirmSelect(){
        try {
            this.channel().confirmSelect();
        } catch (IOException e) {
            throw new IllegalArgumentException("this brokerChannel confirmSelect is IOException.");
        }
    }
    
    /**
     * @see cn.mljia.ddd.common.port.adapter.messaging.rabbitmq.BrokerChannel#isExchange()
     */
    @Override
    protected boolean isExchange() {
        return true;
    }

    /**
     * Answers my type.
     * @return String
     */
    protected String type() {
        return this.type;
    }

    /**
     * Sets my type.
     * @param aType the String to set as my type
     */
    private void setType(String aType) {
        this.type = aType;
    }
    
    
    /**
     * Answers a new instance of a fan-out Exchange with the name aName. The
     * underlying exchange has the isDurable quality, and is not auto-deleted.
     * @param aConnectionSettings the ConnectionSettings
     * @param aName the String name of the exchange
     * @param isDurable the boolean indicating whether or not I am durable
     * @return Exchange
     */
    public static void exchangesFanoutDeclare(ConnectionSettings aConnectionSettings,String[] exchangeNames,
            boolean isDurable) {
        for (int i = 0, length = exchangeNames.length; i < length; i++)
        {
            try
            {
                if(StringUtils.isNotBlank(exchangeNames[i])&&StringUtils.isNotEmpty(exchangeNames[i])){
                    Exchange exchange= new Exchange(aConnectionSettings, exchangeNames[i], "fanout", isDurable);
                    exchange.channel().close();
                    exchange.closeConnection();
                }
            }
            catch (IOException | TimeoutException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
