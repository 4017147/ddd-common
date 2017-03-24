package cn.mljia.ddd.common.port.adapter.messaging.rabbitmq.ha.retry;

import org.apache.log4j.Logger;

import cn.mljia.ddd.common.port.adapter.messaging.rabbitmq.ha.BooleanReentrantLatch;

/**
 * 
 * @ClassName: BlockingRetryStrategy
 * @Description: TODO 阻塞重试策略,没有重试次数和重试时间.后期改进
 * @author: mljia.cn-Marker
 * @date: 2017年1月12日 下午6:51:00
 */
public class BlockingRetryStrategy implements RetryStrategy {

	private static final Logger LOG = Logger.getLogger(BlockingRetryStrategy.class);

	public boolean shouldRetry(final Exception e, final int numOperationInvocations, final BooleanReentrantLatch connectionGate) {

		try {

			if (LOG.isDebugEnabled()) {
				LOG.debug("Waiting for connection gate to open: no timeout - " + e.getMessage());
			}

			connectionGate.waitUntilOpen();

			if (LOG.isDebugEnabled()) {
				LOG.debug("Waited for connection gate to open: connected=" + connectionGate.isOpen());
			}
		} catch (InterruptedException e1) {

			LOG.warn("Interrupted during timeout waiting for next operation invocation to occurr. Retrying invocation now.");
		}
		return true;
	}
}
