package org.wind.orm.exception;


/**
 * @描述 : 【查询】Table数据库操作Exception类
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年5月28日 14:25:59
 */
public class TableSelectException extends TableException{
	
	private static final long serialVersionUID = -8057646750093129334L;
	
	/**抛出异常，打印异常信息**/
    public TableSelectException(String msg) {
        super(msg);
    }
    /**抛出异常，打印异常的错误信息，并指明具体异常的错误地方**/
    public TableSelectException(String msg, Exception e) {
        super(msg, e);
    }
    /**直接抛出具体的异常**/
    public TableSelectException(Exception e) {
    	super(e);
    }
}
