package org.wind.orm.cache;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Map;

import org.wind.orm.Table;
import org.wind.orm.bean.Config;
import org.wind.orm.util.ConfigUtil;
import org.wind.orm.util.TableUtil;

/**
 * @描述 : 二级Cache缓存类
 * @详情 : 存放所有全局数据，生命周期从“创建”—>“程序关闭”<br />
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年8月23日 19:12:18
 */
public class Cache2 {

	//二级缓存主Map
	private static Map<String,Config> configMap=new HashMap<>();		//数据源配置
	public static final String dataSource_default="default";	//默认数据源id
	/*************数据库*********/
	public static final String MySQL="mysql";	//MySQL
	public static final String SQLServer="sql server";	//SQLServer
	public static final String Oracle="oracle";	//Oracle
	/*************数据库（数据库关键词—>class关键词）*********/
	public static final Map<String,String> dbClassKeyMap=new HashMap<String,String>();
	/*************数据源方式*********/
	public static final int dataSourceWay=0;		//主要的（最顶级，具有读写功能）
	public static final int dataSourceWay_write=1;		//写
	public static final int dataSourceWay_read=2;			//读
	
	/**初始化**/
	static{
		Cache2 obj=new Cache2();
		configMap=obj.loadDataSourceConfig();
		//
		dbClassKeyMap.put(MySQL, "MySQL");
		dbClassKeyMap.put(SQLServer, "SQLServer");
		dbClassKeyMap.put(Oracle, "Oracle");
	}
	/**获取 : 数据源（不存在则取默认值）**/
	public static String getDataSource(Class<? extends Table> tableClass){
		String dataSource=TableUtil.getDataSource(tableClass);
		return dataSource!=null?dataSource:dataSource_default;
	}
	/**获取 : 对应配置文件**/
	public static Config getConfig(String poolName){
		return configMap.get(poolName);
	}
	
	/*************本地方法*************/
	/**加载：所有数据源配置（并验证处理）**/
	public Map<String,Config> loadDataSourceConfig(){
		Map<String,Config> configMap=new ConfigUtil().getDataSourceConfig();
		for(Config config:configMap.values()) {
			Connection con = null;
			try {
				Class.forName(config.getDriver());	// 注册 JDBC 驱动程序
				con=TableUtil.getConnection(config.getUrl(), config.getUserName(), config.getPassWord(), config.getConnectionTimeout());
				DatabaseMetaData dmd=con.getMetaData();
				
				/*数据库名称*/
				if(config.getDataBase()==null || config.getDataBase().trim().length()<=0) {
					String databaseName=con.getCatalog();		//数据库名（优先取手动配置的，若没有则取该值）
					config.setDataBase(databaseName);
				}
				/*数据库类型*/
				if(config.getType()==null || config.getType().trim().length()<=0) {
					String databaseProductName=dmd.getDatabaseProductName();		//数据库产品名（优先取手动配置的，若没有则取该值）
					config.setType(databaseProductName!=null?databaseProductName.toLowerCase():null);
				}
				/*数据库最大连接数*/
				int maxConnections=dmd.getMaxConnections();		//为0说明没有最大数
				if(maxConnections>0 && maxConnections<=config.getMax()){
					config.setMax(maxConnections);
				}
			}catch(Exception e) {
				throw new RuntimeException(e);
			}finally {
				TableUtil.closeConnection(con);
			}
		}
		return configMap;
	}
	
}