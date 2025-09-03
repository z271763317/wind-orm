package org.wind.orm.service;

/**
 * @描述 : 数据库连接池接口
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年5月21日 15:14:01
 */
public interface ConnectionPool {

	/**获取 : Connection管理对象**/
	public ConnectionManager get();
	/**释放 : Connection管理对象**/
	public void release(ConnectionManager conManager);
	/**获取 : 数据源名称**/
	public String getDataSource();
	/**获取 : 数据源方式（主、读、写）**/
	public int getDataSourceWay();
	/**获取 : 数据库驱动URL**/
	public String getDbURL();
	/**获取 : 数据库用户名**/
	public String getDbUser();
	/**获取 : 数据库密码**/
	public String getDbPass();
	/**获取 : 数据库连接超时数**/
	public int getConnectionTimeout();
	/**获取 : 闲置超时数**/
	public int getFreeTime();
	
}