package org.wind.orm.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.wind.orm.Table;
import org.wind.orm.annotation.ForeignKey;
import org.wind.orm.annotation.Id;

/**
 * @描述 : 查询工具类——Oracle
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年8月27日 16:06:47
 */
public class SelectUtil_oracle {

	/**
	 * 获取 : 主表自定义的表标签名（Oracle），tableLabelMap的key为全小写
	 * @param oldTableLabel : 原旧表别名（java虚假自定义，非实用）
	 * @param tableLabelIndex : 新的索引
	 * @param tableLabelMap : 表别名Map
	 */
	public static String getTableLabel(String oldTableLabel, int tableLabelIndex,Map<String, String> tableLabelMap) {
		String tableLabel = "t_" + tableLabelIndex;
		tableLabelMap.put(oldTableLabel, tableLabel);
		return tableLabel;
	}

	/**
	 * 获取 : 主表自定义的列标签名（Oracle），colLabelMap的key为全小写
	 * @param oldColLabel : 原旧表列别名（java虚假自定义，非实用）
	 * @param colLabelIndex : 新的索引
	 * @param colLabelMap : 列别名Map
	 */
	public static String getColumnLabel(String oldColLabel, int colLabelIndex,Map<String, String> colLabelMap) {
		String colLabel = "c_" + colLabelIndex;
		colLabelMap.put(oldColLabel, colLabel);
		return colLabel;
	}

	/**
	 * 获取：ORM的对象数据
	 * @param tableClass : 继承Table的ORM类
	 * @param rs : 执行SQL后的ResultSet对象
	 * @param propertyArr : 要设置的成员属性的字段列表
	 * @param col_label : 上一层字段的列标签名
	 * @param colLabelMap : 所有旧列标签名对应的实际显示的列标签名
	 */
	public static List<? extends Table> getObject(Class<? extends Table> tableClass,ResultSet rs, Field[] propertyArr, String col_label,Map<String, String> colLabelMap) throws Exception {
		List<Table> resultList = new ArrayList<Table>(); // 返回结果集
		Map<String, Method> methodMap_set = TableUtil.getMethodMap_set(tableClass); // 获取该Class所有的Metho【set】
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
						// 非List
						if (!List.class.isAssignableFrom(foreign_tableClass)) {
							Field foreignIdField = TableUtil.getPrimaryKeyField(foreign_tableClass);
							String foreign_colName = colName + "_"+ foreignIdField.getName();
							foreign_colName = colLabelMap.get(foreign_colName); // 区别于其他获取数据的方式
							if (foreign_colName != null) {
								Object foreignIdObj = rs.getObject(foreign_colName); // 如果数据库不存在该列将跳过
								foreignIdObj = TableUtil.cast(foreignIdObj,foreignIdField.getType());
								if(foreignIdObj!=null){
									paramValue = foreign_tableClass.newInstance(); // 外键对象
									TableUtil.setPrimaryKeyValue(paramValue,foreignIdObj);
								}
							}
						}
					} else {
						colName = colLabelMap.get(colName); // 区别于其他获取数据的方式
						if (colName != null) {
							paramValue=SelectUtil.getResult(rs, property, colName);
							if (property.isAnnotationPresent(Id.class)) {
								id = paramValue;
							}
						} else {
							continue;
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
	
}