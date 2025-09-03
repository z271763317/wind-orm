package org.wind.orm;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wind.orm.annotation.ForeignKey;
import org.wind.orm.bean.Page;
import org.wind.orm.exception.TableException;
import org.wind.orm.sql.SQLFactory;
import org.wind.orm.util.TableUtil;

/**
 * @描述 : Table父1级类——静态方法
 * @版权 : 湖南省郴州市安仁县胡璐璐 
 * @时间 : 2015年8月26日 17:14:28
 * @功能 : 添删改查的【静态方法】
 */
@SuppressWarnings({"unchecked"})
public abstract class TableParent1 extends TableParent2 {
		
	//动态实例化Table对象
	public static Table getInstance(Class<? extends Table> tableClass){
		try {return tableClass.newInstance();} catch (Exception e) {throw new TableException(e.getMessage(),e);} 
	}
	/*****************************【查询】******************************/
	/**
	 * 查询：根据tableClass所属对象映射的表的ID的1个对象
	 * @param tableClass : 继承Table类的ORM类
	 * @param id : 要查询的表其中的一行数据的主键值
	 * @param specifiedFieldNameArr : 指定显示的列名数组（可变式）
	 */
	public static <T extends Table> T findById(Class<T> tableClass,Object id,String ...specifiedFieldNameArr){
		Table t_obj=getInstance(tableClass);
		t_obj.addSpecifiedFieldName(specifiedFieldNameArr);
		return SQLFactory.getSelect(t_obj).findById(id);
	}
	/*不带分页*/
	public static <T extends Table> List<T> findAll(Class<T> tableClass,boolean isDesc){
		return findAll(tableClass,isDesc,null);
	}
	/*带分页*/
	public static <T extends Table> List<T> findAll(Class<T> tableClass,boolean isDesc,Page page){
		return findAll(tableClass, isDesc,page,null,null);
	}
	/**
	 * 查询：根据tableClass所属对象映射的表的所有对象，List装入
	 * @param tableClass : 继承Table类的ORM类
	 * @param isDesc : 是否降序，false=正序
	 * @param page : 分页对象
	 * @param orderFieldName : 指定排序的列名映射的字段名，为空则取主键列名
	 * @param groupFieldName : 指定分组的列名映射的字段名
	 * @param specifiedFieldNameArr :  指定显示的列名数组（可变式）
	 */
	public static <T extends Table> List<T> findAll(Class<T> tableClass,boolean isDesc,Page page,String orderFieldName,String groupFieldName,String ...specifiedFieldNameArr){
		Table obj=getInstance(tableClass);
		obj.setOrderFieldName(orderFieldName);
		obj.setGroupFieldName(groupFieldName);
		obj.addSpecifiedFieldName(specifiedFieldNameArr);
		return SQLFactory.getSelect(obj).findAll(isDesc,page);
	}
	/***************************find************************/
	/**查询：根据传来的条件语句，List装入，带Page分页对象**/
	public static <T extends Table> List<T> find(Class<T> tableClass,String conditionsSQL,List<? extends Object> conditionsList,boolean isDesc,Page page){
		return find(tableClass, conditionsSQL, conditionsList, isDesc, page, null, null);
	}
	/**查询：根据传来的条件语句，List装入，带Page分页对象、条件；isDesc=是否降序；page=分页对象；orderFieldName=排序字段；groupFieldName=分组字段；specifiedFieldNameArr=显示的字段（非必须）**/
	public static <T extends Table> List<T> find(Class<T> tableClass,String conditionsSQL,List<? extends Object> conditionsList,boolean isDesc,Page page,String orderFieldName,String groupFieldName,String ...specifiedFieldNameArr){
		conditionsList=TableUtil.listInit(conditionsList);
		Table t_obj=getInstance(tableClass);
		t_obj.setOrderFieldName(orderFieldName);
		t_obj.setGroupFieldName(groupFieldName);
		t_obj.addSpecifiedFieldName(specifiedFieldNameArr);
		return t_obj.find(conditionsSQL, conditionsList, isDesc, page);
	}
	/**查询 : 根据条件只查1个（数组式。conditionsSQL=条件SQL；conditionsArr=条件数组；specifiedFieldNameArr=指定显示的字段名称）**/
	public static <T extends Table> T findOne(Class<T> tableClass,String conditionsSQL,Object conditionsArr[],boolean isDesc,String... specifiedFieldNameArr){
		List<Object> conditionsList=conditionsArr!=null?Arrays.asList(conditionsArr):new ArrayList<Object>();
		return findOne(tableClass, conditionsSQL, conditionsList, isDesc,specifiedFieldNameArr);
	}
	/**查询：根据条件只查1个，其他说明和find()一样；specifiedFieldNameArr=指定显示的字段名称*/
	public static <T extends Table> T findOne(Class<T> tableClass,String conditionsSQL,List<? extends Object> conditionsList,boolean isDesc,String... specifiedFieldNameArr){
		List<T> t_list=find(tableClass,conditionsSQL, conditionsList, isDesc, new Page(1,1),null,null,specifiedFieldNameArr);
		if(t_list.size()>0){return t_list.get(0);}else{return null;}
	}
	/**查询：根据指定的idList主键列表值，可指定显示的字段**/
	public static <T extends Table> List<T> findByIdList(Class<T> tableClass,List<? extends Object> idList,String ...specifiedFieldNameArr){
		return findByIdListOrNot(tableClass, idList, true,specifiedFieldNameArr);
	}
	/**查询：根据tableClass所属对象映射的表的，除指定主键列表之外的其他数据，可指定显示的字段**/
	public static <T extends Table> List<T> findByNotIdList(Class<T> tableClass,List<? extends Object> idList,String ...specifiedFieldNameArr){
		return findByIdListOrNot(tableClass, idList, false,specifiedFieldNameArr);
	}
	/**
	 * 查询：根据tableClass所属对象映射的表的主键列表，可查除指定主键列表之外的其他数据，List装入
	 * @param tableClass : 继承Table类的ORM类
	 * @param idList : tableClass类映射表的主键值列表
	 * @param isContain : 是否查询idList的数据
	 * @param specifiedFieldNameArr :  指定显示的列名数组（可变式）
	 */
	private static <T extends Table> List<T> findByIdListOrNot(Class<T> tableClass,List<? extends Object> idList,boolean isContain,String ...specifiedFieldNameArr){
		Table obj=getInstance(tableClass);
		obj.addSpecifiedFieldName(specifiedFieldNameArr);
		return SQLFactory.getSelect(obj).findByIdList(idList,isContain);
	}
	/**
	 * 查询 : 根据tableClass所属对象映射的表的数据总数
	 * @param tableClass : 继承Table类的ORM类
	 */
	public static long findAllSize(Class<? extends Table> tableClass){
		return SQLFactory.getSelect(getInstance(tableClass)).findAllSize();
	}
	/**查询 : 当前数据库日期时间**/
	public static String findDate(Class<? extends Table> tableClass){
		return SQLFactory.getSelect(getInstance(tableClass)).findDate();
	}
	/**
	 * 查询 : 主键列表（条件为空则不查询）
	 * @param conditionsSQL : 条件SQL
	 * @param conditionsList : 条件值List
	 */
	public static <T> List<T> findIdList(Class<? extends Table> tableClass,String conditionsSQL,List<? extends Object> conditionsList){
		conditionsList=TableUtil.listInit(conditionsList);
		return SQLFactory.getSelect(getInstance(tableClass)).findIdList(conditionsSQL, conditionsList);
	}
	/**查询 : 所有主键——List式**/
	public static <T> List<T> findAllIdList(Class<? extends Table> tableClass){
		return SQLFactory.getSelect(getInstance(tableClass)).findIdList(null, null);
	}
	/**
	 *  查询 : 指定列的值列表（条件为空则不查询）
	 * @param conditionsSQL : 条件SQL
	 * @param conditionsList : 条件值List
	 * @param specifiedFieldName : 指定列名映射的字段名（默认为：主键）
	 */
	public static <T> List<T> findSpecifiedList(Class<? extends Table> tableClass,String conditionsSQL,List<? extends Object> conditionsList,String specifiedFieldName){
		conditionsList=TableUtil.listInit(conditionsList);
		return SQLFactory.getSelect(getInstance(tableClass)).findSpecifiedList(conditionsSQL, conditionsList, specifiedFieldName);
	}
	/**
	 * 查询 : 根据传来的SQL（返回的最顶层的List为行，Map为每一行所有的列名【key】和值【value】）
	 * @param sql : SQL语句
	 * @param conditionsList : 条件里占位符对应位置的值
	 */
	public static List<Map<String,Object>> find(Class<? extends Table> tableClass,String sql,List<? extends Object> conditionsList){
		conditionsList=TableUtil.listInit(conditionsList);
		return SQLFactory.getSelect(getInstance(tableClass)).find(sql, conditionsList);
	}
	/**查询 : 总数，conditionsSQL=条件SQL；conditionsList=条件值（List）**/
	public static long findSize(Class<? extends Table> tableClass,String conditionsSQL,List<? extends Object> conditionsList){
		conditionsList=TableUtil.listInit(conditionsList);
		return SQLFactory.getSelect(getInstance(tableClass)).findSize(conditionsSQL,conditionsList);
	}
	/**查询 : 分组总数，conditionsSQL=条件SQL；conditionsList=条件值；groupFieldName=分组列（ORM类字段名映射的列名）——Map返回式**/
	public static <T> Map<T,Long> findSizeMap(Class<? extends Table> tableClass,String conditionsSQL,List<? extends Object> conditionsList,String groupFieldName){
		conditionsList=TableUtil.listInit(conditionsList);
		return SQLFactory.getSelect(getInstance(tableClass)).findSizeMap(conditionsSQL,conditionsList,groupFieldName);
	}
	/***************************查询：外键相关************************/
	/**查询 : 所有外键数据**/
	public static void findAllForeign(Table obj,boolean isDesc,Page page){
		if(obj!=null){
			List<Table> t_list=new ArrayList<Table>();
			t_list.add(obj);
			Table.findAllForeign(t_list, isDesc, page);
		}
	}
	//不分页，正序（单对象）
	public static void findForeign(Table obj,String... foreign){
		findForeign(obj, false,null, foreign);
	}
	/**查询 : 外键数据，根据foreign指定的外键字段（成员变量）名——单对象式**/
	public static void findForeign(Table obj,boolean isDesc,Page page,String... foreign){
		List<Table> list=new ArrayList<Table>();list.add(obj);
		findForeign(list,isDesc, page, null,foreign);
	}
	/**查询：单个外键（指定foreign单个外键字段，specifiedFieldNameArr=外键要显示的字段列表，可变式数组；单对象式）**/
	public static void findOneForeign(Table obj,String foreign,String... specifiedFieldNameArr){
		if(obj!=null){
			List<Table> list=new ArrayList<Table>();list.add(obj);
			findOneForeign(list,foreign, specifiedFieldNameArr);
		}
	}
	/**查询：单个外键（指定foreign单个外键字段，specifiedFieldNameArr=外键要显示的字段列表，可变式数组；列表式）**/
	public static void findOneForeign(List<? extends Table> list,String foreign,String... specifiedFieldNameArr){
		findForeign(list,false, null, specifiedFieldNameArr,foreign);
	}
	//不分页，正序（列表）
	public static void findForeign(List<? extends Table> list,String... foreign){
		findForeign(list,false, null, null,foreign);
	}
	/**
	 * 查询 : 外键数据（外键里的外键对象只留下id），根据foreign指定的外键字段（成员变量）名，指定显示的字段——列表式
	 * @param list : 继承Table的ORM类对象列表
	 * @param isDesc : 是否倒序
	 * @param page : 分页对象
	 * @param specifiedFieldNameArr : 指定外键要显示的字段列表（请传成员变量名，建议对取单个外键的方式使用），数组式，为NULL，则显示所有字段
	 * @param foreign : 指定要取的外键（请传成员变量名），变长式，可数组式
	 */
	public static void findForeign(List<? extends Table> list,boolean isDesc,Page page,String specifiedFieldNameArr[],String... foreign){
		TableUtil.findForeign(list, isDesc, page, specifiedFieldNameArr,foreign);
	}
	/**
	 * 查询 : 所有外键数据（外键里的外键对象只留下id）——列表式
	 * @param list : 继承Table的ORM类对象列表
	 * @param isDesc : 是否倒序
	 * @param page : 分页对象
	 */
	public static void findAllForeign(List<? extends Table> list,boolean isDesc,Page page){
		if(list!=null && list.size()>0){
			List<String> t_foreignFieldNameList=new ArrayList<String>();
			Field t_fieldArr[]=TableUtil.getField(list.get(0).getClass());
			for(Field t_field:t_fieldArr){
				//是外键
				if(t_field.isAnnotationPresent(ForeignKey.class)){
					t_foreignFieldNameList.add(t_field.getName());
				}
			}
			if(t_foreignFieldNameList.size()>0){
				String[] strings = new String[t_foreignFieldNameList.size()];
				TableUtil.findForeign(list, isDesc, page, null,t_foreignFieldNameList.toArray(strings));
			}
		}
	}
	
