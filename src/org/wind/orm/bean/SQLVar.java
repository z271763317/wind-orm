package org.wind.orm.bean;

import java.lang.reflect.Method;

import org.wind.orm.Table;
import org.wind.orm.service.ConnectionManager;
import org.wind.orm.service.ConnectionPoolFactory;

/**
 * @描述 : 执行SQL需要的变量（针对单次操作）
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年8月23日 08:24:08
 */
public class SQLVar {

	private String dataBase;		//当前数据库
	private Table obj;				//当前执行的ORM对象
	private Config config;		//当前绑定的Config
	private Integer dataSourceWay;		//数据源方式（主、读、写）
	private ConnectionManager conManager;		//当前连接管理器
	
	/**非业务**/
	private Object objSqlProxy;			//执行的SQL代理对象
	private Object objSql;		//执行的SQL对象
	private Method method;		//执行的SQL对象方法
	private Object[] methodParam;			//方法参数
	
	public SQLVar(String dataBase,Table obj,Config config,Integer dataSourceWay) {
		this.dataBase=dataBase!=null?dataBase:"";
		this.obj=obj;
		this.config=config;
		this.dataSourceWay=dataSourceWay;
	}
	
	public String getDataBase() {
		return dataBase;
	}
	public Table getObj() {
		return obj;
	}
	public Config getConfig() {
		return config;
	}
	public Integer getDataSourceWay() {
		return dataSourceWay;
	}
	public ConnectionManager getConManager() {
		if(conManager==null) {
			conManager=ConnectionPoolFactory.getConnectionPool(this.getConfig().getId(),this.getDataSourceWay()).get();
		}
		return conManager;
	}
	public ConnectionManager getConManager_notGenerate() {
		return conManager;
	}
	public void clearConnectionManager() {
		conManager=null;
	}
	public Object getObjSql() {
		return objSql;
	}
	public void setObjSql(Object objSql) {
		this.objSql = objSql;
	}
	public Method getMethod() {
		return method;
	}
	public void setMethod(Method method) {
		this.method = method;
	}
	public Object getObjSqlProxy() {
		return objSqlProxy;
	}
	public void setObjSqlProxy(Object objSqlProxy) {
		this.objSqlProxy = objSqlProxy;
	}
	public Object[] getMethodParam() {
		return methodParam;
	}
	public void setMethodParam(Object[] methodParam) {
		this.methodParam = methodParam;
	}
	
}