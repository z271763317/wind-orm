package org.wind.orm.exception;

/**
 * @描述 : SQL工厂Exception类， 生成SQL具体实现类时，发生的错误
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年9月29日 15:34:18
 */
public class SQLFactoryException extends RuntimeException{
	
	private static final long serialVersionUID = -4403809896423058996L;
	
	/**抛出异常，打印异常信息**/
    public SQLFactoryException(String msg) {
        super(msg);
    }
    /**抛出异常，打印异常的错误信息，并指明具体异常的错误地方**/
    public SQLFactoryException(String msg, Exception e) {
        super(msg, e);
    }
    /**直接抛出具体的异常**/
    public SQLFactoryException(Exception e) {
    	super(e);
    }
}
