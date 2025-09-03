package org.test.model.parent;

import org.wind.orm.Table;
import org.wind.orm.annotation.DateTime;
import org.wind.orm.annotation.Id;
import org.wind.orm.annotation.Null;

/**
 * 实体类的父类
 * @author Administrator
 *
 */
public class Model extends Table{

	@Id@Null
	protected Long id;		//主键
	@DateTime("yyyy-MM-dd HH:mm:ss")
	protected String createTime;	//创建时间
	@DateTime("yyyy-MM-dd HH:mm:ss")
	protected String updateTime;	//更新时间
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public String getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}
	
	
}
