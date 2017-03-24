
package cn.mljia.ddd.common.port.adapter.messaging.rabbitmq.ha.retry;

import cn.mljia.ddd.common.port.adapter.messaging.rabbitmq.ha.BooleanReentrantLatch;


public interface RetryStrategy {

    public boolean shouldRetry(Exception e, int numOperationInvocations, BooleanReentrantLatch connectionGate);
}
