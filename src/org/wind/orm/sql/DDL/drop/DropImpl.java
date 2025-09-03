package org.wind.orm.sql.DDL.drop;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.wind.orm.bean.SQLType;
import org.wind.orm.exception.TableSelectException;
import org.wind.orm.sql.DDL.Drop;
import org.wind.orm.util.TableUtil;

/**
 * @描述 : DDL【drop】接口抽象类
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2019年1月23日 17:52:29
 */
public abstract class DropImpl  implements Drop{

	/**drop**/
	public boolean drop(String sql){
		Connection con=this.getConnection();
		PreparedStatement ps=null;
		boolean isSuccess=false;
		try{
			ps=con.prepareStatement(sql);
			this.executeSQL(SQLType.DDL,ps, sql.toString(), null);
			isSuccess=true; 
			
		}catch (Exception e) {
			throw new TableSelectException(e.getMessage(),e);
		}finally{
			TableUtil.close(null, ps);
		}
		return isSuccess;
	}
}