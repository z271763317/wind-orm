package org.wind.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @描述 : 不保存注解
 * @详解 : 加上该注解的属性将不会被保存到数据库里
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2016年3月27日 10:53:20
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Null {
	
	String name() default "";		//描述
	
}