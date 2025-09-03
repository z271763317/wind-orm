package org.test.model;

import org.wind.orm.Table;
import org.wind.orm.annotation.Id;
import org.wind.orm.annotation.Tables;

/**
 * @描述 : 测试表——主键为：varchar
 * @版权 : 胡璐璐
 * @时间 : 2017年10月28日 10:57:51
 */
@Tables("test_varchar")
public class Test_varchar extends Table{

	@Id(Id.UUID)
	private String id;
	private String name;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}