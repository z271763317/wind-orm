package org.wind.orm.plugin;

import java.util.HashMap;
import java.util.Map;

import org.wind.orm.bean.ActionContext;
import org.wind.orm.bean.SQLType;

/**
 * @描述 : 拦截器（拦截sql的执行）
 * @版权 : 湖南省郴州市安仁县胡璐璐 
 * @时间 : 2023年10月27日 11:28:00
 */
public interface Interceptor {

	final static Map<Integer,Interceptor> map=new HashMap<>();
	
	/**
	 * 注册（注：实例化的clazz实现类是单例对象）
	 * @param clazz : 实现{@link Interceptor}接口的Class类
	 * @param typeArr : 注册的SQL类型（参考：{@link SQLType}），可变式数组，可追加多个
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static void register(Class<? extends Interceptor> clazz,int... typeArr) throws InstantiationException, IllegalAccessException {
		Interceptor obj=clazz.newInstance();
		if(typeArr!=null && typeArr.length>0) {
			for(int type:typeArr) {
				if(map.containsKey(type)) {
					throw new IllegalArgumentException("拦截器SQL【"+type+"】已被注册");
				}
				map.put(type, obj);
			}
		}
	}
	
	/*********接口方法*********/
	/**执行前**/
	public void before(ActionContext context) throws Exception;
	/**执行后**/
	public void after(ActionContext context,Object result);
	/**完成后（执行完 {@link Interceptor#after} 后，若出现了异常，则exception会有值，可做异常处理）**/
	public void complete(ActionContext context,Exception exception);
    
}