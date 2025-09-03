package org.wind.orm.sql.DML;

import java.util.List;
import java.util.Map;

import org.wind.orm.bean.Page;
import org.wind.orm.sql.SQL;


/**
 * @描述 : SQL【查询】接口类
 * @备注 : ORM=继承Table类的调用者（类—>映射—>表）对象
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年5月29日 09:46:42
 */
public interface Select extends SQL{

	/**查询：根据ORM的ID的1个对象**/
	public <T> T findById(Object id);
	/**查询：根据ORM的所有对象数据，List装入，带Page分页对象**/
	public <T> List<T> findAll(boolean isDesc,Page page);
	/**查询：根据传来的条件语句，List装入，带Page分页对象、条件及值**/
	public <T> List<T> find(boolean isDesc,Page page,String conditionsSQL,List<? extends Object> conditionsList);
	/**查询：根据传来的id列表，isContain代表是否包含idList列表值的行数，带Page分页对象，List式**/
	public <T> List<T> findByIdList(List<? extends Object> idList,boolean isContain);
	/**查询 : 根据ORM的所有数据总数**/
	public long findAllSize();
	/**查询 : 总数，conditionsSQL=条件SQL；conditionsList=条件值**/
	public long findSize(String conditionsSQL,List<? extends Object> conditionsList);
	/**查询 : 总数，conditionsSQL=条件SQL；conditionsList=条件值；groupFieldName=分组列。ORM类字段名映射的列名——Map返回式**/
	public <T> Map<T,Long> findSizeMap(String conditionsSQL,List<? extends Object> conditionsList,String groupFieldName);
	/**查询 : 当前数据库日期时间（格式：yyyy-MM-dd HH:mm:ss）**/
	public String findDate();
	/**查询 : 主键列表，conditionsSQL=条件SQL；conditionsList=条件值**/
	public <T> List<T> findIdList(String conditionsSQL,List<? extends Object> conditionsList);
	/**查询 : 指定列的值列表，specifiedFieldName=指定列名映射的字段名**/
	public <T> List<T> findSpecifiedList(String conditionsSQL,List<? extends Object> conditionsList,String specifiedFieldName);
	/**查询 : 根据传来的SQL（返回的最顶层的List为行，Map为每一行所有的列名【key】和值【value】），sql=SQL语句；conditionsList=条件里占位符对应位置的值**/
	public List<Map<String,Object>> find(String sql,List<? extends Object> conditionsList);
	
	/**********************数学函数*********************/
	/**求和**/
	public Number sum(String conditionsSQL,List<? extends Object> conditionsList,String fieldName);
	/**求和：根据指定groupFieldName分组字段**/
	public <T> Map<T,Number> sum(String conditionsSQL,List<? extends Object> conditionsList,String fieldName,String groupFieldName);
	
	/**求平均**/
	public Number avg(String conditionsSQL,List<? extends Object> conditionsList,String fieldName);
	/**求平均：根据指定groupFieldName分组字段**/
	public <T> Map<T,Number> avg(String conditionsSQL,List<? extends Object> conditionsList,String fieldName,String groupFieldName);
	
	/**求最大值**/
	public Number max(String conditionsSQL,List<? extends Object> conditionsList,String fieldName);
	/**求最大值（字符串）**/
	public String maxString(String conditionsSQL,List<? extends Object> conditionsList,String fieldName);
	/**求最大值：根据指定groupFieldName分组字段**/
	public <T> Map<T,Number> max(String conditionsSQL,List<? extends Object> conditionsList,String fieldName,String groupFieldName);
	/**求最大值：根据指定groupFieldName分组字段（字符串）**/
	public <T> Map<T,String> maxString(String conditionsSQL,List<? extends Object> conditionsList,String fieldName,String groupFieldName);
	
	/**求最小值**/
	public Number min(String conditionsSQL,List<? extends Object> conditionsList,String fieldName);
	/**求最小值（字符串）**/
	public String minString(String conditionsSQL,List<? extends Object> conditionsList,String fieldName);
	/**求最小值：根据指定groupFieldName分组字段**/
	public <T> Map<T,Number> min(String conditionsSQL,List<? extends Object> conditionsList,String fieldName,String groupFieldName);
	/**求最小值：根据指定groupFieldName分组字段（字符串）**/
	public <T> Map<T,String> minString(String conditionsSQL,List<? extends Object> conditionsList,String fieldName,String groupFieldName);
	
}