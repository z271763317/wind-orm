package org.wind.orm.bean;

import java.util.List;

/**
 * @描述 : 拦截器上下文对象（封装了需要的属性）
 * @版权 : 湖南省郴州市安仁县胡璐璐 
 * @时间 : 2023年11月8日 16:40:03
 */
public class ActionContext {

	private int type;			//SQL类型
	private String sql;		//原生SQL（带【?】占位符）
	private List<? extends Object> placeholderList;	//占位符值
	private SQLVar objVar;		//执行SQL需要的变量（针对单次操作）
	
	public ActionContext(int type,String sql,List<? extends Object> placeholderList,SQLVar objVar) {
		this.type=type;
		this.sql=sql;
		this.placeholderList=placeholderList;
		this.objVar=objVar;
	}
	
	public int getType() {
		return type;
	}
	public String getSql() {
		return sql;
	}
	public List<? extends Object> getPlaceholderList() {
		return placeholderList;
	}
	public SQLVar getObjVar() {
		return objVar;
	}
	
	
}
