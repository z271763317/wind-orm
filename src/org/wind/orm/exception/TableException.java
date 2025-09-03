package org.wind.orm.exception;

/**
 * @描述 : Table数据库操作Exception类
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年5月13日 21:37:09
 */
public class TableException extends RuntimeException{
	
	private static final long serialVersionUID = -563933430502308538L;
    
    /**抛出异常，打印异常信息**/
    public TableException(String msg) {
        super(msg);
    }
    /**抛出异常，打印异常的错误信息，并指明具体异常的错误地方**/
    public TableException(String msg, Exception e) {
        super(msg, e);
    }
    /**直接抛出具体的异常**/
    public TableException(Exception e) {
    	super(e);
    }
}