	/*****************************【保存】******************************/
	/**
	 * 【自动选择】批量保存<br>
	 * 【注】：保存的所有对象是属于同一个类，以List第1个为基础，有值则是更新，否则插入
	 * @param list : Table对象列表
	 * @return 插入（成功的主键id设置到对应ORM的主键里）、更新：返回影响的行数
	 */
	public static int save(List<? extends Table> list){
		if(list!=null && list.size()>0){return SQLFactory.getSave(getInstance(list.get(0).getClass())).batchSaveAuto(list);}else{return 0;}
	}
	/**
	 * 【手动选择】批量保存<br>
	 * 【注】：保存的所有对象是属于同一个类，以List第一个为基础
	 * @param list : Table对象列表
	 * @param isAdd : 是否插入（true=插入；false=更新）
	 * @return 返回影响的行数；若是插入，则成功后，会把主键【设置】到对应【对象映射】的【主键变量】里
	 */
	public static int save(List<? extends Table> list,boolean isAdd){
		if(list!=null && list.size()>0){return SQLFactory.getSave(getInstance(list.get(0).getClass())).batchSave(list,isAdd);}else{return 0;}
	}
	/** 【自定义】保存
	 * @param sql : 自传的insert、update的SQL语句
	 * @param placeholderList : 占位符的值（可为null）
	 */
	public static int save(Class<? extends Table> tableClass,String sql,List<? extends Object> placeholderList){
		placeholderList=TableUtil.listInit(placeholderList);
		return SQLFactory.getSave(getInstance(tableClass)).save(sql, placeholderList);
	}
	/**
	 * 自增减
	 * @param tableClass : 继承Table的ORM类
	 * @param fieldName : 要自增的列映射的字段名称
	 * @param step : 步长（自增减的增量数）
	 * @param id : 条件为主键id值
	 * @return 返回影响的行数，id为null则不执行SQL，返回0
	 */
	public static int increaseOrDecrease(Class<? extends Table> tableClass,String fieldName,double step,Object id){
		return SQLFactory.getSave(getInstance(tableClass)).increaseOrDecrease(fieldName, step, id);
	}
	/**
	 * 自增减（带条件）
	 * @param tableClass : 继承Table的ORM类
	 * @param fieldName : 要自增减的列映射的字段名称
	 * @param step : 步长（自增减的增量数）
	 * @param conditionsSQL : 条件SQL（为空则不更新，返回0）
	 * @param conditionsList : 条件值列表
	 * @return 返回影响的行数
	 */
	public static int increaseOrDecrease(Class<? extends Table> tableClass,String fieldName,double step,String conditionsSQL,List<? extends Object> conditionsList){
		conditionsList=TableUtil.listInit(conditionsList);
		return SQLFactory.getSave(getInstance(tableClass)).increaseOrDecrease(fieldName, step, conditionsSQL, conditionsList);
	}
	/**
	 * 更新 : 根据传来的set和条件SQL语句
	 * @param tableClass : 继承Table的ORM类
	 * @param setSQL : set区语句（不能为空）
	 * @param conditionsSQL : 条件SQL（不能为空）
	 * @param placeholderList : 占位符值
	 **/
	public static int update(Class<? extends Table> tableClass,String setSQL,String conditionsSQL,List<? extends Object> placeholderList){
		placeholderList=TableUtil.listInit(placeholderList);
		return SQLFactory.getSave(getInstance(tableClass)).update(setSQL, conditionsSQL, placeholderList);
	}
	/*****************************【删除】******************************/
	/**
	 * 删除：根据tableClass所属对象映射的表，删除数据库存在的主键值集的数据（可主键类型式（Integer、Long、List【带主键】等等））
	 * @param tableClass : 继承Table类的ORM类
	 * @param ids : 主键值集（可List）
	 */
	public static int delete(Class<? extends Table> tableClass,Object ids){
		if(ids!=null){
			List<Object> idList=new ArrayList<Object>();
			if(ids instanceof List){
				idList.addAll((List<Object>)ids);
			}else{
				idList.add(ids);
			}
			return SQLFactory.getDelete(getInstance(tableClass)).delete(idList);
		}else{
			return 0;
		}
	}

