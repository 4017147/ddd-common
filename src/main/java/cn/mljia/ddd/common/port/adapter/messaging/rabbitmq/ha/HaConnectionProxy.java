package cn.mljia.ddd.common.port.adapter.messaging.rabbitmq.ha;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import cn.mljia.ddd.common.port.adapter.messaging.rabbitmq.ha.retry.RetryStrategy;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * 
 * @ClassName: HaConnectionProxy
 * @Description: TODO 连接的动态代理
 * @author: mljia.cn-Marker
 * @date: 2017年1月12日 下午6:23:26
 */
public class HaConnectionProxy implements InvocationHandler {

	private static final Logger LOG = Logger.getLogger(HaConnectionProxy.class);

	private static Method CREATE_CHANNEL_METHOD;

	private static Method CREATE_CHANNEL_INT_METHOD;

	private final Address[] addrs;

	private Connection target;

	private final Set<HaChannelProxy> channelProxies;

	private final RetryStrategy retryStrategy;

	static {
		try {
			CREATE_CHANNEL_METHOD = Connection.class.getMethod("createChannel");
			CREATE_CHANNEL_INT_METHOD = Connection.class.getMethod("createChannel", int.class);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	

	public HaConnectionProxy(final Address[] addrs, final Connection target, final RetryStrategy retryStrategy) {

		assert addrs != null;
		assert addrs.length > 0;
		assert retryStrategy != null;

		this.target = target;
		this.addrs = addrs;
		this.retryStrategy = retryStrategy;

		channelProxies = new HashSet<HaChannelProxy>();
	}

	public void closeConnectionLatch() {
		for (HaChannelProxy proxy : channelProxies) {
			proxy.closeConnectionLatch();
		}
	}

	public Address[] getAddresses() {
		return addrs;
	}

	public Connection getTargetConnection() {
		return target;
	}

	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

		// 拦截当应用程序调用 createChannel 以及 重载方法 需创建 Channel代理类
		if (method.equals(CREATE_CHANNEL_METHOD) || method.equals(CREATE_CHANNEL_INT_METHOD)) {

			return createChannelAndWrapWithProxy(method, args);
		}

		// 其他Channel内部正常方法的调用
		return InvocationHandlerUtils.delegateMethodInvocation(method, args, target);
	}

	public void markAsOpen() {

		synchronized (channelProxies) {

			for (HaChannelProxy proxy : channelProxies) {
				proxy.markAsOpen();
			}
		}
	}

	protected Channel createChannelAndWrapWithProxy(final Method method, final Object[] args) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {

		Channel targetChannel = (Channel) method.invoke(target, args);

		ClassLoader classLoader = Connection.class.getClassLoader();
		Class<?>[] interfaces = { Channel.class };

		// 创建Channel代理
		HaChannelProxy proxy = new HaChannelProxy(this, targetChannel, retryStrategy);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Creating channel proxy: " + targetChannel.toString());
		}

		// 对代理进行缓存---一定使用同步,可能出现一个连接同事创建Channel的可能
		synchronized (channelProxies) {
			channelProxies.add(proxy);
		}

		return (Channel) Proxy.newProxyInstance(classLoader, interfaces, proxy);
	}

	protected void removeClosedChannel(final HaChannelProxy channelProxy) {
		synchronized (channelProxies) {
			channelProxies.remove(channelProxy);
		}
	}

	protected void replaceChannelsInProxies() throws IOException {
		synchronized (channelProxies) {
			for (HaChannelProxy proxy : channelProxies) {
				// 替换死亡的Channel然后与之前的Channel号保持一致
				int channelNumber = proxy.getTargetChannel().getChannelNumber();
				proxy.setTargetChannel(target.createChannel(channelNumber));
			}
		}
	}

	protected void setTargetConnection(final Connection target) {
		assert target != null;
		this.target = target;
	}

}
