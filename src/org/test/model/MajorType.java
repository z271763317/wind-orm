package org.test.model;

import org.wind.orm.Table;
import org.wind.orm.annotation.Column;
import org.wind.orm.annotation.ForeignKey;
import org.wind.orm.annotation.Id;

/**
 * @描述：专业类型表 
 */
public class MajorType extends Table{

	@Id
	private Integer majorTypeId;
	private String name;
	@ForeignKey@Column("id")
	private Test test;
	
	public Integer getMajorTypeId() {
		return majorTypeId;
	}
	public void setMajorTypeId(Integer majorTypeId) {
		this.majorTypeId = majorTypeId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Test getTest() {
		return test;
	}
	public void setTest(Test test) {
		this.test = test;
	}

}
