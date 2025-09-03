package org.test.model;

import org.wind.orm.Table;
import org.wind.orm.annotation.Id;
import org.wind.orm.annotation.Lock;

/**
 * @描述 : 测试表
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年5月20日 16:55:16
 */
@Lock("with(nolock)")
public class Test extends Table{

	@Id
	private Integer id;
	private String name;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
}
