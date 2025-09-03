package org.wind.orm.service;

import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.wind.orm.bean.Config;
import org.wind.orm.bean.Config_global;
import org.wind.orm.cache.Cache1;
import org.wind.orm.cache.Cache2;
import org.wind.orm.exception.ConnectionPoolException;
import org.wind.orm.time.ConnectionFreeTask;
import org.wind.orm.util.TableUtil;


/**
 * @描述 : 数据库连接池——只能单例实例化
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2014年8月21日 15:57:45 
 */
public class ConnectionPoolImpl implements ConnectionPool{

	private final String poolName;	//数据源名称（连接池名称）
	private String dbURL;			//数据库连接字符串
	private String dbUser;			//用户名
	private String dbPass;			//密码
	private int connectionTimeout;		//获取、生成connection超时（单位：秒）
	private int freeTime;		//闲置超时数
	private int maxCon;			//最大连接数
	private int currentConNum;			//当前连接数
	private int dataSourceWay;		//当前被使用的数据源方式（主、读、写）
	//
	private BlockingQueue<ConnectionManager> queue;		//ConnectionManager队列
	
	public String getDataSource() {
		return this.poolName;
	}
	public int getDataSourceWay() {
		return dataSourceWay;
	}
	public String getDbURL() {
		return dbURL;
	}
	public String getDbUser() {
		return dbUser;
	}
	public String getDbPass() {
		return dbPass;
	}
	public int getConnectionTimeout() {
		return connectionTimeout;
	}
	public int getFreeTime() {
		return freeTime;
	}
	
	//获取 : 当前连接数
	private synchronized int getCurrentMaxCon(){
		return this.queue.size();
	}
	//构造方法（初始化所有的连接）
	public ConnectionPoolImpl(String poolName,int dataSourceWay){
		try{
			Config config=Cache2.getConfig(poolName);
			if(config==null){
				throw new ConnectionPoolException("未配置【"+poolName+"】数据库连接池");
			}
			this.poolName=poolName;
			this.dbURL=config.getUrl();
			this.dbUser=config.getUserName();
			this.dbPass=config.getPassWord();
			this.connectionTimeout=config.getConnectionTimeout();	
			this.maxCon=config.getMax();
			this.dataSourceWay=dataSourceWay;
			this.freeTime=config.getFreeTime();
			//
			Config_global t_item=null;
			switch(dataSourceWay){
				//读
				case Cache2.dataSourceWay_read:{
					t_item=config.getConfigRead();
					break;
				}
				//写
				case Cache2.dataSourceWay_write:{
					t_item=config.getConfigWrite();
					break;
				}
			}
			if(t_item!=null){
				if(t_item.getUrl()!=null){
					this.dbURL=t_item.getUrl();
				}
				if(t_item.getUserName()!=null){
					this.dbUser=t_item.getUserName();
				}
				if(t_item.getPassWord()!=null){
					this.dbPass=t_item.getPassWord();
				}
				if(t_item.getConnectionTimeout()!=null){
					this.connectionTimeout=t_item.getConnectionTimeout();
				}
				if(t_item.getMax()!=null){
					this.maxCon=t_item.getMax();
				}
				if(t_item.getFreeTime()!=null){
					this.freeTime=t_item.getFreeTime();
				}
			}else{
				this.dataSourceWay=Cache2.dataSourceWay;		//默认的
			}
			//
			this.queue=new LinkedBlockingQueue<ConnectionManager>(this.maxCon);
			//初始化连接数，原先是：maxCon
			for(int i=0;i<config.getInit();i++){
				ConnectionManager t_conManager=this.create();
				if(t_conManager!=null){
					this.queue.offer(t_conManager);
				}
			}
		}catch(Exception e){
			throw new ConnectionPoolException(e.getMessage(),e);
		}
	}
	
	/**获取 : Connection管理对象**/
	public ConnectionManager get() throws ConnectionPoolException{
		ConnectionManager conManager=Cache1.get().getConnectionManager(this.poolName,this.dataSourceWay);		//Connection管理器
		if(conManager==null){
			try {
				if(this.queue!=null){
					//获取队列可用的connection（同步）
					conManager=this.getQueue();
					if(conManager==null){
						throw new ConnectionPoolException("获取数据库连接超时");
					}
					boolean isAutoCommit=Cache1.get().isAutoCommit(this.poolName);
					if(this.dataSourceWay!=Cache2.dataSourceWay_read) {
						conManager.getConnection().setAutoCommit(isAutoCommit);
					}
					//开启事务（不自动提交）
					if(!isAutoCommit){
						Cache1.get().setConnectionManager(this.poolName,this.dataSourceWay,conManager);
					}
				}else{
					throw new ConnectionPoolException("数据库连接池未建立");
				}
			} catch (Exception e) {
				throw new ConnectionPoolException(e);
			}
		}
		return conManager;
	}
	/**获取 : 队列的Connection管理对象* */
	private ConnectionManager getQueue() throws InterruptedException,SQLException, TimeoutException {
		ConnectionManager conManager=this.queue.poll();
		if(conManager!=null){
			conManager.available();
		//没有，则试着创建一个新的
		}else{
			conManager=this.create();
			//已达到最大值，阻塞，看看有没有释放的，若超过了连接超时时间数还没有取到，则直接返回null
			if(conManager==null) {
				conManager=this.queue.poll(this.connectionTimeout, TimeUnit.SECONDS );		//等待（秒）
			}
		}
		if(conManager!=null) {
			conManager.setLastOperationTime(System.currentTimeMillis());		//最后一次使用时间
		}
		return conManager;
	}
	//创建 : Connection管理对象
	private synchronized ConnectionManager create() throws SQLException{
		//未达到最大数
		if(this.currentConNum<this.maxCon) {
			ConnectionManager conManage=new ConnectionManagerImpl(this);	
			this.currentConNum++;
			ConnectionFreeTask.add(conManage);	
			return conManage;
		}else{
			return null;
		}
	}
	/**释放 : Connection管理对象**/
	public void release(ConnectionManager conManager) {
		int t_currentMaxCon=this.getCurrentMaxCon();
		//未达到最大值
		if(t_currentMaxCon<this.maxCon){
			this.queue.offer(conManager);
		//达到最大值
		}else{
			TableUtil.closeConnection(conManager.getConnection());
		}
	}
	
}