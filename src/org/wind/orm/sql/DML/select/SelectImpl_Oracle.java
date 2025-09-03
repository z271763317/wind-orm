package org.wind.orm.sql.DML.select;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.wind.orm.util.SelectUtil_oracle;
import org.wind.orm.util.TableUtil;

/**
 * @描述 : SQL【查询】接口实现类——Oracle
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年10月13日 17:24:23
 */
@SuppressWarnings("unchecked")
public class SelectImpl_Oracle extends SelectImpl {

	private int tableLabelIndex = 0; // 当前表标签名新位置
	private int colLabelIndex = 0; // 当前列标签名新位置
	private String rowNumLabel = "oracle_rowNum"; // 分页标签名——行号

	/*-------------------------------【本地方法】-------------------------------*/
	/**
	 * 查询：临时通用方法
	 * @param obj : ORM对象
	 * @param isDesc : 是否降序，false=正序
	 * @param page : Page分页对象
	 * @param conditionsSQL : 条件SQL语句
	 * @param conditionsList : 条件值
	 */
	protected <T> List<T> findTemp(Table obj, boolean isDesc,Page page, String conditionsSQL,List<? extends Object> conditionsList,Set<String> specifiedColumnSet) {
		Connection con = this.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<? extends Table> list=new ArrayList<Table>();		//查询后返回的结果集列表
		List<String> paramRealList = new ArrayList<String>(); // 条件【字段】列表（真实）
		List<Object> paramValueList = new ArrayList<Object>(); // 条件【字段值】列表
		List<String> paramValueStrList = new ArrayList<String>(); // 条件【字段值】字符列表
		specifiedColumnSet=specifiedColumnSet!=null?specifiedColumnSet:obj.getSpecifiedColumnSet();
		try {
			Class<? extends Table> tableClass = obj.getClass();
			Field propertyArr[] = TableUtil.getField(tableClass);
			Map<String,Method> methodMap=TableUtil.getMethodMap_get(tableClass);		//获取该Class所有的Method【get】
			String table = this.getTable(); // 主表实际名称
			Set<String> colSet = SQLUtil.getColumn_Database(this); // 表所有的字段（真实数据库表字段）
			Map<String, String> tableLabelMap = new HashMap<String, String>(); // 所有表标签名
			Map<String, String> colLabelMap = new HashMap<String, String>(); // 所有列标签名
			String orderColumnFieldName=super.getObj().getOrderFieldName();		//指定排序的列映射的字段名
			String orderSQL=super.getObj().getOrderSQL();		//指定排序的SQL
			String groupColumnFieldName=super.getObj().getGroupFieldName();		//指定分组的列映射的字段名
			String groupSQL=super.getObj().getGroupSQL();	//指定分组的SQL
			String orderColumn=null;		//排序的列名
			String groupColumn=null;	//分组的列名
			String sql_rowNum = null; // 行号SQL
			String id_label = null; // 主键标签名
			String first_label = null; // 首个列标签名

			/******************************** 拼接SQL ****************************/
			String old_table_label = SelectUtil.getTableLabel(tableClass); // 旧表标签名
			String old_col_label = SelectUtil.getColumnLabel(tableClass); // 旧列标签名
			String table_label = SelectUtil_oracle.getTableLabel(old_table_label,++tableLabelIndex, tableLabelMap); // 主表自定义的表标签名
			StringBuffer sql = new StringBuffer("select * from (select "+ table_label + ".*,");
			StringBuffer sql_inner = new StringBuffer("select "); // 内sql,from后面的表数据
			
			for (int i = 0; i < propertyArr.length; i++) {
				Field f = propertyArr[i];
				String col = TableUtil.getColumn(f); // 当前层的字段（真实字段）
				// 数据库存在该字段
				if (colSet.contains(col.toLowerCase()) || f.isAnnotationPresent(ForeignKey.class)) {
					String t_col_label = null; // 当前层的字段的标签名;
					Object value = TableUtil.get(obj, methodMap, f);
					Class<?> genericClass = null; // List泛型的Class
					String t_value_str=TableUtil.getColumnValueStr(f);
					// 外键
					if (f.isAnnotationPresent(ForeignKey.class)) {
						col = TableUtil.getForeignKey(f); // 外键真实列名
						genericClass = TableUtil.getGeneric(f);
						value = TableUtil.getPrimaryKeyValue(value);
						//
						if (!List.class.isAssignableFrom(f.getType())) {
							String f_col_label = old_col_label + "_"+ f.getName(); // 当前层表的列签名
							Field f_idField = TableUtil.getPrimaryKeyField(f.getType());
							t_col_label = SelectUtil_oracle.getColumnLabel(f_col_label+ "_" + f_idField.getName(),++colLabelIndex, colLabelMap); // 当前层的字段的标签名;
						}
					} else {
						t_col_label = SelectUtil_oracle.getColumnLabel(old_col_label + "_"+ f.getName(), ++colLabelIndex, colLabelMap); // 当前层的字段的标签名;
					}
					/*排序的列名*/
					if(orderColumn==null){
						//优先判断临时指定的排序字段
						if(orderColumnFieldName!=null){
							if(orderColumnFieldName.equalsIgnoreCase(f.getName())){
								orderColumn=t_col_label;
							}
						//默认指定的排序字段
						}else if(f.isAnnotationPresent(OrderBy.class)){
							orderColumn=t_col_label;
						}
					}
					/*分组的列名*/
					if(groupColumn==null){
						//优先判断临时指定的分组字段
						if(groupColumnFieldName!=null){
							if(groupColumnFieldName.equalsIgnoreCase(f.getName())){
								groupColumn=t_col_label;
							}
						}
					}
					/*加入显示的列名（“一对多”外键除外）*/
					if(genericClass == null) {
						/***判断是否显示的列名（根据传入的字段名称）***/
						//非主键的进入
						if(!f.isAnnotationPresent(Id.class)){
							//若有传指定显示的列（根据ORM类的字段名称），并且当前字段不匹配，则结束本次循环
							if(specifiedColumnSet!=null && specifiedColumnSet.size()>0 && !specifiedColumnSet.contains(f.getName())){
								continue;
							}
						}
						sql_inner.append(this.getPack(col) + " as "+ t_col_label + ",");
						value = TableUtil.isNull(f, value); // 判断是否空值，并返回处理后的值
						if (value!= null) {
							paramRealList.add(this.getPack(col));
							paramValueList.add(value);
							paramValueStrList.add(t_value_str);
						}
						if (f.isAnnotationPresent(Id.class)) {
							id_label = t_col_label;
						}
						if(first_label==null){
							first_label=t_col_label;
						}
					}
				}
			}
			//orderColumn为空，则先取主键。主键不存在，则取第一个列
			if(orderColumn==null || orderColumn.length()<=0){
				orderColumn=id_label;		//默认取主键名
				if(orderColumn==null || orderColumn.length()<=0){
					orderColumn=first_label;
				}
			}
			/*分组SQL*/
			if(groupSQL==null){
				if(groupColumn!=null){
					groupSQL=" group by "+groupColumn+","+sql_inner.substring(0, sql_inner.length() - 1) ;
				}else{
					groupSQL="";	
				}
			}else{
				groupSQL+=" group by "+groupSQL;
			}
			/*排序SQL、分页列*/
			String sort = "asc";
			if (isDesc) {
				sort = "desc";
			}
			if(orderSQL==null){
				orderSQL="order by "+table_label+"."+orderColumn+" "+sort;
			}else{
				orderSQL="order by "+orderSQL;
			}
			sql_rowNum = "dense_rank() over("+orderSQL+") as "+ rowNumLabel;
			
			/****************************** 拼接SQL——条件 ***************************/
			StringBuilder mainConditions = new StringBuilder(); // 主条件SQL
			// 如果有传来的条件SQL参数
			if (conditionsSQL != null) {
				mainConditions.append(conditionsSQL);
				paramValueList = (List<Object>) conditionsList; // 使用传来的条件值
				// 是否有条件参数
			} else {
				for (int i = 0; paramRealList != null && i < paramRealList.size(); i++) {
					String t_col_value_str = paramValueStrList.get(i);
					mainConditions.append(" and "+paramRealList.get(i)+"="+t_col_value_str);	//条件判断方式（完全匹配、模糊匹配）
				}
			}
			/****************************** 拼接SQL——分页 ***************************/
			StringBuffer pageSQL = new StringBuffer();
			if (page != null) {
				pageSQL.append(" where " + table_label + "." + rowNumLabel+ ">=" + page.getBegin() + " and " + table_label + "."+ rowNumLabel + "<=" + page.getEnd());
			}
			/***************** 拼接SQL *****************/
			String whereSQL=TableUtil.getWhereSQL(mainConditions.toString());
			sql_inner = new StringBuffer("("+ sql_inner.substring(0, sql_inner.length() - 1) + " from "+ table + " " +SQLUtil.table_alias+ whereSQL + groupSQL+")");
			sql.append(sql_rowNum + " from " + sql_inner + " " + table_label);
			sql.append(") " + table_label);
			sql.append(pageSQL);
			/*********************************** 执行SQL，并处理 *****************************/
			// 是否带参数条件，或者是否查询全部
			TableUtil.close(rs, ps);
			ps = con.prepareStatement(sql.toString().toUpperCase());
			for (int i = 0; i < paramValueList.size(); i++) {
				ps.setObject(i + 1, paramValueList.get(i));
			}
			rs = super.executeSQL(SQLType.SELECT,ps, sql.toString(), paramValueList);
			list = SelectUtil_oracle.getObject(tableClass, rs, propertyArr, old_col_label,colLabelMap); // 获取查询结果后设置到映射对象的对象List集s
		} catch (Exception e) {
			throw new TableSelectException(e.getMessage(), e);
		} finally {
			TableUtil.close(rs, ps);
		}
		return (List<T>) list;
	}

}