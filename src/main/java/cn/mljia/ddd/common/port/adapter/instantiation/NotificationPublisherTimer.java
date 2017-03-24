package cn.mljia.ddd.common.port.adapter.instantiation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.mljia.ddd.common.application.NotificationApplicationService;
import cn.mljia.ddd.common.lock.Lock;
import cn.mljia.ddd.common.port.adapter.messaging.rabbitmq.MessageProducer;

public class NotificationPublisherTimer extends Thread
{
    
    private static Logger logger = LoggerFactory.getLogger(NotificationPublisherTimer.class);
    
    private String typeName;
    
    private NotificationApplicationService notificationApplicationService;
    
    private Lock lock;
    
    private String domainName;
    
    private MessageProducer messageProducer;
    
    public NotificationPublisherTimer(String typeName, NotificationApplicationService notificationApplicationService,
        Lock lock, String domainName,MessageProducer messageProducer)
    {
        this.setTypeName(typeName);
        
        this.setNotificationApplicationService(notificationApplicationService);
        
        this.setLock(lock);
        
        this.setDomainName(domainName);
        
        this.setMessageProducer(messageProducer);
    }
    
    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                boolean tryLock = false;
                try
                {
                    tryLock = this.getLock().tryLock(this.getDomainName()+"_"+this.getTypeName());
                    if (tryLock)
                    {
                        this.getNotificationApplicationService().publishNotifications(this.getMessageProducer(),this.getTypeName());
                        logger.debug(Thread.currentThread().getName() + "--->" + "resource lock success");
                    }
                    else
                    {
                        logger.debug(Thread.currentThread().getName() + "--->" + "resource lock failed");
                    }
                }
                catch (Exception e)
                {
                    logger.error("tryLock resource is error :" + e.getMessage(), e);
                }
                finally
                {
                    if (tryLock)
                    {
                        if (this.getLock().unLock(this.getDomainName()+"_"+this.getTypeName()))
                        {
                            logger.debug(Thread.currentThread().getName() + "--->" + "resource unlock success");
                        }
                        else
                        {
                            logger.debug(Thread.currentThread().getName() + "--->" + "resource unlock failed");
                        }
                    }
                }
                
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.out.println("Problem publishing notifications from ApplicationServiceLifeCycle.");
            }
            
            try
            {
                Thread.sleep(200L);
            }
            catch (Exception e)
            {
                // ignore
            }
        }
    }
    
    private String getTypeName()
    {
        return typeName;
    }
    
    private NotificationApplicationService getNotificationApplicationService()
    {
        return notificationApplicationService;
    }
    
    private void setNotificationApplicationService(NotificationApplicationService notificationApplicationService)
    {
        this.notificationApplicationService = notificationApplicationService;
    }
    
    private void setTypeName(String typeName)
    {
        this.typeName = typeName;
    }
    
    public void setLock(Lock lock)
    {
        this.lock = lock;
    }

    private Lock getLock()
    {
        return lock;
    }

    private String getDomainName()
    {
        return domainName;
    }

    private MessageProducer getMessageProducer()
    {
        return messageProducer;
    }

    private void setMessageProducer(MessageProducer messageProducer)
    {
        this.messageProducer = messageProducer;
    }

    private void setDomainName(String domainName)
    {
        this.domainName = domainName;
    }
    
    
    
    
}
