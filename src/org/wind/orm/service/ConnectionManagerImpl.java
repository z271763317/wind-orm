package org.wind.orm.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;

import org.wind.orm.cache.Cache1;
import org.wind.orm.exception.ConnectionPoolException;
import org.wind.orm.util.TableUtil;

/**
 * @描述 : 数据库Connection连接管理器接口实现类
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年5月21日 14:25:39
 */
public class ConnectionManagerImpl implements ConnectionManager{

	private ConnectionPool conPool;	//绑定的连接池
	private Connection con;		//Connection对象
	private long lastOperationTime;		//最后操作时间
	
	public void setConnection(Connection con) {
		this.con = con;
	}
	public Connection getConnection(){
		return con;
	}
	public long getLastOperationTime() {
		return lastOperationTime;
	}
	public void setLastOperationTime(long lastOperationTime){
		this.lastOperationTime = lastOperationTime;
	}
	//获取 : 所属的连接池
	public ConnectionPool getConnectionPool() {
		return this.conPool;
	}
	//构造方法
	public ConnectionManagerImpl(ConnectionPool conPool) throws SQLException{
		this.conPool=conPool;
		this.con=this.createConnection();
		this.lastOperationTime=System.currentTimeMillis();
	}
	
	/**处理 : Connection为可用的 **/
	public void available() throws SQLException{
		try{
			//验证连接是否可用，不可用则重新创建——暂定为5000毫秒（5秒）
			if(this.con==null || this.con.isClosed() || !this.isValid(5000)){
				TableUtil.closeConnection(this.con);
				this.con=this.createConnection();
			}
		}catch(SQLException e){
			/*验证失败，重连一次*/
			try{
				TableUtil.closeConnection(this.con);
				this.con=this.createConnection();
			}catch(SQLException e1){
				throw e1;
			}
		}
	}
	//验证con的连接状态，并保持连接；timeOut=超时设置（毫秒）
	private boolean isValid(int timeOut){
		if(this.con!=null){
			PreparedStatement ps=null;
			ResultSet rs=null;
			try{
				String sql="select 1;";
				ps=this.con.prepareStatement(sql);
				ps.setQueryTimeout(5000);
				rs=ps.executeQuery();
				if(rs!=null && rs.next()){
					TableUtil.close(rs, ps);
					return true;
				}
				TableUtil.close(rs, ps);
			}catch(Exception e){
				//e.printStackTrace();		//连接失败
			}finally{
				TableUtil.close(rs, ps);
			}
		}
		return false;
	}
	//创建 : Connection
	private Connection createConnection() throws SQLException{
		return TableUtil.getConnection(conPool.getDbURL(),conPool.getDbUser(),conPool.getDbPass(),conPool.getConnectionTimeout());
	}
	
	/****************数据库的操作****************？
	/**回滚事务*/
	public void rollback(){
		rollback(null);
	}
	/**回滚事务（指定Savepoint保存点）*/
	public void rollback(Savepoint t_savepoint){
		try {
			Connection t_con=this.getConnection();
			if(t_con!=null && !t_con.isClosed()){
				if(t_con.getAutoCommit()==false){
					if(t_savepoint!=null){
						t_con.rollback(t_savepoint);
					}else{
						t_con.rollback();
					}
				}else{
					StackTraceElement t2=Thread.currentThread().getStackTrace()[2];		//就是上一级的方法堆栈 以此类推
					String error="回滚错误，当前Connection的AutoCommit为true的类【"+t2.getClassName()+"】，方法【"+t2.getMethodName()+"】，行数【"+t2.getLineNumber()+"】";
					System.err.println(error);
				}
			}
		} catch (SQLException e) {
			throw new ConnectionPoolException(e.getMessage(),e);
		}
	}
	/**关闭操作数据库相关的类*/
	public void close(){
    	 boolean isAutoCommit=Cache1.get().isAutoCommit(this.conPool.getDataSource());
    	 //当设置为自动提交则释放回当前连接池
    	 if(isAutoCommit){
			 this.release();
    	 }
	}
	/**释放当前对象到所属ConnectionPool队列**/
	public void release(){
		this.conPool.release(this);
		Cache1.get().getSqlVar().clearConnectionManager();
	}
	/**提交事务**/
	public void commit() throws SQLException{
		if(this.con.getAutoCommit()==false){
			this.con.commit();
		}
	}
	
}