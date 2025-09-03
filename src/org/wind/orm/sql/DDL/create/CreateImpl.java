package org.wind.orm.sql.DDL.create;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.wind.orm.bean.SQLType;
import org.wind.orm.exception.TableSelectException;
import org.wind.orm.sql.DDL.Create;
import org.wind.orm.util.TableUtil;

/**
 * @描述 : DDL【create】接口抽象类
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2019年1月23日 17:52:29
 */
public abstract class CreateImpl implements Create{

	/**create**/
	public boolean create(String sql){
		Connection con=this.getConnection();
		PreparedStatement ps=null;
		boolean isSuccess=false;
		try{
			ps=con.prepareStatement(sql);
			this.executeSQL(SQLType.DDL,ps, sql.toString(),null);
			isSuccess=true; 
		}catch (Exception e) {
			throw new TableSelectException(e.getMessage(),e);
		}finally{
			TableUtil.close(null, ps);
		}
		return isSuccess;
	}

}