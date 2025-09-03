package org.wind.orm.sql.DML;

import java.util.List;

import org.wind.orm.sql.SQL;


/**
 * @描述 : SQL【删除】接口类
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年5月29日 09:30:33
 */
public interface Delete extends SQL{

	/**删除：根据当前ORM对象的Id列表**/
	public int delete(List<? extends Object> ids);
	/**删除：根据当前ORM对象的变量是否有值**/
	public int delete();
	/**删除全部：删除当前ORM类所有的数据**/
	public int deleteAll();
	/**删除 : 根据传来的条件语句，conditionsSQL=条件SQL；conditionsList=条件值**/
	public int delete(String conditionsSQL,List<? extends Object> conditionsList);
	
}
