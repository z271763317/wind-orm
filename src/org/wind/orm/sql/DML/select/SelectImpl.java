package org.wind.orm.sql.DML.select;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wind.orm.Table;
import org.wind.orm.annotation.ForeignKey;
import org.wind.orm.bean.Page;
import org.wind.orm.bean.SQLType;
import org.wind.orm.exception.TableSaveException;
import org.wind.orm.exception.TableSelectException;
import org.wind.orm.sql.DML.Select;
import org.wind.orm.util.SQLUtil;
import org.wind.orm.util.TableUtil;

/**
 * @描述 : SQL【查询】接口抽象类
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年5月29日 09:46:56
 */
@SuppressWarnings("unchecked")
public abstract class SelectImpl implements Select{

	/*正则表达式*/
//	private static final Pattern pattern_digital = Pattern.compile("-?[0-9]+.*[0-9]*");		//正负数字（含：小数、整数）
	/*数学函数返回类型*/
	private static final int mathReturnType_Number=1;			//数字类型
	private static final int mathReturnType_String=2;			//字符串
	
	/**【子类方法】**/
	protected abstract <T> List<T> findTemp(Table obj,boolean isDesc,Page page,String conditionsSQL,List<? extends Object> conditionsList,Set<String> specifiedColumnSet);
	
	/**
	 * 查询：根据ORM的ID的1个对象
	 * @param id : 要查询的表其中的一行数据的主键值
	 */
	public <T> T findById(Object id){
		Table result=null;
		if(id!=null){
			Class<? extends Table> tableClass=this.getObj().getClass();
			/*设置主键值*/
			Field idField=TableUtil.getPrimaryKeyField(tableClass);		//获取主键字段
			if(idField==null) {
				throw new TableSelectException("表类"+tableClass.getSimpleName()+"没有设置主键Field字段");
			}
			id=TableUtil.cast(id, idField.getType());
			Map<String,Method> methodMap_set=TableUtil.getMethodMap_set(tableClass);		
			TableUtil.set(this.getObj(), methodMap_set, idField, id);
			List<Table> list=this.findTemp(this.getObj(),false,null);
			if(list.size()>0){
				result=list.get(0);
			}
		}
		return (T) result;
	}
	
	/**
	 * 查询：根据ORM的所有对象数据，List装入，带Page分页对象
	 * @param isDesc : 是否降序，false=正序
	 * @param page : 分页对象
	 */
	public <T> List<T> findAll(boolean isDesc,Page page){
		List<Table> result=new ArrayList<Table>();
		if(result==null || result.size()<=0){
			result=this.findTemp(this.getObj(),isDesc,page);
		}
		return (List<T>) result;
	}
	/**
	 * 查询：根据对象属性的值（为null不参与） 或 传来的条件语句（优先）
	 * @param isDesc : 是否降序，false=正序
	 * @param page : 分页对象
	 * @param conditionsSQL : 条件SQL语句
	 * @param conditionsList : 条件值
	 */
	public <T> List<T> find(boolean isDesc,Page page,String conditionsSQL,List<? extends Object> conditionsList){
		return this.findTemp(this.getObj(),isDesc,page,conditionsSQL,conditionsList,null);
	}
	/**
	 * 查询：根据传来的id列表，List装入（Oracle有单独的）
	 * @param idList : 主键列表值
	 * @param isContain : 是否查询指定idList的数据，false=查询idList以外的数据
	 */
	public <T> List<T> findByIdList(List<? extends Object> idList,boolean isContain){
		List<Table> resultList=new ArrayList<Table>();
		if(idList!=null && idList.size()>0){	//判断是否属同一个类
			List<Object> t_idList=new ArrayList<Object>();
			Class<? extends Table> tableClass=this.getObj().getClass();
			Field idField=TableUtil.getPrimaryKeyField(tableClass);
			Class<?> idClass=idField.getType();
			for(int i=0;i<idList.size();i++){
				Object id=idList.get(i);
				if(idClass==String.class){
					id=id+"";
				}else{
					if(id.getClass()==String.class){
						id=Long.parseLong(id+"");	//转成Long类型
					}
					id=TableUtil.cast(id, idClass);
				}
				t_idList.add(id);
			}
			if(t_idList.size()>0){
//				String table=this.getTable(tableClass);	//主表（真实）
				String id=TableUtil.getPrimaryKey(tableClass);	//主键（真实）
				//没有设置主键
				if(id==null){
					throw new TableSelectException("该ORM类还没有设置主键字段，请前往对应ORM类查看");
				}
				StringBuilder conditionsSQL=new StringBuilder(this.getPack(id));
				StringBuilder idPerchSQL=new StringBuilder();	//id占位符SQL
				//包含
				if(isContain){
					conditionsSQL.append(" in(");	//存在
				}else{
					conditionsSQL.append(" not in(");	//不存在
				}
				for(int i=0;i<t_idList.size();i++){
					idPerchSQL.append("?");
					if(i<t_idList.size()-1){
						idPerchSQL.append(",");
					}
				}
				if(idPerchSQL.length()<=0){
					throw new TableSaveException("缺少主键列表值参数——'idList'");
				}
				conditionsSQL.append(idPerchSQL+")");
				resultList=this.findTemp(this.getObj(),false,null,conditionsSQL.toString(),t_idList,null);
				resultList=TableUtil.sort(resultList, t_idList);		//重新排序
			}
		}
		return (List<T>) resultList;
	}
	/**查询 : 根据ORM的所有数据总数**/
	public long findAllSize() {
		Connection con=this.getConnection();
		PreparedStatement ps=null;
		ResultSet rs=null;
		long result=0;
		try{
			String table=this.getTable();
			String sql="select count(*) from "+table;
			ps=con.prepareStatement(sql);
			rs=this.executeSQL(SQLType.SELECT,ps, sql.toString(), null);
			while(rs.next()){
				result=rs.getLong(1);
			}
		}catch (Exception e) {
			throw new TableSelectException(e.getMessage(),e);
		}finally{
			TableUtil.close(rs, ps);
		}
		return result;
	}
	
