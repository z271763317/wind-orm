package org.wind.orm.sql.DDL.other;

import org.wind.orm.exception.TableException;

/**
 * @描述 : DDL【other】接口实现类——SQLServer
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2021年5月12日 17:49:46
 */
public class OtherImpl_SQLServer extends OtherImpl {

	/**获取 : 当前表创建SQL语句**/
	public String getTableSQL(){
		throw new TableException("SQL Server不支持");
	}
	
}