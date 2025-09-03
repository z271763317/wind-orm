package org.wind.orm.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.wind.orm.cache.Cache2;

/**
 * @描述 : 数据库连接池工厂类
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年5月21日 15:11:30
 */
public final class ConnectionPoolFactory {

	//所有数据源连接池
	private static Map<String,Map<Integer,ConnectionPool>> connectionPoolMap=new ConcurrentHashMap<>();
	
	static {
//		TimerTask task = new ConnectionFreeTask();
//        new Timer().schedule(task,60*1000,60*1000);		//1分钟检查一次
	}
	
	private ConnectionPoolFactory(){
		throw new RuntimeException("请不要试图实例化我");
	}
	
	/**获取 : 连接池，poolName指定的数据库连接池别名，dataSourceWay=读写标志**/
	public static ConnectionPool getConnectionPool(String poolName,int dataSourceWay){
		ConnectionPool t_conPool=getConnectionPool_temp(poolName, dataSourceWay);
		if(t_conPool==null){
			t_conPool=createConnectionPool(poolName,dataSourceWay);
		}
		return t_conPool;
	}
	/**创建 : 连接池，poolName指定的数据库连接池别名**/
	private static synchronized ConnectionPool createConnectionPool(String poolName,int dataSourceWay){
		ConnectionPool t_conPool=getConnectionPool_temp(poolName, dataSourceWay);
		if(t_conPool==null){
			t_conPool=new ConnectionPoolImpl(poolName,dataSourceWay);
			setConnectionPool_temp(poolName, t_conPool.getDataSourceWay(), t_conPool);
		}
		return t_conPool;
	}
	//获取 : 数据源连接池（临时）
	private static ConnectionPool getConnectionPool_temp(String poolName,int dataSourceWay){
		ConnectionPool t_conPool=null;
		Map<Integer, ConnectionPool> t_conPoolMap=connectionPoolMap.get(poolName);
		if(t_conPoolMap!=null){
			switch(dataSourceWay){
				//读
				case Cache2.dataSourceWay_read :{
					t_conPool=t_conPoolMap.get(Cache2.dataSourceWay_read);
					break;
				}
				//写
				case Cache2.dataSourceWay_write : {
					t_conPool=t_conPoolMap.get(Cache2.dataSourceWay_write);
					break;
				}
			}
			//若没获取到，则取【主】的
			if(t_conPool==null){
				t_conPool=t_conPoolMap.get(Cache2.dataSourceWay);
			}
		}
		return t_conPool;
	}
	//设置 : 数据源连接池（临时）
	private static void setConnectionPool_temp(String poolName,int dataSourceWay,ConnectionPool t_conPool){
		if(poolName!=null){
			Map<Integer, ConnectionPool> t_conPoolMap=connectionPoolMap.get(poolName);
			if(t_conPoolMap==null){
				t_conPoolMap=new ConcurrentHashMap<Integer, ConnectionPool>();
				connectionPoolMap.put(poolName, t_conPoolMap);
			}
			t_conPoolMap.put(dataSourceWay, t_conPool);
		}
	}
}