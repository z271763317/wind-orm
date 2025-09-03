package org.wind.orm.bean.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.wind.orm.bean.SQLVar;
import org.wind.orm.cache.Cache1;
import org.wind.orm.service.ConnectionManager;

/**
 * @描述 : 代理——SQL具体执行对象
 * @作者 : 胡璐璐
 * @时间 : 2018年11月10日 14:07:26
 */
public class ProxySQLObject<T> implements InvocationHandler{

	private T sourceObj;
	
	/**构造方法**/
	public ProxySQLObject(T sourceObj){
		if(sourceObj==null){
			throw new IllegalArgumentException("被代理的对象不能为null");
		}
		this.sourceObj=sourceObj;
	}

	/**动态切入**/
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try{
			this.init(proxy, method, args);		//初始化
			Object returnValue=method.invoke(this.sourceObj, args);
			return returnValue;
		}catch (Exception e) {
			throw e;
		}finally{
			ConnectionManager conManager=this.getConnectionManager();
			if(conManager!=null) {
				conManager.close();
			}
		}
	}
	//获取 : 连接管理器
	private ConnectionManager getConnectionManager() {
		return Cache1.get().getSqlVar().getConManager_notGenerate();
	}
	//初始化
	public void init(Object proxy,Method method,Object[] args) {
		SQLVar obj=Cache1.get().getSqlVar();
		obj.setObjSqlProxy(proxy);
		obj.setObjSql(this.sourceObj);
		obj.setMethod(method);
		obj.setMethodParam(args);
		//
		method.setAccessible(true);
	}
	
}