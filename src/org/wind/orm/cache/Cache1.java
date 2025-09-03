package org.wind.orm.cache;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.wind.orm.Table;
import org.wind.orm.bean.ConnectionConfig;
import org.wind.orm.bean.SQLVar;
import org.wind.orm.bean.TableSpecify;
import org.wind.orm.exception.TableException;
import org.wind.orm.service.ConnectionManager;

/**
 * @描述 : 一级Cache缓存
 * @详情 : 当前线程上文所有的数据，生命周期：“线程（会话）创建”—>“线程（会话）销毁”<br />
 * 			 （1）、get()：取出当前线程需要取出的数据；<br />
 *          （2）、remove()：手动删除当前线程需要删除的数据；<br />
 *          
 *          //本地方法（包含了当前线程的全局所有数据）<br />
 *          （1）、事务相关<br />
 * 			（2）、Connection管理器<br />
 * 			（3）、其他临时配置（如：超时数）<br />
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年8月23日 19:12:18
 */
public class Cache1{

	//当前线程（会话）全局数据
	private static final ThreadLocal<Cache1> threadLocal_current=new ThreadLocal<Cache1>();
	//
	protected Map<String,ConnectionConfig> conConfigMap=new HashMap<>();		//每个数据源对应的配置
	private SQLVar sqlVar;			/*保存最后一次的数据，下次执行重新生成*/
	private TableSpecify tableSpecify;		//临时指定的配置
	
	/**获取**/
	public static Cache1 get(){
		Cache1 current=threadLocal_current.get();
		if(current==null){
			current=new Cache1();
			threadLocal_current.set(current);
		}
		return current;
	}
	/**清除（所有数据）**/
	public static void clear(){
		threadLocal_current.remove();
	}
	
	/************************************本地方法************************************/
	//获取 : 指定poolName的连接配置
	private ConnectionConfig get(String poolName) {
		ConnectionConfig conConfig=conConfigMap.get(poolName);
		if(conConfig==null) {
			conConfig=new ConnectionConfig();
			conConfigMap.put(poolName, conConfig);
		}
		return conConfig;
	}
	/**获取：SQLVar对象（最后一次生成的）**/
	public SQLVar getSqlVar() {
		return this.sqlVar;
	}
	/**设置：SQLVar对象**/
	public void setSqlVar(SQLVar sqlVar) {
		this.sqlVar=sqlVar;
	}
	/**获取：TableSpecify对象（最后一次生成的）**/
	public TableSpecify getTableSpecify() {
		return this.tableSpecify;
	}
	/**设置：TableSpecify对象**/
	public void setTableSpecify(TableSpecify tableSpecify) {
		this.tableSpecify=tableSpecify;
	}
	/************ORM类式************/
	/**设置：是否自动提交事务**/
	public void setAutoCommit(Class<? extends Table> tableClass,boolean isAutoCommit) {
		this.setAutoCommit(Cache2.getDataSource(tableClass),isAutoCommit);
	}
	/**是否：自动提交（默认自动）**/
	public boolean isAutoCommit(Class<? extends Table> tableClass){
		return this.isAutoCommit(Cache2.getDataSource(tableClass));
	}
	/**获取：指定数据源的Connection管理器列表（非只读）**/
	public ConnectionManager getConnectionManager_noRead(Class<? extends Table> tableClass) {
		return this.getConnectionManager_noRead(Cache2.getDataSource(tableClass));
	}
	/**获取：指定数据源的Connection管理器列表（只读）**/
	public ConnectionManager getConnectionManager_read(Class<? extends Table> tableClass) {
		return this.getConnectionManager_read(Cache2.getDataSource(tableClass));
	}
	/**设置：指定数据源的Connection管理器**/
	public void setConnectionManager(Class<? extends Table> tableClass,int dataSourceWay,ConnectionManager connectionManager) {
		this.setConnectionManager(Cache2.getDataSource(tableClass), dataSourceWay,connectionManager);
	}
	/**删除：指定数据源的Connection管理器**/
	public void removeConnectionManager(Class<? extends Table> tableClass){
		this.removeConnectionManager(Cache2.getDataSource(tableClass));
	}
	/**设置 : 执行单个SQL超时数**/
	public void setTimeout(Class<? extends Table> tableClass,int timeout){
		this.setTimeout(Cache2.getDataSource(tableClass), timeout);
	}
	/**获取 : 执行单个SQL超时数**/
	public Integer getTimeout(Class<? extends Table> tableClass){
		return this.getTimeout(Cache2.getDataSource(tableClass));
	}
	
	/************数据源名称式************/
	/**设置：是否自动提交事务（数据源名称式）**/
	public void setAutoCommit(String poolName,boolean isAutoCommit){
		ConnectionManager conManager=this.getConnectionManager_noRead(poolName);
		if(conManager!=null){
			try{
				conManager.getConnection().setAutoCommit(isAutoCommit);
			}catch(SQLException e){
				throw new TableException(e.getMessage(),e);
			}
		}
		this.get(poolName).setAutoCommit(isAutoCommit);
	}
	/**是否：自动提交（默认自动——数据源名称式）**/
	public boolean isAutoCommit(String poolName){
		Boolean isAutoCommit=this.get(poolName).getAutoCommit();
		return isAutoCommit!=null?isAutoCommit:true;
	}
	/**获取：指定数据源的Connection管理器（数据源名称式）**/
	public ConnectionManager getConnectionManager(String poolName,int dataSourceWay) {
		if(dataSourceWay==Cache2.dataSourceWay_read) {
			return getConnectionManager_read(poolName);
		}else{
			return getConnectionManager_noRead(poolName);
		}
	}
	/**获取：指定数据源的Connection管理器（数据源名称式）**/
	public ConnectionManager getConnectionManager_noRead(String poolName) {
		return this.get(poolName).getNoRead();
	}
	/**获取：指定数据源的Connection管理器（数据源名称式）**/
	public ConnectionManager getConnectionManager_read(String poolName) {
		return this.get(poolName).getRead();
	}
	/**设置：指定数据源的Connection管理器（数据源名称式）**/
	public void setConnectionManager(String poolName,int dataSourceWay,ConnectionManager connectionManager) {
		if(dataSourceWay==Cache2.dataSourceWay_read) {
			this.get(poolName).setRead(connectionManager);
		}else{
			this.get(poolName).setNoRead(connectionManager);
		}
	}
	/**删除：指定数据源的Connection管理器（数据源名称式）**/
	public void removeConnectionManager(String poolName){
		this.get(poolName).setRead(null);
		this.get(poolName).setNoRead(null);
	}
	/**设置 : 执行单个SQL超时数**/
	public void setTimeout(String poolName,int timeout){
		this.get(poolName).setTimeout(timeout);
	}
	/**获取 : 执行单个SQL超时数**/
	public Integer getTimeout(String poolName){
		return this.get(poolName).getTimeout();
	}
	
}