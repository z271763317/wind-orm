package org.wind.orm.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.wind.orm.Table;
import org.wind.orm.annotation.DateTime;
import org.wind.orm.annotation.ForeignKey;
import org.wind.orm.annotation.Id;

/**
 * @描述 : 查询工具类
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年8月27日 16:06:47
 */
public class SelectUtil {

	/**获取 : 查询结果设置到映射对象的对象List集 */
	public static List<? extends Table> getObject(Class<? extends Table> tableClass,ResultSet rs, Field[] propertyArr, String col_label) throws Exception {
		List<Table> resultList = new ArrayList<Table>(); // 返回结果集
		Map<String, Method> methodMap_set = TableUtil.getMethodMap_set(tableClass); // 获取该Class的Method【set】
		while (rs.next()) {
			Table t_obj = tableClass.newInstance();
			Object id = null;
			for (int i = 0; i < propertyArr.length; i++) {
				Object paramValue = null;
				Field property = propertyArr[i];
				String colName = col_label + "_" + property.getName(); // 列名标签
				try {
					// 外键
					if (property.isAnnotationPresent(ForeignKey.class)) {
						Class<?> foreign_tableClass = property.getType();
						if (!List.class.isAssignableFrom(foreign_tableClass)) {
							Field foreignIdField = TableUtil.getPrimaryKeyField(foreign_tableClass);
							String foreign_colName = colName + "_"+ foreignIdField.getName();
							Object foreignIdObj = rs.getObject(foreign_colName); // 如果数据库不存在该列将跳过
							foreignIdObj = TableUtil.cast(foreignIdObj,foreignIdField.getType());
							if(foreignIdObj!=null){
								paramValue = foreign_tableClass.newInstance(); // 外键对象
								TableUtil.setPrimaryKeyValue(paramValue,foreignIdObj);
							}
						}
					} else {
						paramValue=getResult(rs, property, colName);
						if (property.isAnnotationPresent(Id.class)) {
							id = paramValue;
						}
					}
					TableUtil.set(t_obj, methodMap_set, propertyArr[i], paramValue);
				} catch (Exception e) {
					continue;
				}
			}
			if (id == null) {
				System.err.println("该行数据主键为空");
			}
			resultList.add(t_obj);
		}
		return resultList;
	}

	// 获取 : 自定义【表】标签名
	public static String getTableLabel(Class<?> tableClass) {
		return "t_" + tableClass.getSimpleName(); // 固定表的标签名
	}
	// 获取 : 自定义【列】标签名（前缀）
	public static String getColumnLabel(Class<?> tableClass) {
		return "c"; // 主表自定义的列标签名
	}
	// 获取 : 自定义【主键】标签名
	public static String getPrimaryKeyLabel(Class<? extends Table> tableClass) {
		String col_label = getColumnLabel(tableClass);
		Field fArr[] = TableUtil.getField(tableClass);
		for (int i = 0; i < fArr.length; i++) {
			Field f = fArr[i];
			if (f.isAnnotationPresent(Id.class)) {
				return col_label + "_" + f.getName();
			}
		}
		return null;
	}
	//获取 : 指定colName的值并转换成指定的类型
	public static Object getResult(ResultSet rs,Field property,String colName) throws SQLException, ParseException {
		Object paramValue=null;
		Class<?> propertyClass=property.getType();
		if(propertyClass==String.class) {
			paramValue = rs.getString(colName);
		}else{
			paramValue = rs.getObject(colName);
		}
		//日期时间
		if(property.isAnnotationPresent(DateTime.class)){
			String format=property.getAnnotation(DateTime.class).value();
			//需要格式化
			if(format!=null && format.length()>0) {
				SimpleDateFormat t_sdf=new SimpleDateFormat(format);
				paramValue=t_sdf.format(t_sdf.parse(paramValue.toString()));
			}
		}else{
			paramValue = TableUtil.cast(paramValue,property.getType());
		}
		return paramValue;
	}
	
}
