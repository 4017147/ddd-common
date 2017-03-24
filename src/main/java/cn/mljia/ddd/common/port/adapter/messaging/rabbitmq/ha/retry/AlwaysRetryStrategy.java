package cn.mljia.ddd.common.port.adapter.messaging.rabbitmq.ha.retry;

import cn.mljia.ddd.common.port.adapter.messaging.rabbitmq.ha.BooleanReentrantLatch;

/**
 * 
 * @ClassName: AlwaysRetryStrategy
 * @Description: TODO
 * @author: mljia.cn-Marker
 * @date: 2017年1月17日 上午9:57:50
 */
public class AlwaysRetryStrategy implements RetryStrategy {

	public boolean shouldRetry(final Exception e, final int numOperationInvocations, final BooleanReentrantLatch connectionGate) {
		return true;
	}

}
