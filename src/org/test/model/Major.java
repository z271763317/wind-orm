package org.test.model;

import org.wind.orm.Table;
import org.wind.orm.annotation.Column;
import org.wind.orm.annotation.ForeignKey;
import org.wind.orm.annotation.Id;

/**
 * @描述：专业表 
 */
public class Major extends Table{

	@Id
	private Integer majorId;
	private String name;
	@ForeignKey@Column("studentId")
	private Student student;			//学生表
	@ForeignKey@Column("majorTypeId")
	private MajorType majorType;		//专业类型
	
	public Integer getMajorId() {
		return majorId;
	}
	public void setMajorId(Integer majorId) {
		this.majorId = majorId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public MajorType getMajorType() {
		return majorType;
	}
	public void setMajorType(MajorType majorType) {
		this.majorType = majorType;
	}
	public Student getStudent() {
		return student;
	}
	public void setStudent(Student student) {
		this.student = student;
	}
	
}