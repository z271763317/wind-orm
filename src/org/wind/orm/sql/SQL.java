package org.wind.orm.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import org.wind.orm.Table;
import org.wind.orm.bean.SQLVar;
import org.wind.orm.cache.Cache1;
import org.wind.orm.util.SQLUtil;
import org.wind.orm.util.TableUtil;

/**
 * @描述 : SQL操作接口
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年8月23日 08:24:08
 */
public interface SQL {
	
	/**获取 : SQLVar对象**/
	public default SQLVar getSqlVar(){
		return Cache1.get().getSqlVar();
	}
	/**获取 : 表名**/
	public default String getTable(){
		return SQLUtil.getTable(this.getSqlVar());
	}
	/**获取 : 包装真实名后的名（主要解决关键字、特殊字符的问题和匹配真实名）**/
	public default String getPack(String name){
		return TableUtil.getPack(getSqlVar().getConfig(), name);
	}
	/**获取 : 当前Connection**/
	public default Connection getConnection(){
		return this.getSqlVar().getConManager().getConnection();
	}
	/**获取 : 当前对象**/
	public default Table getObj(){
		return this.getSqlVar().getObj();
	}
	/**执行SQL（{@link SQLUtil#executeSQL(PreparedStatement, String, int, SQLVar)}）**/
	public default <T> T executeSQL(int type,PreparedStatement ps,String sql,List<? extends Object> placeholderList) throws Exception{
		return SQLUtil.executeSQL(type,ps, sql,placeholderList,this.getSqlVar());
	}
	
}