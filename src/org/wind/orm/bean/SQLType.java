package org.wind.orm.bean;

import org.wind.orm.sql.DML.Save;

/**
 * @描述 : SQL类型
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2023年11月8日 16:22:53
 */
public class SQLType {

	/**默认**/
	public static final int DEFAULT=0;
	/**查询**/
	public static final int SELECT=1;
	/**插入**/
	public static final int INSERT=2;
	/**更新**/
	public static final int UPDATE=3;
	/**删除**/
	public static final int DELETE=4;
	/**批量**/
	public static final int BATCH=5;
	/**存储过程**/
	public static final int DDL=6;
	/**保存（主要针对自定义方法{@link Save#save(String, java.util.List)}）**/
	public static final int SAVE=7;
	
}