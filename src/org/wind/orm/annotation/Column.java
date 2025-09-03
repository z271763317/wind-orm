package org.wind.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @描述 : 列注解
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年8月22日 13:18:44
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column{
	
	/***************状态值***************/
	public static final int isNull_yes=1;			//可以
	public static final int isNull_no=0;			//不可以
	public static final int isNull_unknown=-1;			//未知
	
	/***************默认***************/
	/*通用*/
	public static final String default_general="";		//字符串
	public static final int default_general_int=-1;		//整型
	//
	public static final int default_isNull=isNull_unknown;
	public static final int default_type=-99999999;		//未知
	public static final int default_size=default_general_int;	
	public static final int default_decimalDigits=default_general_int;
	
	/***************注解参数***************/
	String name() default default_general;		//名称
	String value() default default_general;		//列名
	int isNull() default default_isNull;		//是否可为空（1=可以；0= 不可以）
	/**
	 * 取自{@link java.sql.Types}，各数据库的列类型对应该类下的标准SQL类型值；<br />
	 * 对应的Java类型，需要查看各数据库提供的对应关系
	 */
	int type() default default_type;		//列类型
	String typeName() default default_general;		//列类型名称（数据源依赖的类型名称，对于 UDT，该类型名称是完全限定的）
	/**
	 * 表示给定列的指定列大小。对于：
	 * <table border="1" bordercolor="#969696">
	 * 		<tr><th style="padding:2px 5px">数据类型</th><th>描述</th></tr>
	 * 		<tr><th>数值</th><td>这是最大精度。对于字符数据，这是字符长度</td> </tr>
	 * 		<tr><th>日期时间</th><td>这是 String 表示形式的字符长度（假定允许的最大小数秒组件的精度）</td></tr>
	 * 		<tr><th>二进制</th><td>这是字节长度</td></tr>
	 * 		<tr><th>ROWID</th><td>这是字节长度。对于列大小不适用的数据类型，则返回 Null</td></tr>
	 * </table>
	 */
	int size() default default_size;		//大小（长度）
	int decimalDigits() default default_decimalDigits;		//小数位数
	String remark() default default_general;		//注释 
	String def() default default_general;		//默认值
	
}