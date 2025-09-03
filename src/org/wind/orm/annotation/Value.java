package org.wind.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @描述 : 值注解
 * @说明 : 主要针对于判断条件、插入、更新、删除的值SQL的替换，如：Value内容为：TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS') <br />
 * 				条件 : where create_time>TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS') <br />
 * 				插入 : insert into student(create_time) values(TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS'))
 * @注意 : 若字段加了@Id注解，则该注解失效
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2019年5月1日 22:32:59
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Value {
	
	String value() default "";		//值
	
}