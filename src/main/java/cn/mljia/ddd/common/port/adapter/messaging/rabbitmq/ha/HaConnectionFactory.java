package cn.mljia.ddd.common.port.adapter.messaging.rabbitmq.ha;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.ConnectException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import cn.mljia.ddd.common.port.adapter.messaging.rabbitmq.ha.retry.BlockingRetryStrategy;
import cn.mljia.ddd.common.port.adapter.messaging.rabbitmq.ha.retry.RetryStrategy;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * 
 * @ClassName: HaConnectionFactory
 * @Description: TODO 自定义连接工厂
 * @author: mljia.cn-Marker
 * @date: 2017年1月10日 下午10:59:02
 */
public class HaConnectionFactory extends ConnectionFactory {

	private static final Logger LOG = Logger.getLogger(HaConnectionFactory.class);

	private static final long DEFAULT_RECONNECTION_WAIT_MILLIS = 1000;// 默认一秒

	private long reconnectionWaitMillis = DEFAULT_RECONNECTION_WAIT_MILLIS;

	private final ExecutorService executorService;

	private RetryStrategy retryStrategy;

	private Set<HaConnectionListener> listeners;

	public HaConnectionFactory() {
		super();

		executorService = Executors.newCachedThreadPool();
		setDefaultRetryStrategy();

		listeners = new HashSet<HaConnectionListener>();
	}

	public void addHaConnectionListener(final HaConnectionListener listener) {
		listeners.add(listener);
	}

	// 包裹一个Connection代理
	@Override
	public Connection newConnection(final Address[] addrs) throws IOException {

		Connection target = null;
		try {
			target = super.newConnection(addrs);

		} catch (IOException ioe) {
			LOG.warn("Initial connection failed, wrapping anyways and letting reconnector go to work: " + ioe.getMessage());
		} catch (TimeoutException te) {
			LOG.warn("Initial connection failed, wrapping anyways and letting reconnector go to work: " + te.getMessage());
		}

		ConnectionSet connectionPair = createConnectionProxy(addrs, target);

		if (target != null) {// 说明连接创建成功
			return connectionPair.wrapped;

		}

		// 否则创建失败,需要异步进行重连
		ReconnectionTask task = new ReconnectionTask(false, connectionPair.listener, connectionPair.proxy);
		task.run();

		return connectionPair.wrapped;
	}

	private class ConnectionSet {

		private final Connection wrapped;

		private final HaConnectionProxy proxy;

		private final HaShutdownListener listener;

		private ConnectionSet(final Connection wrapped, final HaConnectionProxy proxy, final HaShutdownListener listener) {

			this.wrapped = wrapped;
			this.proxy = proxy;
			this.listener = listener;
		}
	}

	private class HaShutdownListener implements ShutdownListener {

		private final HaConnectionProxy connectionProxy;

		// 需要一个当前连接的连接信息,如集群地址,关闭当前连接所有的Channel...
		public HaShutdownListener(final HaConnectionProxy connectionProxy) {

			assert connectionProxy != null;
			this.connectionProxy = connectionProxy;
		}