	/**
	 * 查询总数 : 根据ORM的字段是否存在值来加条件，有条件SQL则优先
	 * @param conditionsSQL : 条件SQL
	 * @param conditionsList : 条件值 
	 */
	public long findSize(String conditionsSQL,List<? extends Object> conditionsList){
		Connection con=this.getConnection();
		PreparedStatement ps=null;
		ResultSet rs=null;
		long result=0;
		try{
			Class<? extends Table> tableClass=this.getObj().getClass();
			String table=this.getTable();
			StringBuilder sql=new StringBuilder("select count(*) from "+table+" "+SQLUtil.table_alias);
			
			Map<String,Object> conditionsRelatedMap=this.getConditionsRelated(con,tableClass);	//SQL条件相关变量
			List<String> paramRealList=(List<String>) conditionsRelatedMap.get("paramRealList");	//条件【字段】列表（真实）
			List<Object> paramValueList=(List<Object>) conditionsRelatedMap.get("paramValueList");	//条件【字段值】列表
			
			//条件为空则不查询
			if(conditionsSQL!=null || (paramRealList!=null && paramRealList.size()>0)){
				/*******************条件*******************/
				String whereSQL=this.getWhereSQL(conditionsSQL, conditionsList, paramRealList, paramValueList);
				sql.append(whereSQL);
				ps=con.prepareStatement(sql.toString());
				for(int i=0;i<paramValueList.size();i++){
					ps.setObject(i+1, paramValueList.get(i));
				}
				rs=this.executeSQL(SQLType.SELECT,ps, sql.toString(), paramValueList);
				while(rs.next()){
					result=rs.getLong(1);
				}
			}
		}catch (Exception e) {
			throw new TableSelectException(e.getMessage(),e);
		}finally{
			TableUtil.close(rs, ps);
		}
		return result;
	}
	/**
	 * 查询 : 总数，ORM类字段名映射的列名——Map返回式
	 * @param conditionsSQL : 条件SQL
	 * @param conditionsList : 条件值
	 * @param groupFieldName : ORM类字段名映射的列名。做分组列，查询时以groupFieldName映射的列名做分组的列名
	 */
	public <T> Map<T,Long> findSizeMap(String conditionsSQL,List<? extends Object> conditionsList,String groupFieldName){
		Connection con=this.getConnection();
		PreparedStatement ps=null;
		ResultSet rs=null;
		Map<T,Long> resultMap=new HashMap<T, Long>();		//返回Map
		try{
			Class<? extends Table> tableClass=this.getObj().getClass();
			String table=this.getTable();
			Field field=TableUtil.getField(tableClass, groupFieldName);
			if(field==null) {
				throw new TableSelectException("字段【"+groupFieldName+"】不存在");
			}
			Class<?> groupFieldClass=field.getType();
			
			Map<String,Object> conditionsRelatedMap=this.getConditionsRelated(con,tableClass,groupFieldName);	//SQL条件相关变量
			List<String> paramRealList=(List<String>) conditionsRelatedMap.get("paramRealList");	//条件【字段】列表（真实）
			List<Object> paramValueList=(List<Object>) conditionsRelatedMap.get("paramValueList");	//条件【字段值】列表
			String specifiedColumn=(String) conditionsRelatedMap.get("specifiedColumn");	//指定的列名
			
			//条件为空则不查询
			if(conditionsSQL!=null || (paramRealList!=null && paramRealList.size()>0)){
				//如果有分组列
				if(specifiedColumn!=null && specifiedColumn.length()>0){
					StringBuilder sql=new StringBuilder("select "+this.getPack(specifiedColumn)+",count(*) from "+table+" "+SQLUtil.table_alias);
					/*******************条件*******************/
					String whereSQL=this.getWhereSQL(conditionsSQL, conditionsList, paramRealList, paramValueList);
					sql.append(whereSQL);
					sql.append(" group by "+this.getPack(specifiedColumn));	//分组列
					ps=con.prepareStatement(sql.toString());
					for(int i=0;i<paramValueList.size();i++){
						ps.setObject(i+1, paramValueList.get(i));
					}
					rs=this.executeSQL(SQLType.SELECT,ps, sql.toString(), paramValueList);
					while(rs.next()){
						Object t_groupCol_value=rs.getObject(1);		//分组
						long t_size=rs.getLong(2);
						resultMap.put((T) TableUtil.cast(t_groupCol_value, groupFieldClass), t_size);
					}
					TableUtil.close(rs, ps);
				}else{
					throw new TableSelectException("指定的分组列不存在");
				}
			}
		}catch (Exception e) {
			throw new TableSelectException(e.getMessage(),e);
		}finally{
			TableUtil.close(rs, ps);
		}
		return resultMap;
	}
	/**获取当前日期时间（根据不同数据库的sql）**/
	public String findDate(){
		return SQLUtil.findDate(this.getSqlVar());
	}
	/**查询 : 主键列表**/
	public <T> List<T> findIdList(String conditionsSQL,List<? extends Object> conditionsList){
		Connection con=this.getConnection();
		PreparedStatement ps=null;
		ResultSet rs=null;
		List<T> idList=new ArrayList<T>();
		try{
			Class<? extends Table> tableClass=this.getObj().getClass();
			String table=this.getTable();
			String id=TableUtil.getPrimaryKey(tableClass);
			Field idField=TableUtil.getPrimaryKeyField(tableClass);
			StringBuilder sql=new StringBuilder("select "+id+" from "+table+" "+SQLUtil.table_alias);
			
			Map<String,Object> conditionsRelatedMap=this.getConditionsRelated(con,tableClass);	//SQL条件相关变量
			List<String> paramRealList=(List<String>) conditionsRelatedMap.get("paramRealList");	//条件【字段】列表（真实）
			List<Object> paramValueList=(List<Object>) conditionsRelatedMap.get("paramValueList");	//条件【字段值】列表
			
			//条件不存在，则不查询
			if(conditionsSQL!=null || (paramRealList!=null && paramRealList.size()>0)){
				/*******************条件*******************/
				String whereSQL=this.getWhereSQL(conditionsSQL, conditionsList, paramRealList, paramValueList);
				sql.append(whereSQL);
				ps=con.prepareStatement(sql.toString());
				for(int i=0;i<paramValueList.size();i++){
					ps.setObject(i+1, paramValueList.get(i));
				}
				rs=this.executeSQL(SQLType.SELECT,ps, sql.toString(), paramValueList);
				while(rs.next()){
					Object t_id=rs.getObject(1);
					if(t_id!=null){
						idList.add((T)(TableUtil.cast(t_id, idField.getType())));
					}
				}
			}
		}catch (Exception e) {
			throw new TableSelectException(e.getMessage(),e);
		}finally{
			TableUtil.close(rs, ps);
		}
		return idList;
	}
	/**
	 * 查询 : 指定列的值列表
	 * @param specifiedFieldName : 指定列名映射的字段名
	 */
	public <T> List<T> findSpecifiedList(String conditionsSQL,List<? extends Object> conditionsList,String specifiedFieldName){
		Connection con=this.getConnection();
		PreparedStatement ps=null;
		ResultSet rs=null;
		List<T> idList=new ArrayList<T>();
		try{
			Class<? extends Table> tableClass=this.getObj().getClass();
			String table=this.getTable();
			
			Map<String,Object> conditionsRelatedMap=this.getConditionsRelated(con,tableClass,specifiedFieldName);	//SQL条件相关变量
			List<String> paramRealList=(List<String>) conditionsRelatedMap.get("paramRealList");	//条件【字段】列表（真实）
			List<Object> paramValueList=(List<Object>) conditionsRelatedMap.get("paramValueList");	//条件【字段值】列表
			String specifiedColumn=(String) conditionsRelatedMap.get("specifiedColumn");	//指定返回数据的列名
			
			//条件不存在，则不查
			if(conditionsSQL!=null || (paramRealList!=null && paramRealList.size()>0)){
				if(specifiedColumn==null){
					specifiedColumn=TableUtil.getPrimaryKey(tableClass);
				}
				StringBuilder sql=new StringBuilder("select "+specifiedColumn+" from "+table+" "+SQLUtil.table_alias);
				/*******************条件*******************/
				String whereSQL=this.getWhereSQL(conditionsSQL, conditionsList, paramRealList, paramValueList);
				sql.append(whereSQL);
				ps=con.prepareStatement(sql.toString());
				for(int i=0;i<paramValueList.size();i++){
					ps.setObject(i+1, paramValueList.get(i));
				}
				rs=this.executeSQL(SQLType.SELECT,ps, sql.toString(), paramValueList);
				while(rs.next()){
					Object resultValue=rs.getObject(1);
					if(resultValue!=null){
						idList.add((T) resultValue);
					}
				}
			}
		}catch (Exception e) {
			throw new TableSelectException(e.getMessage(),e);
		}finally{
			TableUtil.close(rs, ps);
		}
		return idList;
	}
	/**
	 * 查询 : 根据传来的SQL（返回的最顶层的List为行，Map为每一行所有的列名【key】和值【value】）
	 * @param sql : SQL语句
	 * @param conditionsList : 条件里占位符对应位置的值
	 */
	public List<Map<String,Object>> find(String sql,List<? extends Object> conditionsList){
		Connection con=this.getConnection();
		PreparedStatement ps=null;
		ResultSet rs=null;
		List<Map<String,Object>> resultLMapList=new ArrayList<Map<String,Object>>();
		try{
			ps=con.prepareStatement(sql);
			for(int i=0;conditionsList!=null && i<conditionsList.size();i++){
				ps.setObject(i+1, conditionsList.get(i));
			}
			rs=this.executeSQL(SQLType.SELECT,ps, sql, conditionsList);
			
			ResultSetMetaData rsmd = rs.getMetaData();
		    int numberOfColumns = rsmd.getColumnCount();
//		    boolean b = rsmd.isSearchable(1);		//指定的列，能否出现在where字句中
			while(rs.next()){
				Map<String,Object> t_map=new HashMap<String, Object>();
				for(int i=0;i<numberOfColumns;i++){
					String t_showColumn=rsmd.getColumnLabel(i+1);
					Object resultValue=rs.getObject(i+1);
					t_map.put(t_showColumn, resultValue);
				}
				if(t_map.size()>0){
					resultLMapList.add(t_map);
				}
			}
		}catch (Exception e) {
			throw new TableSelectException(e.getMessage(),e);
		}finally{
			TableUtil.close(rs, ps);
		}
		return resultLMapList;
	}
	
