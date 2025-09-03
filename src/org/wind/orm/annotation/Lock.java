package org.wind.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @描述 : 数据表锁SQL语句描述注解
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年11月23日 11:08:43
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Lock {
	
	String name() default "";		//名称
	String value() default "";		//值
	
}