package org.wind.orm.bean.proxy;

import java.lang.reflect.Proxy;

import org.wind.orm.util.TableUtil;

/**
 * @描述 : 代理工厂
 * @作者 : 胡璐璐
 * @时间 : 2019年1月23日 18:37:56
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ProxyFactory{

	/******************获取 : 代理对象****************/
	/****【SQL操作类对象】****/
	public <T> T getSQLObject(final T sourceObj){
    	if(sourceObj!=null){
    		Class<?> t_class=sourceObj.getClass();
    		Class<?> t_interfacesArr[]=TableUtil.getInterfaces(t_class.getSuperclass(), null);
//    		Class<?> t_interfacesArr[]=t_class.getSuperclass().getInterfaces();
    		/**动态代理**/
			Object proxyObject=Proxy.newProxyInstance(
	        		t_class.getClassLoader(),
	        		t_interfacesArr,
        		new ProxySQLObject(sourceObj)
	        );
    		return (T) proxyObject;
    	}else{
    		throw new RuntimeException("传入的实际对象为null，不允许代理");
    	}
    }
	
}