package org.wind.orm.sql.DDL.create;

/**
 * @描述 : DDL【create】接口实现类——Oracle
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2021年5月12日 16:15:30
 */
public class CreateImpl_SQLServer extends CreateImpl {

	/**复制 : 当前表到新表（isCopyData=是否复制数据）**/
	public boolean copy(String newTable,boolean isCopyData){
		String table=super.getTable();
		String sql="select * into ["+newTable+"] as select * from "+table;
		if(!isCopyData){
			sql+=" where 1=2";
		}
		sql+=";";
		return super.create(sql);
	}
	
	
}