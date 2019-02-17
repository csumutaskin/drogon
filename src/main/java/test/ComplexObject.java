package test;

import java.io.Serializable;

public class ComplexObject implements Serializable 
{
	private static final long serialVersionUID = 1L;
	
	private ComplexObjectB complexB;
	
	private ComplexObjectB complexc;
	
	@SuppressWarnings("unused")
	private ComplexObjectB[] complexd;
	
	private String name;
	
	private Long age;

	private Object anotherObject;

	@SuppressWarnings("unused")
	private ComplexObjectWithCollection cwc;
	
//	private ComplexObject co;
	
	public ComplexObjectB getComplexB() 
	{
		return complexB;
	}

	public void setComplexB(ComplexObjectB complexB) 
	{
		this.complexB = complexB;
	}

	public String getName() 
	{
		return name;
	}

	public void setName(String name) 
	{
		this.name = name;
	}

	public Long getAge() 
	{
		return age;
	}

	public void setAge(Long age) 
	{
		this.age = age;
	}

	public Object getAnotherObject() 
	{
		return anotherObject;
	}

	public void setAnotherObject(Object anotherObject) 
	{
		this.anotherObject = anotherObject;
	}

	public ComplexObjectB getComplexc() {
		return complexc;
	}

	public void setComplexc(ComplexObjectB complexc) {
		this.complexc = complexc;
	}
	
	
}
