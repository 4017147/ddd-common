package cn.mljia.ddd.common.port.adapter.messaging.rabbitmq.ha;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import cn.mljia.ddd.common.port.adapter.messaging.rabbitmq.ha.retry.RetryStrategy;

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;

/**
 * 
 * @ClassName: HaChannelProxy
 * @Description: TODO Channel 的代理类,程序在真正使用Channel的时候会使用HaChannelProxy去代理完成
 * @author: mljia.cn-Marker
 * @date: 2017年1月11日 下午13:25:13
 */
public class HaChannelProxy implements InvocationHandler {

	private static final Logger LOG = Logger.getLogger(HaChannelProxy.class);

	private static final String BASIC_CONSUME_METHOD_NAME = "basicConsume";

	private static final String CLOSE_METHOD_NAME = "close";

	private final HaConnectionProxy connectionProxy;

	private Channel target;// 真正connection 创建出来的 Channel

	private final RetryStrategy retryStrategy; // 重试策略---这里接口抽象化方便以后叠加新策略

	private final BooleanReentrantLatch connectionLatch;// 锁

	private final ConcurrentHashMap<Consumer, HaConsumerProxy> consumerProxies;// 缓存

	public HaChannelProxy(final HaConnectionProxy connectionProxy, final Channel target, final RetryStrategy retryStrategy) {

		assert connectionProxy != null;
		assert target != null;
		assert retryStrategy != null;

		this.connectionProxy = connectionProxy;
		this.target = target;
		this.retryStrategy = retryStrategy;

		connectionLatch = new BooleanReentrantLatch();
		consumerProxies = new ConcurrentHashMap<Consumer, HaConsumerProxy>();
	}

	public void closeConnectionLatch() {
		connectionLatch.close();
	}

	// 动态代理,当程序真正调用Channel 内部的方法时候会由此类的此方法进行代理调用
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

		if (LOG.isDebugEnabled()) {
			LOG.debug("Invoke: " + method.getName());
		}

		// 特殊处理,当Channel调用关闭通道的时候 Channel需要关闭
		if (method.getName().equals(CLOSE_METHOD_NAME)) {
			try {
				target.close();
			} catch (Exception e) {

				if (LOG.isDebugEnabled()) {
					LOG.debug("Failed to close underlying channel, not a problem: " + e.getMessage());
				}
			}

			connectionProxy.removeClosedChannel(this);

			return null;
		}

		Exception lastException = null;
		boolean shutdownRecoverable = true;
		boolean keepOnInvoking = true;

		for (int numOperationInvocations = 1; keepOnInvoking && shutdownRecoverable; numOperationInvocations++) {

			synchronized (target) {

				try {

					if (method.getName().equals(BASIC_CONSUME_METHOD_NAME)) {

						Consumer targetConsumer = (Consumer) args[args.length - 1];

						if (!(targetConsumer instanceof HaConsumerProxy)) {

							HaConsumerProxy consumerProxy = consumerProxies.get(targetConsumer);
							if (consumerProxy == null) {
								consumerProxy = new HaConsumerProxy(targetConsumer, this, method, args);
							}

							HaConsumerProxy existingConsumerProxy = consumerProxies.putIfAbsent(targetConsumer, consumerProxy);

							args[args.length - 1] = existingConsumerProxy == null ? consumerProxy : existingConsumerProxy;
						}
					}

					return InvocationHandlerUtils.delegateMethodInvocation(method, args, target);

				} catch (IOException ioe) {
					lastException = ioe;
					shutdownRecoverable = HaUtils.isShutdownRecoverable(ioe);

				} catch (AlreadyClosedException ace) {
					lastException = ace;
					shutdownRecoverable = HaUtils.isShutdownRecoverable(ace);
				} catch (Throwable t) {

					if (LOG.isDebugEnabled()) {
						LOG.debug("Catch all", t);
					}

					throw t;
				}
			}

			if (shutdownRecoverable) {

				if (LOG.isDebugEnabled()) {
					LOG.debug("Invocation failed, calling retry strategy: " + lastException.getMessage());
				}

				keepOnInvoking = retryStrategy.shouldRetry(lastException, numOperationInvocations, connectionLatch);
			}
		}

		if (shutdownRecoverable) {
			LOG.warn("Operation invocation failed after retry strategy gave up", lastException);
		} else {
			LOG.warn("Operation invocation failed with unrecoverable shutdown signal", lastException);
		}

		throw lastException;
	}

	protected Channel getTargetChannel() {
		return target;
	}

	protected void markAsClosed() {
		connectionLatch.close();
	}

	protected void markAsOpen() {
		connectionLatch.open();
	}

	protected void setTargetChannel(final Channel target) {

		assert target != null;

		if (LOG.isDebugEnabled() && this.target != null) {
			LOG.debug("Replacing channel: channel=" + this.target.toString());
		}

		synchronized (this.target) {

			this.target = target;

			if (LOG.isDebugEnabled() && this.target != null) {
				LOG.debug("Replaced channel: channel=" + this.target.toString());
			}
		}
	}
}
