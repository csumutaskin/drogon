package example.test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ComplexObjectWithCollection implements Serializable 
{
	private static final long serialVersionUID = 851010372703757180L;
	
	private List<SampleDTO> lotsOfNames = new ArrayList<>();
	
	private String singleName;
	
	//private ComplexObject complex;


}
