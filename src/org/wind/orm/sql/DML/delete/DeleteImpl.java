package org.wind.orm.sql.DML.delete;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wind.orm.Table;
import org.wind.orm.annotation.ForeignKey;
import org.wind.orm.bean.SQLType;
import org.wind.orm.exception.TableDeleteException;
import org.wind.orm.sql.DML.Delete;
import org.wind.orm.util.SQLUtil;
import org.wind.orm.util.TableUtil;

/**
 * @描述 : SQL【删除】接口抽象类
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年5月29日 09:30:33
 */
public abstract class DeleteImpl implements Delete{

	/******************抽象方法*******************/
	
	/** 删除：根据当前ORM对象的变量是否有值（不为null）**/
	public int delete() {
		return this.deleteTemp(this.getObj(), null, null);
	}
	/**
	 * 删除：根据当前ORM对象的Id列表
	 * @param ids : 当前ORM对象ID列表
	 */
	public int delete(List<? extends Object> ids){
		int result=0;
		//是否同一个，并且不为空
		if(ids!=null && ids.size()>0){
			Class<? extends Table> tableClass=this.getObj().getClass();
			String colId=TableUtil.getPrimaryKey(tableClass);	//获取主键列名
			//
			String t_pleace=TableUtil.getPlaceholder(ids);
			StringBuffer tjSQL=new StringBuffer(this.getPack(colId)+" in("+t_pleace+")");
			result=this.deleteTemp(null, tjSQL.toString(), ids);
		}
		return result;
	}
	/**
	 * 删除全部：删除当前ORM类所有的数据
	 * @return 返回删除所影响的行数
	 **/
	public int deleteAll(){
		int result=this.deleteTemp(null,"1=1", null);
		return result;
	}
	/**
	 * 删除 : 根据传来的条件语句
	 * @param conditionsSQL : 条件SQL
	 * @param conditionsList : 条件值
	 */
	public int delete(String conditionsSQL,List<? extends Object> conditionsList){
		return this.deleteTemp(null,conditionsSQL, conditionsList);
	}
	
	/*****************本地方法***************/
	/**
	 * 【通用】删除方法，优先取conditionsSQL和conditionsList，若为空，则进行obj是否有值加条件
	 * @param obj : 继承Table的ORM对象
	 * @param isExistId : 是否根据主键ID删除
	 * @param conditionsSQL : 条件SQL
	 * @param conditionsList : 条件值
	 * @return
	 */
	protected int deleteTemp(Table obj,String conditionsSQL,List<? extends Object> conditionsList){
		Connection con=this.getConnection();
		PreparedStatement ps=null;
		int result=0;
		try{
			Class<? extends Table> tableClass=this.getObj().getClass();
			String table=this.getTable();
			Field propertyArr[]=TableUtil.getField(tableClass);
			StringBuffer tjSQL=new StringBuffer();	//条件SQL
			List<Object> paramList=new ArrayList<Object>();	//条件参数列表（按设置条件的顺序）
			//有传条件
			if(conditionsSQL!=null && conditionsSQL.length()>0){
				tjSQL.append(conditionsSQL);
				if(conditionsList!=null && conditionsList.size()>0){paramList.addAll(conditionsList);}
			}else if(obj!=null){
				Set<String> colSet=SQLUtil.getColumn_Database(this);		//表所有的字段（真实数据库表字段）
				StringBuffer t_tjSQL=new StringBuffer();	//条件SQL（当前临时）
				Map<String,Method> methodMap=TableUtil.getMethodMap_get(tableClass);		//获取该Class所有的Method【get】
				for(int i=0;i<propertyArr.length;i++){
					Field f=propertyArr[i];
					Object value=null;
					String col=TableUtil.getColumn(f).toLowerCase();
					//外键
					if(f.isAnnotationPresent(ForeignKey.class)){
						Object f_obj=TableUtil.get(this.getObj(), methodMap, f);
						//泛型
						if(List.class.isAssignableFrom(f.getType())){
						
						//预留
						}else{
							value=TableUtil.getPrimaryKeyValue(f_obj);	//取外键的主键值
							col=TableUtil.getForeignKey(f).toLowerCase();
						}
					}else{
						value=TableUtil.get(this.getObj(), methodMap, f);
					}
					value=TableUtil.isNull(f,value);	//判断是否空值，并返回处理后的值
					//数据库存在该字段,并且有数据
					if(colSet.contains(col) && value!=null){
						t_tjSQL.append(" and "+this.getPack(col)+"=?");
						paramList.add(value);
					}
				}
				//有值（有条件SQL）
				if(t_tjSQL!=null && t_tjSQL.length()>0){
					tjSQL.append(t_tjSQL);
				}else{
					//throw new TableDeleteException("没有删除条件");
				}
			}
			//有条件则删除
			if(tjSQL!=null && tjSQL.length()>0){
				/***执行SQL操作***/
				String whereSQL=TableUtil.getWhereSQL(tjSQL.toString());
				String sql="delete from "+table+whereSQL;
				ps=con.prepareStatement(sql.toString());
				for(int i=0; i<paramList.size();i++){
					ps.setObject(i+1, paramList.get(i));
				}
				result=this.executeSQL(SQLType.DELETE,ps, sql.toString(), paramList);
			}
		}catch(Exception e){
			throw new TableDeleteException(e.getMessage(),e);
		}finally{
			TableUtil.close(null, ps);
		}
		return result;
	}
	
}