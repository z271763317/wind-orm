package org.wind.orm.bean;

/**
 * @描述 : 数据源配置类——即连接池配置类，配置主类，支持读写
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年8月23日 19:29:24
 */
public class Config extends Config_global{

	private Config_Write configWrite;		//写
	private Config_Read configRead;			//读
	
	public Config_Write getConfigWrite() {
		return configWrite;
	}
	public void setConfigWrite(Config_Write configWrite) {
		this.configWrite = configWrite;
	}
	public Config_Read getConfigRead() {
		return configRead;
	}
	public void setConfigRead(Config_Read configRead) {
		this.configRead = configRead;
	}

}