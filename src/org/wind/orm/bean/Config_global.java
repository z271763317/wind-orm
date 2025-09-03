package org.wind.orm.bean;

import org.wind.orm.annotation.Default;
import org.wind.orm.cache.Cache2;

/**
 * @描述 : 数据源配置抽象类——即连接池配置类，通用配置
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2018年8月26日 13:50:50
 */
public abstract class Config_global {

	@Default(Cache2.dataSource_default)
	protected String id;			//数据源ID
	protected String driver;			//数据库驱动字符串
	protected String url;				//数据库连接字符串
	protected String dataBase;		//数据库名称
	protected String userName;		//数据库用户名
	protected String passWord;		//数据库密码
	@Default("1")
	protected Integer max;				//最大连接数（连接池）
	@Default("1")
	protected Integer init;				//初始化连接数
	@Default(Cache2.MySQL)
	protected String type;				//数据库类型（mysql【默认】、sql server、oracle等）
	@Default(name="print_sql",value="false")
	protected Boolean printSQL;		//是否打印SQL
	@Default("30")
	protected Integer timeout;				//全局执行SQL超时（单位：秒，默认为：30秒）
	@Default("5")
	protected Integer connectionTimeout;	//生成、获取connection超时数（单位：秒，默认：5秒）
	@Default("1800")
	protected Integer freeTime;			//空间时间；超过该数将关闭（单位：秒，默认：1800秒）
	@Default("true")
	protected Boolean isSpecifyDatabaseName;			//是否在执行SQL的时候，指定数据库名（默认：true）
	//
	public String getId() {
		return id;
	}
	public String getDriver() {
		return driver;
	}
	public String getUrl() {
		return url;
	}
	public String getDataBase() {
		return dataBase;
	}
	public String getUserName() {
		return userName;
	}
	public String getPassWord() {
		return passWord;
	}
	public Integer getMax() {
		return max;
	}
	public String getType() {
		return type;
	}
	public Integer getInit() {
		return init;
	}
	public void setDriver(String driver) {
		this.driver = driver;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public void setDataBase(String dataBase) {
		this.dataBase = dataBase;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}
	public Boolean getIsSpecifyDatabaseName() {
		return isSpecifyDatabaseName;
	}
	public void setIsSpecifyDatabaseName(Boolean isSpecifyDatabaseName) {
		this.isSpecifyDatabaseName = isSpecifyDatabaseName;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setMax(Integer max) {
		this.max = max;
	}
	public void setInit(Integer init) {
		this.init = init;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Integer getTimeout() {
		return timeout;
	}
	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}
	public Integer getConnectionTimeout() {
		return connectionTimeout;
	}
	public void setConnectionTimeout(Integer connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}
	public Integer getFreeTime() {
		return freeTime;
	}
	public void setFreeTime(Integer freeTime) {
		this.freeTime = freeTime;
	}
	public Boolean isSpecifyDatabaseName() {
		return isSpecifyDatabaseName;
	}
	public void setSpecifyDatabaseName(Boolean isSpecifyDatabaseName) {
		this.isSpecifyDatabaseName = isSpecifyDatabaseName;
	}
	public Boolean getPrintSQL() {
		return printSQL;
	}
	public void setPrintSQL(Boolean printSQL) {
		this.printSQL = printSQL;
	}
	
}