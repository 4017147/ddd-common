package cn.mljia.ddd.common.port.adapter.messaging.rabbitmq.ha;

import java.io.EOFException;
import java.io.IOException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.client.impl.AMQImpl;

/**
 * 
 * @ClassName: HaUtils
 * @Description: TODO 工具类
 * @author: mljia.cn-Marker
 * @date: 2017年1月10日 下午15:20:15
 */
public final class HaUtils {

	private HaUtils() {

	}

	/**
	 * 
	 * @Title: isShutdownRecoverable
	 * @Description: TODO 判断是否是应用本身引起的宕机异常,如果不是着可以进行重连
	 * @param s
	 *            宕机异常
	 * @return
	 * @return: boolean
	 */
	public static boolean isShutdownRecoverable(final ShutdownSignalException s) {

		if (s != null) {
			int replyCode = 0;

			if (s.getReason() instanceof AMQImpl.Connection.Close) {
				replyCode = ((AMQImpl.Connection.Close) s.getReason()).getReplyCode();
			}

			if (s.isInitiatedByApplication()) {

				return replyCode == AMQP.CONNECTION_FORCED || replyCode == AMQP.INTERNAL_ERROR || s.getCause() instanceof EOFException || s instanceof AlreadyClosedException;
			}
		}

		return false;
	}

	public static boolean isShutdownRecoverable(final IOException ioe) {

		if (ioe.getCause() instanceof ShutdownSignalException) {
			return isShutdownRecoverable((ShutdownSignalException) ioe.getCause());
		}

		return true;
	}
}
