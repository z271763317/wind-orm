package org.wind.orm.sql.DML.select;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wind.orm.Table;
import org.wind.orm.annotation.ForeignKey;
import org.wind.orm.annotation.Id;
import org.wind.orm.annotation.OrderBy;
import org.wind.orm.bean.Page;
import org.wind.orm.bean.SQLType;
import org.wind.orm.exception.TableSelectException;
import org.wind.orm.util.SQLUtil;
import org.wind.orm.util.SelectUtil;
import org.wind.orm.util.TableUtil;

/**
 * @描述 : SQL【查询】接口实现类——MySQL
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年9月29日 14:39:04
 */
@SuppressWarnings("unchecked")
public class SelectImpl_MySQL extends SelectImpl{
	
	/*-------------------------------【本地方法】-------------------------------*/
	/**
	 * 查询：临时通用方法
	 * @param obj : ORM对象
	 * @param isDesc : 是否降序，false=正序
	 * @param page : Page分页对象
	 * @param conditionsSQL : 条件SQL语句
	 * @param conditionsList : 条件值
	 * @param specifiedColumnSet : 指定显示的列名
	 */
	protected <T> List<T> findTemp(Table obj,boolean isDesc,Page page,String conditionsSQL,List<? extends Object> conditionsList,Set<String> specifiedColumnSet){
		Connection con=this.getConnection();
		PreparedStatement ps=null;
		ResultSet rs=null;
		List<? extends Table> list=new ArrayList<Table>();		//查询后返回的结果集列表
		List<String> paramRealList=new ArrayList<String>();	//条件【字段】列表（真实）
		List<Object> paramValueList=new ArrayList<Object>();	//条件【字段值】列表
		List<String> paramValueStrList = new ArrayList<String>(); // 条件【字段值】字符列表
		specifiedColumnSet=specifiedColumnSet!=null?specifiedColumnSet:obj.getSpecifiedColumnSet();
		try {
			Class<? extends Table> tableClass=obj.getClass();
			Field propertyArr[]=TableUtil.getField(tableClass);
			Map<String,Method> methodMap=TableUtil.getMethodMap_get(tableClass);		//获取该Class所有的Method【get】
			String table=super.getTable();		//主表（真实名称，带包装）
			Set<String> colSet=SQLUtil.getColumn_Database(this);	//表所有的字段（真实数据库表字段）
			String orderColumnFieldName=super.getObj().getOrderFieldName();		//指定排序的列映射的字段名
			String orderSQL=super.getObj().getOrderSQL();		//指定排序的SQL
			String groupColumnFieldName=super.getObj().getGroupFieldName();		//指定分组的列映射的字段名
			String groupSQL=super.getObj().getGroupSQL();	//指定分组的SQL
			String orderColumn=null;		//排序的列名
			String groupColumn=null;		//分组的列名
			boolean isIndex=false;			//是否有索引（二段排序的主键）
			String id=null;	//主键（真实字段）
			String first_col= null; // 首个列
			
			/********************************拼接SQL****************************/
			String col_label=SelectUtil.getColumnLabel(tableClass);	//主表自定义的列标签名
			StringBuilder sql_col=new StringBuilder();	//sql显示的列
			
			for(int i=0;i<propertyArr.length;i++){
				Field f=propertyArr[i];
				String col=TableUtil.getColumn(f);	//当前层的字段（真实字段）
				//数据库存在该字段
				if(colSet.contains(col.toLowerCase()) || f.isAnnotationPresent(ForeignKey.class)){
					String t_col_label=col_label+"_"+f.getName();//当前层的字段的标签名;
					Object value=TableUtil.get(obj, methodMap, f);
					Class<?> genericClass=null;		//List泛型的Class
					String t_value_str=TableUtil.getColumnValueStr(f);
					//主键
					if(f.isAnnotationPresent(Id.class)){
						id=this.getPack(col);
					//外键
					}else if(f.isAnnotationPresent(ForeignKey.class)){
						col=TableUtil.getForeignKey(f);	//外键真实列名
						genericClass=TableUtil.getGeneric(f);	
						value=TableUtil.getPrimaryKeyValue(value);
						//
						if(!List.class.isAssignableFrom(f.getType())){
							Field f_idField=TableUtil.getPrimaryKeyField(f.getType());
							t_col_label=t_col_label+"_"+f_idField.getName();	//当前层的字段的标签名;
						}
					}
					/*排序的列名*/
					if(orderColumn==null){
						//优先判断临时指定的排序字段
						if(orderColumnFieldName!=null){
							if(orderColumnFieldName.equalsIgnoreCase(f.getName())){
								orderColumn=this.getPack(col);
							}
						//默认指定的排序字段
						}else if(f.isAnnotationPresent(OrderBy.class)){
							orderColumn=this.getPack(col);
						}
					}
					/*分组的列名*/
					if(groupColumn==null){
						//优先判断临时指定的分组字段
						if(groupColumnFieldName!=null){
							if(groupColumnFieldName.equalsIgnoreCase(f.getName())){
								groupColumn=this.getPack(col);
							}
						}
					}
					/*加入显示的列名（“一对多”外键除外）*/
					if(genericClass==null){
						/***判断是否显示的列名（根据传入的字段名称）***/
						//非主键的进入
						if(!f.isAnnotationPresent(Id.class)){
							//若有传指定显示的列（根据ORM类的字段名称），并且当前字段不匹配，则结束本次循环
							if(specifiedColumnSet!=null && specifiedColumnSet.size()>0 && !specifiedColumnSet.contains(f.getName())){
								continue;
							}
						}
						sql_col.append(this.getPack(col)+" as '"+t_col_label+"',");
						value=TableUtil.isNull(f,value);	//判断是否空值，并返回处理后的值
						if(value!=null){
							paramRealList.add(this.getPack(col));
							paramValueList.add(value);
							paramValueStrList.add(t_value_str);
						}
						if(first_col==null){
							first_col=col;
						}
					}
				}
			}
			if(id==null){
				id=first_col;
			}
			//orderColumn为空。或超高效分页
			String temp_orderColumn=orderColumn;		//指定的排序列名
			if(orderColumn==null || orderColumn.length()<=0){
				orderColumn=id;		//默认取主键名
				isIndex=true;
			}
			/*******************条件*******************/
			StringBuilder mainConditions=new StringBuilder();		//主条件SQL
			//如果有传来的条件SQL参数
			if(conditionsSQL!=null){
				mainConditions.append(conditionsSQL);
				paramValueList=(List<Object>) conditionsList;	//使用传来的条件值
			//是否有条件参数
			}else{
				for(int i=0;paramRealList!=null && i<paramRealList.size();i++){
					String t_col_value_str = paramValueStrList.get(i);
					mainConditions.append(" and "+paramRealList.get(i)+"="+t_col_value_str);	//条件判断方式（完全匹配、模糊匹配）
				}
			}
			/*******************分页*******************/
			StringBuilder sql_id=new StringBuilder("select "+SQLUtil.table_alias+"."+id+" from "+table+" "+SQLUtil.table_alias);		//分页后的ID之SQL（带别名）
			String sort=isDesc?"desc":"asc";		//数据排序（默认：正序【顺序】）
//			String pageSort=sort;		//分页排序（默认：正序【顺序】）
			//分页时的2段顺序需要
			if(page!=null && page.getId2()!=null){
				mainConditions.append(" and "+id+">?");
				paramValueList.add(page.getId2());
				isIndex=false;
			}
			/*分组SQL*/
			if(groupSQL==null){
				if(groupColumn!=null){
					groupSQL=" group by "+groupColumn;
				}else{
					groupSQL="";
				}
			}else{
				groupSQL+=" group by "+groupSQL;
			}
			/*排序SQL*/
			if(orderSQL==null){
				orderSQL=" order by "+orderColumn+" "+sort;
			}else{
				orderSQL=" order by "+orderSQL;
			}
			int size=paramValueList.size();
			int totalSize=size;
			//分页
			if(page!=null){
				//超高效分页
				if(page.isEfficient()){
					long pageNew=page.getPageNew();
					long start=(Math.abs(pageNew)-1)*page.getLimit();	//起始位置
					boolean isPageAfter=true;		//是否后页，false=前页
					//降序
					if(isDesc){
						//前页
						if(pageNew>0){
							isPageAfter=true;
						//后页
						}else{
							isPageAfter=false;
						}
					//正序
					}else{
						//前页
						if(pageNew<0){
							isPageAfter=true;
						//后页
						}else{
							isPageAfter=false;
						}
					}
					String symbol=">";		//默认：正序
					if(isPageAfter){
						orderSQL=" order by "+orderColumn+" desc";
						symbol="<";
					}else{
						orderSQL=" order by "+orderColumn+" asc";
					}
					if(page.getId2()!=null){
						symbol+="=";
					}
					if(page.getId()!=null){
						mainConditions.append(" and "+orderColumn+symbol+"?");
						paramValueList.add(page.getId());
						//重新赋值条件数
						size=paramValueList.size();
						totalSize=size;
					}
					String whereSQL=TableUtil.getWhereSQL(mainConditions.toString());
					sql_id.append(whereSQL+groupSQL+orderSQL+(!isIndex?","+id+" asc":"")+" limit "+start+","+page.getLimit());
				//高效分页
				}else{
					String pageSQL=" limit 0,"+page.getLimit();
					//没有指定排序的列名（根据id）
					if(temp_orderColumn==null){
						totalSize=size*2;
						String t_mainConditions=mainConditions.toString().replaceAll(SQLUtil.table_alias+"\\.", SQLUtil.table_inner_alias+".");		//将用户使用的别名替换成内部别名
						String t_whereSQL=TableUtil.getWhereSQL(t_mainConditions.toString());
						StringBuilder sql_id_inner=new StringBuilder("select "+orderColumn+" from "+table+" "+SQLUtil.table_inner_alias+t_whereSQL+groupSQL+orderSQL+" limit "+(page.getBegin()-1)+",1");		//取所有ID的SQL
						mainConditions.append(" and "+orderColumn+(isDesc?"<":">")+"=("+sql_id_inner+")");		//条件
						String whereSQL=TableUtil.getWhereSQL(mainConditions.toString());
						sql_id.append(whereSQL+groupSQL+orderSQL+pageSQL);
					}else{
						String whereSQL=TableUtil.getWhereSQL(mainConditions.toString());
						sql_id=new StringBuilder("select "+id+" from "+table+" "+SQLUtil.table_alias+whereSQL+groupSQL+orderSQL+(!isIndex?","+id+" asc":"")+" limit "+(page.getBegin()-1)+","+page.getLimit());
					}
				}
			//不分页
			}else{
				String whereSQL=TableUtil.getWhereSQL(mainConditions.toString());
				sql_id.append(whereSQL+groupSQL+orderSQL);
			}
			/**************执行SQL，并处理************/
			TableUtil.close(rs, ps);
			ps=con.prepareStatement(sql_id.toString(),ResultSet.TYPE_SCROLL_INSENSITIVE); //不受更改底层数据更改影响
			for(int i=0;i<totalSize;i++){
				//条件数和占位符数相同
				if(size==totalSize){
					ps.setObject(i+1, paramValueList.get(i));
				}else{
					ps.setObject(i+1, paramValueList.get(i%size));
				}
			}
			rs =super.executeSQL(SQLType.SELECT,ps, sql_id.toString(), paramValueList);	//执行SQL
			List<Object> idList=new ArrayList<Object>();
			while(rs.next()){
				idList.add(rs.getObject(1));
			}
			if(idList.size()>0){
				String idPlaceholder=TableUtil.getPlaceholder(idList);
				/*****************拼接SQL*****************/
				StringBuilder sql=new StringBuilder("select "+sql_col.substring(0,sql_col.length()-1)+" from "+table);
				sql.append(" where "+id+" in("+idPlaceholder.toString()+") "+orderSQL);		//分开执行
				
				TableUtil.close(rs, ps);
				ps=con.prepareStatement(sql.toString(),ResultSet.TYPE_SCROLL_INSENSITIVE); //不受更改底层数据更改影响
				for(int i=0;i<idList.size();i++){
					ps.setObject(i+1, idList.get(i));
				}
				/**************执行************/
				rs =super.executeSQL(SQLType.SELECT,ps, sql.toString(), idList);	//执行SQL
				list=SelectUtil.getObject(tableClass, rs, propertyArr, col_label);		//获取查询结果后设置到映射对象的对象List集
				TableUtil.close(rs, ps);
				//非ID的数据重新排序
				if(orderColumnFieldName!=null){
					list=TableUtil.sort(list, idList);
				}
			}
		}catch (Exception e) {
			throw new TableSelectException(e.getMessage(),e);
		}finally{
			TableUtil.close(rs, ps);
		}
		return (List<T>) list;
	}
	
}