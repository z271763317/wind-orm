package org.wind.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @描述 : 数据源注解
 * @详情 : 该注解支持子类继承父类的（extends）使用方式。
 * 				如果子类有该注解，则优先使用自己的，
 * 				否则按照OO思想的父子层级思想，往上找有没有该注解的父类，一直往父类的深层次父类去找，直到先找到有该注解的父类才停止。
 * 				如果父类的深层次也没有，那么一直到父类为Object类（java顶级类）时则停止寻找。
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年8月21日 15:30:00
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DataSource {
	
	String name() default "";		//名称
	String value() default "";		//值
	
}