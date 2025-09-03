package org.wind.orm;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Savepoint;
import java.util.Set;

import org.wind.orm.bean.TableSpecify;
import org.wind.orm.cache.Cache1;
import org.wind.orm.service.ConnectionManager;

/**
 * @描述 : Table父2级类——配置
 * @版权 : 湖南省郴州市安仁县胡璐璐 
 * @时间 : 2015年8月26日 17:14:28
 * @功能 : 配置
 */
public abstract class TableParent2{

	private TableSpecify _s=null;	//临时配置
	
	/***************本地方法（临时手动指定的）***************/
	private TableSpecify _s() {
		_s=_s!=null?_s:new TableSpecify();
		return _s;
	}
	public String getTable() {
		return _s().getTable();
	}
	public void setTable(String table) {
		_s().setTable(table);
	}
	public String getOrderFieldName() {
		return _s().getOrderFieldName();
	}
	public String getGroupFieldName() {
		return _s().getGroupFieldName();
	}
	public Set<String> getSpecifiedColumnSet() {
		return _s().getSpecifiedColumnSet();
	}
	/**orderFieldName=ORM类的字段（成员属性）名称**/
	public void setOrderFieldName(String orderFieldName) {
		_s().setOrderFieldName(orderFieldName);
	}
	/**groupFieldName=ORM类的字段（成员属性）名称**/
	public void setGroupFieldName(String groupFieldName) {
		_s().setGroupFieldName(groupFieldName);
	}
	public String getOrderSQL() {
		return _s().getOrderSQL();
	}
	public void setOrderSQL(String orderSQL) {
		_s().setOrderSQL(orderSQL);
	}
	public String getGroupSQL() {
		return _s().getGroupSQL();
	}
	public void setGroupSQL(String groupSQL) {
		_s().setGroupSQL(groupSQL);
	}
	/**specifiedFieldNameArr=ORM类的字段（成员属性）名称——可变式数组**/
	public void addSpecifiedFieldName(String... specifiedFieldNameArr){
		_s().addSpecifiedFieldName(specifiedFieldNameArr);
	}
	/**清除所有指定显示的列名映射的字段（成员属性）名称**/
	public void clearSpecifiedFieldName(){
		_s().clearSpecifiedFieldName();
	}
	
	/***************静态方法***************/
	/**设置 : 自动提交（会话）**/
	public static void setAutoCommit(Class<? extends Table> tableClass,boolean isAutoCommit) {
		Cache1.get().setAutoCommit(tableClass,isAutoCommit);
	}
	/**是否 : 自动提交（会话）**/
	public static void isAutoCommit(Class<? extends Table> tableClass) {
		Cache1.get().isAutoCommit(tableClass);
	}
	/**提交事务**/
	public static void commit(Class<? extends Table> tableClass) throws SQLException{
		ConnectionManager conManager=Cache1.get().getConnectionManager_noRead(tableClass);
		if(conManager!=null){
			conManager.commit();
		}
	}
	/**
	 * 设置 : 事务保存点，并返回。若抛出{@link SQLFeatureNotSupportedException}，则表示驱动程序不支持此操作。
	 * @说明 : 指定的tableClass所属事务至少需要操作一次SQL，才可以取到{@link ConnectionManager}，否则抛出SQLException错误
	 * @param name : 设置的指定tableClass事务保存点的名称
	 **/
	public static Savepoint setSavepoint(Class<? extends Table> tableClass,String name) throws SQLException{		
		ConnectionManager conManager=Cache1.get().getConnectionManager_noRead(tableClass);
		if(conManager!=null){
			Connection t_con=conManager.getConnection();
			if(name!=null){
				return t_con.setSavepoint(name);
			}else{
				return t_con.setSavepoint();
			}
		}else{
			throw new SQLException("没有取到事务的Connection");
		}
	}
	/**移除指定的 Savepoint 和后续 Savepoint 对象。在已移除保存点之后，对该保存点的任何引用都会导致抛出 SQLException。<br />若抛出{@link SQLFeatureNotSupportedException}，则表示驱动程序不支持此操作。 **/
	public static void releaseSavepoint(Class<? extends Table> tableClass,Savepoint savepoint) throws SQLException{
		ConnectionManager conManager=Cache1.get().getConnectionManager_noRead(tableClass);
		if(conManager!=null){
			conManager.getConnection().releaseSavepoint(savepoint);
		}
	}
	/**回滚事务**/
	public static void rollback(Class<? extends Table> tableClass) {
		rollback(tableClass, null);
	}
	/**回滚事务（{@link TableParent2#setSavepoint}，t_savepoint=回滚至该事务保存点，为null则回滚所有）**/
	public static void rollback(Class<? extends Table> tableClass,Savepoint t_savepoint) {
		ConnectionManager conManager=Cache1.get().getConnectionManager_noRead(tableClass);
		if(conManager!=null){
			conManager.rollback(t_savepoint);
		}
	}
	/**释放连接**/
	public static void close(Class<? extends Table> tableClass){
		ConnectionManager conManager_noRead=Cache1.get().getConnectionManager_noRead(tableClass);
		if(conManager_noRead!=null){
			conManager_noRead.rollback();
		}
		Table.setAutoCommit(tableClass, true);
		if(conManager_noRead!=null){
			conManager_noRead.close();
		}
		ConnectionManager conManager_read=Cache1.get().getConnectionManager_read(tableClass);
		if(conManager_read!=null){
			conManager_read.close();
		}
		Cache1.get().removeConnectionManager(tableClass);
	}
	/**设置 : 执行单个SQL时的超时数（单位：秒），此为设置【单会话】的超时配置，并非全局**/
	public static void setTimeout(Class<? extends Table> tableClass,int timeout){
		Cache1.get().setTimeout(tableClass, timeout);
	}
	/**获取 : 执行单个SQL时的超时数（单位：秒），此为获取【单会话】的超时配置，并非全局，未设置则返回null**/
	public static Integer getTimeout(Class<? extends Table> tableClass){
		return Cache1.get().getTimeout(tableClass);
	}
	
}