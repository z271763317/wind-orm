package org.wind.orm;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.wind.orm.bean.Page;
import org.wind.orm.sql.SQLFactory;
import org.wind.orm.util.TableUtil;

/**
 * @描述 : 操作数据库抽象类，ORM类要继承此类（不允许实例化）
 * @版权 : 湖南省郴州市安仁县胡璐璐 
 * @时间 : 2014年8月21日 11:39:16
 * @功能 : 成员“添删改查”方法
 */
public abstract class Table extends TableParent1{

	/********************************************【查询】********************************************/
	public <T extends Table> List<T> find(){
		return this.find(false,null);
	}
	/**查询：根据ORM的字段是否存在值来加条件（带null的不加入）；isDesc=是否降序、Page分页对象**/
	public <T extends Table> List<T> find(boolean isDesc,Page page){
		return SQLFactory.getSelect(this).find(isDesc,page,null,null);
	}
	/**查询：根据ORM的字段是否存在值来加条件，只查1个，其他说明和find()一样*/
	public <T extends Table> T findOne(boolean isDesc){
		List<T> t_list=this.find(isDesc,new Page(1,1));
		if(t_list.size()>0){return t_list.get(0);}else{return null;}
	}
	/**
	 * 查询：根据条件。
	 *          可设置排序（{@link #setOrderFieldName(String)}）<br/>
	 *          可设置分组（{@link #setGroupFieldName(String)}）<br/>
	 *          可设置显示的字段（{@link #addSpecifiedFieldName(String...)}）<br/>
	 * @param conditionsSQL : 条件SQL
	 * @param conditionsList : 条件值
	 * @param isDesc : 是否降序
	 * @param page : 分页
	 **/
	public <T extends Table> List<T> find(String conditionsSQL,List<? extends Object> conditionsList,boolean isDesc,Page page){
		if(conditionsSQL==null || conditionsSQL.trim().length()<=0){
			return new ArrayList<T>();
		}else{
			conditionsList=TableUtil.listInit(conditionsList);
			return SQLFactory.getSelect(this).find(isDesc,page,conditionsSQL,conditionsList);
		}
	}
	/**查询 : 总数，根据ORM的字段是否存在值来加条件**/
	public long findSize(){
		return SQLFactory.getSelect(this).findSize(null,null); 
	}
	/**查询 : 分组总数，根据ORM的字段是否存在值来加条件**/
	public <T> Map<T,Long> findSizeMap(String groupFieldName){
		return SQLFactory.getSelect(this).findSizeMap(null,null,groupFieldName);
	}
	
	/********************************************【保存（插入、更新）】********************************************/
	/**保存（更新：当主键变量存在值时；插入：主键变量为null时候，插入成功后，会把生成的主键【设置】到该【对象映射】的【主键变量】里）**/
	public int save(){
		return SQLFactory.getSave(this).save();
	}
	/**保存（与上同。isAdd=是否插入【true=插入；false=更新】） */
	public int save(boolean isAdd){
		List<Table> list=new ArrayList<Table>();list.add(this);
		if(list!=null && list.size()>0){return SQLFactory.getSave(this).batchSave(list,isAdd);}else{return 0;}
	}
	
	/********************************************【删除：方法列表】********************************************/
	/**删除：根据当前ORM对象的变量是否有值**/
	public int delete(){
		return SQLFactory.getDelete(this).delete();
	}
	
}