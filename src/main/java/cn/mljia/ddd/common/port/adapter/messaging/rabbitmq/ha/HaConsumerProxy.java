package cn.mljia.ddd.common.port.adapter.messaging.rabbitmq.ha;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.client.AMQP.BasicProperties;

/**
 * 
 * @ClassName: HaConsumerProxy
 * @Description: TODO
 * @author: mljia.cn-Marker
 * @date: 2017年1月12日 下午6:40:47
 */
public class HaConsumerProxy implements Consumer {

	private static final Logger LOG = Logger.getLogger(HaConsumerProxy.class);

	private final Consumer target;

	private final HaChannelProxy channelProxy;

	private final Method basicConsumeMethod;

	private final Object[] basicConsumeArgs;

	private final ExecutorService executor;

	public HaConsumerProxy(final Consumer target, final HaChannelProxy channelProxy, final Method basicConsumeMethod, final Object[] basicConsumeArgs) {

		assert target != null;
		assert channelProxy != null;
		assert basicConsumeMethod != null;
		assert basicConsumeArgs != null;

		this.target = target;
		this.channelProxy = channelProxy;
		this.basicConsumeMethod = basicConsumeMethod;
		this.basicConsumeArgs = basicConsumeArgs;

		executor = Executors.newCachedThreadPool();
	}

	public void handleCancel(final String consumerTag) throws IOException {
		target.handleCancel(consumerTag);
	}

	public void handleCancelOk(final String consumerTag) {
		target.handleCancelOk(consumerTag);
	}

	public void handleConsumeOk(final String consumerTag) {
		target.handleConsumeOk(consumerTag);
	}

	public void handleDelivery(final String consumerTag, final Envelope envelope, final BasicProperties properties, final byte[] body) throws IOException {
		target.handleDelivery(consumerTag, envelope, properties, body);
	}

	public void handleRecoverOk(final String consumerTag) {
		target.handleRecoverOk(consumerTag);
	}

	public void handleShutdownSignal(final String consumerTag, final ShutdownSignalException sig) {

		if (LOG.isDebugEnabled()) {
			LOG.debug("Consumer asked to handle shutdown signal, reregistering consume. " + sig.getMessage());
		}

		channelProxy.closeConnectionLatch();

		executor.submit(new ConsumeRunner());
	}

	private class ConsumeRunner implements Callable<Object> {

		public Object call() throws Exception {

			try {
				return channelProxy.invoke(channelProxy, basicConsumeMethod, basicConsumeArgs);

			} catch (Throwable e) {

				// bad news?
				if (LOG.isDebugEnabled()) {
					LOG.debug("Error reinvoking basicConsume", e);
				}

				return e;
			}
		}
	}
}
