package cn.mljia.ddd.common.port.adapter.messaging.rabbitmq.ha;

import com.rabbitmq.client.ShutdownSignalException;

/**
 * 
 * @ClassName: HaConnectionListener
 * @Description: TODO 自定义连接监听器
 * @author: mljia.cn-Marker
 * @date: 2017年1月12日 下午6:43:26
 */
public interface HaConnectionListener {

	void onConnectFailure(final HaConnectionProxy connectionProxy, final Exception exception);

	void onConnection(final HaConnectionProxy connectionProxy);

	void onDisconnect(final HaConnectionProxy connectionProxy, final ShutdownSignalException shutdownSignalException);

	void onReconnectFailure(final HaConnectionProxy connectionProxy, final Exception exception);

	void onReconnection(final HaConnectionProxy connectionProxy);
}
