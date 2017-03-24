package cn.mljia.ddd.common.port.adapter.lock.redis;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import redis.clients.jedis.Jedis;
import cn.mljia.ddd.common.lock.Lock;

/**
 * Jedis实现分布式锁
 * 
 * @ClassName: JedisLock
 * @Description: TODO
 * @author: mljia.cn-Marker
 * @date: 2016年9月26日 下午3:34:00
 */
public class JedisLock implements Lock
{
    
    private static Logger logger = LoggerFactory.getLogger(JedisLock.class);
    
    private static final String PREFIX = "redis.domain.lock.";
    
    private static final int waitInterVal = 1000; // 获取锁失败睡眠周期
    
    // 超时时间:默认5s
    private int waitTimeOut = 5;
    
    // 锁的过期时间：默认1分钟
    private int lockExpireTime = 60;
    
    // 锁的名字
    private String lockName;
    
    // Spring-Redis连接工厂
    private JedisConnectionFactory jedisConnectionFactory;
    
    public void setJedisConnectionFactory(JedisConnectionFactory jedisConnectionFactory)
    {
        this.jedisConnectionFactory = jedisConnectionFactory;
    }
    
    public JedisLock()
    {
        super();
    }
    
    /**
     * 初始化参数
     * 
     * @param lockName
     */
    public JedisLock(String lockName)
    {
        this.setLockName(lockName);
    }
    
    /**
     * 初始化参数
     * 
     * @param lockName
     * @param waitTimeOut :seconds
     * @param lockExpireTime :seconds
     */
    public JedisLock(String lockName, int waitTimeOut, int lockExpireTime)
    {
        this.setLockName(lockName);
        this.waitTimeOut = waitTimeOut;
        this.lockExpireTime = lockExpireTime;
    }
    
    /**
     * 初始化参数
     * 
     * @param jedisPool
     * @param lockName
     * @param waitTimeOut :seconds
     * @param lockExpireTime :seconds
     */
    public JedisLock(JedisConnectionFactory jedisConnectionFactory, int waitTimeOut, int lockExpireTime)
    {
        this.jedisConnectionFactory = jedisConnectionFactory;
        this.waitTimeOut = waitTimeOut;
        this.lockExpireTime = lockExpireTime;
    }
    
    /**
     * 获取锁
     */
    @Override
    public boolean tryLock(String lockName)
    {
        this.setLockName(lockName);
        Jedis jedis = null;
        boolean needReturn = true;
        boolean result = false;
        
        try
        {
            // 从JEDIS连接池获取Jedis
            jedis = getJedis();
            String uuid = UUID.randomUUID().toString();
            long end = this.waitTimeOut() * 1000 + System.currentTimeMillis();
            while (System.currentTimeMillis() < end)
            {
                if (jedis.setnx(this.lockName(), uuid) > 0)
                {
                    jedis.expire(this.lockName(), this.lockExpireTime());
                    result = true;
                    break;
                }
                
                // ttl=-1 说明key存在，但是没有过期时间。原因是：上次setNx成功后，程序crash，但是没有执行expire
                // 这种情况下可以把上次的锁占为己有
                if (jedis.ttl(this.lockName()) == -1)
                {
                    jedis.expire(this.lockName(), this.lockExpireTime());
                    result = true;
                    break;
                }
                
                try
                {
                    Thread.sleep(waitInterVal);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                    logger.error("Interrupted Thread Exception e:" + e.getMessage(), e);
                    Thread.currentThread().interrupt();
                }
            }
        }
        catch (Exception e)
        {
            if (jedis != null)
            {
                needReturn = false;
                jedis.close();
            }
            e.printStackTrace();
            logger.error("tryLock resource Exception e:" + e.getMessage(), e);
        }
        finally
        {
            if (jedis != null && needReturn)
            {
                jedis.close();
            }
        }
        return result;
    }
    
    /**
     * 释放锁
     */
    @Override
    public boolean unLock(String lockName)
    {
        this.setLockName(lockName);
        Jedis jedis = null;
        boolean needReturn = true;
        boolean result = false;
        
        try
        {
            // 从JEDIS连接池获取Jedis
            jedis = getJedis();
            int retryTimes = 3;
            while (retryTimes > 0)
            {
                // 假如某个key 正处于 WATCH 命令的监视之下，且事务块中有和这个key相关的命令，那么EXEC
                // 命令只在这个key没有被其他命令所改动的情况下执行并生效，否则该事务被打断(abort)。
                if (jedis.exists(this.lockName()))
                {
                    Long del = jedis.del(this.lockName());
                    if (del < 1)
                    {
                        retryTimes--;
                        continue;
                    }
                    result = true;
                }
                break;
            }
        }
        catch (Exception e)
        {
            if (jedis != null)
            {
                needReturn = false;
                jedis.close();
            }
            e.printStackTrace();
            logger.error("unLock resource Exception e:" + e.getMessage(), e);
        }
        finally
        {
            if (jedis != null && needReturn)
            {
                jedis.close();
            }
        }
        return result;
    }
    
    public void setWaitTimeOut(int waitTimeOut)
    {
        this.waitTimeOut = waitTimeOut;
    }
    
    public void setLockExpireTime(int lockExpireTime)
    {
        this.lockExpireTime = lockExpireTime;
    }
    
    public void setLockName(String lockName)
    {
        this.lockName = PREFIX + lockName;
    }
    
    private Jedis getJedis()
    {
        Jedis jedis = null;
        RedisConnection jedisConnection = jedisConnectionFactory.getConnection();
        jedis = (Jedis)jedisConnection.getNativeConnection();
        return jedis;
    }
    
    @Override
    public int waitTimeOut()
    {
        // TODO Auto-generated method stub
        return this.waitTimeOut;
    }
    
    @Override
    public int lockExpireTime()
    {
        // TODO Auto-generated method stub
        return this.lockExpireTime;
    }
    
    private String lockName()
    {
        return lockName;
    }
    
}
