package org.wind.orm.sql.DDL;

import java.util.Set;

import org.wind.orm.sql.SQL;

/**
 * @描述 : DDL【其他】接口类
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2019年1月24日 09:45:46
 */
public interface Other extends SQL{

	/**获取 : 当前表创建SQL语句**/
	public String getTableSQL();
	/**判断 : 指定表是否存在**/
	public boolean isTableExist(String table);
	/**获取 : 列名**/
	public Set<String> getColumn();
	
}
