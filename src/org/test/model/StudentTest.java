package org.test.model;

import java.io.Serializable;

import org.test.model.parent.Model;
import org.wind.orm.annotation.Lock;
import org.wind.orm.annotation.Tables;


/**
 * @描述：学生表——测试
 */

@Tables("student_text")
@Lock("with(nolock)")
public class StudentTest extends Model implements Serializable{

	private static final long serialVersionUID = -7803367381363551458L;
	
	private Long sexId;
	private Long majorId;
	private String data;
	private Long test2Id;
	private Long sexIda;
	
	public StudentTest(){
		
	}
	public StudentTest(Long sexId,Long majorId,String data,Long test2Id,Long sexIda){
		this.sexId=sexId;
		this.majorId=majorId;
		this.data=data;
		this.test2Id=test2Id;
		this.sexIda=sexIda;
	}
	public Long getSexId() {
		return sexId;
	}
	public Long getMajorId() {
		return majorId;
	}
	public String getData() {
		return data;
	}
	public Long getTest2Id() {
		return test2Id;
	}
	public Long getSexIda() {
		return sexIda;
	}
	public void setSexId(Long sexId) {
		this.sexId = sexId;
	}
	public void setMajorId(Long majorId) {
		this.majorId = majorId;
	}
	public void setData(String data) {
		this.data = data;
	}
	public void setTest2Id(Long test2Id) {
		this.test2Id = test2Id;
	}
	public void setSexIda(Long sexIda) {
		this.sexIda = sexIda;
	}
	
	
}