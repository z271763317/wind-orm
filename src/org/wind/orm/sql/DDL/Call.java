package org.wind.orm.sql.DDL;

import java.util.List;

import org.wind.orm.sql.SQL;

/**
 * @描述 : DDL【call】接口类
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2016年3月1日 16:25:56
 */
public interface Call extends SQL{

	/**调用 : 存储过程并返回1个值（proc=过程名；paramArr=参数数组；returnType=返回值类型；**/
	public <T> T call(String proc,Object[] paramArr,Integer returnType);
	/**调用 : 存储过程并返回所有值（proc=过程名；paramArr=参数数组；returnTypeArr=返回值类型数组）**/
	public <T> List<T> callList(String proc,Object[] paramArr,Integer[] returnTypeArr);
	
}
