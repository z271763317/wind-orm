package org.test.model;

import org.wind.orm.Table;
import org.wind.orm.annotation.Id;
import org.wind.orm.annotation.Lock;

/**
 * @描述 : 测试表2
 * @版权 : 湖南省郴州市安仁县胡璐璐
 * @时间 : 2015年5月20日 16:55:16
 */
@Lock("with(nolock)")
public class Test2 extends Table {

	@Id
	private Integer test2Id;
	private String name;

	public Integer getTest2Id() {
		return test2Id;
	}
	public void setTest2Id(Integer test2Id) {
		this.test2Id = test2Id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

}
