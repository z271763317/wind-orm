package org.wind.orm.sql.DDL;

import org.wind.orm.sql.SQL;

/**
 * @描述 : DDL【drop】接口类
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2019年1月24日 10:46:34
 */
public interface Drop extends SQL{

	/**drop**/
	public boolean drop(String sql);
	
}
