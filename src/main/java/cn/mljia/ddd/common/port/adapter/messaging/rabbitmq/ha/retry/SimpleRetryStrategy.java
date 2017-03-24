package cn.mljia.ddd.common.port.adapter.messaging.rabbitmq.ha.retry;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import cn.mljia.ddd.common.port.adapter.messaging.rabbitmq.ha.BooleanReentrantLatch;

/***
 * 
 * @ClassName: SimpleRetryStrategy
 * @Description: TODO 时间次数重试策略
 * @author: mljia.cn-Marker
 * @date: 2017年1月16日 上午19:58:32
 */
public class SimpleRetryStrategy implements RetryStrategy {

	private static final Logger LOG = Logger.getLogger(SimpleRetryStrategy.class);

	/**
	 * Default value = 10000 = 10 seconds
	 */
	public static final long DEFAULT_OPERATION_RETRY_TIMEOUT_MILLIS = 10000;

	/**
	 * Default value = 2 (one retry)
	 */
	public static final int DEFAULT_MAX_OPERATION_INVOCATIONS = 2;

	private long operationRetryTimeoutMillis = DEFAULT_OPERATION_RETRY_TIMEOUT_MILLIS;

	private int maxOperationInvocations = DEFAULT_MAX_OPERATION_INVOCATIONS;

	public void setMaxOperationInvocations(final int maxOperationInvocations) {

		Validate.isTrue(maxOperationInvocations >= 2, "max operation invocations must be 2 or greater, otherwise use a simpler strategy");
		this.maxOperationInvocations = maxOperationInvocations;
	}

	public void setOperationRetryTimeoutMillis(final long timeout) {

		Validate.isTrue(timeout >= 0, "timeout must be a positive number");
		operationRetryTimeoutMillis = timeout;
	}

	public boolean shouldRetry(final Exception e, final int numOperationInvocations, final BooleanReentrantLatch connectionGate) {

		if (LOG.isDebugEnabled()) {
			LOG.debug("Operation invocation failed on IOException: numOperationInvocations=" + numOperationInvocations + ", maxOperationInvocations=" + maxOperationInvocations + ", message=" + e.getMessage());
		}

		if (numOperationInvocations == maxOperationInvocations) {

			if (LOG.isDebugEnabled()) {
				LOG.debug("Max number of operation invocations reached, not retrying: " + maxOperationInvocations);
			}

			return false;
		}

		if (operationRetryTimeoutMillis > 0) {

			if (LOG.isDebugEnabled()) {
				LOG.debug("Sleeping before next operation invocation (millis): " + operationRetryTimeoutMillis);
			}

			try {
				Thread.sleep(operationRetryTimeoutMillis);
			} catch (InterruptedException ie) {
				LOG.warn("Interrupted during timeout waiting for next operation invocation to occurr. " + "Retrying invocation now.");
			}
		} else {

			if (LOG.isDebugEnabled()) {
				LOG.debug("No timeout set, retrying immediately");
			}
		}

		return true;
	}

}