	/*****************************【数学函数】*****************************/
	/**求和**/
	public Number sum(String conditionsSQL,List<? extends Object> conditionsList,String fieldName){
		return this.mathTemp_single(mathReturnType_Number, "sum", conditionsSQL, conditionsList, fieldName);
	}
	/**求和：根据指定groupFieldName分组字段**/
	public <T> Map<T,Number> sum(String conditionsSQL,List<? extends Object> conditionsList,String fieldName,String groupFieldName){
		return this.mathTemp(mathReturnType_Number, "sum",conditionsSQL, conditionsList, fieldName, groupFieldName);
	}
	/**求平均**/
	public Number avg(String conditionsSQL,List<? extends Object> conditionsList,String fieldName){
		return this.mathTemp_single(mathReturnType_Number, "avg", conditionsSQL, conditionsList, fieldName);
	}
	/**求平均：根据指定groupFieldName分组字段**/
	public <T> Map<T,Number> avg(String conditionsSQL,List<? extends Object> conditionsList,String fieldName,String groupFieldName){
		return this.mathTemp(mathReturnType_Number, "avg",conditionsSQL, conditionsList, fieldName, groupFieldName);
	}
	/**求最大值**/
	public Number max(String conditionsSQL,List<? extends Object> conditionsList,String fieldName){
		return this.mathTemp_single(mathReturnType_Number, "max", conditionsSQL, conditionsList, fieldName);
	}
	/**求最大值（字符串）**/
	public String maxString(String conditionsSQL,List<? extends Object> conditionsList,String fieldName){
		return this.mathTemp_single(mathReturnType_String, "max", conditionsSQL, conditionsList, fieldName);
	}
	/**求最大值：根据指定groupFieldName分组字段**/
	public <T> Map<T,Number> max(String conditionsSQL,List<? extends Object> conditionsList,String fieldName,String groupFieldName){
		return this.mathTemp(mathReturnType_Number,"max",conditionsSQL, conditionsList, fieldName, groupFieldName);
	}
	/**求最大值：根据指定groupFieldName分组字段（字符串）**/
	public <T> Map<T,String> maxString(String conditionsSQL,List<? extends Object> conditionsList,String fieldName,String groupFieldName){
		return this.mathTemp(mathReturnType_String,"max",conditionsSQL, conditionsList, fieldName, groupFieldName);
	}
	/**求最小值**/
	public Number min(String conditionsSQL,List<? extends Object> conditionsList,String fieldName){
		return this.mathTemp_single(mathReturnType_Number, "min", conditionsSQL, conditionsList, fieldName);
	}
	/**求最小值（字符串）**/
	public String minString(String conditionsSQL,List<? extends Object> conditionsList,String fieldName){
		return this.mathTemp_single(mathReturnType_String, "min", conditionsSQL, conditionsList, fieldName);
	}
	/**求最小值：根据指定groupFieldName分组字段**/
	public <T> Map<T,Number> min(String conditionsSQL,List<? extends Object> conditionsList,String fieldName,String groupFieldName){
		return this.mathTemp(mathReturnType_Number,"min",conditionsSQL, conditionsList, fieldName, groupFieldName);
	}
	/**求最小值：根据指定groupFieldName分组字段（字符串）**/
	public <T> Map<T,String> minString(String conditionsSQL,List<? extends Object> conditionsList,String fieldName,String groupFieldName){
		return this.mathTemp(mathReturnType_String,"min",conditionsSQL, conditionsList, fieldName, groupFieldName);
	}
	/*-------------------------------【本地方法】-------------------------------*/
	/**查询：临时通用方法（1）**/
	protected <T> List<T> findTemp(Table obj,boolean isDesc,Page page){
		return this.findTemp(obj,isDesc,page,null,null,null);
	}
	//获取 : SQL条件相关变量——specifiedFieldName=字段名，返回映射的列名
	private Map<String,Object> getConditionsRelated(Connection con,Class<? extends Table> tableClass,String... specifiedFieldNameArr) throws SQLException{
		Map<String,Object> resultMap=new HashMap<String, Object>();
		Field propertyArr[]=TableUtil.getField(tableClass);
		Map<String,Method> methodMap_get=TableUtil.getMethodMap_get(tableClass);	//获取该Class所有的Method【get】
		Set<String> colSet=SQLUtil.getColumn_Database( this);	//表所有的字段（真实数据库表字段）
		Set<String> specifiedFieldNameSet=new HashSet<String>();
		for(int i=0;specifiedFieldNameArr!=null && i<specifiedFieldNameArr.length;i++){
			String t_specifiedFieldName=specifiedFieldNameArr[i];
			if(t_specifiedFieldName!=null){
				specifiedFieldNameSet.add(t_specifiedFieldName.toLowerCase());
			}
		}
		/*****返回的变量*****/
		List<String> paramRealList=new ArrayList<String>();	//条件【字段】列表（真实）
		List<Object> paramValueList=new ArrayList<Object>();	//条件【字段值】列表
		Set<String> specifiedColumnSet=new LinkedHashSet<String>();		//指定的列名列表
		for(int i=0;i<propertyArr.length;i++){
			Field f=propertyArr[i];
			String col=TableUtil.getColumn(f);	//当前层的字段（真实字段）
			//数据库存在该字段
			if(colSet.contains(col.toLowerCase()) || f.isAnnotationPresent(ForeignKey.class)){
				Object value=TableUtil.get(this.getObj(), methodMap_get, f);
				Class<?> genericClass=null;		//List泛型的Class
				//外键
				if(f.isAnnotationPresent(ForeignKey.class)){
					col=TableUtil.getForeignKey(f);	//外键真实列名
					value=TableUtil.getPrimaryKeyValue(value);
				}
				//不加入非“一对多”外键
				if(genericClass==null && value!=null){
					paramRealList.add(col);
					paramValueList.add(value);
				}
				//指定列
				if(specifiedFieldNameSet.contains(f.getName().toLowerCase())){
					specifiedColumnSet.add(col);
				}
			}
		}
		/***返回值***/
		resultMap.put("paramRealList", paramRealList);
		resultMap.put("paramValueList", paramValueList);
		resultMap.put("specifiedColumnSet", specifiedColumnSet);
		if(specifiedColumnSet.size()>0){
			String specifiedColumn=specifiedColumnSet.iterator().next();
			resultMap.put("specifiedColumn", specifiedColumn);		//取第1个
		}
		return resultMap;
	}
	//获取 : 条件SQL
	private String getWhereSQL(String conditionsSQL,List<? extends Object> conditionsList,List<String> paramRealList,List<Object> paramValueList){
		StringBuilder mainConditions=new StringBuilder();		//主条件SQL
		//如果有传来的条件SQL参数
		if(conditionsSQL!=null && conditionsSQL.trim().length()>0){
			mainConditions.append(conditionsSQL);
			paramValueList.clear();
			paramValueList.addAll(conditionsList);	//使用传来的条件值
		//是否有条件参数
		}else{
			for(int i=0;paramRealList!=null && i<paramRealList.size();i++){
				String t_col=paramRealList.get(i);
				mainConditions.append(" and "+this.getPack(t_col)+"=?");	//条件判断方式（完全匹配、模糊匹配）
			}
		}
		String whereSQL=TableUtil.getWhereSQL(mainConditions.toString());
		return whereSQL;
	}
	/**
	 * 【通用】数据函数：根据传入的条件，指定分组字段，指定fieldName做数学函数处理的字段
	 * @param returnType : 返回值类型（查看本类的：【mathReturnType_】开头的变量）
	 * @param mathName : 数学函数名称（如：sum、avg）
	 * @param conditionsSQL : 条件SQL
	 * @param conditionsList : 条件占位符值
	 * @param fieldName : 指定做数学函数处理的字段名称
	 * @param groupFieldName : 指定分组的字段名称
	 * @return 返回Map<T, S>类型，S处，若为Number类型可直接调用对应的方法获取对应的返回值，如：【Number对象】.longValue();<br />
	*/
	private <T,S>Map<T, S> mathTemp(int returnType,String mathName,String conditionsSQL,List<? extends Object> conditionsList,String fieldName,String groupFieldName){
		Map<T,S> resultMap=new HashMap<T, S>();
		if(mathName!=null && mathName.trim().length()>0){
			mathName=mathName.trim();
			Connection con=this.getConnection();
			PreparedStatement ps=null;
			ResultSet rs=null;
			try{
				Class<? extends Table> tableClass=this.getObj().getClass();
				Field field=TableUtil.getField(tableClass, fieldName);
				if(field!=null){
					String t_column=null;
					//外键
					if(field.isAnnotationPresent(ForeignKey.class)){
						t_column=TableUtil.getForeignKey(field);
					}else{
						t_column=TableUtil.getColumn(field);
					}
					if(t_column!=null){
						String t_groupColumn=null;		//分组列名
						Field t_groupField=null;		//分组字段
						if(groupFieldName!=null){
							t_groupField=TableUtil.getField(tableClass, groupFieldName);
							//外键
							if(t_groupField.isAnnotationPresent(ForeignKey.class)){
								t_groupColumn=TableUtil.getForeignKey(t_groupField);
								t_groupField=TableUtil.getPrimaryKeyField(t_groupField.getType());
							}else{
								t_groupColumn=TableUtil.getColumn(t_groupField);
							}
						}
						//
						String t_table=this.getTable();
						StringBuilder sql_showColumn=new StringBuilder();
						StringBuilder sql_group=new StringBuilder();
						//分组列
						if(t_groupColumn!=null){
							String t_groupColumn_pack=this.getPack(t_groupColumn);
							sql_group.append(" group by "+SQLUtil.table_alias+"."+t_groupColumn_pack);
							sql_showColumn.append(SQLUtil.table_alias+"."+t_groupColumn_pack+",");
						}
						sql_showColumn.append(mathName+"("+SQLUtil.table_alias+"."+this.getPack(t_column)+") as 'wind_t_math'");
						StringBuilder sql=new StringBuilder("select "+sql_showColumn+" from "+t_table+" "+SQLUtil.table_alias);
						//有条件
						if(conditionsSQL!=null){
							sql.append(" where "+conditionsSQL);
						}
						sql.append(sql_group);
						//
						ps=con.prepareStatement(sql.toString());
						for(int i=0;conditionsList!=null && i<conditionsList.size();i++){
							ps.setObject(i+1, conditionsList.get(i));
						}
						rs=this.executeSQL(SQLType.SELECT,ps, sql.toString(),conditionsList );
						while(rs.next()){
							Object t_result=rs.getObject("wind_t_math");
							if(t_result==null){
								//默认值
								switch(returnType){
									case mathReturnType_Number :t_result=0;break;
								}
							}
							if(t_result!=null){
								//返回类型
								switch(returnType){
									case mathReturnType_Number :{
										//不是数字型
										if(!(t_result instanceof Number)){
											t_result=t_result.toString().trim();		//左右去空
											if(t_result.toString().length()<=0){
												t_result=0;
											}
											t_result=new BigDecimal(t_result.toString());
										}
										break;
									}
									default:{
										t_result=t_result.toString();
									}
								}
								Object t_t_groupColumnValue=1;		//默认
								//有分组
								if(t_groupColumn!=null){
									t_t_groupColumnValue=TableUtil.cast(rs.getObject(t_groupColumn),t_groupField.getType());
								}
								resultMap.put((T)t_t_groupColumnValue, (S)t_result);	//默认
							}
						}
						TableUtil.close(rs, ps);
					}else{
						throw new TableSelectException("指定的字段【"+fieldName+"】映射的列名不存在2");
					}
				}else{
					throw new TableSelectException("指定的字段【"+fieldName+"】映射的列名不存在1");
				}
			}catch (Exception e) {
				throw new TableSelectException(e.getMessage(),e);
			}finally{
				TableUtil.close(rs, ps);
			}
			return (Map<T, S>) resultMap; 
		}else{
			throw new TableSelectException("未指定要使用的数学函数");
		}
	}
	//返回单值
	private <S> S mathTemp_single(int returnType,String mathName,String conditionsSQL,List<? extends Object> conditionsList,String fieldName){
		Map<Object,S> t_map=this.mathTemp(returnType, mathName,conditionsSQL, conditionsList, fieldName, null);
		if(t_map.containsKey(1)){
			return (S) t_map.get(1);
		}else{
			return null;
		}
	}
}