		@Override
		public void shutdownCompleted(final ShutdownSignalException shutdownSignalException) {

			if (LOG.isDebugEnabled()) {
				LOG.debug("Shutdown signal caught: " + shutdownSignalException.getMessage());
			}

			for (HaConnectionListener listener : listeners) {
				listener.onDisconnect(connectionProxy, shutdownSignalException);
			}

			// 如果不是应用引起的宕机需要进行重新连接
			if (!shutdownSignalException.isInitiatedByApplication()) {

				// 使用线程去重连-----核心
				executorService.submit(new ReconnectionTask(true, this, connectionProxy));

			} else {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Ignoring shutdown signal, application initiated");
				}
			}
		}
	}

	private class ReconnectionTask implements Runnable {

		private final boolean reconnection;// 是否需要重连

		private final ShutdownListener shutdownListener;// 宕机监听器

		private final HaConnectionProxy connectionProxy;// 连接代理

		public ReconnectionTask(final boolean reconnection, final ShutdownListener shutdownListener, final HaConnectionProxy connectionProxy) {

			Validate.notNull(shutdownListener, "shutdownListener is required");
			Validate.notNull(connectionProxy, "connectionProxy is required");

			this.reconnection = reconnection;
			this.shutdownListener = shutdownListener;
			this.connectionProxy = connectionProxy;
		}

		public void run() {

			// 连接断了需要关闭当前连接所有的channels
			connectionProxy.closeConnectionLatch();

			// 获取当前连接的RabbitMQ服务器集群列表
			String addressesAsString = getAddressesAsString();

			if (LOG.isDebugEnabled()) {
				LOG.info("Reconnection starting, sleeping: addresses=" + addressesAsString + ", wait=" + reconnectionWaitMillis);
			}

			boolean connected = false;
			while (!connected) {

				try {
					Thread.sleep(reconnectionWaitMillis);
				} catch (InterruptedException ie) {

					if (LOG.isDebugEnabled()) {
						LOG.debug("Reconnection timer thread was interrupted, ignoring and reconnecting now");
					}
				}

				Exception exception = null;
				try {
					Connection connection = newTargetConnection(connectionProxy.getAddresses());// 创建一个新连接---如果一直连接不上会报错,然后进行循环重试

					if (LOG.isDebugEnabled()) {
						LOG.info("Reconnection complete: addresses=" + addressesAsString);
					}

					// 新创建的连接需要添加监听,防止二次连接断掉
					connection.addShutdownListener(shutdownListener);

					// connectionProxy设置新的可用连接
					connectionProxy.setTargetConnection(connection);

					// 设置连接关联的Channel进行与断之前的通道号码重新进行绑定,可以对接之前使用的Channel再次使用
					connectionProxy.replaceChannelsInProxies();

					connected = true;// 说明连接是可用了,退出循环连接

					if (reconnection) {// 通知重连监听
						for (HaConnectionListener listener : listeners) {
							listener.onReconnection(connectionProxy);
						}

					} else {
						for (HaConnectionListener listener : listeners) {
							listener.onConnection(connectionProxy);
						}
					}

					connectionProxy.markAsOpen();// 打开通道

				} catch (ConnectException ce) {
					// 连接异常
					exception = ce;

				} catch (IOException ioe) {
					// 其他一些连接异常
					exception = ioe;
				} catch (TimeoutException te) {
					// 创建连接超时异常
					exception = te;
				}

				if (exception != null) {

					LOG.warn("Failed to reconnect, retrying: addresses=" + addressesAsString + ", message=" + exception.getMessage());

					if (reconnection) {
						for (HaConnectionListener listener : listeners) {
							listener.onReconnectFailure(connectionProxy, exception);
						}

					} else {
						for (HaConnectionListener listener : listeners) {
							listener.onConnectFailure(connectionProxy, exception);
						}
					}
				}
			}
		}

		private String getAddressesAsString() {

			StringBuilder sb = new StringBuilder();
			sb.append('[');

			for (int i = 0; i < connectionProxy.getAddresses().length; i++) {

				if (i > 0) {
					sb.append(',');
				}

				sb.append(connectionProxy.getAddresses()[i].toString());
			}

			sb.append(']');
			return sb.toString();
		}
	}

	public void setHaConnectionListener(final Set<HaConnectionListener> listeners) {

		Validate.notEmpty(listeners, "listeners are required and none can be null");
		this.listeners = new ConcurrentSkipListSet<HaConnectionListener>(listeners);
	}

	public void setReconnectionWaitMillis(final long reconnectionIntervalMillis) {

		Validate.isTrue(reconnectionIntervalMillis > 0, "reconnectionIntervalMillis must be greater than 0");
		reconnectionWaitMillis = reconnectionIntervalMillis;
	}

	public void setRetryStrategy(final RetryStrategy retryStrategy) {
		this.retryStrategy = retryStrategy;
	}

	protected ConnectionSet createConnectionProxy(final Address[] addrs, final Connection targetConnection) {

		ClassLoader classLoader = Connection.class.getClassLoader();
		Class<?>[] interfaces = { Connection.class };

		HaConnectionProxy proxy = new HaConnectionProxy(addrs, targetConnection, retryStrategy);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Creating connection proxy: " + (targetConnection == null ? "none" : targetConnection.toString()));
		}

		Connection target = (Connection) Proxy.newProxyInstance(classLoader, interfaces, proxy);
		HaShutdownListener listener = new HaShutdownListener(proxy);

		// 创建的代理连接需要进行宕机监听
		if (targetConnection != null) {
			target.addShutdownListener(listener);
		}

		return new ConnectionSet(target, proxy, listener);
	}

	private Connection newTargetConnection(final Address[] addrs) throws IOException, TimeoutException {
		return super.newConnection(addrs);
	}

	private void setDefaultRetryStrategy() {
		retryStrategy = new BlockingRetryStrategy();
	}
}
