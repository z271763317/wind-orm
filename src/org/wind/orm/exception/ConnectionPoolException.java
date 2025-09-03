package org.wind.orm.exception;

/**
 * @描述 : ConnectionPool线程池Exception类
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年5月13日 21:37:09
 */
public class ConnectionPoolException extends RuntimeException{
	
	private static final long serialVersionUID = 7891902574896086637L;

	public ConnectionPoolException(String msg) {
        super(msg);
    }
    public ConnectionPoolException(String msg, Exception e) {
        super(msg, e);
    }
    public ConnectionPoolException(Exception e) {
        super(e);
    }
}
