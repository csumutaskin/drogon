package example.test;

import java.io.Serializable;
import java.util.List;

public class ComplexObjectB implements Serializable 
{
	private static final long serialVersionUID = -7402425917962197343L;

	private String nameB;
	
	private Long ageB;
	
	public Sample sample;
	
	//List<ComplexObjectWithArray> a;
	
	List<SampleDTO> sampleDtolist;
	
	public String getNameB() {
		return nameB;
	}

	public void setNameB(String nameB) {
		this.nameB = nameB;
	}

	public Long getAgeB() {
		return ageB;
	}

	public void setAgeB(Long ageB) {
		this.ageB = ageB;
	}

	public Sample getSample() {
		return sample;
	}

	public void setSample(Sample sample) {
		this.sample = sample;
	}
	
	
}
