package org.wind.orm.exception;

/**
 * @描述 : ConnectionPool线程池生产Exception类
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年8月21日 15:38:44
 */
public class ConnectionPoolFactoryException extends RuntimeException{
	
	private static final long serialVersionUID = -9051495839932503293L;
	
	public ConnectionPoolFactoryException(String msg) {
        super(msg);
    }
    public ConnectionPoolFactoryException(String msg, Exception e) {
        super(msg, e);
    }
    public ConnectionPoolFactoryException(Exception e) {
        super(e);
    }
}
