package org.wind.orm.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.wind.orm.Table;
import org.wind.orm.annotation.Column;
import org.wind.orm.annotation.DataSource;
import org.wind.orm.annotation.ForeignKey;
import org.wind.orm.annotation.Id;
import org.wind.orm.annotation.Tables;
import org.wind.orm.annotation.Value;
import org.wind.orm.bean.Config;
import org.wind.orm.bean.Page;
import org.wind.orm.cache.Cache2;
import org.wind.orm.exception.TableException;


/**
 * @描述 : Table常用工具类
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年5月15日 13:49:23
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class TableUtil {
	
	/*****缓存*****/
	private static final Map<Class<?>,Field> primaryKeyFieldMap=new ConcurrentHashMap<>();	//主键字段
	private static final Map<Class<?>,Field[]> fieldMap=new ConcurrentHashMap<>();		//所有字段（含深层次父类）
	private static final Map<Class<?>,Map<String,Method>> methodMap_set=new ConcurrentHashMap<>();		//set方法（含深层次父类）
	private static final Map<Class<?>,Map<String,Method>> methodMap_get=new ConcurrentHashMap<>();		//get方法（含深层次父类）	
	
	/**获取 : 前缀为"set"的Method（缓存）**/
	public static Map<String,Method> getMethodMap_set(Class<?> tableClass){
		Map<String,Method> map=methodMap_set.get(tableClass);
		if(map==null) {
			map=getMethodMap(tableClass, Table.class, "set");
			methodMap_set.put(tableClass, map);
		}
		return map;
	}
	/**获取 : 前缀为"get"的Method（缓存）**/
	public static Map<String,Method> getMethodMap_get(Class<?> tableClass){
		Map<String,Method> map=methodMap_get.get(tableClass);
		if(map==null) {
			map=getMethodMap(tableClass, Table.class, "get");
			methodMap_get.put(tableClass, map);
		}
		return map;
	}
	/**
	 * 获取 : 前缀为prefix的Method（Map式）
	 * @param tableClass : 指定要获取的类
	 * @param parentClass : 停止寻找的父类（不处理该类）
	 * @param prefix : 方法名前缀，可模糊取prefix匹配的方法,传null则不验证
	 * @return 格式 : key=（小写）方法名+所有参数类型名；value=Method对象
	 */
	public static Map<String,Method> getMethodMap(Class<?> tableClass,Class<?> parentClass,String prefix){
		Map<String,Method> methodMap=new HashMap<String,Method>();
		Class<?> t_parentClass = tableClass;
		while (t_parentClass!= null && t_parentClass!= parentClass) {
			Method t_methodArr[] = t_parentClass.getDeclaredMethods();
			for (Method m : t_methodArr) {
				//排除静态方法
  	            if(!Modifier.isStatic(m.getModifiers())) {
					StringBuffer key = new StringBuffer(m.getName());
					if(prefix==null || key.indexOf(prefix)==0) {
						Class<?> paramArr[] = m.getParameterTypes();
						for (int j = 0; j < paramArr.length; j++) {
							key.append(paramArr[j].getSimpleName());
						}
						//是否不存在
						if (!methodMap.containsKey(key.toString().toLowerCase())) {
							m.setAccessible(true);
							methodMap.put(key.toString().toLowerCase(), m);
						}
					}
  	            }
			}
			t_parentClass = t_parentClass.getSuperclass();
		}
		return methodMap;
	}
	/**
	 * 获取：设置Method参数NULL值，Object数组式返回
	 * @详解 : 假如m方法为setTest(String s1,Integer int2);<br>
	 * 				则返回Object objArr[2]=[null,null];其中：<br>
	 * 				objArr[0]等于String的null<br>
	 * 				objArr[1]等于Integer的null
	 * @param m : Method对象
	 * @return 返回指定的Method对象的所有参数，组成数组参数
	 */
	public static Object[] getParameterNull(Method m){
		if(m!=null){
			Object paramArr[]=new Object[m.getParameterTypes().length];
			for(int i=0;i<paramArr.length;i++){
				paramArr[i]=null;
			}
			return paramArr;
		}else{
			return null;
		}
	}
	/**
	 * 【get方法】：反射执行对象get方法的Method后的返回值
	 * @param obj : java对象
	 * @param methodMap : obj的所有Method对象，并且key是以"get"+【小写f字段名】
	 * @param f : obj的一个属性Field对象
	 * @return 返回反射执行getXXX()方法后的返回值
	 */
	public static Object get(Object obj,Map<String,Method> methodMap,Field f){
		Method m=methodMap.get("get"+f.getName().toLowerCase());
		Object paramArr[]=getParameterNull(m);
		try {
			if(paramArr!=null && obj!=null){
				return m.invoke(obj, paramArr);
			}else{
				return null;
			}
		} catch (Exception e) {
			throw new TableException(e.getMessage(),e);
		}
	}
	/**
	 * 【set方法】：反射执行对象set方法的Method后的返回值
	 * @param obj : java对象
	 * @param methodMap : obj的所有Method对象，并且key是以"set"+【小写f字段名】
	 * @param f : obj的一个属性Field对象
	 * @papram paramValue : 参数值
	 * @return 返回反射执行setXXX()方法后的返回值
	 */
	public static Object set(Object obj,Map<String,Method> methodMap,Field field,Object paramValue){
		if(field!=null){
			StringBuffer menthod=new StringBuffer("set"+field.getName());
			Class<?> t_class=field.getType();
			menthod.append(t_class.getSimpleName());
			menthod=new StringBuffer(menthod.toString().toLowerCase());
			Method m=methodMap.get(menthod.toString());
			try {
				Object paramValueArr[]={paramValue};
				return m.invoke(obj, paramValueArr);
			} catch (Exception e) {
				throw new TableException(e.getMessage(),e);
			}
		}else{
			return null;
		}
	}
	/**设置：obj主键值**/
	public static void setPrimaryKeyValue(Object obj,Object value){
		if(obj!=null){
			Class<?> tableClass=obj.getClass();
			Field f=getPrimaryKeyField(tableClass);
			if(f!=null) {
				Map<String,Method> methodMap=getMethodMap_set(tableClass);
				TableUtil.set(obj, methodMap, f, value);
			}
		}
	}
	/**获取：obj主键值**/
	public static Object getPrimaryKeyValue(Object obj){
		if(obj!=null){
			Class<?> tableClass=obj.getClass();
			Field f=getPrimaryKeyField(tableClass);
			if(f!=null){
				Map<String,Method> methodMap=getMethodMap_get(tableClass);	//获取该tableClass所有的Method
				return TableUtil.get(obj, methodMap, f);
			}
		}
		return null;
	}
	
	/**获取 : tableClass所映射的主键@Id字段**/
	public static Field getPrimaryKeyField(Class<?> tableClass){
		Field field=primaryKeyFieldMap.get(tableClass);
		if(field==null) {
			Field fArr[]=getField(tableClass);
			for(int i=0;fArr!=null && i<fArr.length;i++){
				Field f=fArr[i];
				if(f.isAnnotationPresent(Id.class)){
					primaryKeyFieldMap.put(tableClass, f);
					return f;
				}
			}
			return null;
		}else{
			return field;
		}
	}
	/**
	 * 对象类型转换（含：基础数据类型、String、Boolean、cast强转）
	 * @param source : 源对象
	 * @param converTypeClass : 目标类型Class
	 * @return 返回目标类型对象
	 */
	public static Object cast(Object source,Class<?> dstTypeClass){
		Object dstObj=null;
		if(source!=null && dstTypeClass!=null){
			if(source instanceof Number){
				Number number=(Number)source;
				if(dstTypeClass==Byte.class){
					dstObj=number.byteValue();
				}else if(dstTypeClass==Double.class){
					dstObj=number.doubleValue();
				}else if(dstTypeClass==Float.class){
					dstObj=number.floatValue();
				}else if(dstTypeClass==Integer.class){
					dstObj=number.intValue();
				}else if(dstTypeClass==Long.class){
					dstObj=number.longValue();
				}else if(dstTypeClass==Short.class){
					dstObj=number.shortValue();
				}
			}
			//不是Number、基础数据类型
			if(dstObj==null){
				if (dstTypeClass.isAssignableFrom(source.getClass())){
					dstObj=dstTypeClass.cast(source);
				//String
				}else if(dstTypeClass==String.class){
					dstObj=source.toString();
				//Integer
				}else if(dstTypeClass==Integer.class){
					dstObj=Integer.parseInt(source.toString());
				//Long
				}else if(dstTypeClass==Long.class){
					dstObj=Long.parseLong(source.toString());
				//Float
				}else if(dstTypeClass==Float.class){
					dstObj=Float.parseFloat(source.toString());
				//Double
				}else if(dstTypeClass==Double.class){
					dstObj=Double.parseDouble(source.toString());
				//Byte
				}else if(dstTypeClass==Byte.class){
					dstObj=Byte.parseByte(source.toString());
				//Short
				}else if(dstTypeClass==Short.class){
					dstObj=Short.parseShort(source.toString());
				//Boolean
				}else if(dstTypeClass==Boolean.class){
					dstObj=Boolean.parseBoolean(source.toString());
				}else{
					dstObj=source;
				}
			}
		}else{
			dstObj=source;
		}
		return dstObj;
	}
	
	/**
	 * 获取 : Class类映射的数据源（数据库）的id值，如果没有@DataSource注解或value()为空，则返回null。<br />
	 * 			 若本类没有，则寻找父类；若父类没有，则继续寻找父类的父类.....直到有或父类为Table为止
	 * @param tableClass : 带有DataSource注解的Class（本类没有，则寻找父类，深层次到Table为止）
	 */
	public static String getDataSource(Class<? extends Table> tableClass){
		if(tableClass!=null){
			DataSource dataSource=getAnnotation(tableClass, DataSource.class);
			if(dataSource!=null && dataSource.value()!=null && dataSource.value().trim().length()>0){
				return dataSource.value().trim();
			}
		}
		return null;
	}
	/**获取 : 指定类指定注解所在的类或父类，若该类没有则去寻找深层次的父类，直到Table类为止**/
	public static Class<? extends Table> getClass(Class<? extends Table> tableClass,Class<? extends Annotation> anClass){
		Class<?> t_tableClass=tableClass;
		//没有anClass注解
		while(!t_tableClass.isAnnotationPresent(anClass)){
			Class<?> t_tableClass_parent=t_tableClass.getSuperclass();
			//若该父类是属于Table
			if(Table.class.isAssignableFrom(t_tableClass_parent)){
				t_tableClass= t_tableClass_parent;
			}else{
				return null;
			}
		}
		return (Class<? extends Table>) t_tableClass;
	}
	/**获取 : 指定类指定的注解的对象，若该类没有则取寻找深层次的父类，直到Table类为止**/
	public static <T extends Annotation> T getAnnotation(Class<? extends Table> tableClass,Class<T> anClass){
		Class<? extends Table> resultClass=getClass(tableClass, anClass);
		return resultClass!=null?resultClass.getAnnotation(anClass):null;
	}
	/**
	 * 获取 : 数据源（连接池）Config配置对象，对象式
	 * @param obj : 继承Table类的对象
	 */
	public static Config getConfig(Table obj){
		if(obj!=null){
			return getConfig(getDataSource(obj.getClass()));
		}else{
			return getConfig(Cache2.dataSource_default);
		}
	}
	/**
	 * 获取 : 数据源（连接池）Config配置对象
	 * @param tableClass : 继承Table类的Class类
	 */
	public static Config getConfig(Class<? extends Table> tableClass){
		return getConfig(getDataSource(tableClass));
	}
	/**
	 * 获取 : 数据源（连接池）Config配置对象，根据数据库连接池id，默认‘default’配置
	 * @param poolName : 连接池id
	 */
	public static Config getConfig(String poolName){
		if(poolName==null){
			poolName=Cache2.dataSource_default;
		}
		return Cache2.getConfig(poolName);
	}
	/**
	 * 获取 : Class类映射的表名，如果没有Tables注解或value()为空，则返回前缀+类名（注：不带表全路径，只单纯的返回表名）
	 * @param tableClass : 带有Tables注解的Class
	 */
	public static String getTable(Class<? extends Table> tableClass){
		if(tableClass.isAnnotationPresent(Tables.class)){
			Tables tables=tableClass.getAnnotation(Tables.class);
			if(tables.value()!=null && tables.value().trim().length()>0){
				return tables.value().trim();
			}
		}
		return tableClass.getSimpleName();
	}
	/**
	 * 获取 : tableClass所映射的主键名。如果没有Column注解或value()为空，则返回列前缀+字段名（注：前缀是由@ColumnPrefix设置）
	 * @param tableClass : 继承Table的类
	 */
	public static String getPrimaryKey(Class<? extends Table> tableClass){
		Field idFiled=getPrimaryKeyField(tableClass);
		if(idFiled!=null) {
			Column col=idFiled.getAnnotation(Column.class);
			String t_col_value=col!=null?col.value().trim():null;
			if(t_col_value!=null && t_col_value.length()>0){
				return t_col_value;
			}
			return idFiled.getName();
			
		}
		return null;
	}
	/**获取 : List字段的泛型Class**/
	public static Class<?> getGeneric(Field genericField){
		//List类型
		if(genericField!=null && List.class.isAssignableFrom(genericField.getType())){
             Type fc = genericField.getGenericType(); // 关键的地方，如果是List类型，得到其Generic的类型  
             if(fc instanceof ParameterizedType){
            	 ParameterizedType pt = (ParameterizedType) fc;  
            	 Type tArr[]=pt.getActualTypeArguments();
            	 Type type=tArr[0];
            	 return (Class<?>)type;
             }
		}
        return null;
	}
	/**
  	 * bean对象属性浅拷贝，忽略null对象
  	 * @param soure : 源对象，不能为空
  	 * @param target : 目标对象，不能为空
  	 */
  	public static void copyProperties(Object source,Object target){
  		if(source!=null && target!=null){
  			Field fieldArr_source[]=getField(source.getClass());
  			Field fieldArr_target[]=getField(target.getClass());
  			AccessibleObject.setAccessible(fieldArr_source, true);	//跳过访问权限
  			AccessibleObject.setAccessible(fieldArr_target, true);	//跳过访问权限
  			Map<String,Field> fieldMap_target=new HashMap<String,Field>();
  			for(Field f:fieldArr_target){
  				fieldMap_target.put(f.getName(),f);
  			}
  			for(Field filed_source:fieldArr_source){
  				String modifier=Modifier.toString(filed_source.getModifiers());	//修饰名
  				if(modifier.indexOf("static")==-1){
	  				String fieldName_soure=filed_source.getName();
	  				Field field_target=fieldMap_target.get(fieldName_soure);
	  				//存在字段
	  				if(field_target!=null){
	  					try {
							Object value_source=filed_source.get(source);
							//值不为空
							if(value_source!=null){
								field_target.set(target, value_source);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
	  				}
  				}
  			}
  		}
  	}
  	/**获取：指定类的所有Field字段（默认），直到父类为Table为止**/
  	public static Field[] getField(Class<?> tableClass){
  		Field fieldArr[]=fieldMap.get(tableClass);
  		if(fieldArr==null) {
  			fieldArr=getField(tableClass, Table.class);
  			fieldMap.put(tableClass, fieldArr);
  		}
  		return fieldArr;
  	}
  	/**
  	 * 获取：指定类的所有Field字段，包含父类，直到指定父类停止（不在获取）
  	 * @param tableClass : 要获取所有Field字段的Class类
  	 * @param parentClass : 指定要停止获取父类Field的Class（为空则一直获取到底）
  	 * @return tableClass为空则返回null
  	 */
  	public static Field[] getField(Class<?> tableClass,Class<?> parentClass){
  		if(tableClass!=null){
	  		if(parentClass!=null){
	  			Map<String,Field> fieldMap=new LinkedHashMap<String, Field>();		//key=字段名称
	  			Class<?> t_parentClass=tableClass;
	  			while(t_parentClass!=null && t_parentClass!=parentClass){
	  				Field t_fieldArr[]=t_parentClass.getDeclaredFields();
		  			for(Field f:t_fieldArr){
		  				//排除静态字段
		  	            if (!Modifier.isStatic(f.getModifiers())) {
			  				String t_fieldName=f.getName();
			  				//若不存在
			  				if(!fieldMap.containsKey(t_fieldName)){
			  					fieldMap.put(t_fieldName,f);
			  				}
		  	            }
		  			}
		  			t_parentClass=t_parentClass.getSuperclass();
	  			}
	  			Field fArr[]=new Field[fieldMap.size()];
	  			int i=0;
	  			for(Field t_field:fieldMap.values()){
	  				fArr[i]=t_field;
	  				i++;
	  			}
	  			return fArr;
	  		}else{
	  			return tableClass.getDeclaredFields();	
	  		}
  		}else{
  			return null;
  		}
  	}
  	/**
	 * 获取：指定ORM类指定字段名称的Field字段对象
	 * @param fieldName : 字段名称
	 * @return 返回Field字段对象
	 */
	public static Field getField(Class<?> tableClass,String fieldName){
		Field fieldArr[]=TableUtil.getField(tableClass);
		for(Field t_f:fieldArr){
			String t_name=t_f.getName();
			if(t_name.equalsIgnoreCase(fieldName)){
				return t_f;
			}
		}
		return null;
	}
  	/**获取指定类的所有Method方法（默认）**/
	public static Method[] getMethod(Class<?> tableClass){
		return getMethod(tableClass, Table.class);
	}
	/**
  	 * 获取指定类的所有Method方法，包含父类，直到指定父类停止（不在获取）
  	 * @param tableClass : 要获取所有Method字段的Class类
  	 * @param parentClass : 指定要停止获取父类Method的Class（为空则一直获取到底）
  	 * @return tableClass为空则返回null
  	 */
	public static Method[] getMethod(Class<?> tableClass,Class<?> parentClass){
  		if(tableClass!=null){
	  		if(parentClass!=null){
	  			List<Method> list=new ArrayList<Method>();
	  			Map<String,Method> methodMap=new LinkedHashMap<String, Method>();		//key=方法名称
	  			Class<?> t_parentClass=tableClass;
	  			while(t_parentClass!=null && t_parentClass!=parentClass){
	  				Method t_methodArr[]=t_parentClass.getDeclaredMethods();
		  			for(Method m:t_methodArr){
		  				list.add(m);
		  				StringBuffer key=new StringBuffer(m.getName());
		  				Class<?> paramArr[]=m.getParameterTypes();
						for(int j=0;j<paramArr.length;j++){
							key.append(paramArr[j].getSimpleName());
						}
						//是否不存在
						if(!methodMap.containsKey(key.toString())){
							methodMap.put(key.toString(), m);
						}
		  			}
		  			t_parentClass=t_parentClass.getSuperclass();
	  			}
	  			Method mArr[]=new Method[methodMap.size()];
	  			int i=0;
	  			for(Method t_m:methodMap.values()){
	  				mArr[i]=t_m;
	  				i++;
	  			}
	  			return mArr;
	  		}else{
	  			return tableClass.getMethods();	
	  		}
  		}else{
  			return null;
  		}
  	}
  	/**获取 : 指定ORM类指定外键ORM类的Field字段（不推荐用，因为会重复）**/
  	public static Field getForeignKeyField(Class<? extends Table> tableClass,Class<? extends Table> foreignClass){
  		if(tableClass!=null && foreignClass!=null){
	  		Field fArr[]=getField(tableClass);
			for(int i=0;fArr!=null && i<fArr.length;i++){
				Field f=fArr[i];
				Class<?> t_fieldClass=f.getType();
				if(f.isAnnotationPresent(ForeignKey.class) && t_fieldClass.equals(foreignClass)){
					return f;
				}
			}
			throw new TableException("表类"+tableClass.getSimpleName()+"没有设置外键"+foreignClass.getSimpleName()+"Field字段");
  		}else{
  			return null;
  		}
  	}
  	/**
	 * 获取 : 指定ORM类对象列表的主键id列表
	 * @param list : 继承Table类的对象列表，泛型请指定是同一个类
	 */
	public static List<Object> getIdList(List<? extends Table> list){
		List<Object> idList=new ArrayList<Object>();
		if(list!=null && list.size()>0){
			Table obj=list.get(0);
			Field idField=TableUtil.getPrimaryKeyField(obj.getClass());
			if(idField!=null){
				Map<String, Method> methodMap=getMethodMap_get(obj.getClass());
				for(Table t_obj:list){
					Object idObj=TableUtil.get(t_obj, methodMap, idField);
					if(idObj!=null){
						idList.add(idObj);
					}
				}
			}
		}
		return idList;
	}
	/**
	 * 获取 : 指定ORM类对象列表指定字段名称的值列表
	 * @param list : 继承Table类的对象列表，泛型请指定是同一个类
	 * @param specifiedFieldName : 指定字段的名称（若该字段是外键，则取外键的id）
	 */
	public static <T> List<T> getSpecifiedList(List<? extends Table> list,String specifiedFieldName){
		List<Object> valueList=new ArrayList<Object>();
		if(list!=null && list.size()>0 && specifiedFieldName!=null){
			Table obj=list.get(0);
			Field t_field=TableUtil.getField(obj.getClass(), specifiedFieldName);
			if(t_field!=null){
				boolean isForeignKey=t_field.isAnnotationPresent(ForeignKey.class);		//是否外键
				Class<?> genericClass=TableUtil.getGeneric(t_field);		//泛型Class
				boolean isForeignGeneric=false;		//是否外键泛型
				Map<String, Method> methodMap_foreign=null;		//外键Class的所有method
				Field idField_foreign=null;		//外键Class的主键字段
				//泛型_外键
				if(genericClass!=null && Table.class.isAssignableFrom(genericClass)){
					isForeignGeneric=true;
					methodMap_foreign=getMethodMap_get(genericClass);
					idField_foreign=getPrimaryKeyField(genericClass);
				//外键
				}else if(isForeignKey){
					methodMap_foreign=getMethodMap_get(t_field.getType());
					idField_foreign=getPrimaryKeyField(t_field.getType());
				}
				Map<String, Method> methodMap=getMethodMap_get(obj.getClass());
				for(Table t_obj:list){
					Object t_value=TableUtil.get(t_obj, methodMap, t_field);
					if(t_value!=null){
						//泛型
						if(isForeignGeneric){
							List<? extends Table> t_t_valueList=(List<? extends Table>) t_value;
							for(int g=0;g<t_t_valueList.size();g++){
								Table t_table_g=t_t_valueList.get(g);
								Object t_t_id=TableUtil.get(t_table_g, methodMap_foreign, idField_foreign);
								if(t_t_id!=null){
									valueList.add(t_t_id);
								}
							}
						}else{
							//外键
							 if(isForeignKey){
								t_value=TableUtil.get(t_value, methodMap_foreign, idField_foreign);
							 }
							 if(t_value!=null){
								valueList.add(t_value);
							 }
						}
					}
				}
			}
		}
		return (List<T>) valueList;
	}
	/**
	 * 获取 : field所映射的外键名（优先@Column注解的）
	 * @param field : 带有ForeignKey注解的字段
	 * @return 返回映射的外键名，如果没有，则取：【一对一】=取映射的主键名；【一对多】=取该字段所属的Class的主键名
	 */
	public static String getForeignKey(Field field){
		if(field!=null){
			if(field.isAnnotationPresent(ForeignKey.class)){
				Column an=field.getAnnotation(Column.class);
				if(an!=null && an.value().trim().length()>0){
					return an.value().trim();
				}
				Class<?> genericClass=TableUtil.getGeneric(field);		//List泛型的Class
				Class<?> tableClass=field.getType();
				//泛型
				if(genericClass!=null && Table.class.isAssignableFrom(genericClass)){
					return getPrimaryKey((Class<? extends Table>) field.getDeclaringClass());
				}else if(Table.class.isAssignableFrom(tableClass)){
					return getPrimaryKey((Class<? extends Table>) tableClass);
				}
			}
			throw new TableException("成员变量"+field.getName()+"不是外键映射的变量");
		}else{
			return null;
		}
	}
	/**
	 * 获取：Field类映射的列名，如果没有Column注解或value()为空，则返回Field字段名
	 * @param colField : 带有Column注解的Field
	 */
	public static String getColumn(Field colField){
		if(colField.isAnnotationPresent(Column.class)){
			Column col=colField.getAnnotation(Column.class);
			if(col.value()!=null && col.value().trim().length()>0){
				return col.value().trim();
			}
		}
		return colField.getName();
	}
	/**获取：Field类映射的列的值字符形式（若拥有@Id注解，则不会取@Value注解的值）**/
	public static String getColumnValueStr(Field colField){
		String t_value_str="?";		//默认为：？
		if(!colField.isAnnotationPresent(Id.class)) {
			Value t_an_value=colField.getAnnotation(Value.class);
			if(t_an_value!=null){
				t_value_str=t_an_value.value();
			}
		}
		return t_value_str;
	}
	/**Object数组转List**/
	public static List<Object> arrayConverList(Object objArr[]){
		List<Object> objList=new ArrayList<Object>();
		for(int i=0;objArr!=null && i<objArr.length;i++){
			objList.add(objArr[i]);
		}
		return objList;
	}
	/**
	 * 对list数据重新排序，并返回新的列表——根据传来的idList（id）列表顺序
	 * @param list : 需要排序的继承Table的ORM对象列表
	 * @param idList : 主键值，根据此参数值的顺序排序
	 * @exception 抛出RuntimeException异常，一般是get方法反射错误
	*/
	public static <T extends Table> List<T> sort(List<T> list,List<? extends Object> idList){
		if(list!=null && list.size()>0 && idList!=null && idList.size()>0){
			Table obj=(Table) list.get(0);
			Class<? extends Table> tableClass=obj.getClass();
			Field idField=TableUtil.getPrimaryKeyField(tableClass);		//主键ID字段
			Map<String,Method> methodMap=getMethodMap_get(tableClass);
			Method t_method=methodMap.get("get"+idField.getName().toLowerCase());
			//找到了排序的字段get方法
			if(t_method!=null){
				//重新排序（按id的顺序）
				Map<Object,List<Table>> t_tableMap=new HashMap<Object,List<Table>>();
				for(int i=0;i<list.size();i++){
					Table t_obj=list.get(i);
					try {
						Object t_id = t_method.invoke(t_obj, new Object[]{});
						if(t_id!=null){
							List<Table> t_list=t_tableMap.get(t_id);
							if(t_list==null){
								t_list=new ArrayList<Table>();
							}
							t_list.add(t_obj);
							t_tableMap.put(t_id, t_list);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				List<Table> newList=new ArrayList<Table>();
				for(Object t_id:idList){
					t_id=TableUtil.cast(t_id, idField.getType());
					List<Table> t_list=t_tableMap.get(t_id);
					if(t_list!=null){
						for(Table t_tableObj:t_list){
							newList.add(t_tableObj);
						}
					}
				}
				return (List<T>) newList;
			}else{
				throw new RuntimeException("没有可排序的字段");
			}
		}else{
			return (List<T>) list;
		}
	}
	/**判断List里所有对象是否全部为同一个类，并且不为空**/
	public static boolean isMatch(List<?> list) throws TableException{
		if(list!=null && list.size()>0){
			Class<?> tableClass=list.get(0).getClass();
			//判断所有的对象是否属于同一个类
			for(int i=1;i<list.size();i++){
				if(list.get(i).getClass() !=tableClass){
					throw new TableException("List的所有对象有不属于同一个类的");
				}
			}
			return true;
		}else{
			return false;
		}
	}
	/**
	 * 获取：指定表的主键列名（数据库真实列名）
	 * @param con : 当前Connection
	 * @param table : 表名
	 */
	public static String getPrimaryKey_Database(Connection con,String table) throws SQLException{
		if(con==null){
			throw new TableException("con不能为空");
		}
		if(table==null){
			throw new TableException("table不能为空");
		}
		ResultSet rs = null;
		String primaryKeyName=null;
		try {
			String tableArr[]=table.split("\\.");
			rs = con.getMetaData().getPrimaryKeys(con.getCatalog(), con.getSchema(), tableArr[tableArr.length-1]);
			if(rs.next()){
				primaryKeyName=rs.getString("COLUMN_NAME");	//主键
			}
		}finally{
			close(rs,null);	//关闭ResultSet和PreparedStatement
		}
		if(primaryKeyName==null){
			throw new TableException("该表还没有设置主键，请前往数据库查看");
		}
		return primaryKeyName;
	}
	/**获取 : Connection连接（url=连接字符串；userName=数据库用户名；passWord=密码；timeout=连接超时数。为空则不设置）**/
	public static Connection getConnection(String url,String userName,String passWord,Integer timeout) throws SQLException{
		if(timeout!=null && DriverManager.getLoginTimeout()!=timeout){
			DriverManager.setLoginTimeout(timeout);		//连接数据库超时设置
		}
		return DriverManager.getConnection(url,userName,passWord);
	}
	/**关闭 : Connection连接**/
	public static void closeConnection(Connection con){
		if(con!=null){try {con.close();} catch (SQLException e) {}}
	}
	/**关闭 : ResultSet和Statement*/
	public static void close(ResultSet rs,Statement ps){
		if(rs!=null){try {rs.close();} catch (SQLException e) {}}
		if(ps!=null){try {ps.close();} catch (SQLException e) {}}
	}
	/**获取 : 指定ORM类映射的数据库表，真实列名列表（表名式）**/
	public static Set<String> getColumn_Database(Connection con,String table) throws SQLException{
		Set<String> colSet=new LinkedHashSet<>();
		ResultSet rs = null;
		try {
			rs = con.getMetaData().getColumns(con.getCatalog(), con.getSchema(), table, null);		//查询指定表所有列的信息
			//把列名存入Map的key中（全部转为小写）
			while(rs.next()){
				colSet.add(rs.getString(4).toLowerCase());
			}
		} catch (SQLException e) {
			throw e;
		}finally{
			TableUtil.close(rs,null);
		}
		if(colSet.size()<=0) {
			throw new TableException("表【"+table+"】没有获取到【列的信息】，可能指定的表不存在");
		}
		return colSet;
	}
	/**List处理，返回新List（主要是调用ORM方法时：list是否为空、数据的拷贝等的处理；浅复制）**/
	public static <T> List<T> listInit(List<T> sourceList){
		List<T> newList=new ArrayList<T>();
		if(sourceList!=null){
			newList.addAll(sourceList);
		}
		return newList;
	}
	/**获取（生成）in的占位符（如：list有5个数据，则返回“?,?,?,?,?”）**/
	public static String getPlaceholder(List<? extends Object> list){
		StringBuffer placeholder=new StringBuffer();
		for(int i=0;list!=null && i<list.size();i++){
			placeholder.append("?");
			if(i<list.size()-1){
				placeholder.append(",");
			}
		}
		return placeholder.toString();
	}
	/**获取对象（生成新对象，根据构造方法的参数对象）；paramsArr=构造方法的参数对象（按顺序）**/
	public static <T> T getObject(Class<T> tableClass,Object... paramsArr){
		if(tableClass!=null && paramsArr!=null && paramsArr.length>0){
			try{
				 //获取所有构造方法
		        Constructor<?>[] ctor = tableClass.getDeclaredConstructors();
		        out:for (Constructor<?> constructor : ctor) {
            		//当前构造方法的参数
		            Class<?> tableParamArr[] = constructor.getParameterTypes();
		            if(tableParamArr.length==paramsArr.length){
	            		for(int i=0;i<paramsArr.length;i++){
	            			Class<?> tableParamClass=tableParamArr[i];
	            			Object t_paramsObj=paramsArr[i];
	            			if(t_paramsObj!=null){
	            				Class<?> paramsClass=t_paramsObj.getClass();
	            				//当前位置参数类型不同
		            			if(tableParamClass!=paramsClass){
		            				continue out;
		            			}
	            			}
	            		}
		            	return (T) constructor.newInstance(paramsArr);
		            }
		        }
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * 获取 : list指定字段（成员变量）名的值，列表式——可用于外键
	 * @param list : 继承Table的ORM对象列表
	 * @param fieldName : 指定要取的字段（成员变量）名
	 */
	public static <T> List<T> getObjectList(List<? extends Table> list,String fieldName){
		if(list!=null && list.size()>0 && fieldName!=null && fieldName.trim().length()>0){
			Class<? extends Table> tableClass=list.get(0).getClass();
			/*主查询的Field*/
			Field field=TableUtil.getField(tableClass, fieldName.trim());
			if(field!=null){
				Map<String,Method> methodMap=getMethodMap_get(tableClass);
				List<Object> returnList=new ArrayList<Object>();
				for(Table t_obj:list){
					Object t_foreignObj=TableUtil.get(t_obj, methodMap, field);
					if(t_foreignObj!=null){
						//List
						if(t_foreignObj instanceof List){
							List<Object> t_foreignList=(List<Object>)t_foreignObj;
							returnList.addAll(t_foreignList);
						}else{
							returnList.add(t_foreignObj);
						}
					}
				}
				return (List<T>) returnList;
			}
		}
		return null;
	}
	/**
	 * 设置 : list指定字段（成员变量）名的值——用于外键
	 * @param list : 继承Table的ORM对象列表
	 * @param foreignFieldName : 指定要设置的字段（成员变量）名，外键字段名称
	 * @param foreignList : 要设置到list指定外键字段名的列表
	 */
	public static <T extends Table> void setObjectList(List<T> list,String foreignFieldName,List<? extends Table> foreignList){
		if(list!=null && list.size()>0 && foreignFieldName!=null && foreignFieldName.trim().length()>0 && foreignList!=null && foreignList.size()>0){
			Class<? extends Table> tableClass=list.get(0).getClass();
			/*主查询的Field*/
			Field field=TableUtil.getField(tableClass, foreignFieldName.trim());
			if(field!=null){
				Class<? extends Table> foreignClass=foreignList.get(0).getClass();
				Field foreignIdField=TableUtil.getPrimaryKeyField(foreignClass);
				Map<String,Method> foreignMethodMap=getMethodMap_get(foreignClass);
				Map<Object,Object> foreignMap=new HashMap<Object, Object>();
				for(Table t_foreign:foreignList){
					Object t_foreignId=TableUtil.get(t_foreign, foreignMethodMap, foreignIdField);
					if(t_foreignId!=null){
						foreignMap.put(t_foreignId, t_foreign);	//外键的主键—>外键对象
					}
				}
				Map<String,Method> methodMap_get=TableUtil.getMethodMap_get(tableClass);
				Map<String,Method> methodMap_set=TableUtil.getMethodMap_set(tableClass);
				for(Table t_obj:list){
					Object t_foreignObj=TableUtil.get(t_obj, methodMap_get, field);
					if(t_foreignObj!=null){
						Object foreignObj_new=null;
						//t_foreignObj是List
						if(t_foreignObj instanceof List){
							List<Object> t_foreignList=(List<Object>) t_foreignObj;
							for(int i=0;i<t_foreignList.size();i++){
								Object t_foreignObj_source=t_foreignList.get(i);		//原List外键里的对象
								Object t_foreignId_source=TableUtil.get(t_foreignObj_source, foreignMethodMap, foreignIdField);	//取出原外键对象的id
								if(t_foreignId_source!=null){
									Object t_foreignObj_new=foreignMap.get(t_foreignId_source);		//根据原外键对象ID，取出新的外键对象
									if(t_foreignObj_new!=null){
										t_foreignList.set(i, t_foreignObj_new);
									}
								}
							}
							foreignObj_new=t_foreignList;
						}else{
							Object t_foreignId_source=TableUtil.get(t_foreignObj, foreignMethodMap, foreignIdField);	//取出原外键对象的id
							if(t_foreignId_source!=null){
								foreignObj_new=foreignMap.get(t_foreignId_source);		//根据原外键对象ID，取出新的外键对象
							}
						}
						//有改动则更新
						if(foreignObj_new!=null){
							TableUtil.set(t_obj, methodMap_set, field, foreignObj_new);
						}
					}
				}
			}
		}
	}
	/**
	 * List数据转为Map（key=主键；value=对象）
	 * @注意 : 所有的异常均用【RuntimeException】抛出
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException
	 */
	public static <T,S extends Table> Map<T,S> listToMap(List<S> list){
		Map<T,S> map=new LinkedHashMap<T,S>();
		if(list!=null && list.size()>0){
			Field t_field_primary=TableUtil.getPrimaryKeyField(list.get(0).getClass());
			try{
				t_field_primary.setAccessible(true);		//跳过所有访问权限
				for(Table t_obj:list){
					T t_id=(T) t_field_primary.get(t_obj);
					map.put(t_id, (S) t_obj);
				}
			}catch(Exception e){
				throw new RuntimeException(e.getMessage(),e);
			}
		}
		return map;
	}
	/**获取 : 包装真实名后的名，Config式*/
	public static String getPack(Config config,String column){
		return getPack(config.getType(), column);
	}
	/**获取 : 包装真实名后的名（含：数据库名、表名、列名等。主要解决关键字的问题和匹配真实列名）**/
	public static String getPack(String db,String name){
		if(db!=null){
			if(db.equalsIgnoreCase(Cache2.SQLServer)){
				return "["+name+"]";
			}else if(db.equalsIgnoreCase(Cache2.Oracle)){
				return "\""+name.toUpperCase()+"\"";
			}else if(db.equalsIgnoreCase(Cache2.MySQL)){
				return "`"+name+"`";
			}else{
				return name;
			}
		}else{
			return name;
		}
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
		//清除空对象
		for(int i=0;list!=null && i<list.size();i++){
			Table t_obj=list.get(i);
			if(t_obj==null){
				list.remove(i);
				i--;
			}
		}
    	if(list!=null && list.size()>0 && foreign!=null && foreign.length>0){
    		Table obj=list.get(0);
    		Class<? extends Table> tableClass=obj.getClass();
    		Field fArr[]=TableUtil.getField(tableClass);
    		Field idField=TableUtil.getPrimaryKeyField(tableClass);	//主键
    		Map<String,Method> methodMap_get=getMethodMap_get(tableClass);
    		Map<String,Method> methodMap_set=getMethodMap_set(tableClass);
    		Map<Field,List<Object>> foreignIdMap=new HashMap<Field,List<Object>>();
    		Map<Field,Map<Object,Object>> foreignMap=new HashMap<Field,Map<Object,Object>>();		//外键字段—>（外键id—>List外键对象）
    		/**获取外键Field**/
    		for(Field f:fArr){
				String name=f.getName();
    			for(String s:foreign){
	    			if(name.equalsIgnoreCase(s)){
	    				foreignIdMap.put(f, new ArrayList<Object>());
	    				break;
	    			}
	    		}
    		}
    		/**获取指定外键的id（一对多取当前主键）**/
    		for(Table t_obj:list){
    			for(Entry<Field,List<Object>> entry:foreignIdMap.entrySet()){
    				Field key=entry.getKey();
    				Class<?> foreignClass=key.getType();
    				boolean isGeneric=false;		//是否泛型（一对多）
    				if(List.class.isAssignableFrom(foreignClass)){
    					foreignClass=TableUtil.getGeneric(key);
    					isGeneric=true;
    				}
    				if(Table.class.isAssignableFrom(foreignClass)){
    					List<Object> idList=entry.getValue();
    					if(idList==null){
    						idList=new ArrayList<Object>();
    					}
    					//一对一
    					if(!isGeneric){
    						Object foreignObj=TableUtil.get(t_obj, methodMap_get, key);
	    					Object idObj=TableUtil.getPrimaryKeyValue(foreignObj);
	    					if(idObj!=null){
		    					idList.add(idObj);
	    					}
	    				//一对多（泛型）
    					}else{
    						Object mainIdObj=TableUtil.get(t_obj, methodMap_get, idField);	//取出tableClass对象的主键
    						idList.add(mainIdObj);
    						//List字段—>主对象的id
    					}
    					foreignIdMap.put(key, idList);
    				}
    			}
    		}
    		/**查询指定外键的数据**/
    		for(Entry<Field,List<Object>> entry:foreignIdMap.entrySet()){
    			Field key=entry.getKey();
				Class<?> t_foreignClass=key.getType();
				Class<? extends Table> foreignClass=null;
				boolean isGeneric=false;		//是否泛型（一对多）
				if(List.class.isAssignableFrom(t_foreignClass)){
					foreignClass=(Class<? extends Table>) TableUtil.getGeneric(key);
					isGeneric=true;
				}else{
					foreignClass=(Class<? extends Table>) t_foreignClass;
				}
				List<Object> idList=entry.getValue();	//id列表
				Map<Object,Object> t_foreignMap2=foreignMap.get(key);
				if(t_foreignMap2==null){
					t_foreignMap2=new HashMap<Object, Object>();
				}
				if(idList!=null && idList.size()>0){
    				//一对一
    				if(!isGeneric){
						Map<String,Method> t_methodMap_get=getMethodMap_get(foreignClass);
						Field foreignIdField=TableUtil.getPrimaryKeyField(foreignClass);	//主键
						
						Class t_t_foreignClass=foreignClass;
		    			List<Object> foreignList=Table.findByIdList(t_t_foreignClass, idList,specifiedFieldNameArr);		//根据所有的外键ID查出外键对象列表，不需要排序、分页，有缓存取缓存，不设置排序字段
		    			for(int i=0;foreignList!=null && i<foreignList.size();i++){
		    				Object t_idObj=TableUtil.get(foreignList.get(i), t_methodMap_get, foreignIdField);
		    				if(t_idObj!=null){
		    					t_foreignMap2.put(t_idObj, foreignList.get(i));
		    				}
		    			}
		    		//一对多（泛型）
    				}else{
    					String idCol=TableUtil.getPrimaryKey(foreignClass);	//外键的主键id列名
    					Field foreignField=TableUtil.getForeignKeyField(foreignClass, tableClass);		//外键里的外键成员字段映射的列名（外键成员字段，对应主tableClass）
    					String col=TableUtil.getForeignKey(foreignField);	//外键中的外键名（当前tableClass的列名）
    					if(col!=null){
	    					String placeholder_id=TableUtil.getPlaceholder(idList);
	    					long begin = 0,end = 0;		//分页起始到结束位置
	    					if(page!=null){
	    						begin=page.getBegin();
	    						end=page.getEnd();
	    					}
	    					String symbol="<";		//符号。大于或小于，取决于是否降序、正序
	    					String sort_str="asc";	//排序关键字，asc=正序；desc=降序
	    					//是否降序
	    					if(isDesc){
	    						symbol=">";
	    						sort_str="desc";
	    					}
	    					
	    					String foreignTable=TableUtil.getTable(foreignClass);		//外键表名
	    					StringBuffer t_generic_sql=new StringBuffer("select t_cs."+idCol+" from (select t1."+idCol+",(select count(*)+1 from "+foreignTable+" where "+col+"=t1."+col+" and "+idCol+symbol+"t1."+idCol+" ) as group_id");
	    					t_generic_sql.append(" from "+foreignTable+" t1 where t1."+col+" in("+placeholder_id+") order by t1."+idCol+" "+sort_str+") t_cs");
	    					if(end>0){
	    						t_generic_sql.append(" where t_cs.group_id>="+begin+" and t_cs.group_id<="+end);
	    					}
	    					List<Map<String, Object>> t_genericList_manual=Table.find(foreignClass, t_generic_sql.toString(), idList);
	    					List<Object> t_generic_idList=new ArrayList<Object>();
	    					for(Map<String,Object> t_idMap:t_genericList_manual){
	    						Object t_id=t_idMap.get(idCol);
	    						if(t_id!=null){
	    							t_generic_idList.add(t_id);
	    						}
	    					}
	    					
							Class t_t_foreignClass=foreignClass;
	    					List<Table> t_genericList=Table.findByIdList(t_t_foreignClass, t_generic_idList,specifiedFieldNameArr);		//泛型外键列表
	    					t_genericList=TableUtil.sort(t_genericList,t_generic_idList);		//重新排序
	    					
	    					Map<String,Method> t_methodMap=getMethodMap_get(foreignClass);
	    					for(Object t_t_obj:t_genericList){
	    						Object t_t_t_obj=TableUtil.get(t_t_obj, t_methodMap, foreignField);	//取出外键对象（本tableClass在外键的对象）
	    						Object t_id=TableUtil.get(t_t_t_obj, methodMap_get, idField);	//获取外键对象的id值（本tableClass在外键的主键值）
	    						List<Object> foreignList=(List<Object>) t_foreignMap2.get(t_id);
	    						if(foreignList==null){
	    							foreignList=new ArrayList<Object>();
	    						}
	    						if(t_t_t_obj!=null && t_id!=null){
	    							foreignList.add(t_t_obj);
	    						}
	    						t_foreignMap2.put(t_id, foreignList);		//本tableClass的id—>泛型外键List（有排序顺序）
	    					}
    					}
    				}
    				foreignMap.put(key, t_foreignMap2);
				}
    		}
    		/**设置外键数据**/
    		for(Entry<Field,Map<Object,Object>> entry:foreignMap.entrySet()){
    			Field key=entry.getKey();
    			Map<Object,Object> t_foreignObjectMap=entry.getValue();
    			Class<?> t_foreignClass=key.getType();
				Class<? extends Table> foreignClass=null;
				boolean isGeneric=false;		//是否泛型（一对多）
				if(List.class.isAssignableFrom(t_foreignClass)){
					foreignClass=(Class<? extends Table>) TableUtil.getGeneric(key);
					isGeneric=true;
				}else{
					foreignClass=(Class<? extends Table>) t_foreignClass;
				}
				//一对一
				if(!isGeneric){
					Map<String,Method> t_methodMap_get=getMethodMap_get(foreignClass);
					Field foreignClassIdField=TableUtil.getPrimaryKeyField(foreignClass);	//主键
		    		for(Table t_obj:list){
	    				Object foreignObj=TableUtil.get(t_obj, methodMap_get, key);		//外键对象
	    				Object t_idObj=TableUtil.get(foreignObj, t_methodMap_get, foreignClassIdField);		//外键对象id
	    				Object t_foreignObj=t_foreignObjectMap.get(t_idObj);		//查询出的外键对象
	    				if(t_foreignObj!=null){
	    					TableUtil.set(t_obj, methodMap_set, key, t_foreignObj);
	    				}
	    			}
		    	//一对多（泛型）
				}else{
					for(Table t_obj:list){
						Object mainIdObj=TableUtil.get(t_obj, methodMap_get, idField);	//主键
						Object foreignList=t_foreignObjectMap.get(mainIdObj);
						if(foreignList==null){
							foreignList=new ArrayList<Object>();
						}
						if(foreignList!=null){
							TableUtil.set(t_obj, methodMap_set, key, foreignList);
						}
					}
				}
    		}
		}
	}
	/**判断 : 是否空值，并返回**/
	public static Object isNull(Field f,Object value){
		if(f.getType()==String.class && value!=null && value.toString().length()<=0){
			return null;
		}else{
			return value;
		}
	}
 	/**
  	 * 获取 : 指定类的所有接口类，包含父类，直到指定父类停止（不在获取）
  	 * @param tableClass : 要获取所有接口的Class类
  	 * @param parentClass : 指定要停止获取父接口的Class（为空则一直获取到底）
  	 * @param interfacesClassSet : 按顺序存放所有的接口类
  	 * @return tableClass为空则返回null
  	 */
  	public static Class<?>[] getInterfaces(Class<?> tableClass,Class<?> parentClass){
  		Set<Class<?>> interfacesClassSet=new LinkedHashSet();
  		getInterfaces(tableClass, parentClass,interfacesClassSet);
  		Class<?> classArr[]=new Class<?>[interfacesClassSet.size()];
  		int i=0;
  		for(Class<?> t_class:interfacesClassSet) {
  			classArr[i]=t_class;
  			i++;
  		}
  		return classArr;
  	}
  	//获取 : 指定类的所有接口类，包含父类，直到指定父类停止（不在获取）
  	private static void getInterfaces(Class<?> tableClass,Class<?> parentClass,Set<Class<?>> interfacesClassSet){
  		if(tableClass!=null){
			Class<?> t_classArr[]=tableClass.getInterfaces();
  			for(Class t_class:t_classArr){
	  			//不需要：已存在的，并且当前类不是指定停止类
				if(!interfacesClassSet.contains(t_class) && t_class!=parentClass) {
					interfacesClassSet.add(t_class);
				}
  			}
  			for(Class t_class:t_classArr){
  				//当前类不是指定停止类
  				if(t_class!=parentClass) {
  					getInterfaces(t_class,parentClass,interfacesClassSet);
  				}
  			}
  		}
  	}
  	/**获取 : 处理后的where语句（如果有内容，先清除首内容为【 and 】的内容，并且加上【 where 】的首内容。否则返回原内容）**/
  	public static String getWhereSQL(String whereSQL) {
  		if(whereSQL!=null && whereSQL.length()>0) {
  			String str=" and ";
  			int index=whereSQL.indexOf(str);
  			if(index==0) {
  				return " where "+whereSQL.substring(str.length());
  			}else{
  				return " where "+whereSQL;
  			}
  		}
  		return whereSQL;
  	}
}