package cn.mljia.ddd.common.port.adapter.messaging.rabbitmq.ha.retry;

import cn.mljia.ddd.common.port.adapter.messaging.rabbitmq.ha.BooleanReentrantLatch;

/**
 * 
 * @ClassName: NeverRetryStrategy
 * @Description: TODO
 * @author: mljia.cn-Marker
 * @date: 2017年1月17日 上午9:57:58
 */
public class NeverRetryStrategy implements RetryStrategy {

	public boolean shouldRetry(final Exception e, final int numOperationInvocations, final BooleanReentrantLatch connectionGate) {
		return false;
	}
}
