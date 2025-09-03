package org.wind.orm.sql.DDL.create;

/**
 * @描述 : DDL【create】接口实现类——MySQL
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2019年1月23日 18:08:14
 */
public class CreateImpl_MySQL extends CreateImpl {

	/**复制 : 当前表到新表（isCopyData=是否复制数据）**/
	public boolean copy(String newTable,boolean isCopyData){
		String sql=null;
		if(isCopyData){
			sql="CREATE TABLE "+newTable+" SELECT * FROM "+super.getTable();
		}else{
			sql="CREATE TABLE "+newTable+" LIKE "+super.getTable();
		}
		return super.create(sql);
	}
}