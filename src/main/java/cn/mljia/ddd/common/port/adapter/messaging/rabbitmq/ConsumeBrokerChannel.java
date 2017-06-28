package cn.mljia.ddd.common.port.adapter.messaging.rabbitmq;

public class ConsumeBrokerChannel extends BrokerChannel
{
    
    public static ConsumeBrokerChannel instance(ConnectionSettings aConnectionSettings)
    {
        return new ConsumeBrokerChannel(aConnectionSettings);
    }
    
    protected ConsumeBrokerChannel(ConnectionSettings aConnectionSettings)
    {
        super(aConnectionSettings);
    }
    
}
