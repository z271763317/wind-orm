package org.wind.orm.exception;


/**
 * @描述 : 【插入、更新】Table数据库操作Exception类
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年5月28日 14:27:57
 */
public class TableSaveException extends TableException{
	
	private static final long serialVersionUID = 3055590352158862665L;
	
	/**抛出异常，打印异常信息**/
    public TableSaveException(String msg) {
        super(msg);
    }
    /**抛出异常，打印异常的错误信息，并指明具体异常的错误地方**/
    public TableSaveException(String msg, Exception e) {
        super(msg, e);
    }
    /**直接抛出具体的异常**/
    public TableSaveException(Exception e) {
    	super(e);
    }
}
