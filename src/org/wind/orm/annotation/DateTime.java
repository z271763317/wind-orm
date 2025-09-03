package org.wind.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @描述 : 日期时间注解———如果是日期时间相关的注解，请务必加上，并且接受者必须是String类型，否则将会有不可预支的错误。可对从数据库取出的数据格式化，格式为常见的如：yyyy-MM-dd HH:mm:ss、yyyy-MM-dd、HH:mm:ss等
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2021年6月21日 02:45:27
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DateTime {
	
	String value() default "";		//格式（不设置则为不格式化）
	
}