package org.wind.orm.sql.DDL.call;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.wind.orm.bean.SQLType;
import org.wind.orm.exception.TableDDLException;
import org.wind.orm.sql.DDL.Call;
import org.wind.orm.util.TableUtil;

/**
 * @描述 : DDL【call】接口抽象类
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2016年3月1日 16:29:37
 */
@SuppressWarnings("unchecked")
public abstract class CallImpl implements Call{

	/**
	 * 调用 : 存储过程并返回1个值（如有多个请用callList方法，只取第1个）
	 * @param proc : 过程名
	 * @param paramArr[] : 参数数组
	 * @param returnType : 返回值类型（请用java的java.sql.Types包下的类型，如： java.sql.Types.BIGINT）
	 */
	public <T> T call(String proc,Object[] paramArr,Integer returnType) {
		List<T> list=this.callList(proc, paramArr, returnType!=null?new Integer[]{returnType}:null);
		if(list!=null && list.size()>0){
			return list.get(0);
		}
		return null;
	}
	/**
	 * 调用 : 存储过程并返回所有值
	 * @param proc : 过程名（带返回值类型，请放在括号最后）
	 * @param paramArr[] : 参数数组
	 * @param returnTypeArr[] : 返回值类型数组（请用java的java.sql.Types包下的类型，如： java.sql.Types.BIGINT）
	 */
	public <T> List<T> callList(String proc,Object[] paramArr,Integer[] returnTypeArr)  {
		List<Object> list=new ArrayList<Object>();
		if(proc!=null && proc.trim().length()>0){
			proc=proc.trim();
			Connection con=this.getConnection();
			CallableStatement cs=null;
			try{
				StringBuffer sql=new StringBuffer("{call "+proc+"(");
				int paramLength=paramArr!=null?paramArr.length:0;
				int retunTypeLength=returnTypeArr!=null?returnTypeArr.length:0;
				int length=paramLength+retunTypeLength;
				for(int i=0;i<length;i++){
					sql.append("?");
					if(i<length-1){
						sql.append(",");
					}
				}
				sql.append(")}");
				cs = con.prepareCall(sql.toString());
				for(int i=0;i<paramLength;i++){
					cs.setObject(i+1, paramArr[i]);
				}
				for(int i=0; i<retunTypeLength;i++){
					cs.registerOutParameter(i+1+paramLength, returnTypeArr[i]);
				}
				this.executeSQL(SQLType.DDL,cs, sql.toString(), null);
				for(int i=0;i<retunTypeLength;i++){
					list.add(cs.getObject(i+1+paramLength));
				}
//				ResultSetMetaData rsmd=rs.getMetaData();
//				int columns=rsmd.getColumnCount();	//查询显示的列数
//				if(rs.next()){
//					for(int i=0;i<columns;i++){
//						list.add(rs.getObject(i+1+paramLength));
//					}
//				}
			}catch(Exception e){
				throw new TableDDLException(e.getMessage(),e);
			}finally{
				TableUtil.close(null, cs);
			}
		}
		return (List<T>) list;
	}

}