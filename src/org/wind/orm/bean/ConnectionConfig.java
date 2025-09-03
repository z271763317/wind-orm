package org.wind.orm.bean;

import org.wind.orm.service.ConnectionManager;

/**
 * @描述 : 连接配置（可临时设置的）
 * @作者 : 胡璐璐
 * @时间 : 2021年9月27日 08:51:08
 */
public class ConnectionConfig {

	private Boolean autoCommit;		//自动提交状态
	private ConnectionManager noRead;		//非只读
	private ConnectionManager read;	//只读
	private Integer timeout;			//执行单条SQL超时数
	
	public Boolean getAutoCommit() {
		return autoCommit;
	}
	public void setAutoCommit(Boolean autoCommit) {
		this.autoCommit = autoCommit;
	}
	public ConnectionManager getNoRead() {
		return noRead;
	}
	public void setNoRead(ConnectionManager noRead) {
		this.noRead = noRead;
	}
	public ConnectionManager getRead() {
		return read;
	}
	public void setRead(ConnectionManager read) {
		this.read = read;
	}
	public Integer getTimeout() {
		return timeout;
	}
	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}
	
}