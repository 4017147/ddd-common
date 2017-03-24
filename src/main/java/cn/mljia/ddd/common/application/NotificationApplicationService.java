package cn.mljia.ddd.common.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import cn.mljia.ddd.common.notification.NotificationPublisher;
import cn.mljia.ddd.common.port.adapter.messaging.rabbitmq.MessageProducer;

public class NotificationApplicationService
{
    
    private static Logger logger = LoggerFactory.getLogger(NotificationApplicationService.class);
    
    private NotificationPublisher notificationPublisher;
    
    public NotificationApplicationService()
    {
        super();
    }
    
    public NotificationApplicationService(NotificationPublisher notificationPublisher)
    {
        super();
        this.setNotificationPublisher(notificationPublisher);
    }
    
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void publishNotifications(MessageProducer messageProducer,String typeName) throws Exception
    {
        try
        {
            this.getNotificationPublisher().publishNotifications(messageProducer,typeName);
        }
        catch (Exception e)
        {
            logger.error("NotificationApplicationService publishNotifications Exception e:"+e.getMessage(), e);
            throw e;
        }
    }
    
    
    public NotificationPublisher getNotificationPublisher()
    {
        return notificationPublisher;
    }
    
    public void setNotificationPublisher(NotificationPublisher notificationPublisher)
    {
        this.notificationPublisher = notificationPublisher;
    }
    
}
