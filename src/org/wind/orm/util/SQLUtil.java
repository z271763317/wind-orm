package org.wind.orm.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.wind.orm.Table;
import org.wind.orm.annotation.Id;
import org.wind.orm.bean.Config;
import org.wind.orm.bean.ActionContext;
import org.wind.orm.bean.SQLType;
import org.wind.orm.bean.SQLVar;
import org.wind.orm.cache.Cache2;
import org.wind.orm.exception.ConnectionPoolException;
import org.wind.orm.exception.TableDDLException;
import org.wind.orm.exception.TableSelectException;
import org.wind.orm.plugin.Interceptor;
import org.wind.orm.sql.SQL;
import org.wind.orm.sql.DML.Select;

/**
 * @描述 : SQL工具类
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年8月23日 08:24:08
 */
@SuppressWarnings("unchecked")
public class SQLUtil {

	private static final Map<Class<?>,Set<String>> colMap=new HashMap<>();		//列名（真实）
	/*表别名*/
	public static final String table_alias="t";		//表别名
	public static final String table_inner_alias="t_inner";		//表别名（内部）
	
	/**获取一个Var对象（单次执行需要的数据）**/
	public static SQLVar getVar(Table obj,Class<?> sqlClass) {	
		Config config=TableUtil.getConfig(obj);
		String dateBase = null;
		Integer dataSourceWay;
		if(config!=null){
			//数据库名
			if(config.getDataBase()!=null && config.getDataBase().trim().length()>0) {
				dateBase=TableUtil.getPack(config, config.getDataBase())+".";		//数据库名一致
			}
			//Select（读）
			if(Select.class.isAssignableFrom(sqlClass)){
				dataSourceWay=Cache2.dataSourceWay_read;
			//写
			}else{
				dataSourceWay=Cache2.dataSourceWay_write;
			}
		}else{
			throw new ConnectionPoolException("未配置默认（"+Cache2.dataSource_default+"）数据库连接池");
		}
		return new SQLVar(dateBase, obj, config, dataSourceWay);
	}
	/**获取：当前对象对应的表名（优先取动态名，不做任何处理）**/
	public static String getTable(Table obj){
		String t_table=obj.getTable();
		if(t_table!=null) {
			return t_table;
		}else{
			return TableUtil.getTable(obj.getClass());
		}
	}
	/**
	 * 获取：Class类映射的表名，如果没有Tables注解或value()为空，则用类名代表。优先取SQLVar里的obj临时设置的表名<br />
	 * 格式：【数据库名】（若isSpecifyDatabaseName配置为true）.【表名】（会被包装，包装参照{@link TableUtil#getPack(String, String)}
	 * @param objVar : 本次SQL执行的变量
	 */
	public static String getTable(SQLVar objVar){
		Table obj=objVar.getObj();
		Class<? extends Table> tableClass=obj.getClass();
		String table=obj.getTable();
		Config config=objVar.getConfig();
		if(table==null) {
			table=TableUtil.getTable(tableClass);
		}
		table=TableUtil.getPack(config,table);
		if(config.getIsSpecifyDatabaseName()){
			return objVar.getDataBase()+table;
		}
		return table;
	}
	/**获取 : 当前日期时间（根据不同数据库的sql）**/
	public static String findDate(SQLVar objVar){
		Connection con=objVar.getConManager().getConnection();
		PreparedStatement ps=null;
		ResultSet rs=null;
		String result=null;
		try{
			String db=objVar.getConfig().getType();
			String dateSQL=null;
			if(db.equalsIgnoreCase(Cache2.SQLServer)){
				dateSQL="select getDate()";
			}else if(db.equalsIgnoreCase(Cache2.Oracle)){
				dateSQL="select sysdate from dual";
			}else{
				dateSQL="select now()";
			}
			ps=con.prepareStatement(dateSQL);
			rs=executeSQL(SQLType.SELECT,ps, dateSQL,null ,objVar);
			while(rs.next()){
				result=rs.getString(1).substring(0,19);	//如有毫秒将截断
			}
			TableUtil.close(rs, ps);
		}catch (Exception e) {
			throw new TableSelectException(e.getMessage(),e);
		}finally{
			TableUtil.close(rs, ps);
		}
		return result;
	}
	
