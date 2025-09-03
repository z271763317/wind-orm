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
import org.wind.orm.exception.TableSelectException;
import org.wind.orm.sql.DML.Save;
import org.wind.orm.util.SQLUtil;
import org.wind.orm.util.TableUtil;


/**
 * @描述 : SQL【插入、更新】接口抽象类
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年5月29日 09:46:32
 */
public abstract class SaveImpl implements Save{

	/**
	 * 保存（更新：当主键变量存在值时；插入：主键变量为null时候）
	 * @param isInsertPrimary : 插入时是否插入主键数据
	 */
	public int save(){
		List<Table> list=new ArrayList<Table>();
		list.add(this.getObj());
		boolean isAdd=true;	//是否更新
		Object value=TableUtil.getPrimaryKeyValue(this.getObj());
		if(value!=null){
			isAdd=false;
		}
		return this.batchSaveTemp(list, isAdd);
	}
	/**
	 * 保存（自传的insert、update的SQL语句）
	 * @param sql : insert或update语句
	 * @param placeholderList : 占位符的值
	 */
	public int save(String sql,List<? extends Object> placeholderList){
		Connection con=this.getConnection();
		PreparedStatement ps=null;
		int result=0;
		try{
			ps=con.prepareStatement(sql.toString(), Statement .RETURN_GENERATED_KEYS);
			//加入预SQL的值
			for(int i=0;placeholderList!=null && i<placeholderList.size();i++){
				ps.setObject(i+1, placeholderList.get(i));	
			}
			result=this.executeSQL(SQLType.SAVE,ps, sql.toString(),placeholderList);	//执行SQL
		}catch(Exception e){
			throw new TableSaveException(e.getMessage(),e);
		}finally{
			TableUtil.close(null, ps);
		}
		return result;
	}
	/**【自动选择】批量保存（保存的所有对象是属于同一个类，以List第一个为基础）**/
	public int batchSaveAuto(List<? extends Table> list){
		return this.batchSaveTemp(list, null);
	}
	/**【手动选择】批量保存：isAdd代表是否“插入”，反之“更新**/
	public int batchSave(List<? extends Table> list,boolean isAdd){
		return this.batchSaveTemp(list, isAdd);
	}
	/**
	 * 更新：根据传来的条件
	 * @param setSQL : set区语句
	 * @param conditionsSQL : 条件SQL
	 * @param placeholderList : 占位符值（含条件值）
	 */
	public int update(String setSQL,String conditionsSQL,List<? extends Object> placeholderList){
		int result=0;
		if(setSQL!=null && conditionsSQL!=null && conditionsSQL.length()>0){
			Connection con=this.getConnection();
			PreparedStatement ps=null;
			try{
				String table=this.getTable();
				StringBuffer sql=new StringBuffer("update "+table+" set "+setSQL+" where "+conditionsSQL);	
				ps=con.prepareStatement(sql.toString());
				//加入预SQL的值
				for(int i=0;placeholderList!=null && i<placeholderList.size();i++){
					ps.setObject(i+1, placeholderList.get(i));	
				}
				result=this.executeSQL(SQLType.UPDATE,ps, sql.toString(),placeholderList);	//执行SQL
			}catch(Exception e){
				throw new TableSaveException(e.getMessage(),e);
			}finally{
				TableUtil.close(null, ps);
			}
		}
		return result;
	}
	/*------------------------------【本地方法】------------------------------*/
	/**【？？】批量保存
	 * 【注】：保存的所有对象是属于同一个类，以List第一个为基础
	 * @param list : 当前ORM表类对象列表
	 * @param isAdd : 是否插入，为空则分开插入
	 */
	protected int batchSaveTemp(List<? extends Table> list,Boolean isAdd){
		Connection con=this.getConnection();
		int result=0;
		try{
			if(isAdd!=null){
				result=this.batchSaveTemp(con, list, isAdd);
			}else{
				if(list.size()>0){
					Table t_t_obj=list.get(0);
					Field idField=TableUtil.getPrimaryKeyField(t_t_obj.getClass());
					if(idField!=null){
						Object value=TableUtil.getPrimaryKeyValue(t_t_obj);
						isAdd=false;
						if(value==null){
							isAdd=true;
						}
						result=this.batchSaveTemp(con, list, isAdd);
					}else{
						throw new TableSelectException("表类"+t_t_obj.getClass().getSimpleName()+"没有设置主键Field字段");
					}
				}
			}
		}catch(TableSaveException e){
			throw e;
		}catch(Exception e){
			throw new TableSaveException(e);
		}
		return result;
	}
	/**
	 * 【手动选择】批量保存<br>
	 * 【注】：保存的所有对象是属于同一个类，以List第一个为基础
	 * @param obj : 当前ORM表类
	 * @param list : 当前ORM表类对象列表
	 * @param isAdd : 是否插入
	 * @param isInsertPrimary : 插入时是否插入主键数据
	 * @return 插入：按顺序返回插入的主键值列表；更新：返回影响的行数列表
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
					sql="insert into "+table+"(";
					type=SQLType.INSERT;
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
							col=TableUtil.getForeignKey(f).toLowerCase();
							Object f_obj=TableUtil.get(t_obj, methodMap_get, f);
							value=TableUtil.getPrimaryKeyValue(f_obj);	//取外键的主键值
						//列
						}else{
							col=TableUtil.getColumn(f).toLowerCase();	
							value=TableUtil.get(t_obj, methodMap_get, f);
						}
						//列存在
						if(colSet.contains(col)) {
							String t_value_str=TableUtil.getColumnValueStr(f);
							//主键（存在）
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
	/**
	 * 自增减
	 * @param fieldName : 要自增的列映射的字段名称
	 * @param step : 步长（自增减的增量数）
	 * @param id : 条件为主键id值
	 * @return 返回影响的行数，id为null则不执行SQL，返回0
	 */
	public int increaseOrDecrease(String fieldName,double step,Object id){
		if(id!=null){
			Field field=TableUtil.getField(this.getObj().getClass(), fieldName);
			if(field!=null){
				String primaryKey=this.getPack(TableUtil.getPrimaryKey(this.getObj().getClass()));		//主键列名（真）
				String column=this.getPack(TableUtil.getColumn(field));
				String t_value_str=TableUtil.getColumnValueStr(field);
				StringBuffer sql= new StringBuffer("update "+this.getTable()+" set "+column+"="+column+(step>=0?"+"+step:step)+" where "+primaryKey+"="+t_value_str);
				List<Object> conditionsList=new ArrayList<Object>();
				conditionsList.add(id);
				return this.save(sql.toString(), conditionsList);
			}else{
				throw new TableSaveException("映射字段【"+fieldName+"】没有找到对应的表列");
			}
		}else{
			return 0;
		}
	}
	/**
	 * 自增减（带条件）
	 * @param fieldName : 要自增减的列映射的字段名称
	 * @param step : 步长（自增减的增量数）
	 * @param conditionsSQL : 条件SQL
	 * @param conditionsList : 条件值列表
	 * @return 返回影响的行数
	 */
	public int increaseOrDecrease(String fieldName,double step,String conditionsSQL,List<? extends Object> conditionsList){
		if(conditionsSQL!=null && conditionsSQL.trim().length()>0){
			Field field=TableUtil.getField(this.getObj().getClass(), fieldName);
			if(field!=null){
				String column=this.getPack(TableUtil.getColumn(field));
				StringBuffer sql= new StringBuffer("update "+this.getTable()+" set "+column+"="+column+(step>=0?"+"+step:step));
				sql.append(" where "+conditionsSQL);
				return this.save(sql.toString(), conditionsList);
			}else{
				throw new TableSaveException("映射字段【"+fieldName+"】没有找到对应的表列");
			}
		}else{
			return 0;
		}
	}
	
}