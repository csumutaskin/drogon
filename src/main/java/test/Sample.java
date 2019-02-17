package test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drogonframework.mapping.annotation.Converter;

@Converter(converterEntity="Sample222",id="umut")
@SuppressWarnings(value= {"unused","rawtypes"})
public class Sample 
{
	private Sample2 mySample;
	private List<String> myListOfString;
	private int myInt;
	private Long[] myLongArray;
	private List listWithoutGenerics;
	private Map<String, Long> mapWithGenerics;
	private List<List<String>> nestedListWithGenerics;
	private List<List<List<Set<Sample2>>>> umut;
	private List<Map> nestedListAndMapWithoutGenerics;
	private List<Set> nestedCollectionWithoutGenerics;
	private Long[][] nestedLongArray;
	private Sample2[][] nestedObjectArray;
	private List<Sample3>[] arrayOfListOfSample3Object;
	private List<Long[]> nestedArrayInList;//burdayim... 
	private List<List<Long>[][]> aboooo;
	private List<Integer> wrapperIntegerList;
	//Mapleri de dene...hem ilk hem de 2. fieldini cek..
	//duz generic dene Sample3<Sample2> gibi
		
	public Sample2 getMySample() {
		return mySample;
	}
	public void setMySample(Sample2 mySample) {
		this.mySample = mySample;
	}

	public int getMyInt() {
		return myInt;
	}
	public void setMyInt(int myInt) {
		this.myInt = myInt;
	}
	
	public void go()
	{
		
	}
	
	class innerClass
	{
		private String shouldntBeAdded;		
	}	
}