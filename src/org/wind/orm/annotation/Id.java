package org.wind.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @描述 : 主键注解
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2014年8月28日 15:37:51
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Id {
	
	/***************生成策略**************/
	public static final int autoIncrement=1;		//自增
	public static final int UUID=2;		//UUID
	
	/***************注解参数***************/
	String name() default "";		//名称
	int value() default 1;		//主键生成策略（1【默认】=数据库自增）
	String sequence() default "";		//索引名（自增时，针对不支持自增，但支持索引的数据库）
	
}