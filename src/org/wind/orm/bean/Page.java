package org.wind.orm.bean;

import org.wind.orm.exception.TableSelectException;


/**
 * @描述 : 分页类
 * @默认 : pageNew=1，limit=100，id=0
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年8月27日 17:24:21
 */
public class Page {
	
	private int pageNew=1;	//新的页数（目标页数）
	private int limit=100;		//分页起始位置后面多少条数据、每页多少条（默认100条）
	private Object id;		/*
										当前页的最大或最小id主键（表为【字符串主键】的，则该值一般为：创建时间），
										如果指定页是当前页的上页，则是当前页的最小ID，反之最大ID
										（对于MySQL，可超高效分页）
									*/
	private Object id2;	/*第2层的排序id，一般在id不是主键值时（可能是【创建时间】）需要；该列的值必须为主键【唯一】*/
	private boolean isEfficient=false;		//是否超高效分页
	
	/*****************构造方法*************/
	public Page(){
		//
	}
	/**
	 * @param pageNew : 跳转的页数（小于等于0时候。将自动设置为1）
	 * @param limit	: 每页多少条数据（不能为负数）
	 */
	public Page(int pageNew,int limit){
		if(limit<0){
			throw new TableSelectException("'limit'不能为负数");
		}
		this.pageNew=pageNew>0?pageNew:1;
		this.limit=limit;
	}
	/**
	 * 超高效分页——pageLastId为null，则转为高效分页，pageNew取绝对值（-如：5转为5）
	 * @param pageNew : 当前页的第几页（负数=前第几页；正数=后第几页）
	 * @param limit	: 前（后）第几页多少条数据
	 * @param pageLastId : 如果指定页是当前页的上页，则是当前页的最小ID，反之最大ID
	 * @exception TableSelectException 'limit'不能为负数
	 */
	public Page(int pageNew,int limit,Object pageLastId) {
		if(limit<0){
			throw new TableSelectException("'limit'不能为负数");
		}
		this.limit=limit;
		if(pageLastId==null || (pageLastId instanceof String && pageLastId.toString().trim().length()<=0)){
			this.pageNew=Math.abs(pageNew);
		}else{
			this.id=pageLastId;
			this.isEfficient=true;
			this.pageNew=pageNew;
		}
	}
	/**
	 * 超高效分页——pageLastId为null，则转为高效分页，pageNew取绝对值（-如：5转为5）
	 * @param pageNew : 当前页的第几页（负数=前第几页；正数=后第几页）
	 * @param limit	: 前（后）第几页多少条数据
	 * @param pageLastId : 如果指定页是当前页的上页，则是当前页的最小ID，反之最大ID（可能为创建时间等）
	 *  @param pageLastId 2: 第2层的id（可空；主键，唯一）
	 * @exception TableSelectException 'limit'不能为负数
	 */
	public Page(int pageNew,int limit,Object pageLastId,Object pageLastId2) {
		this(pageNew, limit, pageLastId);
		this.id2=pageLastId2;
	}
	/***************成员方法***************/
	public int getPageNew() {
		return pageNew;
	}
	public void setPageNew(int pageNew) {
		this.pageNew = pageNew;
	}
	public long getBegin() {
		return (pageNew*limit-limit)+1L;
	}
	public int getLimit() {
		return limit;
	}
	public void setLimit(int limit) {
		this.limit = limit;
	}
	public long getEnd(){
		return pageNew*limit;
	}
	public boolean isEfficient() {
		return isEfficient;
	}
	public Object getId() {
		return id;
	}
	public Object getId2() {
		return id2;
	}
	public void setId(Object id) {
		this.id = id;
	}
	public void setId2(Object id2) {
		this.id2 = id2;
	}
	/**
	 * 获取总页数
	 * @param size : 查询的数据总数（不是带分页的数据）
	 * @return
	 */
	public long getPageCount(long size){
		return size%limit==0?size/limit:(size/limit)+1;
	}
}