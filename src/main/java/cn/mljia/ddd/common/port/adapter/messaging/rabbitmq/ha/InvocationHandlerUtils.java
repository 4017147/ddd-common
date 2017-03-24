package cn.mljia.ddd.common.port.adapter.messaging.rabbitmq.ha;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 
 * @ClassName: InvocationHandlerUtils
 * @Description: TODO 反射方法调用工具类
 * @author: mljia.cn-Marker
 * @date: 2017年1月10日 下午16:47:37
 */
public final class InvocationHandlerUtils {

	private InvocationHandlerUtils() {

	}

	/**
	 * 
	 * @Title: delegateMethodInvocation
	 * @Description: TODO
	 * @param method
	 *            目标方法
	 * @param args
	 *            目标方法参数
	 * @param target
	 *            目标对象
	 * @return
	 * @throws Throwable
	 * @return: Object
	 */
	public static Object delegateMethodInvocation(final Method method, final Object[] args, final Object target) throws Throwable {

		try {
			return method.invoke(target, args);
		} catch (InvocationTargetException ite) {
			throw ite.getTargetException();
		}
	}
}