	/**
	 * 删除全部：根据当前tableClass所映射的表，删除该映射表所有的数据
	 * @param tableClass : 继承Table类的ORM类
	 */
	public static int delete(Class<? extends Table> tableClass){
		return SQLFactory.getDelete(getInstance(tableClass)).deleteAll();
	}
	/**
	 * 删除：根据传来的条件语句
	 * @param tableClass : 继承Table类的ORM类
	 * @param conditionsSQL : 条件SQL
	 * @param conditionsList : 占位符的值（可为null）
	 */
	public static int delete(Class<? extends Table> tableClass,String conditionsSQL,List<? extends Object> conditionsList){
		conditionsList=TableUtil.listInit(conditionsList);
		return SQLFactory.getDelete(getInstance(tableClass)).delete(conditionsSQL, conditionsList);
	}

	/*****************************【求和】******************************/
	/**求和：根据指定fieldName映射字段的所有数据【总数求和】，不带条件 */
	public static Number sum(Class<? extends Table> tableClass,String fieldName){
		return sum(tableClass, null, null, fieldName);
	}
	/**
	 * 求和：带条件数据的总和，根据指定fieldName字段名称映射的列的数据
	 * @param tableClass : 继承Table的tableClass
	 * @param conditionsSQL : 条件SQL
	 * @param conditionsList : 条件占位符值
	 * @param fieldName : 指定的要做sum求和的字段名称
	 * @return 返回Number类型，可调用指定的方法返回不同的值（详情查看JDK的API文档），默认值为：0。调用示例：<br />
	 * 					1、【Number对象】.longValue();		//返回【long】类型的值		<br/>
	 * 					2、【Number对象】.doubleValue();		//返回【double】类型的值		<br/>
	 *					3、【Number对象】.intValue();		//返回【int】类型的值		<br/>
	 *					4、【Number对象】.floatValue();		//返回【float】类型的值		<br/>
	 *							....按照上述的规范，调用其他类型的，如：byteValue();		<br/>
	 */
	public static Number sum(Class<? extends Table> tableClass,String conditionsSQL,List<? extends Object> conditionsList,String fieldName){
		return SQLFactory.getSelect(getInstance(tableClass)).sum(conditionsSQL, conditionsList, fieldName);
	}
	/**
	 * 求和：根据指定的fieldName字段名称做【总数求和】，指定的groupFieldName字段的值做返回Map的key，value为每组的总数
	 * @param tableClass : 继承Table的tableClass
	 * @param conditionsSQL : 条件SQL
	 * @param conditionsList : 条件占位符值
	 * @param fieldName : 指定的要做sum求和的字段名称
	 * @param groupFieldName : 分组字段名称，返回的Map的key是为该字段映射的列名的值（及类型）
	 * @return 返回Number类型，可调用指定的方法返回不同的值（详情查看JDK的API文档），默认值为：0。调用示例：如上的方法
	 */
	public static <T> Map<T,Number> sum(Class<? extends Table> tableClass,String conditionsSQL,List<? extends Object> conditionsList,String fieldName,String groupFieldName){
		return SQLFactory.getSelect(getInstance(tableClass)).sum(conditionsSQL, conditionsList, fieldName, groupFieldName);
	}
	/*****************************【求平均】******************************/
	/**求平均 */
	public static Number avg(Class<? extends Table> tableClass,String fieldName){
		return avg(tableClass, null, null, fieldName);
	}
	/**求平均：带条件*/
	public static Number avg(Class<? extends Table> tableClass,String conditionsSQL,List<? extends Object> conditionsList,String fieldName){
		return SQLFactory.getSelect(getInstance(tableClass)).avg(conditionsSQL, conditionsList, fieldName);
	}
	/**求平均：根据指定的fieldName字段名称做【总数求平均】，指定的groupFieldName字段的值做返回Map的key，value为每组的平均*/
	public static <T> Map<T,Number> avg(Class<? extends Table> tableClass,String conditionsSQL,List<? extends Object> conditionsList,String fieldName,String groupFieldName){
		return SQLFactory.getSelect(getInstance(tableClass)).avg(conditionsSQL, conditionsList, fieldName, groupFieldName);
	}
	/*****************************【求最大值】******************************/
	/**求最大值**/
	public static Number max(Class<? extends Table> tableClass,String fieldName){
		return max(tableClass, null, null, fieldName);
	}
	/**求最大值：带条件*/
	public static Number max(Class<? extends Table> tableClass,String conditionsSQL,List<? extends Object> conditionsList,String fieldName){
		return SQLFactory.getSelect(getInstance(tableClass)).max(conditionsSQL, conditionsList, fieldName);
	}
	/**求最大值：指定的groupFieldName字段的值做返回Map的key，value为每组的值*/
	public static <T> Map<T,Number> max(Class<? extends Table> tableClass,String conditionsSQL,List<? extends Object> conditionsList,String fieldName,String groupFieldName){
		return SQLFactory.getSelect(getInstance(tableClass)).max(conditionsSQL, conditionsList, fieldName, groupFieldName);
	}
	/**求最大值（返回类型：字符串）**/
	public static String maxString(Class<? extends Table> tableClass,String fieldName){
		return maxString(tableClass, null, null, fieldName);
	}
	/**求最大值：带条件（返回类型：字符串）*/
	public static String maxString(Class<? extends Table> tableClass,String conditionsSQL,List<? extends Object> conditionsList,String fieldName){
		return SQLFactory.getSelect(getInstance(tableClass)).maxString(conditionsSQL, conditionsList, fieldName);
	}
	/**求最大值：指定的groupFieldName字段的值做返回Map的key，value为每组的值（返回类型：字符串）*/
	public static <T> Map<T,String> maxString(Class<? extends Table> tableClass,String conditionsSQL,List<? extends Object> conditionsList,String fieldName,String groupFieldName){
		return SQLFactory.getSelect(getInstance(tableClass)).maxString(conditionsSQL, conditionsList, fieldName, groupFieldName);
	}
	/*****************************【求最小值】******************************/
	/**求最小值**/
	public static Number min(Class<? extends Table> tableClass,String fieldName){
		return min(tableClass, null, null, fieldName);
	}
	/**求最小值：带条件**/
	public static Number min(Class<? extends Table> tableClass,String conditionsSQL,List<? extends Object> conditionsList,String fieldName){
		return SQLFactory.getSelect(getInstance(tableClass)).min(conditionsSQL, conditionsList, fieldName);
	}
	/**求最小值：指定的groupFieldName字段的值做返回Map的key，value为每组的值**/
	public static <T> Map<T,Number> min(Class<? extends Table> tableClass,String conditionsSQL,List<? extends Object> conditionsList,String fieldName,String groupFieldName){
		return SQLFactory.getSelect(getInstance(tableClass)).min(conditionsSQL, conditionsList, fieldName, groupFieldName);
	}
	/**求最小值（返回类型：字符串）**/
	public static String minString(Class<? extends Table> tableClass,String fieldName){
		return minString(tableClass, null, null, fieldName);
	}
	/**求最小值：带条件（返回类型：字符串）**/
	public static String minString(Class<? extends Table> tableClass,String conditionsSQL,List<? extends Object> conditionsList,String fieldName){
		return SQLFactory.getSelect(getInstance(tableClass)).minString(conditionsSQL, conditionsList, fieldName);
	}
	/**求最小值：指定的groupFieldName字段的值做返回Map的key，value为每组的值（返回类型：字符串）**/
	public static <T> Map<T,String> minString(Class<? extends Table> tableClass,String conditionsSQL,List<? extends Object> conditionsList,String fieldName,String groupFieldName){
		return SQLFactory.getSelect(getInstance(tableClass)).minString(conditionsSQL, conditionsList, fieldName, groupFieldName);
	}
	
