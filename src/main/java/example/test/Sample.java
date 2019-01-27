package example.test;

import org.drogonframework.mapping.annotation.Converter;
import org.drogonframework.mapping.entity.ConvertibleDTO;

@Converter(id="merhaba",converterEntity="UmutClass")
public class Sample implements ConvertibleDTO
{
	public String deneme;
	
	//public ComplexObject a;

	public String getDeneme() {
		return deneme;
	}

	public void setDeneme(String deneme) {
		this.deneme = deneme;
	}

//	public ComplexObject getA() {
//		return a;
//	}
//
//	public void setA(ComplexObject a) {
//		this.a = a;
//	}
	
	
}
