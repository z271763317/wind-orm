package org.wind.orm.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

/**
 * @描述 : 数据库Connection连接管理器接口
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年5月21日 13:54:59
 */
public interface ConnectionManager {

	/**获取：Connection对象**/
	public Connection getConnection();
	/**提交事务**/
	public void commit() throws SQLException ;
	/**回滚事务**/
	public void rollback();
	/**回滚事务（指定Savepoint保存点）**/
	public void rollback(Savepoint t_savepoint);
	/**关闭操作数据库相关的类**/
	public void close();
	/**设置 : 最后一次操作的时间**/
	public void setLastOperationTime(long lastOperationTime);
	/**获取 : 最后一次操作的时间**/
	public long getLastOperationTime();
	/**获取 : 所属的连接池**/
	public ConnectionPool getConnectionPool();
	/**处理 : Connection为可用的 **/
	public void available() throws SQLException;
	
}