package org.wind.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @描述 : 排序注解
 * @详解 : 表明当前字段做为默认的排序字段（分页时可用到）
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2017年12月2日 13:47:22
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OrderBy {
	
	String name() default "";		//描述
	
}