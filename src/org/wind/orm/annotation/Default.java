package org.wind.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @描述 : 默认值注解
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2023年10月27日 16:31:47
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Default{
	
	String name() default "";		//名称
	String value() ;		//值
	
}