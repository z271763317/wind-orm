package org.wind.orm.exception;


/**
 * @描述 : 【DDL】Table数据库操作Exception类
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2016年7月22日 15:53:19
 */
public class TableDDLException extends TableException{
	
	private static final long serialVersionUID = -2335514160477309437L;
	
	/**抛出异常，打印异常信息**/
    public TableDDLException(String msg) {
        super(msg);
    }
    /**抛出异常，打印异常的错误信息，并指明具体异常的错误地方**/
    public TableDDLException(String msg, Exception e) {
        super(msg, e);
    }
    /**直接抛出具体的异常**/
    public TableDDLException(Exception e) {
    	super(e);
    }
}
