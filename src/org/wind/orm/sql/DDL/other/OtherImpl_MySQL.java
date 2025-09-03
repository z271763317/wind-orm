package org.wind.orm.sql.DDL.other;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.wind.orm.bean.SQLType;
import org.wind.orm.exception.TableSelectException;
import org.wind.orm.util.TableUtil;

/**
 * @描述 : DDL【other】接口实现类——MySQL
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2019年1月24日 09:46:22
 */
public class OtherImpl_MySQL extends OtherImpl {

	/**获取 : 当前表创建SQL语句**/
	public String getTableSQL(){
		Connection con=this.getConnection();
		PreparedStatement ps=null;
		ResultSet rs=null;
		String tableSQL=null;
		try{
			String sql="show create table "+super.getTable();
			ps=con.prepareStatement(sql);
			rs=super.executeSQL(SQLType.SELECT,ps, sql.toString(), null);
			while(rs.next()){
				tableSQL=rs.getString(2);		//1=表名；2=建表SQL
			}
		}catch (Exception e) {
			throw new TableSelectException(e.getMessage(),e);
		}finally{
			TableUtil.close(rs, ps);
		}
		return tableSQL;
	}

}