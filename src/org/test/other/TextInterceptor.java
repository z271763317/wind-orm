package org.test.other;

import org.wind.orm.bean.ActionContext;
import org.wind.orm.bean.SQLVar;
import org.wind.orm.plugin.Interceptor;

/**
 * 自定义拦截器
 */
public class TextInterceptor implements Interceptor{

	@Override
	public void before(ActionContext context) throws Exception {
		SQLVar obj=context.getObjVar();
		System.out.println("执行前："+context.getType()+"；"+obj.getObjSql().getClass().getSimpleName()+"#"+obj.getMethod().getName()+"("+obj.getMethodParam()+")");
//		System.out.println("SQL执行前：");
	}
	@Override
	public void after(ActionContext context,Object result) {
		System.out.println("SQL执行后："+result);
	}
	@Override
	public void complete(ActionContext context, Exception exception) {
		System.out.println("处理完成后："+exception);
	}

}