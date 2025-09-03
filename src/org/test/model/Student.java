package org.test.model;

import java.io.Serializable;
import java.util.List;

import org.test.model.parent.Model;
import org.wind.orm.annotation.Column;
import org.wind.orm.annotation.DateTime;
import org.wind.orm.annotation.ForeignKey;
import org.wind.orm.annotation.Lock;
import org.wind.orm.annotation.Tables;


/**
 * @描述：学生表 
 */
@Tables("Student")
@Lock("with(nolock)")
public class Student extends Model implements Serializable{

	private static final long serialVersionUID = -7803367381363551458L;
	
	@ForeignKey@Column("sexId")
	private Sex sex;		//性别
	@Column("date")
	private String date;	//出生日期
	@ForeignKey@Column("test2Id")
	private Test2 test2;	//测试类2
	@ForeignKey
	private List<Major> majorList;	//专业列表
	@ForeignKey@Column("sexIda")
	private Sex sexa;		//性别（测试）
	@DateTime("yyyy-MM-dd HH:mm:ss")
	protected String updateTime;	//更新时间
	
	public Sex getSex() {
		return sex;
	}
	public void setSex(Sex sex) {
		this.sex = sex;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public Test2 getTest2() {
		return test2;
	}
	public void setTest2(Test2 test2) {
		this.test2 = test2;
	}
	public List<Major> getMajorList() {
		return majorList;
	}
	public void setMajorList(List<Major> majorList) {
		this.majorList = majorList;
	}
	public Sex getSexa() {
		return sexa;
	}
	public void setSexa(Sex sexa) {
		this.sexa = sexa;
	}
	public String getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}
	
}