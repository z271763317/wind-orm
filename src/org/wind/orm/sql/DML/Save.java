package org.wind.orm.sql.DML;

import java.util.List;

import org.wind.orm.Table;
import org.wind.orm.sql.SQL;


/**
 * @描述 : SQL【插入、更新】接口类
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年5月29日 09:46:21
 */
public interface Save extends SQL{

	/**保存 : （更新：当主键变量存在值时；插入：主键变量为null时候；isInsertPrimary：插入时是否插入主键数据）**/
	public int save();
	/**保存 : （自传的insert、update的SQL语句；placeholderList=占位符的值）**/
	public int save(String sql,List<? extends Object> placeholderList);
	/**保存 : 【自动选择】批量保存，返回更新的长度**/
	public int batchSaveAuto(List<? extends Table> list);
	/**保存 : 【手动选择】批量保存，isAll代表是否保存全部字段，返回保存影响的行数**/
	public int batchSave(List<? extends Table> list,boolean isAdd);
	/**更新 : 自增减；fieldName=要自增减的列映射的字段名称；step=步长（自增减的增量数）；conditionsSQL=条件SQL；conditionsList=条件值列表**/
	public int increaseOrDecrease(String fieldName,double step,String conditionsSQL,List<? extends Object> conditionsList);
	/**更新 : 自增减；fieldName=要自增减的列映射的字段名称；step=步长（自增减的增量数）；id=主键值（为null则直接返回0）**/
	public int increaseOrDecrease(String fieldName,double step,Object id);
	/**更新 : 根据传来的set和条件语句；setSQL=set区语句；conditionsSQL=条件SQL；conditionsList=条件值**/
	public int update(String setSQL,String conditionsSQL,List<? extends Object> placeholderList);
	
}