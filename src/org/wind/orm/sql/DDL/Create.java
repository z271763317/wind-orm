package org.wind.orm.sql.DDL;

import org.wind.orm.sql.SQL;

/**
 * @描述 : DDL【create】接口类
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2019年1月23日 17:52:29
 */
public interface Create extends SQL{

	/**create**/
	public boolean create(String sql);
	/**复制 : 当前表到新表（isCopyData=是否复制数据）**/
	public boolean copy(String newTable,boolean isCopyData);
	
}