	/**
	 * 执行SQL，并返回对应执行类型的结果（ResultSet、int等等）
	 * @param type : 执行何种SQL（参考{@link SQL}类的静态变量），默认为：execute()，返回boolean类型
	 * @param ps : 要执行SQL的PreparedStatement
	 * @param sql : sql语句
		@param placeholderList : 占位符值列表（带【?】的值）
	 * @param objVar : SQL执行的数据
	 */
	public static <T> T executeSQL(int type,PreparedStatement ps,String sql,List<? extends Object> placeholderList,SQLVar objVar) throws Exception{
		if(objVar.getConfig().getPrintSQL()){
			System.out.println("SQL："+sql);
		}
		Object rs=null;
		Integer t_timeout=Table.getTimeout(objVar.getObj().getClass());
		if(t_timeout!=null){
			ps.setQueryTimeout(t_timeout);		//超时设置
		}else{
			ps.setQueryTimeout(objVar.getConfig().getTimeout());		//超时设置
		}
		/**拦截器（执行前）**/
		ActionContext context=getInterceptorContext(type, sql, placeholderList, objVar);
		Exception exception = null;
		try {
			interceptor_before(context);
			switch (type) {
				case SQLType.SELECT : rs=ps.executeQuery();break;	//查询
				case SQLType.INSERT : rs=ps.executeUpdate();break;	//插入
				case SQLType.UPDATE : rs=ps.executeUpdate();break;	//更新
				case SQLType.DELETE : rs=ps.executeUpdate();break;	//删除
				case SQLType.BATCH : rs=ps.executeBatch();break;	//批量
				case SQLType.DDL : rs=ps.executeUpdate();break;	//DDL（如：存储过程、create、drop等）
				case SQLType.SAVE : rs=ps.executeUpdate();break;	//保存（更新相关：插入、更新、删除等）
				default : rs=ps.execute();break;	//默认
			}
			/**拦截器（执行后）**/
			interceptor_after(context,rs);
		}catch(Exception e) {
			exception=null;
			throw e;
		}finally{
			/**拦截器（完成后）**/
			interceptor_complete(context,exception);
		}
		return (T) rs;
	}
	//获取 : 拦截器上下文
	private static ActionContext getInterceptorContext(int type,String sql,List<? extends Object> placeholderList,SQLVar objVar) {
		if(Interceptor.map.containsKey(type)) {
			return new ActionContext(type, sql, placeholderList, objVar);
		}
		return null;
	}
	//拦截器 : 执行前
	private static void interceptor_before(ActionContext context) throws Exception {
		if(context!=null) {
			Interceptor objInterceptor=Interceptor.map.get(context.getType());
			if(objInterceptor!=null) {
				objInterceptor.before(context);
			}
		}
	}
	//拦截器 : 执行后
	private static void interceptor_after(ActionContext context,Object result){
		if(context!=null) {
			Interceptor objInterceptor=Interceptor.map.get(context.getType());
			if(objInterceptor!=null) {
				objInterceptor.after(context,result);
			}
		}
	}
	//拦截器 : 完成后
	private static void interceptor_complete(ActionContext context,Exception exception){
		if(context!=null) {
			Interceptor objInterceptor=Interceptor.map.get(context.getType());
			if(objInterceptor!=null) {
				objInterceptor.complete(context,exception);
			}
		}
	}
	/**获取 : 指定ORM类映射的数据库表，真实列名列表（对象式）**/
	public static Set<String> getColumn_Database(SQL obj){
		Set<String> colSet=colMap.get(obj.getObj().getClass());
		if(colSet==null) {
			synchronized (obj.getObj().getClass()) {
				colSet=colMap.get(obj.getObj().getClass());
				if(colSet==null) {
					try {
						colSet=TableUtil.getColumn_Database(obj.getConnection(), getTable(obj.getObj()));
					}catch (Exception e) {
						throw new TableDDLException(e.getMessage(),e);
					}
					colMap.put(obj.getObj().getClass(), colSet);
				}
			}
		}
		return colSet;
	}
	/************************************【其他】*************************************/
	/**
	 * 设置主键值到ORM对象里
	 * @param obj : 要更新主键的ORM对象
	 * @param methodMap : obj对象类的所有方法（必须带get方法）
	 * @param idField : 主键字段
	 * @param idValue : 主键值
	 */
	public static void setPrimarykeyValue(Table obj,Map<String,Method> methodMap,Field idField,Object idValue){
		if(obj!=null && methodMap!=null && idField!=null && idValue!=null){
			TableUtil.set(obj, methodMap, idField, idValue);
		}
	}
	/**生成主键（并返回）**/
	public static Object generatePrimaryKey(Field field_pk,Object value) throws IllegalArgumentException, IllegalAccessException{
		//为空才处理
		if(value==null || value.toString().length()<=0){
			Id an_id=field_pk.getAnnotation(Id.class);
			if(an_id!=null){
				field_pk.setAccessible(true);		//取消任何限制的访问权限
				int strategy=an_id.value();		//策略
				switch(strategy){
					//UUID
					case Id.UUID:{return UUID.randomUUID().toString();}
				}
			}
		}
		return value;
	}
}