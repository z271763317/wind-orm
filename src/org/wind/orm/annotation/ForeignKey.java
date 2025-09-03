package org.wind.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @描述 : 外键注解
 * @说明 : <b style="color:red">优先取@Column注解的value</b><br />
 * 				未找到，将取该字段所属Class的主键的列名<br />
 * 				字段【属性】为不同的对象加该注解则是不同的关联模式：
 *				<table border="1" bordercolor="#969696" style="margin:3px 0px">
 * 					<tr><th style="padding:2px 5px">对象类型</th><th>描述</th></tr>
 * 					<tr><th>Table</th><td>加该注解，则是：【一对一】——取外键的主键</td> </tr>
 * 					<tr><th>List&lt;Table对象&gt;</th><td>加该注解，则是：【一对多】——取外键里关联当前对象的主键</td></tr>
 * 				</table>
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2014年8月28日 15:37:51
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ForeignKey {
	
	
}