	/*****************************【DDL区】******************************/
	/**存储过程**/
	/**
	 * 调用 : 存储过程（returnType不为空返回一个值）
	 * @param tableClass : 继承Table类的ORM类
	 * @param proc : 过程名
	 * @param paramArr[] : 参数数组（可为空）
	 * @param returnType : 返回值类型（可为空，请用java的java.sql.Types包下的类型，如： java.sql.Types.BIGINT）
	 */
	public static <T> T call(Class<? extends Table> tableClass,String proc,Object[] paramArr,Integer returnType){
		return SQLFactory.getCall(getInstance(tableClass)).call(proc, paramArr, returnType);
	}
	/**
	 * 调用 : 存储过程并返回所有值（根据returnTypeArr数组）
	 * @param tableClass : 继承Table类的ORM类
	 * @param proc : 过程名
	 * @param paramArr[] : 参数数组（可为空）
	 * @param returnType[] : 返回值类型数组（可为空，请用java的java.sql.Types包下的类型，如： java.sql.Types.BIGINT）
	 */
	public static <T> List<T> callList(Class<? extends Table> tableClass,String proc,Object[] paramArr,Integer[] returnTypeArr){
		return SQLFactory.getCall(getInstance(tableClass)).callList(proc, paramArr, returnTypeArr);
	}
	/**create**/
	public static boolean create(Class<? extends Table> tableClass,String sql){
		return SQLFactory.getCreate(getInstance(tableClass)).create(sql);
	}
	/**复制 : 当前表到新表（isCopyData=是否复制数据）**/
	public static boolean copy(Class<? extends Table> tableClass,String newTable,boolean isCopyData){
		return SQLFactory.getCreate(getInstance(tableClass)).copy(newTable, isCopyData);
	}
	/**drop**/
	public static boolean drop(Class<? extends Table> tableClass,String sql){
		return SQLFactory.getDrop(getInstance(tableClass)).drop(sql);
	}
	/**获取 : 表SQL**/
	public static String getTableSQL(Class<? extends Table> tableClass){
		return SQLFactory.getOther(getInstance(tableClass)).getTableSQL();
	}
	/**判断 : 当前tableClass对应的表是否存在**/
	public static boolean isTableExist(Class<? extends Table> tableClass){
		return SQLFactory.getOther(getInstance(tableClass)).isTableExist(TableUtil.getTable(tableClass));
	}
	/**判断 : 指定的table表是否存在**/
	public static boolean isTableExist(Class<? extends Table> tableClass,String table){
		return SQLFactory.getOther(getInstance(tableClass)).isTableExist(table);
	}
	/**获取 : 列名**/
	public static Set<String> getColumn(Class<? extends Table> tableClass){
		return SQLFactory.getOther(getInstance(tableClass)).getColumn();
	}
	
}