package cn.mljia.ddd.common.event.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EventListener {
	/**
	 * 
	 * @Title: isListening
	 * @Description: TODO 是否开启监听 -默认开启
	 * @return
	 * @return: boolean
	 */
	boolean isListening() default true;

}
