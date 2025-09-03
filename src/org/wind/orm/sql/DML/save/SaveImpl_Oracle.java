package org.wind.orm.sql.DML.save;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wind.orm.Table;
import org.wind.orm.annotation.ForeignKey;
import org.wind.orm.annotation.Id;
import org.wind.orm.annotation.Null;
import org.wind.orm.bean.SQLType;
import org.wind.orm.exception.TableSaveException;
import org.wind.orm.util.SQLUtil;
import org.wind.orm.util.TableUtil;


/**
 * @描述 : SQL【插入、更新】接口实现类——SQL Oracle
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年9月29日 14:37:24
 */
public class SaveImpl_Oracle extends SaveImpl{
	
	/**
	 * 【手动选择】批量保存<br>
	 * 【注】：保存的所有对象是属于同一个类，以List第一个为基础
	 * @param obj : 当前ORM表类
	 * @param list : 当前ORM表类对象列表
	 * @param isAdd : 是否插入
	 * @param isInsertPrimary : 插入时是否插入主键数据
	 * @return 返回影响的行数列表
	 */
	protected int batchSaveTemp(Connection con,List<? extends Table> list,boolean isAdd){
		int result=0;	//结果影响的行数
		if(list.size()>0){
			PreparedStatement ps=null;
			ResultSet rs=null;
			try {
				Class<? extends Table> tableClass=this.getObj().getClass();
				String table=this.getTable();
				Field idField=TableUtil.getPrimaryKeyField(tableClass);		//主键字段（更新、返回数据转换用）
				Field propertyArr[]=TableUtil.getField(tableClass);
				Set<String> colSet=SQLUtil.getColumn_Database(this);	//表所有的字段（真实数据库表字段）
				Map<String,Method> methodMap_get=TableUtil.getMethodMap_get(tableClass);		//get方法
				Map<String,Method> methodMap_set=TableUtil.getMethodMap_set(tableClass);		//set方法
				String sql="update "+table+" set ";	//默认 : 更新
				int type=SQLType.UPDATE;
				//插入
				if(isAdd){
					sql="insert into "+table+" (";
					type=SQLType.INSERT;
				}
				/*索引*/
				String seq_autoId=null;
				if(idField!=null) {
					Id t_an_id=idField.getAnnotation(Id.class);
					//自增
					if(t_an_id.value()==Id.autoIncrement) {
						seq_autoId=t_an_id.sequence();
						//未设置
						if(seq_autoId.length()<=0) {
							seq_autoId=null;
						}
					}
				}
				//批量保存
				for(int k=0;k<list.size();k++){
					StringBuffer t_sql=new StringBuffer(sql);		//临时
					StringBuffer sql_like=new StringBuffer();		//?的连接,update则还要接where条件
					Table t_obj=list.get(k);
					Object idValue=null;		//主键值（更新用）
					String idLabel=null;	//主键标签名
					List<Object> placeholderList=new ArrayList<Object>();	//占位符值
					//填写要插入的字段 或 获取更新的ID字段名称
					for(int i=0;i<propertyArr.length;i++){
						Field f=propertyArr[i];
						String col=null;		//列名
						Object value=null;
						//外键（非List泛型）
						if(f.isAnnotationPresent(ForeignKey.class) && !List.class.isAssignableFrom(f.getType())){
							col=TableUtil.getForeignKey(f);
							Object f_obj=TableUtil.get(t_obj, methodMap_get, f);
							value=TableUtil.getPrimaryKeyValue(f_obj);	//取外键的主键值
						}else{
							col=TableUtil.getColumn(f);
							value=TableUtil.get(t_obj, methodMap_get, f);
						}
						//列存在
						if(colSet.contains(col)) {
							String t_value_str=TableUtil.getColumnValueStr(f);
							//主键
							if(f.isAnnotationPresent(Id.class)){
								//插入
								if(isAdd){
									value=SQLUtil.generatePrimaryKey(f,value);		//生成主键
								}
								idValue=value;
								idLabel=col;
							//不保存
							}else if(f.isAnnotationPresent(Null.class)){
								value=null;
							}
							/*存在*/
							if(value!=null){
								/**插入**/
								if(isAdd){
									t_sql.append(this.getPack(col)+",");
									sql_like.append(t_value_str+",");
								/**更新**/
								}else{
									t_sql.append(this.getPack(col)+"="+t_value_str+",");	//set其他字段值
								}
								placeholderList.add(value);
							}
							
							/*不存在*/
							if(value==null){
								/**插入（主键策略为：自增，使用自增索引）**/
								if(isAdd && f.isAnnotationPresent(Id.class) && seq_autoId!=null){
									t_sql.append(this.getPack(col)+",");
									sql_like.append(seq_autoId+".nextval,");
								}
							}
							
						}
						//拼接SQL完毕
						if(i>=propertyArr.length-1){
							/**插入**/
							if(isAdd){
								//到达最后一个字段，去除“,”加上其他的字符串
								if(sql_like!=null && sql_like.length()>0){
									t_sql=new StringBuffer(t_sql.substring(0,t_sql.length()-1)+") values(");
									sql_like=new StringBuffer(sql_like.substring(0,sql_like.length()-1)+")");	
								}else{
									throw new TableSaveException("缺少要插入的字段");
								}
							/**更新**/
							}else{
								if(idLabel!=null){
									if(idValue!=null) {
										t_sql=new StringBuffer(t_sql.substring(0,t_sql.length()-1)+" where "+this.getPack(idLabel)+"=?");	//set完毕后，设置where条件为主键条件
										placeholderList.add(idValue);
									}else {
										throw new TableSaveException("主键不能为空");
									}
								}else{
									throw new TableSaveException("缺少主键字段");
								}
							}
						}
					}
					t_sql.append(sql_like);	//拼接起预SQL
					//
					ps=con.prepareStatement(t_sql.toString(), Statement .RETURN_GENERATED_KEYS);
					//加入预SQL的值
					for(int i=0;i<placeholderList.size();i++){
						ps.setObject(i+1, placeholderList.get(i));	
					}
					result+=(int)this.executeSQL(type,ps, t_sql.toString(),placeholderList);	//执行SQL
					/**插入**/
					if(isAdd){
						//无值
						if(idValue==null){
							//主键存在
							if(idField!=null) {
								rs=ps.getGeneratedKeys();
								if(rs!=null && rs.next()){
									idValue=TableUtil.cast(rs.getObject(1),idField.getType());
								}else{
									throw new TableSaveException("该表没有找到主键字段");
								}
							}else{
								throw new TableSaveException("缺少主键字段");
							}
						}
						SQLUtil.setPrimarykeyValue(t_obj, methodMap_set, idField, idValue);
					}
					TableUtil.close(rs, ps);
				}
			}catch(Exception e){
				throw new TableSaveException(e.getMessage(),e);
			}finally{
				TableUtil.close(rs, ps);
			}
		}
		return result;
	}
	
}