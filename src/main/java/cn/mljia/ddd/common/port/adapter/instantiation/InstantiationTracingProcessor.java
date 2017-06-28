package cn.mljia.ddd.common.port.adapter.instantiation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import cn.mljia.ddd.common.application.NotificationApplicationService;
import cn.mljia.ddd.common.application.configuration.CommonConfiguration;
import cn.mljia.ddd.common.application.configuration.RabbitmqConfiguration;
import cn.mljia.ddd.common.domain.model.DomainEvent;
import cn.mljia.ddd.common.lock.Lock;
import cn.mljia.ddd.common.port.adapter.messaging.rabbitmq.ConnectionSettings;
import cn.mljia.ddd.common.port.adapter.messaging.rabbitmq.Exchange;
import cn.mljia.ddd.common.port.adapter.messaging.rabbitmq.MessageProducer;
import cn.mljia.ddd.common.port.adapter.packagescan.ClassInfo;
import cn.mljia.ddd.common.port.adapter.packagescan.ClassPath;

public class InstantiationTracingProcessor implements ApplicationListener<ContextRefreshedEvent>
{
    private static Logger logger = LoggerFactory.getLogger(InstantiationTracingProcessor.class);
    
    private static final Map<String, Class<?>> DOMAIN_EVENTS = new HashMap<String, Class<?>>();
    
    private static ScheduledExecutorService scheduledThreadPool = null;
    
    private NotificationApplicationService notificationApplicationService;
    
    private String scanPackage;
    
    private Lock lock;
    
    private CommonConfiguration commonConfiguration;
    
    private RabbitmqConfiguration rabbitmqConfiguration;
    
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event)
    {
        // root application context 没有parent，他就是老大.
        if (event.getApplicationContext().getParent() == null)
        {
            try
            {
                this.loaderDomainEvents(this.getScanPackage(), DomainEvent.class);
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
                logger.error("init domain event tracker is exception ===>{}", e.getMessage(), e);
            }
            
            this.postThreadPoolExecutor();
            
        }
    }
    
    private void loaderDomainEvents(String packageName, Class<?> event)
        throws ClassNotFoundException
    {
        List<Class<?>> returnClassList = null;
        if (event.isInterface())
        {
            ClassPath classPath = ClassPath.from(DomainEvent.class.getClassLoader());
            List<ClassInfo> classInfos = classPath.getTopLevelRecursiveAllClass(this.getScanPackage());
            if (classInfos != null)
            {
                returnClassList = new ArrayList<Class<?>>();
                for (ClassInfo c : classInfos)
                {
                    
                    Class<?> clazz = Class.forName(c.getName());
                    // 判断是否是同一个接口
                    if (event.isAssignableFrom(clazz))
                    {
                        // 本身不加入进去
                        if (!event.equals(clazz))
                        {
                            returnClassList.add(clazz);
                        }
                    }
                }
            }
        }
        for (Class<?> clas : returnClassList)
        {
            DOMAIN_EVENTS.put(clas.getName(), clas);
        }
    }
    
    public void postThreadPoolExecutor()
    {
        scheduledThreadPool = Executors.newScheduledThreadPool(DOMAIN_EVENTS.size());
        for (Map.Entry<String, Class<?>> event : DOMAIN_EVENTS.entrySet())
        {
        	if(!"cn.mljia.ddd.common.domain.model.process.ProcessTimedOut".equals(event.getKey())){
        		scheduledThreadPool.scheduleWithFixedDelay(new NotificationPublisherTimer(event.getKey(),
                        this.getNotificationApplicationService(), this.getLock(),
                        this.getCommonConfiguration().getDomainName(), this.messageProducer(event.getKey())),
                        100,
                        400,
                        TimeUnit.MILLISECONDS);
        	}
        }
    }
    
    private MessageProducer messageProducer(String exchangeName)
    {
        
        // creates my exchange if non-existing
        Exchange exchange =
            Exchange.fanOutInstance(ConnectionSettings.instance(this.getRabbitmqConfiguration().getAddress(),
                this.getRabbitmqConfiguration().getVirtualHost(),
                this.getRabbitmqConfiguration().getUsername(),
                this.getRabbitmqConfiguration().getPassword()), exchangeName, true, true);
        // create a message producer used to forward events
        MessageProducer messageProducer = MessageProducer.instance(exchange);
        
        return messageProducer;
    }
    
    public Lock getLock()
    {
        return lock;
    }
    
    public void setLock(Lock lock)
    {
        this.lock = lock;
    }
    
    public String getScanPackage()
    {
        return scanPackage;
    }
    
    public void setScanPackage(String scanPackage)
    {
        this.scanPackage = scanPackage;
    }
    
    public NotificationApplicationService getNotificationApplicationService()
    {
        return notificationApplicationService;
    }
    
    public void setNotificationApplicationService(NotificationApplicationService notificationApplicationService)
    {
        this.notificationApplicationService = notificationApplicationService;
    }
    
    public CommonConfiguration getCommonConfiguration()
    {
        return commonConfiguration;
    }
    
    public void setCommonConfiguration(CommonConfiguration commonConfiguration)
    {
        this.commonConfiguration = commonConfiguration;
    }
    
    public RabbitmqConfiguration getRabbitmqConfiguration()
    {
        return rabbitmqConfiguration;
    }
    
    public void setRabbitmqConfiguration(RabbitmqConfiguration rabbitmqConfiguration)
    {
        this.rabbitmqConfiguration = rabbitmqConfiguration;
    }
    
}
