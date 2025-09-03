package org.wind.orm.bean;

import java.util.HashSet;
import java.util.Set;

/**
 * @描述 : Table指定的（临时更改指定的配置，如：数据源、表名、显示的字段、排序字段、分组字段等）
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年8月26日 17:14:28
 */
public class TableSpecify {

	private String table;							//指定【表名】处理
	private String orderFieldName;		//指定【排序】的列名映射的字段名
	private String orderSQL;					//指定【排序】的SQL（优先）
	private String groupFieldName;		//指定【分组】的列名映射的字段名
	private String groupSQL;					//指定【分组】的SQL（优先）
	private Set<String> specifiedColumnSet;	//指定【显示】的列名映射的字段名称
	
	public String getTable() {
		return table;
	}
	public void setTable(String table) {
		this.table = table;
	}
	public String getOrderFieldName() {
		return orderFieldName;
	}
	public String getGroupFieldName() {
		return groupFieldName;
	}
	public Set<String> getSpecifiedColumnSet() {
		return specifiedColumnSet;
	}
	/**orderFieldName=ORM类的字段（成员属性）名称**/
	public void setOrderFieldName(String orderFieldName) {
		this.orderFieldName = orderFieldName;
	}
	/**groupFieldName=ORM类的字段（成员属性）名称**/
	public void setGroupFieldName(String groupFieldName) {
		this.groupFieldName = groupFieldName;
	}
	public String getOrderSQL() {
		return orderSQL;
	}
	public void setOrderSQL(String orderSQL) {
		this.orderSQL = orderSQL;
	}
	public String getGroupSQL() {
		return groupSQL;
	}
	public void setGroupSQL(String groupSQL) {
		this.groupSQL = groupSQL;
	}
	/**specifiedFieldNameArr=ORM类的字段（成员属性）名称——可变式数组**/
	public void addSpecifiedFieldName(String... specifiedFieldNameArr){
		if(specifiedFieldNameArr!=null && specifiedFieldNameArr.length>0){
			specifiedColumnSet=specifiedColumnSet!=null?specifiedColumnSet:new HashSet<String>();
			for(String t_fieldName:specifiedFieldNameArr){
				if(t_fieldName!=null){
					specifiedColumnSet.add(t_fieldName);
				}
			}
		}
	}
	/**清除所有指定显示的列名映射的字段（成员属性）名称**/
	public void clearSpecifiedFieldName(){
		this.specifiedColumnSet=null;
	}
	
}