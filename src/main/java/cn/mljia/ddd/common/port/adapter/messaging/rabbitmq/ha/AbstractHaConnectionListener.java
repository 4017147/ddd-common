package cn.mljia.ddd.common.port.adapter.messaging.rabbitmq.ha;

import com.rabbitmq.client.ShutdownSignalException;

/**
 * 
 * @ClassName: AbstractHaConnectionListener
 * @Description: TODO 抽象的实现{ @link HaConnectionListener }与空的方法实现。
 * @author: mljia.cn-Marker
 * @date: 2017年1月11日 下午15:43:31
 */
public abstract class AbstractHaConnectionListener implements HaConnectionListener {

	/**
	 * 连接失败
	 */
	public void onConnectFailure(final HaConnectionProxy connectionProxy, final Exception exception) {
	}

	/**
	 * 正常连接
	 */
	public void onConnection(final HaConnectionProxy connectionProxy) {
	}

	/**
	 * 连接断开
	 */
	public void onDisconnect(final HaConnectionProxy connectionProxy, final ShutdownSignalException shutdownSignalException) {
	}

	/**
	 * 重连失败
	 */
	public void onReconnectFailure(final HaConnectionProxy connectionProxy, final Exception exception) {
	}

	/**
	 * 重连
	 */
	public void onReconnection(final HaConnectionProxy connectionProxy) {
	}
}
