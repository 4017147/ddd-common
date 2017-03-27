package cn.mljia.ddd.common.port.adapter.instantiation;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    
    private String lockName;
    
    public NotificationPublisherTimer(String typeName, NotificationApplicationService notificationApplicationService,
        Lock lock, String domainName, MessageProducer messageProducer)
    {
        this.setTypeName(typeName);
        
        this.setNotificationApplicationService(notificationApplicationService);
        
        this.setLock(lock);
        
        this.setDomainName(domainName);
        
        this.setMessageProducer(messageProducer);
        
        this.setLockName(this.getDomainName() + "_" + this.getTypeName());
    }
    
    @Override
    public void run()
    {
        this.setName(this.getDomainName() + "_" + this.getTypeName());
        if (logger.isDebugEnabled())
        {
            DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
            logger.debug("Notification Message Tracker Name======>{},currentTime:====>{}",
                this.getTypeName(),
                dateFormat.format(new Date()));
        }
        
        try
        {
            boolean tryLock = false;
            try
            {
                tryLock = this.getLock().tryLock(this.getLockName());
                if (tryLock)
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("thread-name:=====>{},lockName:=======>{},tryLock-status:=======>{}",
                            Thread.currentThread().getName(),
                            lockName,
                            tryLock);
                    }
                    else
                    {
                        this.getNotificationApplicationService().publishNotifications(this.getMessageProducer(),
                            this.getTypeName());
                    }
                    
                }
                else
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("thread-name:=====>{},lockName:=======>{},tryLock-status:=======>{}",
                            Thread.currentThread().getName(),
                            lockName,
                            tryLock);
                    }
                }
            }
            catch (Exception e)
            {
                logger.debug("current Tracker Name======>{}", this.getTypeName());
                logger.error("tryLock resource is error :" + e.getMessage(), e);
            }
            finally
            {
                if (tryLock)
                {
                    if (this.getLock().unLock(this.getLockName()))
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("thread-name:=====>{},lockName:=======>{},unLock-status:=======>{}",
                                Thread.currentThread().getName(),
                                lockName,
                                "success");
                        }
                    }
                    else
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("thread-name:=====>{},lockName:=======>{},unLock-status:=======>{}",
                                Thread.currentThread().getName(),
                                lockName,
                                "fail");
                        }
                    }
                }
            }
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Problem publishing notifications from ApplicationServiceLifeCycle.");
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
    
    private String getLockName()
    {
        return lockName;
    }
    
    private void setLockName(String lockName)
    {
        this.lockName = lockName;
    }
    
}
