package org.wind.orm.sql;

import java.util.HashMap;
import java.util.Map;

import org.wind.orm.Table;
import org.wind.orm.bean.Config;
import org.wind.orm.bean.SQLVar;
import org.wind.orm.bean.proxy.ProxyFactory;
import org.wind.orm.cache.Cache1;
import org.wind.orm.cache.Cache2;
import org.wind.orm.exception.SQLFactoryException;
import org.wind.orm.sql.DDL.Call;
import org.wind.orm.sql.DDL.Create;
import org.wind.orm.sql.DDL.Drop;
import org.wind.orm.sql.DDL.Other;
import org.wind.orm.sql.DDL.call.CallImpl;
import org.wind.orm.sql.DDL.create.CreateImpl;
import org.wind.orm.sql.DDL.drop.DropImpl;
import org.wind.orm.sql.DDL.other.OtherImpl;
import org.wind.orm.sql.DML.Delete;
import org.wind.orm.sql.DML.Save;
import org.wind.orm.sql.DML.Select;
import org.wind.orm.sql.DML.delete.DeleteImpl;
import org.wind.orm.sql.DML.save.SaveImpl;
import org.wind.orm.sql.DML.select.SelectImpl;
import org.wind.orm.util.SQLUtil;
import org.wind.orm.util.TableUtil;


/**
 * @描述 : SQL处理对象生产类
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年5月29日 09:36:58
 */
public final class SQLFactory {

	private static final Map<String,Object> objectMap=new HashMap<>();		//class路径—>对象
	
	/**单例，不允许外部实例化**/
	private SQLFactory(){
		
	}
	/************DML************/
	/**获取：Select操作类**/
	public static Select getSelect(Table obj){
		return getSQLObject(obj, SelectImpl.class);
	}
	/**获取：Save操作类**/
	public static Save getSave(Table obj){
		return getSQLObject(obj, SaveImpl.class);
	}
	/**获取：Delete操作类**/
	public static Delete getDelete(Table obj){
		return getSQLObject(obj, DeleteImpl.class);
	}
	
	/************DDL************/
	/**获取：call操作类**/
	public static Call getCall(Table obj){
		return getSQLObject(obj, CallImpl.class);
	}
	/**获取：create操作类**/
	public static Create getCreate(Table obj){
		return getSQLObject(obj, CreateImpl.class);
	}
	/**获取：drop操作类**/
	public static Drop getDrop(Table obj){
		return getSQLObject(obj, DropImpl.class);
	}
	/**获取：其他操作类**/
	public static Other getOther(Table obj){
		return getSQLObject(obj, OtherImpl.class);
	}
	
	//获取 : 对应数据库的SQL操作类
	private static <T> T getSQLObject(Table obj,Class<T> sqlClass) {
		Config config=TableUtil.getConfig(obj);
		if(config==null){
			throw new SQLFactoryException("【"+obj.getClass().getName()+"】未绑定数据源，可以检查下配置文件格式是否正确");
		}
		String dbType=config.getType();	//数据库类型
		T sqlObj=null;
		try{
			sqlObj=getSQLObject(sqlClass, dbType);		//动态获取SQL操作类
			init(obj, sqlClass);		//初始化
		}catch(Throwable e){
			e.printStackTrace();
			throw new SQLFactoryException("生成【"+config.getType()+"】的"+sqlClass.getName()+"错误，请上报给作者");
		}
		return sqlObj;
	}
	//获取 : 对应数据库的SQL操作类（动态）
	@SuppressWarnings("unchecked")
	private static <T> T getSQLObject(Class<T> sqlClass,String dbType) throws Exception{
		dbType=dbType.toLowerCase();		//转为小写
		String t_dbClassKey=Cache2.dbClassKeyMap.get(dbType);
		String t_classPath=sqlClass.getName()+"_"+t_dbClassKey;
		Object t_obj=objectMap.get(t_classPath);
		if(t_obj==null){
			synchronized(t_classPath.intern()){
				if(t_obj==null){
					Class<?> t_class=Class.forName(t_classPath);
					t_obj=t_class.newInstance();
					t_obj=new ProxyFactory().getSQLObject(t_obj);
					objectMap.put(t_classPath, t_obj);
				}
			}
		}
		return (T) t_obj;
	}
	//初始化
	private static <T> void init(Table obj,Class<T> sqlClass) {
		SQLVar sqlVar=SQLUtil.getVar(obj, sqlClass);
		Cache1.get().setSqlVar(sqlVar);	
	}
	
}