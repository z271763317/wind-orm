package org.wind.orm.sql.DDL.other;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Set;

import org.wind.orm.bean.Config;
import org.wind.orm.cache.Cache2;
import org.wind.orm.exception.TableSelectException;
import org.wind.orm.sql.DDL.Other;
import org.wind.orm.util.SQLUtil;
import org.wind.orm.util.TableUtil;

/**
 * @描述 : DDL【其他】接口抽象类
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2019年1月24日 09:45:38
 */
public abstract class OtherImpl implements Other{

	/**判断 : 指定表是否存在**/
	public boolean isTableExist(String table) {
		Connection con=this.getConnection();
		ResultSet rs=null;
		boolean isExist=false;
		try{
			DatabaseMetaData meta = con.getMetaData();  
			Config config=this.getSqlVar().getConfig();
			String db=config.getType();
			//Oracle
			if(db.equalsIgnoreCase(Cache2.Oracle)){
				// 第二个参数schemaPattern在ORACLE中对应用户名  
				rs = meta.getTables(null, config.getUserName(), table.toUpperCase(),new String[] {"TABLE"});  
			}else{
				rs = meta.getTables(con.getCatalog(), con.getSchema(), table, new String[] {"TABLE"});  
			}
            if(rs.next()) {  
            	isExist=true;
            }
		}catch (Exception e) {
			throw new TableSelectException(e.getMessage(),e);
		}finally{
			TableUtil.close(rs, null);
		}
		return isExist;
	}
	/**获取 : 列名**/
	public Set<String> getColumn(){
		return SQLUtil.getColumn_Database(this);
	}

}