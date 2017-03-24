package cn.mljia.ddd.common.port.adapter.instantiation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import cn.mljia.ddd.common.application.NotificationApplicationService;
import cn.mljia.ddd.common.lock.Lock;
import cn.mljia.ddd.common.port.adapter.messaging.rabbitmq.MessageProducer;

public class DistributeEventTracker implements Runnable
{
    private static Logger logger = LoggerFactory.getLogger(DistributeEventTracker.class);
    
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    
    private String typeName;
    
    private NotificationApplicationService notificationApplicationService;
    
    private Lock lock;
    
    private String domainName;
    
    private MessageProducer messageProducer;
    
    public DistributeEventTracker(ThreadPoolTaskExecutor threadPoolTaskExecutor, String typeName,
        NotificationApplicationService notificationApplicationService, Lock lock, String domainName,
        MessageProducer messageProducer)
    {
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
        this.typeName = typeName;
        this.notificationApplicationService = notificationApplicationService;
        this.lock = lock;
        this.domainName = domainName;
        this.messageProducer = messageProducer;
    }
    
    @Override
    public synchronized void run()
    {
        
        logger.debug("distribute event task >>{}" + typeName + "  and commit task");
        
        threadPoolTaskExecutor.execute(new NotificationPublisherTimer(this.typeName,
            this.notificationApplicationService, this.lock, this.domainName, this.messageProducer));
        
    }
    
}
