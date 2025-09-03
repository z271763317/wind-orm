 package org.test.action;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.test.model.Sex;
import org.test.model.Student;
import org.test.model.Test2;
import org.test.model.Test_varchar;
import org.test.model.parent.Model;
import org.test.other.RegexUtil;
import org.wind.orm.Table;
import org.wind.orm.bean.Page;
import org.wind.orm.util.TableUtil;


public class TestAction extends Thread{

	private static Class<Student> objClass=Student.class;		//主ORM类
	
	public static  void main(String[] args) {
		TestAction t=new TestAction();
		long startTime=System.currentTimeMillis();
		for(int i=0;i<1;i++){
//			t.start();
			t.run();
		}
		long endTime=System.currentTimeMillis();
		System.out.println("耗时："+(endTime-startTime));
	}
	//
	public void run(){
		try {
			runSQL();
//			callTest();
//			for(int t=0;t<2;t++){
//				for(int i=0;i<3;i++){
//					//System.out.println("---------【"+(i+1)+"】--------");
//					final int t_i=i;
//					new Thread(){
//						public void run(){
//							long startTime=System.currentTimeMillis();
//							Table.findById(objClass, 2,true);
//							long endTime=System.currentTimeMillis();
//							System.out.println("【"+(t_i+1)+"】耗时："+(endTime-startTime));
//						}
//					}.start();
//				}
//			}
			
			/*
			Class<StudentTest> tableClass=StudentTest.class;
			Table.setAutoCommit(tableClass, false);
			int size=10000000;
			System.out.println("---开始批量添加数据（"+size+"条）.....");
			Random r=new Random();
			int num=0;
			List<Table> saveList=new ArrayList<Table>(); 
			for(int i=0;i<size;i++){
				StudentTest t_obj=new StudentTest(r.nextLong(), r.nextLong(), RegexUtil.getDateRQ(), r.nextLong(), r.nextLong());
				saveList.add(t_obj);
				if(saveList.size()>1000){
					try{
						Table.save(saveList);
						Table.commit(tableClass);
					}catch(Exception e){
						e.printStackTrace();
					}
					saveList.clear();
					num++;
					System.out.println("第"+num+"次");
				}
			}
			Table.close(tableClass);
			System.out.println("---End...批量添加数据完成.....");
			*/
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//运行全功能SQL
	public void runSQL() throws Exception{
		Table.setAutoCommit(objClass, false);	//开启事务
//		Interceptor.register(TextInterceptor.class, SQLType.SELECT,SQLType.INSERT,SQLType.UPDATE);		//注册拦截器
		
		/***********************************【find区】***********************************/
		System.out.println("----------------------------【findById方法】------------------------------》");
		Student obj = Table.findById(objClass, 2);
		Table.findAllForeign(obj, false, null);		//查询所有的外键
		printObject(obj);
		
		System.out.println("----------------------------【findById方法——不取外键】------------------------------》");
		obj = Table.findById(objClass, 2);
		printObject(obj);
		
		System.out.println("----------------------------【findAll方法】---------------------------------》");
		List<? extends Model> list = Table.findAll(objClass,false);
		Table.findAllForeign(list, false, null);		//查询所有的外键
		printObjectList(list);
		
		System.out.println("----------------------------【findAll方法——分页】------------------------》");
		list = Table.findAll(objClass,false,new Page(2,2));
//		list = Table.findAll(objClass,false,true,new Page(-3,2,10));
		Table.findAllForeign(list, false, null);		//查询所有的外键
		printObjectList(list);
		
		System.out.println("----------------------------【findAll方法——指定排序的列名】------------------------》");
//		list = Table.findAll(objClass, false, true, null,"sexa",null);
		list = Table.findAll(objClass, false, new Page(1,10),"sexa",null);
		Table.findAllForeign(list, false, null);		//查询所有的外键
		printObjectList(list);
		
		System.out.println("----------------------------【find方法】-----------------------------------》");
		//obj.setId(null);
		Student obj_find=new Student();
//		obj_find.setDate("2015-06-16 15:48:32");
		Test2 test2=new Test2();
		test2.setTest2Id(1);
		obj_find.setTest2(test2);
		List<Student> t_list = obj_find.find(false,null);
		Table.findAllForeign(t_list, false, null);		//查询所有的外键
		printObjectList(t_list);
		
		System.out.println("----------------------------【find方法——不取外键】-----------------------------------》");
		t_list = obj_find.find(false,null);
		printObjectList(t_list);
		
		System.out.println("----------------------------【find方法——分页】---------------------------》");
//		t_list = obj_find.find(false,false,new Page(2,2));
		obj_find.setOrderSQL("sexId desc,id");
		t_list = obj_find.find(false,new Page(3,2));
		obj_find.setOrderSQL(null);
		Table.findAllForeign(t_list, false, null);		//查询所有的外键
		printObjectList(t_list);
		
		System.out.println("----------------------------【find方法——分页、正序、不取外键】---------------------------》");
		t_list=obj_find.find("sexId=2", null, false, new Page(3,2,6L));
		printObjectList(t_list);
		
		System.out.println("----------------------------【findByIdList方法】---------------------------》");
		List<Object> idList_select=new ArrayList<Object>();
		idList_select.add(2);
		idList_select.add(3);
		idList_select.add(4);
		List<? extends Table> list_idList=Table.findByIdList(objClass, idList_select);	
		printObjectList(list_idList);
		
		
		System.out.println("----------------------------【findByIdList方法——指定显示的列名】---------------------------》");
		list_idList=Table.findByIdList(objClass,idList_select, null,null,"date","sex");
		printObjectList(list_idList);
		
		System.out.println("----------------------------【findAllSize方法】---------------------------》");
		long size=Table.findAllSize(objClass);
		System.out.println("Student表总数："+size);
		
		System.out.println("----------------------------【findSize方法】---------------------------》");
		size=obj_find.findSize();
		System.out.println("查询条件总数："+size);
		
		System.out.println("----------------------------【findSize方法——带条件】---------------------------》");
		List<Object> t_size_tj=new ArrayList<Object>();
		t_size_tj.add(1L);
		size=Table.findSize(objClass,"sexid>?",t_size_tj);
		System.out.println("Student表总数（带条件）："+size);
		
		System.out.println("----------------------------【findSize方法——带条件，Map式】---------------------------》");
		Map<Long,Long> sizeMap=obj_find.findSizeMap("sex");
		System.out.println("Student表总数（带条件）："+sizeMap);
		
		System.out.println("----------------------------【findDate方法】---------------------------》");
		String date=Table.findDate(objClass);
		System.out.println("当前数据库日期时间："+date);
		
		System.out.println("----------------------------【findIdList方法】---------------------------》");
		List<Object> t_idList_tj=new ArrayList<Object>();
		t_idList_tj.add(2);
		List<Long> idList_findIdList=Table.findIdList(objClass, "sexIda=?", t_idList_tj);
		printIdList(idList_findIdList);
		
		System.out.println("----------------------------【findSpecifiedList方法】---------------------------》");
		List<Long> idList_findSpecifiedList=Table.findSpecifiedList(objClass, null, null,"sexa");
		printIdList(idList_findSpecifiedList);
		
		System.out.println("----------------------------【find方法（传SQL）】---------------------------》");
		List<Map<String,Object>> resultMapList_find=Table.find(objClass, "select sexIda as 't_sexId' from student", null);
		printList(resultMapList_find);
		
		 /***********************************【save区】***********************************/
		System.out.println("----------------------------【save方法1——插入】-------------------------》");
		obj.setDate(RegexUtil.getDate());
		obj.setId(null);
		obj.save();
		Object result_id=obj.getId();
		System.out.println("save1返回："+result_id);
		
		System.out.println("----------------------------【save方法1.1——插入（指定主键）】-------------------------》");
		obj.setDate(RegexUtil.getDate());
		obj.setId(System.currentTimeMillis());
		obj.save(true);
		result_id=obj.getId();
		System.out.println("save1.1返回："+result_id);
		
		System.out.println("----------------------------【save方法2——插入（批量）】-------------------------》");
		for(Model t_obj:list) {
			t_obj.setId(null);
		}
		Table.save(list, true);
		System.out.print("save2返回：");
		List<Object> t_idList=printORMIdList(list);
		
		System.out.println("----------------------------【save方法3——插入（批量，自动选择）】-------------------------》");
		int resultSize=Table.save(list);		//上面已插入，这次是更新
		System.out.println("save3返回："+resultSize);
		
		System.out.println("----------------------------【save方法4——插入（主键：UUID）】-------------------------》");
		Test_varchar t_obj_varchar=new Test_varchar();
		t_obj_varchar.setName(RegexUtil.getRandEngLish(10));
		int resultSize_uuid=t_obj_varchar.save();
		System.out.println("save4返回："+resultSize_uuid+"；主键："+t_obj_varchar.getId());
		
		System.out.println("----------------------------【save方法5——更新（批量）】-------------------------》");
		resultSize=Table.save(list,false);		//上面已插入，这次是更新
		System.out.println("save5返回："+resultSize);		
		
		System.out.println("----------------------------【update方法1】-------------------------》");
		List<Object> t_updateList=new ArrayList<Object>();
		t_updateList.add(RegexUtil.getDateRQ());
		t_updateList.add(1);
		int updateResultSize=Table.update(objClass, "date=?", "sexIda=?", t_updateList);
		System.out.println("update方法1返回："+updateResultSize);
			
		/***********************************【delete区】***********************************/
		System.out.println("----------------------------【delete方法1——根据ID列表（List式）】---------------------------------》");
		int result=Table.delete(objClass,t_idList);
		System.out.println("delete2返回："+result);
		
		System.out.println("----------------------------【delete方法2——根据对象字段是否有值】---------------------------------》");
//		obj=new Student();
		obj.setDate(RegexUtil.getDateRQ());
		obj.setId(null);
		obj.setCreateTime(null);
		obj.setUpdateTime(null);
		result=obj.save();
		result=obj.delete();
		System.out.println("delete4返回："+result);
		
		System.out.println("----------------------------【delete方法3——根据ID】---------------------------------》");
		result=Table.delete(objClass, result_id);
		System.out.println("delete5返回："+result);
		
		/***********************************【sum区】***********************************/
		System.out.println("----------------------------【sum方法1——所有数据】---------------------------------》");
		Number t_sum=Table.sum(objClass, "sexa");
		System.out.println(t_sum.doubleValue());
		
		System.out.println("----------------------------【sum方法2——带条件数据】---------------------------------》");
		List<Object> tjList_sum=new ArrayList<Object>();
		tjList_sum.add(2);
		Number t_sum_tj=Table.sum(objClass, "test2Id=?",tjList_sum,"sexa");
		System.out.println(t_sum_tj.longValue());
		
		System.out.println("----------------------------【sum方法3——带条件、分组列】---------------------------------》");
		Map<Object,Number> t_sumMap=Table.sum(objClass, "test2Id=?", tjList_sum, "sexa", "sex");
		System.out.println(t_sumMap);
		
		/***********************************【avg区】***********************************/
		System.out.println("----------------------------【avg方法1——所有数据】---------------------------------》");
		Number t_avg=Table.avg(objClass, "sexa");
		System.out.println(t_avg.doubleValue());
		
		System.out.println("----------------------------【avg方法2——带条件数据】---------------------------------》");
		List<Object> tjList_avg=new ArrayList<Object>();
		tjList_avg.add(2);
		Number t_avg_tj=Table.avg(objClass, "test2Id=?",tjList_avg,"sexa");
		System.out.println(t_avg_tj.longValue());
		
		System.out.println("----------------------------【avg方法3——带条件、分组列】---------------------------------》");
		Map<Object,Number> t_avgMap=Table.avg(objClass, "test2Id=?", tjList_avg, "sexa", "sex");
		System.out.println(t_avgMap);
		
		/***********************************【max区】***********************************/
		System.out.println("----------------------------【max方法1——所有数据】---------------------------------》");
		Number t_max=Table.max(objClass, "sexa");
		System.out.println(t_max.doubleValue());
		
		System.out.println("----------------------------【max方法1——所有数据（字符串）】---------------------------------》");
		String t_maxString=Table.maxString(objClass, "sexa");
		System.out.println(t_maxString+"【"+t_maxString.getClass().getSimpleName()+"】");
		
		System.out.println("----------------------------【max方法2——带条件数据】---------------------------------》");
		List<Object> tjList_max=new ArrayList<Object>();
		tjList_max.add(2);
		Number t_max_tj=Table.max(objClass, "test2Id=?",tjList_max,"sexa");
		System.out.println(t_max_tj);
		
		System.out.println("----------------------------【max方法2——带条件数据（字符串）】---------------------------------》");
		String t_maxString_tj=Table.maxString(objClass, "test2Id=?",tjList_max,"sexa");
		System.out.println(t_maxString_tj+"【"+t_maxString_tj.getClass().getSimpleName()+"】");
		
		System.out.println("----------------------------【max方法3——带条件、分组列】---------------------------------》");
		Map<Object,Number> t_maxMap=Table.max(objClass, "test2Id=?", tjList_max, "sexa", "sex");
		System.out.println(t_maxMap);
		
		System.out.println("----------------------------【max方法3——带条件、分组列（字符串）】---------------------------------》");
		Map<Object,String> t_maxStringMap=Table.maxString(objClass, "test2Id=?", tjList_max, "sexa", "sex");
		System.out.println(t_maxStringMap);
		
		/***********************************【min区】***********************************/
		System.out.println("----------------------------【min方法1——所有数据】---------------------------------》");
		Number t_min=Table.min(objClass, "sexa");
		System.out.println(t_min.longValue());
		
		System.out.println("----------------------------【min方法1——所有数据（字符串）】---------------------------------》");
		String t_minString=Table.minString(objClass, "sexa");
		System.out.println(t_minString+"【"+t_minString.getClass().getSimpleName()+"】");
		
		System.out.println("----------------------------【min方法2——带条件数据】---------------------------------》");
		List<Object> tjList_min=new ArrayList<Object>();
		tjList_min.add(2);
		Number t_min_tj=Table.min(objClass, "test2Id=?",tjList_min,"sexa");
		System.out.println(t_min_tj);
		
		System.out.println("----------------------------【min方法2——带条件数据（字符串）】---------------------------------》");
		String t_minString_tj=Table.minString(objClass, "test2Id=?",tjList_min,"sexa");
		System.out.println(t_minString_tj+"【"+t_minString_tj.getClass().getSimpleName()+"】");
		
		System.out.println("----------------------------【min方法3——带条件、分组列】---------------------------------》");
		Map<Object,Number> t_minMap=Table.min(objClass, "test2Id=?", tjList_min, "sexa", "sex");
		System.out.println(t_minMap);
		
		System.out.println("----------------------------【min方法3——带条件、分组列（字符串）】---------------------------------》");
		Map<Object,String> t_minStringMap=Table.minString(objClass, "test2Id=?", tjList_min, "sexa", "sex");
		System.out.println(t_minStringMap);
		
		System.out.println("----------------------------【create——复制表】---------------------------------》");
		boolean isSuccess_copy=Table.copy(Sex.class, "sex2", false);
		System.out.println(isSuccess_copy);
		
		System.out.println("----------------------------【create——复制表（含数据）】---------------------------------》");
		isSuccess_copy=Table.copy(Sex.class, "sex3", true);
		System.out.println(isSuccess_copy);
		
		System.out.println("----------------------------【drop】---------------------------------》");
		boolean isSuccess_drop=Table.drop(Sex.class, "drop table sex2");
		System.out.println("【删除表sex2】："+isSuccess_drop);
		isSuccess_drop=Table.drop(Sex.class, "drop table sex3");
		System.out.println("【删除表sex3】："+isSuccess_drop);
		
		System.out.println("----------------------------【获取表SQL】---------------------------------》");
		String t_tableSQL=Table.getTableSQL(objClass);
		System.out.println(t_tableSQL);
		
		System.out.println("----------------------------【判断当前表是否存在1】---------------------------------》");
		boolean isTableExist=Table.isTableExist(objClass);
		System.out.println(isTableExist);
		
		System.out.println("----------------------------【判断指定表是否存在2】---------------------------------》");
		isTableExist=Table.isTableExist(objClass,"major1");
		System.out.println(isTableExist);
		
		System.out.println("----------------------------【获取列名】---------------------------------》");
		Set<String> colSet=Table.getColumn(objClass);
		System.out.println(colSet);
		
		/********事务处理*****/
		Table.commit(objClass);
		Table.close(objClass);
	}
	
	public static void printObject(Object obj){
		if(obj!=null){
			Field propertyArr[]=TableUtil.getField(obj.getClass());
			AccessibleObject.setAccessible(propertyArr, true);						//跳过访问权限
			for(int i = 0;i<propertyArr.length; i++){
				Field property=propertyArr[i];
				try {
					//不打印serialVersionUID
					if(!property.getName().equalsIgnoreCase("serialVersionUID")){
						Object t_obj=property.get(obj);
						String t_id=null;
						if(t_obj instanceof Table){
							t_id=TableUtil.getPrimaryKeyValue(t_obj)+"";
						}
						String t_idStr="";
						if(t_id!=null && t_id.length()>0){
							t_idStr="【"+t_id+"】";
						}
						System.out.print(property.getName()+"="+t_obj+t_idStr+"；");
					}
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
			System.out.println();
		}
	}
	//打印对象列表
	public static void printObjectList(List<? extends Table> list){
		for(Table t:list){
			printObject(t);
		}
	}
	//多个用【,】分隔
	public static String getIdStr(List<Object> list){
		String s="";
		for(int i=0;i<list.size();i++){
			s+=list.get(i)+",";
		}
		return s;
	}
	//打印ORM的id数据列表
	public static List<Object> printORMIdList(List<? extends Model> list){
		List<Object> t_idList=new ArrayList<Object>();
		if(list!=null && list.size()>0){
			for(int i=0;list!=null && i<list.size();i++){
				t_idList.add(list.get(i).getId());
				System.out.print(list.get(i).getId()+",");
			}
			System.out.println();
		}
		return t_idList;
	}
	//打印id数据列表
	public static void printIdList(List<? extends Object> list){
		if(list!=null && list.size()>0){
			for(int i=0;list!=null && i<list.size();i++){
				System.out.print(list.get(i)+",");
			}
			System.out.println();
		}
	}
	//打印数据列表
	public static void printList(List<? extends Object> list){
		if(list!=null && list.size()>0){
			for(int i=0;list!=null && i<list.size();i++){
				System.out.println(list.get(i));
			}
		}
	}
	//存储过程测试
	public static void callTest() {
//		String s=Table.call(Order.class, "generateSerialNumber", null, java.sql.Types.VARCHAR);
//		System.out.println(s);
		TreeMap<String, Integer> map=cs();
		for(Entry<String,Integer> entry:map.entrySet()){
			System.out.println(entry.getKey()+"——"+entry.getValue());
		}
	}
	public static synchronized TreeMap<String, Integer> cs(){
		final TreeMap<String, Integer> map=new TreeMap<String, Integer>();
		int i=100;
		while(i>0){
			new Thread(){
				public void run(){
					String s=Table.call(objClass, "generateSerialNumber", null, java.sql.Types.VARCHAR);
					System.out.println(s);
					Integer t_s=map.get(s);
					if(t_s==null){
						t_s=1;
					}else{
						t_s++;
					}
					map.put(s, t_s);
				}
			}.start();
			i--;
		}
		while(map.size()<100){
//			System.out.println(map.size());
		}
		return map;
	}
}