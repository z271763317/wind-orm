package org.test.model;

import org.wind.orm.Table;
import org.wind.orm.annotation.Id;
import org.wind.orm.annotation.Lock;

/**
 * @描述：性别表 
 */
@Lock("with(nolock)")
public class Sex extends Table{

	@Id
	private Integer sexId;
	private String name;
	
	public Integer getSexId() {
		return sexId;
	}
	public void setSexId(Integer sexId) {
		this.sexId = sexId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}
